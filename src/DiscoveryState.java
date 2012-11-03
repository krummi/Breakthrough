import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscoveryState {

    ///////////////////////////////////////////////////////////////////////////
    // Types
    ///////////////////////////////////////////////////////////////////////////
    enum Result { Unknown, Loss, Win }

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    // FEN
    private static final String INITIAL_FEN =
            "bbbbbbbbbbbbbbbb................................wwwwwwwwwwwwwwww 0";

    private static final Pattern FEN_PATTERN = Pattern.compile("([wb\\.]{64}) ?([01]?)");

    // Bitmaps
    private static final long EMPTY = 0x0000000000000000L;

    private static final long RANK_8 = 0xFF00000000000000L;
    private static final long RANK_7 = 0x00FF000000000000L;
    private static final long RANK_6 = 0x0000FF0000000000L;
    private static final long RANK_5 = 0x000000FF00000000L;
    private static final long RANK_4 = 0x00000000FF000000L;
    private static final long RANK_3 = 0x0000000000FF0000L;
    private static final long RANK_2 = 0x000000000000FF00L;
    private static final long RANK_1 = 0x00000000000000FFL;

    private static final long[] RANKS = {
            RANK_8, RANK_7, RANK_6, RANK_5, RANK_4, RANK_3, RANK_2, RANK_1,
    };

    public static final long FILE_A = 0x0101010101010101L;
    private static final long FILE_B = 0x0202020202020202L;
    private static final long FILE_C = 0x0404040404040404L;
    private static final long FILE_D = 0x0808080808080808L;
    private static final long FILE_E = 0x1010101010101010L;
    private static final long FILE_F = 0x2020202020202020L;
    private static final long FILE_G = 0x4040404040404040L;
    public static final long FILE_H = 0x8080808080808080L;

    private static final long[] FILES = new long[]{
            FILE_A, FILE_B, FILE_C, FILE_D, FILE_E, FILE_F, FILE_G, FILE_H
    };

    // Board
    public static final int NO_OF_SQUARES = 64;
    public static final int NO_OF_COLORS = 2;

    public static final String[] SQUARES = new String[]{
        "H1", "G1", "F1", "E1", "D1", "C1", "B1", "A1", // 0  - 7
        "H2", "G2", "F2", "E2", "D2", "C2", "B2", "A2", // 8  - 15
        "H3", "G3", "F3", "E3", "D3", "C3", "B3", "A3", // 16 - 23
        "H4", "G4", "F4", "E4", "D4", "C4", "B4", "A4", // 24 - 31
        "H5", "G5", "F5", "E5", "D5", "C5", "B5", "A5", // 32 - 39
        "H6", "G6", "F6", "E6", "D6", "C6", "B6", "A6", // 40 - 47
        "H7", "G7", "F7", "E7", "D7", "C7", "B7", "A7", // 48 - 55
        "H8", "G8", "F8", "E8", "D8", "C8", "B8", "A8", // 56 - 63
    };

    // Colors
    public static final int WHITE = 0;
    public static final int BLACK = 1;

    // Deltas
    private static final int DELTA_N  = -8; private static final int DELTA_S  = 8;
    private static final int DELTA_NW = -9; private static final int DELTA_SW = 7;
    private static final int DELTA_NE = -7; private static final int DELTA_SE = 9;

    ///////////////////////////////////////////////////////////////////////////
    // Member variables
    ///////////////////////////////////////////////////////////////////////////

    private long WP; // White pieces.
    private long BP; // Black pieces.
    private int sideToMove;
    private Result result;
    public long key;

    ///////////////////////////////////////////////////////////////////////////
    // Functions
    ///////////////////////////////////////////////////////////////////////////

    public DiscoveryState() {
        setup(INITIAL_FEN);
    }

    public DiscoveryState(String fen) {
        setup(fen);
    }

    public ArrayList<Integer> getCaptureMoves(int first) {
        ArrayList<Integer> moves = new ArrayList<Integer>();
        if (sideToMove == WHITE) {
            long WP_NW             = (WP & ~FILE_H) << 9;
            long WP_NE             = (WP & ~FILE_A) << 7;
            long WP_NW_CAPTURES    = WP_NW & BP;
            long WP_NE_CAPTURES    = WP_NE & BP;
            addMoves(moves, WP_NW_CAPTURES, DELTA_NW, true, first);
            addMoves(moves, WP_NE_CAPTURES, DELTA_NE, true, first);
        } else {
            long BP_SW             = (BP & ~FILE_H) >>> 7;
            long BP_SE             = (BP & ~FILE_A) >>> 9;
            long BP_SW_CAPTURES    = BP_SW & WP;
            long BP_SE_CAPTURES    = BP_SE & WP;
            addMoves(moves, BP_SW_CAPTURES, DELTA_SW, true, first);
            addMoves(moves, BP_SE_CAPTURES, DELTA_SE, true, first);
        }
        return moves;
    }

    public ArrayList<Integer> getAllMoves(int first) {
        ArrayList<Integer> moves = new ArrayList<Integer>();

        long emptySquares = ~(WP | BP);
        if (sideToMove == WHITE) {
            long WP_N              = (WP << 8) & emptySquares;
            long WP_NW             = (WP & ~FILE_H) << 9;
            long WP_NE             = (WP & ~FILE_A) << 7;
            long WP_NW_NONCAPTURES = WP_NW & emptySquares;
            long WP_NE_NONCAPTURES = WP_NE & emptySquares;
            long WP_NW_CAPTURES    = WP_NW & BP;
            long WP_NE_CAPTURES    = WP_NE & BP;

            addMoves(moves, WP_NW_CAPTURES, DELTA_NW, true, first);
            addMoves(moves, WP_NE_CAPTURES, DELTA_NE, true, first);
            addMoves(moves, WP_N, DELTA_N, false, first);
            addMoves(moves, WP_NW_NONCAPTURES, DELTA_NW, false, first);
            addMoves(moves, WP_NE_NONCAPTURES, DELTA_NE, false, first);
        } else {
            long BP_S              = (BP >>> 8) & emptySquares;
            long BP_SW             = (BP & ~FILE_H) >>> 7;
            long BP_SE             = (BP & ~FILE_A) >>> 9;
            long BP_SW_NONCAPTURES = BP_SW & emptySquares;
            long BP_SE_NONCAPTURES = BP_SE & emptySquares;
            long BP_SW_CAPTURES    = BP_SW & WP;
            long BP_SE_CAPTURES    = BP_SE & WP;

            addMoves(moves, BP_SW_CAPTURES, DELTA_SW, true, first);
            addMoves(moves, BP_SE_CAPTURES, DELTA_SE, true, first);
            addMoves(moves, BP_S, DELTA_S, false, first);
            addMoves(moves, BP_SW_NONCAPTURES, DELTA_SW, false, first);
            addMoves(moves, BP_SE_NONCAPTURES, DELTA_SE, false, first);
        }

        return moves;
    }

    private void addMoves(ArrayList<Integer> moves, long bitboard, int delta, boolean areCaptures, int pv) {
        while (bitboard != 0) {
            long h = Long.highestOneBit(bitboard);
            bitboard &= ~h;
            int pos = Long.numberOfTrailingZeros(h);
            int move = DiscoveryMove.createMove(pos + delta, pos, areCaptures);
            if (pv != DiscoveryMove.MOVE_NONE && pv == move) {
                moves.add(0, move);
            } else {
                moves.add(move);
            }
        }
    }

    public void make(int move) {
        assert !isTerminal();

        int from    = DiscoveryMove.getFrom(move);
        int to      = DiscoveryMove.getTo(move);
        boolean capture = DiscoveryMove.isCapture(move);

        // TODO: CODE DUPLICATION OF DEATH.
        if (sideToMove == WHITE) {
            // Clears the "from" square.
            WP &= ~(1L << from); key ^= Zobrist.PIECES[WHITE][from];

            // Clear the opponents "to" square in case of a capture.
            if (capture) {
                BP &= ~(1L << to); key ^= Zobrist.PIECES[BLACK][to];
                if (Long.bitCount(BP) == 0) {
                    result = Result.Loss;
                }
            }

            // Put the piece to the "to" square.
            WP |= (1L << to); key ^= Zobrist.PIECES[WHITE][to];

            // Checks if "to" square was on rank 1 or rank 8, if was: loss.
            if ((WP & RANK_8) != 0) { // omg (WP & RANK_8) > 0 => A LOT of debugging effort.
                result = Result.Loss;
            }
        } else {
            // Clears the "from" square.
            BP &= ~(1L << from); key ^= Zobrist.PIECES[BLACK][from];

            // Clear the opponents "to" square in case of a capture.
            if (capture) {
                WP &= ~(1L << to); key ^= Zobrist.PIECES[WHITE][to];
                if (Long.bitCount(WP) == 0) {
                    result = Result.Loss;
                }
            }

            // Put the to the "to" square.
            BP |= (1L << to); key ^= Zobrist.PIECES[BLACK][to];

            // Checks if "to" square was on rank 1 if was: loss.
            if ((BP & RANK_1) != 0) {
                result = Result.Loss;
            }
        }

        // Swap the side to move.
        sideToMove = oppColor(sideToMove);

        // Update the hash-key.
        key ^= Zobrist.SIDE_TO_MOVE;
    }

    public void retract(int move) {

        // Retrieve the from and to square.
        int from        = DiscoveryMove.getFrom(move);
        int to          = DiscoveryMove.getTo(move);
        boolean capture = DiscoveryMove.isCapture(move);

        // Toggle the side to move:
        sideToMove = oppColor(sideToMove);

        // Update the hash-key.
        key ^= Zobrist.SIDE_TO_MOVE;

        if (sideToMove == WHITE) {
            // Puts the piece back to its initial location (from) and clears the "to"-square:
            WP &= ~(1L << to);    key ^= Zobrist.PIECES[WHITE][to];
            WP |= (1L << from);   key ^= Zobrist.PIECES[WHITE][from];
            if (capture) {
                BP |= (1L << to); key ^= Zobrist.PIECES[BLACK][to];
            }
        } else {
            BP &= ~(1L << to);    key ^= Zobrist.PIECES[BLACK][to];
            BP |= (1L << from);   key ^= Zobrist.PIECES[BLACK][from];
            if (capture) {
                WP |= (1L << to); key ^= Zobrist.PIECES[WHITE][to];
            }
        }

        // No longer a terminal node (in case we were in a terminal node).
        result = Result.Unknown;
    }

    public boolean isTerminal() {
        return result != Result.Unknown;
    }

    public Result getResult() {
        return result;
    }

    public int getPlayerToMove() {
        return sideToMove;
    }

    public void reset() {
        setup(INITIAL_FEN);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int index = 63;
        while (index >= 0) {
            long whiteSquare = (WP >> index) & 0x01;
            long blackSquare = (BP >> index) & 0x01;
            if (whiteSquare > 0) sb.append("w");
            else if (blackSquare > 0) sb.append("b");
            else sb.append(".");
            index--;
        }
        sb.append(' ');
        sb.append(sideToMove == WHITE ? '0' : '1');
        return sb.toString();
    }

    public void display() {
        int index = 63;
        System.out.println("Side: " + (sideToMove == WHITE ? "White" : "Black"));
        System.out.println("  A B C D E F G H ");
        for (int rank = 0; rank < 8; rank++) {
            System.out.print((8 - rank) + " ");
            for (int col = 0; col < 8; col++) {
                long whiteSquare = (WP >> index) & 0x01;
                long blackSquare = (BP >> index) & 0x01;
                if (whiteSquare > 0) {
                    System.out.print("w ");
                } else if (blackSquare > 0) {
                    System.out.print("b ");
                } else {
                    System.out.print(". ");
                }

                index--;
            }
            System.out.println();
        }
        System.out.println("  A B C D E F G H ");
        System.out.println();
    }

    public int getEvaluation() {
        // Is terminal?
        if (isTerminal()) {
            switch (result) {
                case Win: return State.WIN_VALUE;
                case Loss: return State.LOSS_VALUE;
                default: System.out.println("should not happen"); System.exit(-1);
            }
        }

        // Not a terminal node: Evaluate material.
        int value = Long.bitCount(WP) - Long.bitCount(BP);

        return (sideToMove == WHITE ? value : -value);
    }

    public Move isLegalMove(String strMove) {
        return null;
    }

    public boolean setup(String fen) {
        // Valid FEN?
        Matcher matcher = FEN_PATTERN.matcher(fen);
        if (!matcher.find()) {
            System.out.println("> INVALID FEN, EXITING: " + fen);
            System.exit(-1);
        }

        // Resets the state.
        empty();

        // Create the WP and BP bitboards.
        for (int i = 0; i < NO_OF_SQUARES; i++) {
            switch (matcher.group(1).charAt(i)) {
                case 'w': WP |= (1L << (63L - i)); break;
                case 'b': BP |= (1L << (63L - i)); break;
            }
        }

        // Parse the side to move.
        sideToMove = matcher.group(2).equals("0") || matcher.group(2).equals("") ? WHITE : BLACK;
        result = Result.Unknown;

        // Update hash key.
        key = Zobrist.getZobristKey(this);

        // TODO: Validate legality?
        return true;
    }

    private void empty() {
        WP = EMPTY;
        BP = EMPTY;
    }

    public void printBitboard(long bb) {
        int index = 63;
        for (int rank = 0; rank < 8; rank++) {
            for (int i = 0; i < 8; i++) {
                System.out.print((bb >> index) & 0x01);
                index--;
            }
            System.out.println();
        }
        System.out.println();
    }

    public static int oppColor(int color) {
        return color ^ 1;
    }

}
