public class DiscoveryMove {

    public static void main(String[] args) {
        int move = DiscoveryMove.createMove(63, 54, false);

        System.out.println("To: " + DiscoveryMove.getTo(move));
        System.out.println("From: " + DiscoveryMove.getFrom(move));
        System.out.println("IsCapture: " + DiscoveryMove.isCapture(move));
    }

    // Constants
    public static final int MOVE_NONE   = -1;
    private static final int FROM_SHIFT = 1;
    private static final int TO_SHIFT   = 7; // 1 + 6

    // Static helper functions
    public static int createMove(int from, int to, boolean capture) {
        return (capture ? 1 : 0) | (from << FROM_SHIFT) | (to << TO_SHIFT);
    }

    public static int getTo(int move) {
        return (move >> TO_SHIFT) & 0x3f;
    }

    public static int getFrom(int move) {
        return (move >> FROM_SHIFT) & 0x3f;
    }

    public static boolean isCapture(int move) {
        return (move & 1) == 1;
    }

    public static int deserialize(Move m) {
        int from = m.from_row * 8 + (7 - m.from_col);
        int to   = m.to_row   * 8 + (7 - m.to_col);
        return createMove(from, to, m.capture);
    }

    public static Move serialize(int move) {
        int from        = getFrom(move);
        int to          = getTo(move);
        boolean capture = isCapture(move);

        int from_row = from / 8;
        int from_col = 7 - (from - (from_row * 8));
        int to_row   = to / 8;
        int to_col   = 7 - (to - (to_row * 8));

        return new Move(from_col, from_row, to_col, to_row, capture);
    }


    public static String stringify(int move) {
        int from         = getFrom(move);
        int to           = getTo(move);
        boolean capture  = isCapture(move);
        return "" + DiscoveryState.SQUARES[from].toLowerCase()
                  + (capture ? "x" : "-") + DiscoveryState.SQUARES[to].toLowerCase();
    }

}
