import java.util.ArrayList;

public class Evaluator {

    public static void main(String[] args) {

        //String setup = "bbbbbbbbbbbbbbbb................................wwwwwwwwwwwwwwww 0";

        /*
        String setup =
                "........" +
                "........" +
                "........" +
                "........" +
                "bbbbbbb." +
                "bbbbbw.." +
                "wwwwwww." +
                "wwwwwwww 1";
        */

        String setup = "b........wb.bb..bb.b.bb.........b....w...w.....ww..w....w.ww..ww 1";

        BitboardState state = new BitboardState();
        state.setup(setup);
        state.display();
        //long x = (state.WP << 8) & ~(state.BP | state.WP);
        //state.printBitboard(x);
        //state.printBitboard(x);
        //while (x != 0) {
        //    long h = Long.highestOneBit(x);
        //    x &= ~h;
        //    int pos = 63 - Long.numberOfLeadingZeros(h);
        //    System.out.println("pos: " + pos);
        //}
        ArrayList<Move> moves = state.getActions(null);
        System.out.println(state.isTerminal());
        for (Move m : moves) {
            state.make(m);
            //if (state.isTerminal()) {
                System.out.println(m.toStr());
            //}
            state.retract(m);
        }
        //state.isplay();
        //state.printBitboard(state.WP);
        System.out.println(state.isTerminal());
        System.exit(0);

        //String setup = "bbb.b.bbbbb.bbbb........w.b.b.....b.......w.www.wwwww.w...w.wwww 1";

        //DiscoveryState    state1 = new DiscoveryState();
        BreakthroughState state1 = new BreakthroughState(8, 8);
        BitboardState     state2 = new BitboardState();
        state1.setup(setup);
        state2.setup(setup);

        /*
        ArrayList<Move> moves1 = state1.getActions(null);
        System.out.println("[Discovery] No of moves: " + moves1.size());
        ArrayList<Move> moves2 = state1.getActions(null);
        System.out.println("[Bitboard]  No of moves: " + moves2.size());
        */

        //perft(6, state1, state2);

        /*
        for (Move m : moves) {
            state.make(m);
            if (!state.isTerminal()) {
                ArrayList<Move> moves2 = state.getActions(null);
                for (Move move2 : moves2) {
                    state.make(move2);
                    state.retract(move2);
                }
            }
            state.retract(m);
        }
        state.display();
        */

        Agent a = new AgentAlphaBetaPlain();
        a.setSilence(false);
        a.setThinklimit(0, 0, 0);
        Move m = a.playMove(state1);
        state2.display();
        //state.display();

        //Agent a = new AgentAlphaBeta();
        //a.setThinklimit(5, 0, 0);

        //Move m = a.playMove(state);
        //System.out.println(state.key);
        //System.out.println("Move: " + m.toStr());
        //state.display();
    }

    public static void perft(int depth, DiscoveryState state1, BitboardState state2) {
        if (depth <= 0 || state1.isTerminal()) {
            assert !state1.isTerminal() || state2.isTerminal();
            return;
        }

        ArrayList<Move> moves1 = state1.getActions(null);
        ArrayList<Move> moves2 = state2.getActions(null);
        for (Move m : moves1) {

            Move move2 = null;
            for (Move aaa : moves2) {
                if (aaa.toStr().equals(m.toStr())) {
                    move2 = aaa;
                    break;
                }
            }
            if (move2 == null) {
                System.out.println("NO BITBOARD MOVE FOUND FOR: " + m.toStr());
                //state2.printBitboard(state2.BP);
                //state2.printBitboard(state2.WP);
                //long a = ((state2.WP & ~BitboardState.FILE_A) << 9) & state2.BP;
                /*
                while (a != 0) {
                    long h = Long.highestOneBit(a);
                    a &= ~h;
                    int pos = 63 - Long.numberOfLeadingZeros(h);
                    System.out.println("Move from: " + (pos + -9) + " -> " + pos);
                    //moves.add(new Move(pos + delta, -2, pos, -2, areCaptures));
                }
                */
                System.out.println("DISCOVERY:");
                state1.display();
                System.out.println("BITBOARD:");
                state2.display();
            }

            state1.make(m);
            state2.make(move2);
            perft(depth - 1, state1, state2);
            state1.retract(m);
            state2.retract(move2);
        }
    }

    public static void compareTwoLists(ArrayList<String> s1, ArrayList<String> s2) {
        System.out.println("NOT IN BITBOARD");
        for (String s : s1) {
            if (!s2.contains(s)) {
                System.out.println(s);
            }
        }
        System.out.println("NOT IN DISCOVERY");
        for (String s : s2) {
            if (!s1.contains(s)) {
                System.out.println(s);
            }
        }
    }

}
