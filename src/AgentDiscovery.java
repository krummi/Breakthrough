import java.util.ArrayList;
import java.util.Random;

/**
 * Agent Discovery!
 *
 * (1) Bitboard state representation
 * (2) Move ordering
 * (3) Transposition table
 * (4) Quiescence search
 *
 * @author Hrafn Eiriksson <hrafne (at) gmail.com>
 */
public class AgentDiscovery implements Agent {

    // Constants
    private static final int INFINITY_VALUE   = 10001;
    private static final int MAX_SEARCH_DEPTH = 100;
    private static final Random random        = new Random();

    public static final int SCORE_LOWER = 0;
    public static final int SCORE_UPPER = 1;
    public static final int SCORE_EXACT = 2;

    // Member variables
    private DiscoveryPV m_pv;
    private long        m_msec;
    private boolean     abort;
    private boolean     m_silent;
    private long        m_depthLimit;
    private long        m_nodeLimit;
    private long        m_timeLimit;

    private long        nodes;
    private long        qnodes;
    private long        transHits;

    public DiscoveryState      state;
    private TranspositionTable transTable;

    public AgentDiscovery() {
        m_silent = true;
        m_pv = new DiscoveryPV(MAX_SEARCH_DEPTH + 1);
        m_depthLimit = m_nodeLimit = m_timeLimit = 0;

        // State and transposition table.
        state = new DiscoveryState();
        transTable = new TranspositionTable(2 << 18);
    }

    /*
     * Go think!
     */
    public Move playMove(State otherState, Game history) {

        // Initialize stuff
        m_msec    = System.currentTimeMillis();
        nodes     = 0;
        qnodes    = 0;
        abort     = false;
        transHits = 0;

        // Set our internal state up accordingly.
        state.setup(otherState.toString());

        // Clears the transposition table.
        transTable.clear();

        // Non-determinism:
        ArrayList<Integer> moves = state.getAllMoves(DiscoveryMove.MOVE_NONE);
        int bestMove = moves.get(random.nextInt(moves.size()));

        long maxDepth = m_depthLimit;
        if (maxDepth == 0 || maxDepth > MAX_SEARCH_DEPTH) {
            maxDepth = MAX_SEARCH_DEPTH;
        }

        if (!m_silent) System.out.println("To move: " + getName());
        for (int depth = 1; depth <= maxDepth && !abort; depth++) {
            int value = search(0, depth, -INFINITY_VALUE, INFINITY_VALUE, bestMove);
            ArrayList<Integer> pv = m_pv.getPV();

            long msec = System.currentTimeMillis() - m_msec;
            if ( !m_silent ) { System.out.printf( "\t%2d %10d %7d", depth, nodes, msec ); }
            if ( !pv.isEmpty() && pv.get(0) != null ) {
                if ( !m_silent ) { System.out.printf( " %+6d ", value ); }
                for ( int move : pv ) {
                    if ( !m_silent ) { System.out.print( ' ' + DiscoveryMove.stringify(move) ); }
                }
                bestMove = pv.get( 0 );
            }
            if ( !m_silent ) { System.out.println(); }
        }

        System.out.println("nodes  : " + nodes);
        System.out.println("qnodes : " + qnodes);
        System.out.println("trans  : " + transHits);

        // Apply the move to our internal state.
        state.make(bestMove);

        // Serialize the move to the external move representation.
        Move serialized = DiscoveryMove.serialize(bestMove);
        assert serialized.toStr().equals(DiscoveryMove.stringify(bestMove));
        return serialized;
    }

