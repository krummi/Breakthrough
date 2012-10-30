import java.nio.channels.NonWritableChannelException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * Agent using alpha-beta-search to think ahead.
 *
 */
public class AgentAlphaBeta implements Agent
{
    private static final int INFINITY_VALUE = 10001;
    private static final int MAX_SEARCH_DEPTH = 100;

    private boolean m_silent;
    private long    m_depthLimit;
    private long    m_nodeLimit;
    private long    m_timeLimit;

    private Random m_rng = new Random();
    private PV m_pv;
    private long m_nodes;
    private long m_msec;
    private boolean m_abort;
    private long transHits;

    private TranspositionTable transTable;

    public AgentAlphaBeta() {
        m_silent = true;
        m_rng = new Random();
        m_pv = new PV(MAX_SEARCH_DEPTH  + 1);
        m_depthLimit = m_nodeLimit = m_timeLimit = 0;

        transTable = new TranspositionTable(2 << 18);
    }

    public void setSilence(boolean on) {
        m_silent = on;
    }

    public void setThinklimit(long maxLimit, long maxNodes, long maxTimeMsec) {
        m_depthLimit = maxLimit;
        m_nodeLimit = maxNodes;
        m_timeLimit = maxTimeMsec;
    }

    public Move playMove(State state) {

        // Initialize stuff
        m_msec = System.currentTimeMillis();
        m_nodes = 0;
        m_abort = false;
        transHits = 0;
        Move bestMove = null;

        // Clears the transposition table.
        transTable.clear();

        // Non-determinism:
        ArrayList<Move> moves = state.getActions(null);
        bestMove = moves.get(m_rng.nextInt(moves.size()));
        // TODO: bestMove = moves.get(0); ?

        long maxDepth = m_depthLimit;
        if (maxDepth == 0 || maxDepth > MAX_SEARCH_DEPTH) {
            maxDepth = MAX_SEARCH_DEPTH;
        }

        for (int depth = 1; depth <= maxDepth && !m_abort; depth++) {
             int value = alphaBeta(0, depth, -INFINITY_VALUE, INFINITY_VALUE, state, bestMove);
            ArrayList<Move> pv = m_pv.getPV();

            // TODO: MOVE TO SOME FUNCTION.
            long msec = System.currentTimeMillis() - m_msec;
            if ( !m_silent ) { System.out.printf( "\t%2d %10d %7d", depth, m_nodes, msec ); }
            if ( !pv.isEmpty() && pv.get(0) != null ) {
                if ( !m_silent ) { System.out.printf( " %+6d ", value ); }
                for ( Move move : pv ) {
                    if ( !m_silent ) { System.out.print( ' ' + move.toStr() ); }
                }
                bestMove = pv.get( 0 );
            }
            if ( !m_silent ) { System.out.println(); }
        }

        return bestMove;
    }

    private int alphaBeta(int ply, int depth, int alpha, int beta, State s, Move firstMoveToLookAt) {
        assert alpha >= -INFINITY_VALUE && alpha < beta && beta <= INFINITY_VALUE;

        //DiscoveryState state = (DiscoveryState) s;
        BitboardState state = (BitboardState) s;
        m_nodes++;
        m_pv.set(ply);

        // Transposition table lookup
        // TODO: Okay, talk to Yngvi.
        /*
        TranspositionTable.HashEntry entry = transTable.get(state.key);
        if (entry != null) {
            if (entry.depth >= depth) {
                transHits++;
                if (entry.type == DiscoveryState.SCORE_EXACT) {
                    return entry.eval;
                } else if (entry.type == DiscoveryState.SCORE_ALL && entry.eval <= alpha) {
                    return entry.eval;
                } else if (entry.type == DiscoveryState.SCORE_CUT && entry.eval >= beta) {
                    return entry.eval;
                }
            }
        }
        */

        // Horizon?
        if (depth <= 0 || state.isTerminal()) {
            int eval = state.getEvaluation();
            //transTable.putLeaf(state.key, eval, alpha, beta);
            m_abort = reachedALimit();
            return eval;
        }

        Move move = null;
        int eval = 0;
        int bestValue = Integer.MIN_VALUE;
        ArrayList<Move> moves = state.getActions(firstMoveToLookAt);
        for (int i = 0; i < moves.size(); i++) {
            move = moves.get(i);

            state.make(move);
            eval = -alphaBeta(ply + 1, depth - 1, -beta, -alpha, state, null);
            state.retract(move);
            if (m_abort) { break; }

            if (eval > bestValue) {
                bestValue = eval;
                m_pv.set(ply, move);
            }

            // Raising alpha?
            if (bestValue > alpha) {
                alpha = bestValue;
                if (alpha >= beta) break; // Beta cutoff
            }
        }

        // Update the transposition table.
        /*
        if (bestValue <= alpha) {
            transTable.put(state.key, DiscoveryState.SCORE_ALL, depth, eval, move);
        } else if (bestValue >= beta) {
            transTable.put(state.key, DiscoveryState.SCORE_CUT, depth, eval, move);
        } else {
            transTable.put(state.key, DiscoveryState.SCORE_EXACT, depth, eval, move);
        }
        */

        return bestValue;
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
            new int[]{ DiscoveryState.SW, DiscoveryState.S, DiscoveryState.SE },
            new int[]{ DiscoveryState.NW, DiscoveryState.N, DiscoveryState.NE }
    };
    private final boolean EVAL_DEBUG = false;

    public int evaluate(DiscoveryState state) {

        // Initialization.
        DiscoveryState.PieceList pieces = state.pieces;
        int WHITE = DiscoveryState.WHITE;
        int BLACK = DiscoveryState.BLACK;
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

        int[][] files = new int[DiscoveryState.NO_OF_COLORS][8];

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

    private boolean reachedALimit()
    {
        if ( (m_nodeLimit > 0) && (m_nodes >= m_nodeLimit) ) {
            return true;
        }
        if ( (m_timeLimit > 0) && ((m_nodes % 1000) == 0) ) {
            if ( (java.lang.System.currentTimeMillis() - m_msec) >= m_timeLimit ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "AlphaBeta";
    }

}
