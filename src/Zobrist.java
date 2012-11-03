import java.util.Random;

public class Zobrist {

    private static final Random random = new Random(32L);

    public static final long[][] PIECES = new long[OldState.NO_OF_COLORS][OldState.NO_OF_SQUARES];
    public static final long SIDE_TO_MOVE;

    // Functions

    static {
        // Generates random keys for each square, of each color:
        for (int a = 0; a < OldState.NO_OF_COLORS; a++) {
            for (int b = 0; b < OldState.NO_OF_SQUARES; b++) {
                PIECES[a][b] = Math.abs(random.nextLong());
            }
        }

        SIDE_TO_MOVE = Math.abs(random.nextLong());
    }

    public static long getZobristKey(final OldState state) {

        long key = 0L;

        // Every piece on the board:
        for (int i = 0; i < OldState.NO_OF_SQUARES; i++) {
            int square = state.squares[i];
            if (square == OldState.WHITE || square == OldState.BLACK) {
                key ^= PIECES[square][i];
            }
        }

        // Side to move
        if (state.sideToMove == OldState.BLACK) {
            key ^= SIDE_TO_MOVE;
        }

        return key;
    }

}
