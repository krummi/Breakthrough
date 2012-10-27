import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Perft {

    public static void main(String[] args) {

        String setup =
                "........" +
                "..b....." +
                "........" +
                "....w..." +
                "........" +
                "........" +
                "........" +
                "........";

        setup = "bbb.b.bbbbb.bbbb........w.b.b.....b.......w.www.wwwww.w...w.wwww 1";
        /*
        setup = "" +
                "b......b" +
                "bb.b...b" +
                ".bb.bb.b" +
                "........" +
                "www.w..." +
                "w..w.www" +
                ".ww..w.." +
                "...ww..w";
        /////////abcdefgh
        */

        /*
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            br.readLine();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        */

        long before = System.currentTimeMillis();
        //State state = new BreakthroughState(8, 8);
        State state = new DiscoveryState();
        state.setup(
                "b......b" +
                        "bb.b...b" +
                        ".bb.bb.b" +
                        "........" +
                        "www.w..." +
                        "w..w.www" +
                        ".ww..w.." +
                        "...ww..w 0");

        state.display();

        Agent a = new AgentAlphaBeta();
        a.setSilence(false);
        a.setThinklimit(9, 0, 0);
        Move m = a.playMove(state);
        System.out.println("Move: " + m.toStr());
        state.display();
        System.out.println();
        System.out.println("Delta: " + (System.currentTimeMillis() - before));
    }

}
