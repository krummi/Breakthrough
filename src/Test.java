import java.util.ArrayList;

public class Test {

    public static void main(String[] args) {
        State state = new DiscoveryState("bbb.b.bbbbb.bbbb........w.b.b.....b.......w.www.wwwww.w...w.wwww 1");
        System.out.println(state);
        state.display();
        /*ArrayList<Move> moves = state.getActions(null);
        System.out.println("Number of moves: " + moves.size());
        for (Move move : moves) {
            state.display();
            System.out.println("Move: " + move);
            state.make(move);
            state.display();
            state.retract(move);
        }
        */
    }

}
