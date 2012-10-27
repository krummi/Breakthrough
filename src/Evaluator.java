public class Evaluator {

    public static void main(String[] args) {

        String setup = "bbb.b.bbbbb.bbbb........w.b.b.....b.......w.www.wwwww.w...w.wwww 1";

        State state = new DiscoveryState();
        state.setup(setup + " 1");
        state.display();

        Agent a = new AgentMCTS();

        Move m = a.playMove(state);
        System.out.println("Move: " + m.toStr());

    }

}
