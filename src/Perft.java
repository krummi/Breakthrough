import sun.management.resources.agent;

public class Perft {

    public static void main(String[] args) {

        String setup =
                "........" +
                "........" +
                "..b....." +
                "........" +
                "....w..." +
                "........" +
                "........" +
                "........";

        State state = new BreakthroughState(8, 8);
        state.setup(setup + " 0");
        state.display();

        Agent a = new AgentMCTS();
        a.setSilence(false);
        a.setThinklimit(5, 0, 0);
        Move m = a.playMove(state);
        System.out.println("Move: " + m.toStr());
        state.make(m);
        state.display();

        /*
        Agent[] agents = new Agent[]{ new AgentMinimax(), new AgentAlphaBeta() };
        agents[0].setThinklimit( 7, 0, 0 );
        agents[1].setThinklimit( 7, 0, 0 );

        agents[0].setSilence(false);
        agents[1].setSilence(false);

        //Move move = agents[0].playMove(state);
        //System.out.println("Best move: " + move.toStr());

        Move move2 = agents[1].playMove(state);
        System.out.println("Best move: " + move2.toStr());
        */
    }

}
