import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscoveryState implements State {

    ///////////////////////////////////////////////////////////////////////////
    // Data types
    ///////////////////////////////////////////////////////////////////////////

    public class PieceList {

        // Constants

        private final int NO_INDEX = -1;

        // Variables

        public int[] counter;
        private int[] iteratorIndex;
        public int[][] squares;

        // Functions

        private PieceList(int maximumNoOfPieces) {
            counter = new int[]{0, 0};
            iteratorIndex = new int[]{0, 0};
            squares = new int[NO_OF_COLORS][maximumNoOfPieces];

            for (int a = 0; a < maximumNoOfPieces; a++) {
                squares[WHITE][a] = NO_INDEX;
                squares[BLACK][a] = NO_INDEX;
            }
        }

        public void add(int color, int square) {

            // Updates the indices table:
            indices[square] = counter[color];

            // Updates the piece-list:
            squares[color][counter[color]] = square;
            counter[color]++;
        }

        public void remove(int color, int index) {

            counter[color]--;

            // Nullify the indices table at index:
            indices[squares[color][index]] = NO_INDEX;

            // Gets the last piece-entry in this piece list and puts it at index.
            int square = squares[color][counter[color]];
            squares[color][index] = square;
            indices[square] = index;
        }

        public int get(int color, int index) {
            return squares[color][index];
        }

        public void clear() {
            counter[WHITE] = 0;
            counter[BLACK] = 0;
        }

        public int getNext(int color) {
            if (counter[color] == iteratorIndex[color]) {
                iteratorIndex[color] = 0;
                return Square.NONE;
            }

            return squares[color][iteratorIndex[color]++];
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    // FEN
    private static final String INITIAL_FEN =
            "bbbbbbbbbbbbbbbb................................wwwwwwwwwwwwwwww 0";

    private static final Pattern FEN_PATTERN = Pattern.compile("[wb\\.]{64} ?[01]?");

    // Board
    public static final int A1 = 0x22, B1 = 0x23, C1 = 0x24, D1 = 0x25, E1 = 0x26, F1 = 0x27, G1 = 0x28, H1 = 0x29;
    public static final int A2 = 0x31, B2 = 0x32, C2 = 0x33, D2 = 0x34, E2 = 0x35, F2 = 0x36, G2 = 0x37, H2 = 0x38;
    public static final int A3 = 0x40, B3 = 0x41, C3 = 0x42, D3 = 0x43, E3 = 0x44, F3 = 0x45, G3 = 0x46, H3 = 0x47;
    public static final int A4 = 0x4f, B4 = 0x50, C4 = 0x51, D4 = 0x52, E4 = 0x53, F4 = 0x54, G4 = 0x55, H4 = 0x56;
    public static final int A5 = 0x5e, B5 = 0x5f, C5 = 0x60, D5 = 0x61, E5 = 0x62, F5 = 0x63, G5 = 0x64, H5 = 0x65;
    public static final int A6 = 0x6d, B6 = 0x6e, C6 = 0x6f, D6 = 0x70, E6 = 0x71, F6 = 0x72, G6 = 0x73, H6 = 0x74;
    public static final int A7 = 0x7c, B7 = 0x7d, C7 = 0x7e, D7 = 0x7f, E7 = 0x80, F7 = 0x81, G7 = 0x82, H7 = 0x83;
    public static final int A8 = 0x8b, B8 = 0x8c, C8 = 0x8d, D8 = 0x8e, E8 = 0x8f, F8 = 0x90, G8 = 0x91, H8 = 0x92;

    public static final int[] SQUARES_64 = {
            A1, B1, C1, D1, E1, F1, G1, H1,
            A2, B2, C2, D2, E2, F2, G2, H2,
            A3, B3, C3, D3, E3, F3, G3, H3,
            A4, B4, C4, D4, E4, F4, G4, H4,
            A5, B5, C5, D5, E5, F5, G5, H5,
            A6, B6, C6, D6, E6, F6, G6, H6,
            A7, B7, C7, D7, E7, F7, G7, H7,
            A8, B8, C8, D8, E8, F8, G8, H8
    };

    public static final int NO_OF_RANKS = 12; // 12 x 15
    public static final int NO_OF_FILES = 15; // 12 x 15
    public static final int NO_OF_SQUARES = NO_OF_RANKS * NO_OF_FILES; // 180

    // Borders
    public static final int UPPER_BORDER_SIZE = 2;
    public static final int LOWER_BORDER_SIZE = 2;
    public static final int LEFT_BORDER_SIZE = 4;
    public static final int RIGHT_BORDER_SIZE = 3;

    // Colors
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int NO_OF_COLORS = 2;

    // Increments/deltas
    public static final int NW = +14; public static final int NE = +16;
    public static final int SW = -16; public static final int SE = -14;

    public static final int N = +15; public static final int S = -15;
    public static final int E =  +1; public static final int W =  -1;

    // Pieces
    public static final int PIECE_VALUE = 100;

    ///////////////////////////////////////////////////////////////////////////
    // Member variables
    ///////////////////////////////////////////////////////////////////////////

    public int[] squares;           // 15x8 board representation
    public int[] indices;           // Indexes to the piece-lists
    public int sideToMove;          // Who's turn is it?
    public long key;                // The hash of this board state
    public PieceList pieces;        // Keeps track of the pieces.
    public Result result;           // The result of the current state.

    ///////////////////////////////////////////////////////////////////////////
    // Member functions
    ///////////////////////////////////////////////////////////////////////////

    public DiscoveryState() {
        this(INITIAL_FEN);
    }

    public DiscoveryState(String fen) {

        // Initializes the squares.
        squares = new int[NO_OF_SQUARES];
        indices = new int[NO_OF_SQUARES];

        // Puts a border around the board and fills the actual board with "empty squares":
        for (int a = 0; a < NO_OF_SQUARES; a++) {
            squares[a] = Square.BORDER;
        }
        for (int a = 0; a < SQUARES_64.length; a++) {
            squares[SQUARES_64[a]] = Square.EMPTY;
        }

        // Initializes the piece-list.
        pieces = new PieceList(16);

        // Setup the initial FEN.
        setup(fen);

        // TODO: Create the Zobrist key.
    }

    @Override
    public ArrayList<Move> getActions(Move first) {
        ArrayList<Move> moves = new ArrayList<Move>();

        int from;
        int otherSide = oppColor(sideToMove);
        while ((from = pieces.getNext(sideToMove)) != Square.NONE) {
            int[] deltas = (sideToMove == WHITE ? new int[]{NW, N, NE} : new int[]{SE, S, SW});
            for (int inc : deltas) {
                int to = from + inc;
                boolean isCapture;
                if (Square.isEmpty(squares[to])) {
                    isCapture = false;
                } else if (inc != N && inc != S && squares[to] == otherSide) {
                    isCapture = true;
                } else {
                    continue;
                }
                Move firstMoveBackup = null;
                Move move = new Move(from, -1, to, -1, isCapture);

                // Orders the moves in PV-move, captures and non-captures.
                if (first != null && move.equals(first)) {
                    assert firstMoveBackup != null;
                    firstMoveBackup = new Move(from, -1, to, -1, isCapture);
                } else {
                    if (isCapture) {
                        moves.add(0, move);
                    } else {
                        moves.add(move);
                    }
                }
                // Prepends the PV move.
                if (firstMoveBackup != null) {
                    moves.add(0, firstMoveBackup);
                }
            }
        }

        return moves;
    }

    @Override
    public void make(Move move) {

        // Assertions.
        assert !isTerminal();
        assert squares[move.from_col] == sideToMove;
        assert !move.capture || squares[move.to_col] == oppColor(sideToMove);

        // Retrieve the from and to squares.
        int from = move.from_col;
        int to   = move.to_col;

        // Clears the "from" square.
        clearSquare(from, true);

        // Clear the "to" square as well in case of a capture.
        if (move.capture) {
            assert squares[to] == oppColor(sideToMove);
            clearSquare(to, true);
            if (pieces.counter[oppColor(sideToMove)] == 0) {
                result = Result.Loss;
            }
        }

        // Checks if the "to" square is on RANK_1 or RANK_8.
        if (Square.getRank(to) == Square.RANK_8 || Square.getRank(to) == Square.RANK_1) {
            result = Result.Loss;
        }

        // The piece arrives at its "to" square.
        fillSquare(to, sideToMove, true);

        // Toggle the side to move
        sideToMove = oppColor(sideToMove);

        // TODO: key ^= Zobrist.SIDE_TO_MOVE;
    }

    @Override
    public void retract(Move move) {

        // Retrieve the from and to square.
        int from = move.from_col; // TODO: update.
        int to   = move.to_col; // TODO: fix.

        // Toggle the side to move:
        sideToMove = oppColor(sideToMove);
        // TODO: key ^= Zobrist.SIDE_TO_MOVE;

        // Puts the piece back to its initial location (from) and clears the "to"-square:
        clearSquare(to, true);
        fillSquare(from, sideToMove, true);

        // Puts a piece back in its place in case of a capture.
        if (move.capture) {
            fillSquare(to, oppColor(sideToMove), true);
        }

        // No longer a terminal node (in case we were in a terminal node).
        result = Result.Unknown;
    }

    @Override
    public boolean isTerminal() {
        return result != Result.Unknown;
    }

    @Override
    public Result getResult() {
        return result;
    }

    @Override
    public int getPlayerToMove() {
        return sideToMove;
    }

    @Override
    public void reset() {
        setup(INITIAL_FEN);
    }

    @Override
    public void display() {
        System.out.println("Turn:       " + (sideToMove == WHITE ? "White" : "Black"));
        System.out.println("Evaluation: " + getEvaluation());
        System.out.println();
        System.out.print("  a b c d e f g h\n8 ");

        for (int a = SQUARES_64.length - 8, b = 7; a >= 0; a -= 8) {
            for (int c = 0; c < 8; c++) {
                if (Square.isEmpty(squares[SQUARES_64[a + c]])) {
                    System.out.print(". ");
                } else {
                    System.out.print(squares[SQUARES_64[a + c]] == WHITE ? "w " : "b ");
                }
            }
            if (b != 0) {
                System.out.print("\n" + b-- + " ");
            }
        }
        System.out.println();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int rank : new int[]{ A8, A7, A6, A5, A4, A3, A2, A1 }) {
            int square = rank;
            for (int i = 0; i < 8; i++) {
                char c = ' ';
                switch(squares[square + i]) {
                    case Square.EMPTY: c = '.'; break;
                    case Square.WHITE: c = 'w'; break;
                    case Square.BLACK: c = 'b'; break;
                    default: assert false; break;
                }
                sb.append(c);
            }
        }
        sb.append(' ');
        sb.append(sideToMove == WHITE ? '0' : '1');
        return sb.toString();
    }

    /*
    @Override
    public int getEvaluation() {
        int value = 0;
        if (isTerminal()) {
            if (result == Result.Win) {
                value = State.WIN_VALUE;
            } else if (result == Result.Loss) {
                value = State.LOSS_VALUE;
            } else {
                assert false : "Should not happen.";
                value = 0;
            }
        } else {
            value = pieces.counter[WHITE] - pieces.counter[BLACK];
            if (sideToMove == BLACK) {
                value = -value;
            }
        }
        return value;
    }
    */

    @Override
    public int getEvaluation() {
        int value = 0;
        if (isTerminal()) {
            if (result == Result.Win) {
                value = State.WIN_VALUE;
            } else if (result == Result.Loss) {
                value = State.LOSS_VALUE;
            } else {
                assert false : "Should not happen.";
                value = 0;
            }
        } else {
            value = pieces.counter[WHITE] - pieces.counter[BLACK];
            if (sideToMove == BLACK) {
                value = -value;
            }
        }
        return value;
    }

    @Override
    public Move isLegalMove(String strMove) {
        return null;
    }

    @Override
    public boolean setup(String fen) {
        Matcher matcher = FEN_PATTERN.matcher(fen);

        if (!matcher.find()) {
            System.out.println("> INVALID FEN, EXITING: " + fen);
            System.exit(-1);
        }

        // Resets the state.
        empty();

        int index = 0;
        for (int rank : new int[]{ A8, A7, A6, A5, A4, A3, A2, A1 }) {
            int square = rank;
            for (int i = 0; i < 8; i++) {
                char c = fen.charAt(index);
                if (c != '.') {
                    fillSquare(square, c == 'w' ? Square.WHITE : Square.BLACK, false);
                }
                square++;
                index++;
            }
        }
        index++;

        sideToMove = fen.charAt(index) == '0' ? WHITE : BLACK;

        result = Result.Unknown;

        // TODO: Validate legality?

        return true;
    }

    private void fillSquare(int square, int color, boolean rehash) {

        if (rehash) {
            // Adds this piece on this square to the hash-key:
            // TODO: key ^= Zobrist.PIECES[piece - 1][color][square];
        }

        // TODO: Update material.

        // Updates the piece list.
        pieces.add(color, square);

        // ...and update the square:
        squares[square] = color;
    }

    private void clearSquare(int square, boolean rehash) {

        // Retrieve the color of the square in question.
        int color = squares[square];

        // Retracts the hashing.
        if (rehash) {
            // Undoes this piece on this square from the hash-key:
        // TODO:    key ^= Zobrist.PIECES[piece - 1][color][square];
        }

        // TODO: Update material.

        // Updates the piece-list.
        pieces.remove(color, indices[square]);

        // ...and "emptify" the square.
        squares[square] = Square.EMPTY;
    }

    private void addMove(ArrayList<Move> moves, Move move, Move placeFirst) {
        if (placeFirst != null && placeFirst.equals(move)) {
            moves.add(0, move);
        } else {
            moves.add(move);
        }
    }

    private void empty() {
        // Initializes the squares.
        for (int a = 0; a < NO_OF_SQUARES; a++) {
            squares[a] = Square.BORDER;
            indices[a] = 0;
        }
        for (int a = 0; a < SQUARES_64.length; a++) {
            squares[SQUARES_64[a]] = Square.EMPTY;
        }

        // Initializes the piece-list.
        pieces = new PieceList(16);

        sideToMove = 0;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Static functions
    ///////////////////////////////////////////////////////////////////////////

    public static int oppColor(int color) {
        return color ^ 1;
    }

}

class Square {

    // Constants

    public static final int NONE = -1;
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int EMPTY = 2;
    public static final int BORDER = 3;

    public static final int RANK_1 = 1;
    public static final int RANK_8 = 8;

    public static final String[] SQUARE_NAMES = {
            "", "", "", "", "A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1", "", "", "",
            "", "", "", "", "A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2", "", "", "",
            "", "", "", "", "A3", "B3", "C3", "D3", "E3", "F3", "G3", "H3", "", "", "",
            "", "", "", "", "A4", "B4", "C4", "D4", "E4", "F4", "G4", "H4", "", "", "",
            "", "", "", "", "A5", "B5", "C5", "D5", "E5", "F5", "G5", "H5", "", "", "",
            "", "", "", "", "A6", "B6", "C6", "D6", "E6", "F6", "G6", "H6", "", "", "",
            "", "", "", "", "A7", "B7", "C7", "D7", "E7", "F7", "G7", "H7", "", "", "",
            "", "", "", "", "A8", "B8", "C8", "D8", "E8", "F8", "G8", "H8", "", "", "",
    };

    private static final int[] RANKS = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0,
            0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0,
            0, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0,
            0, 0, 0, 0, 5, 5, 5, 5, 5, 5, 5, 5, 0, 0, 0,
            0, 0, 0, 0, 6, 6, 6, 6, 6, 6, 6, 6, 0, 0, 0,
            0, 0, 0, 0, 7, 7, 7, 7, 7, 7, 7, 7, 0, 0, 0,
            0, 0, 0, 0, 8, 8, 8, 8, 8, 8, 8, 8, 0, 0, 0
    };

    private static final int[] FILES = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0
    };

    // Functions

    public static boolean isEmpty(int square) {
        return square == Square.EMPTY;
    }

    public static String getSquareName(int square) {
        int indexToArray = square - (DiscoveryState.UPPER_BORDER_SIZE * DiscoveryState.NO_OF_FILES);
        return SQUARE_NAMES[indexToArray];
    }

    public static int getRank(int square) {
        return RANKS[square];
    }

    public static int getFile(int square) {
        return FILES[square];
    }

}