    /*
     * Normal alpha-beta search.
     */
    private int search(int ply, int depth, int alpha, int beta, int firstMoveToLookAt) {
        assert alpha >= -INFINITY_VALUE && alpha < beta && beta <= INFINITY_VALUE;

        nodes++;
        int eval = 0;
        m_pv.set(ply);

        // Transposition table lookup.
        TranspositionTable.HashEntry entry = transTable.get(state.key);
        if (entry != null) {
            if (entry.depth >= depth) {
                transHits++;
                if (entry.type == SCORE_EXACT) {
                    return entry.eval;
                } else if (entry.type == SCORE_LOWER && entry.eval <= alpha) {
                    return entry.eval;
                } else if (entry.type == SCORE_UPPER && entry.eval >= beta) {
                    return entry.eval;
                }
            }
        }

        // Terminal position?
        if (state.isTerminal()) {
            abort = reachedALimit();
            return state.getEvaluation();
        }

        // Horizon? Quiescence search!
        if (depth <= 0) {
            eval = qsearch(ply, alpha, beta, firstMoveToLookAt);
            transTable.putLeaf(state.key, eval, alpha, beta);
            abort = reachedALimit();
            return eval;
        }

        // Do null move pruning?
        /*
        if (depth >= 2
                && beta < INFINITY_VALUE
                && nmAllowed
                && Long.bitCount(state.BP | state.WP) > 14) {

            state.makeNullMove();
            eval = -alphaBeta(ply + 1, depth - 3, -beta, -alpha, state, null, false);
            state.retractNullMove();

            if (eval >= beta) {
                return eval;
            }
        }
        */

        // Normal search
        int scoreType = SCORE_LOWER;
        int bestMove = DiscoveryMove.MOVE_NONE;
        int move = DiscoveryMove.MOVE_NONE;
        int bestValue = Integer.MIN_VALUE;
        ArrayList<Integer> moves = state.getAllMoves(firstMoveToLookAt);
        for (int i = 0; i < moves.size(); i++) {
            move = moves.get(i);
            state.make(move);
            assert Zobrist.getZobristKey(state) == state.key;
            eval = -search(ply + 1, depth - 1, -beta, -alpha, DiscoveryMove.MOVE_NONE);
            state.retract(move);
            if (abort) { break; }

            if (eval > bestValue) {
                m_pv.set(ply, move);
                if (eval >= beta) {
                    transTable.put(state.key, SCORE_UPPER, depth, eval, move);
                    return beta;
                }
                bestValue = eval;
                bestMove = move;
                if (eval > alpha) {
                    scoreType = SCORE_EXACT;
                    alpha = eval;
                }
            }
            /*
            if (eval > bestValue) {
                bestValue = eval;
                m_pv.set(ply, move);
            }

            // Raising alpha?
            if (bestValue > alpha) {
                alpha = bestValue;
                if (alpha >= beta) {
                    transTable.put(state.key, SCORE_UPPER, depth, eval, move);
                    return beta;
                }
            }
            */
        }

        assert move != DiscoveryMove.MOVE_NONE;

        // Updates the transposition table.
        if (scoreType == SCORE_EXACT) {
            transTable.put(state.key, SCORE_EXACT, depth, eval, bestMove);
        } else if (scoreType == SCORE_LOWER) {
            transTable.put(state.key, SCORE_LOWER, depth, eval, bestMove);
        } else {
            assert false : "What?";
        }

        return bestValue;
    }

    /*
     * Quiescence search.
     */
    private int qsearch(int ply, int alpha, int beta, int firstMoveToLookAt) {
        nodes++;
        qnodes++;

        if (state.isTerminal()) return state.getEvaluation();
        int eval = state.getEvaluation();
        if (eval >= beta) {
            return beta;
        }
        if (eval > alpha) {
            alpha = eval;
        }

        // Generate captures.
        ArrayList<Integer> moves = state.getCaptureMoves(firstMoveToLookAt);
        for (int m : moves) {
            assert DiscoveryMove.isCapture(m);
        }
        for (int move : moves) {
            state.make(move);
            eval = -qsearch(ply + 1, -beta, -alpha, DiscoveryMove.MOVE_NONE);
            state.retract(move);

            if (eval >= beta) {
                return beta; // Beta cutoff.
            }
            if (eval > alpha) {
                alpha = eval;
            }
        }
        return alpha;
    }

    // Evaluation constants!

    private final int PIECE_VALUE = 100;
    private final int PIECE_DEFENSE_VALUE = 5;
    private final int EMPTY_FILE_PENALTY = -20;

    private final int[][] INVERT_ARR = new int[][]{
            new int[]{ 0, 1, 2, 3, 4, 5, 6, 7 },
            new int[]{ 7, 6, 5, 4, 3, 2, 1, 0 }
    };
    private final int[] RANK_SCORES = new int[]{ -5, 0, 5, 10, 15, 20, 50, 0 };
    int[][] DEFENSE_INC = new int[][]{
            new int[]{ OldState.SW, OldState.S, OldState.SE },
            new int[]{ OldState.NW, OldState.N, OldState.NE }
    };
    private final boolean EVAL_DEBUG = false;

