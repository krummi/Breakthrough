import java.util.Random;

public class DiscoveryZobrist {

    // Constants
    private static final Random random = new Random(32L);

    public static final long[][] PIECES = new long[DiscoveryState.NO_OF_COLORS][DiscoveryState.NO_OF_SQUARES];
    public static final long SIDE_TO_MOVE;

    // Functions

    static {
        // Generates random keys for each square, of each color:
        for (int a = 0; a < DiscoveryState.NO_OF_COLORS; a++) {
            for (int b = 0; b < DiscoveryState.NO_OF_SQUARES; b++) {
                PIECES[a][b] = Math.abs(random.nextLong());
            }
        }
        SIDE_TO_MOVE = Math.abs(random.nextLong());
    }

    public static long getZobristKey(final DiscoveryState state) {
        long key = 0L;

        // Every piece on the board (hack, I know):
        String s = state.toString();
        for (int i = 0; i < 64; i++) {
            switch (s.charAt(i)) {
                case 'b': key ^= PIECES[DiscoveryState.BLACK][63 - i]; break;
                case 'w': key ^= PIECES[DiscoveryState.WHITE][63 - i]; break;
            }
        }
        // Side to move
        if (state.getPlayerToMove() == DiscoveryState.BLACK) { // Black to move.
            key ^= SIDE_TO_MOVE;
        }
        return key;
    }

}