    public int evaluate(OldState state) {

        // Initialization.
        OldState.PieceList pieces = state.pieces;
        int WHITE = OldState.WHITE;
        int BLACK = OldState.BLACK;
        int sideToMove = state.sideToMove;
        int[] board = state.squares;

        // Checks for a win/loss.
        if (state.isTerminal()) {
            if (state.result == State.Result.Win) {
                return State.WIN_VALUE;
            } else if (state.result == State.Result.Loss) {
                return State.LOSS_VALUE;
            }
        }

        // Well, its not a win/loss.
        int[] rankScores = new int[]{ 0, 0 };
        int[] defenseScores = new int[]{ 0, 0 };
        int[] emptyFilesScores = new int[]{ 0, 0 };

        int[][] files = new int[OldState.NO_OF_COLORS][8];

        for (int color = WHITE; color <= BLACK; color++) {
            int square;

            while ((square = pieces.getNext(color)) != Square.NONE) {
                // Retrieve data on the square.
                int squareRank = Square.getRank(square);
                int squareFile = Square.getFile(square);

                //
                // PER SQUARE EVALUATION
                //

                // (2) Rank scoring.
                rankScores[color] += RANK_SCORES[INVERT_ARR[color][squareRank - 1]];


                // (3) Piece defense.
                //     NOT on ranks 1 and 2 for WHITE and 7 and 8 for BLACK.
                //     Extra score for pieces that are backed up by the same color.
                if ((color == WHITE && squareRank != 1 && squareRank != 2) ||
                    (color == BLACK && squareRank != 7 && squareRank != 8)) {
                    for (int inc : DEFENSE_INC[color]) {
                        int newSquare = square + inc;
                        if (board[newSquare] == color) {
                            defenseScores[color] += PIECE_DEFENSE_VALUE;
                        }
                    }
                }

                // (4) Penalize empty files.
                files[color][squareFile - 1] += 1;
            }

            //
            // PER COLOR EVALUATION
            //

            // (4) Penalize empty files.
            for (int i = 0; i < 8; i++) {
                if (files[color][i] == 0) {
                    emptyFilesScores[color] += EMPTY_FILE_PENALTY;
                }
            }
        }

        int value = (pieces.counter[WHITE] - pieces.counter[BLACK]) * PIECE_VALUE;
        value += (rankScores[WHITE] - rankScores[BLACK]);
        value += (defenseScores[WHITE] - defenseScores[BLACK]);
        value += (emptyFilesScores[WHITE] - emptyFilesScores[BLACK]);

        // Negates the value if black should move.
        if (sideToMove == BLACK) {
            value = -value;
        }

        // Debug.
        if (EVAL_DEBUG) {
            state.display();
            System.out.println(String.format("               | WHITE | BLACK"));
            System.out.println(String.format("=============================="));
            System.out.println(String.format("Piece values   | %5d | %5d",
                    pieces.counter[WHITE] * PIECE_VALUE, pieces.counter[BLACK] * PIECE_VALUE));
            System.out.println(String.format("Rank scores    | %5d | %5d",
                    rankScores[WHITE], rankScores[BLACK]));
            System.out.println(String.format("Defense scores | %5d | %5d",
                    defenseScores[WHITE], defenseScores[BLACK]));
            System.out.println(String.format("Empty file pen.| %5d | %5d",
                    emptyFilesScores[WHITE], emptyFilesScores[BLACK]));
            System.out.println(String.format("=============================="));
            System.out.println(String.format("Total          | %d", value));
        }

        return value;
    }

    @Override
    public String getName() {
        return "Discovery";
    }

    private boolean reachedALimit() {
        if ( (m_nodeLimit > 0) && (nodes >= m_nodeLimit) ) {
            return true;
        }
        if ( (m_timeLimit > 0) && ((nodes % 1000) == 0) ) {
            if ( (java.lang.System.currentTimeMillis() - m_msec) >= m_timeLimit ) {
                return true;
            }
        }
        return false;
    }

    public void setSilence(boolean on) {
        m_silent = on;
    }

    public void setThinklimit(long maxLimit, long maxNodes, long maxTimeMsec) {
        m_depthLimit = maxLimit;
        m_nodeLimit  = maxNodes;
        m_timeLimit  = maxTimeMsec;
    }

}
