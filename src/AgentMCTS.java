import java.util.ArrayList;
import java.util.Random;

/**
 *
 * Agent using MCTS to think ahead.
 *
 */
public class AgentMCTS implements Agent {

    public enum RootMoveSelector {
        MostVisits,
        HighestAverage
    }

    private static final Random random = new Random();
    private static final double EXPLORATION_CONSTANT = 5000;

    // Configuration stuff
    private RootMoveSelector rootMoveSelector;

    private boolean m_silent;
    private long    m_depthLimit;
    private long    m_nodeLimit;
    private long    m_timeLimit;

    private long m_nodes;
    private long m_msec;
    private long m_simulations;

    public AgentMCTS () {
        m_silent = true;
        rootMoveSelector = RootMoveSelector.MostVisits;
    }

    public AgentMCTS(RootMoveSelector rms) {
        this.rootMoveSelector = rms;
    }

    public void setSilence(boolean on) {
        m_silent = on;
    }

    public void setThinklimit(long maxLimit, long maxNodes, long maxTimeMsec) {
        m_depthLimit = maxLimit;
        m_nodeLimit  = maxNodes;
        m_timeLimit  = maxTimeMsec;
    }

    public Move playMove(State state) {
        m_msec = System.currentTimeMillis();
        m_nodes = 0;
        m_simulations = 0;

        // TODO: Deterministic mode!
        // random.setSeed(3);

        Move move = UCTSearch(state);
        if (!m_silent) System.out.println("No of simulations: " + m_simulations);
        return move;
    }

    private Move UCTSearch(State state) {
        MCTSNode v0 = new MCTSNode();
        String stateStr = state.toString();
        // TODO: Make this better?
        while (!reachedALimit()) {
            // TODO: Move neðst?
            state.setup(stateStr);
            MCTSNode v1 = treePolicy(v0, state);
            int delta = defaultPolicy(state);
            backup(v1, delta);
        }
        state.setup(stateStr);

        for (int i = 0; i < v0.getChildCount(); i++) {
            if (!m_silent) System.out.println(v0.getChild(i).move.toStr() + ": " + v0.getChild(i).value + " / " + v0.getChild(i).visits + " = " + (v0.getChild(i).value / v0.getChild(i).visits));
        }

        if (rootMoveSelector == RootMoveSelector.MostVisits) {
            Move bestMove = null;
            int mostVisits = Integer.MIN_VALUE;
            for (int i = 0; i < v0.getChildCount(); i++) {
                if (v0.getChild(i).visits > mostVisits) {
                    mostVisits = v0.getChild(i).visits;
                    bestMove = v0.getChild(i).move;
                }
            }
            assert bestMove != null;
            return bestMove;
        }
        // else rms = HighestAverage:
        return bestChild(v0, 0).move;
    }

    private MCTSNode treePolicy(MCTSNode node, State state) {

        while (!state.isTerminal()) {
            if (node.noOfChildrenExplored == 0) {
                assert node.getChildCount() == 0;
                ArrayList<Move> moves = state.getActions(null);
                for (Move m : moves) {
                    MCTSNode newNode = new MCTSNode(m);
                    node.addChild(newNode);
                }
            }
            // Checks whether the node IS ___FULLY___ EXPANDED.
            if (node.noOfChildrenExplored < node.getChildCount()) {
                MCTSNode out = expand(node);
                state.make(out.move);
                return out;
            } else {
                node = bestChild(node, EXPLORATION_CONSTANT);
                state.make(node.move);
            }
        }


        return node;
    }

    private MCTSNode bestChild(MCTSNode parent, double c) {
        MCTSNode bestNode = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (MCTSNode child : parent.getChildren()) {
            assert child.visits != 0;

            double value = child.value / child.visits;
            value += c * Math.sqrt(Math.log(parent.visits) / child.visits);

            if (value > bestValue) {
                bestNode = child;
                bestValue = value;
            }
        }

        assert bestNode != null;
        return bestNode;
    }

    private void backup(MCTSNode node, int delta) {
        while (node != null) {
            node.visits++;
            node.value += delta;
            delta = -delta;
            node = node.getParent();
        }
    }

    private MCTSNode expand(MCTSNode node) {
        assert node.getChildCount() > 0;

        // Finds TODO: the next node that which has not been tried? Is this okay?
        MCTSNode next = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            MCTSNode testNode = node.getChild(i);
            if (!testNode.explored) {
                next = testNode;
                break;
            }
        }

        // This particular node has been tried out:
        assert next != null;
        next.explored = true;

        // ... and a single more node has been explored in the context of the parent.
        node.noOfChildrenExplored += 1;

        return next;
    }

    private int defaultPolicy(State state) {
        int initialSideToMove = oppColor(state.getPlayerToMove());

        while (!state.isTerminal()) {
            // Commenting this out generates beautiful ASCII waterfalls:
            // System.out.println(state.toString());
            ArrayList<Move> moves = state.getActions(null);

            if (moves.size() == 0) {
                state.display();
            }

            // Choose a move at random.
            Move move = moves.get(random.nextInt(moves.size()));
            state.make(move);
        }

        // Increase the number of simulations
        m_simulations++;

        // WHITE NODE - WHITE LOSES.
        // BLACK NODE - BLACK LOSES.
        if (initialSideToMove == state.getPlayerToMove()) {
            // Side to move wins.
            return State.LOSS_VALUE;
        } else {
            // Side to move loses.
            return State.WIN_VALUE;
        }
    }

    private static int oppColor(int color) {
        return color ^ 1;
    }

    // TODO: General utility functions.

    private boolean reachedALimit() {
        if (m_depthLimit > 0 && m_simulations >= m_depthLimit) {
            return true;
        }
        if (m_nodeLimit > 0 && m_nodes >= m_nodeLimit) {
            return true;
        }
        if (m_timeLimit > 0 && (m_nodes % 1000) == 0) {
            if ((System.currentTimeMillis() - m_msec) >= m_timeLimit ) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return String.format("MCTS (rms: %s, c: %.0f)",
                rootMoveSelector.toString(), EXPLORATION_CONSTANT);
    }

}



class MCTSNode {

    // Members

    // TODO: Move to getters/setters.
    public Move move;
    public long value;
    public int visits;
    public boolean explored;
    public int noOfChildrenExplored;

    public ArrayList<MCTSNode> children;
    public MCTSNode parent;

    // Constructors

    public MCTSNode() {
        this.children = new ArrayList<MCTSNode>();
        this.visits = 0;
        this.value = 0;
        this.noOfChildrenExplored = 0;
    }

    public MCTSNode(Move move) {
        this();
        this.move = move;
    }

    // Functions

    public ArrayList<MCTSNode> getChildren() {
        return children;
    }

    public int getChildCount() {
        return children.size();
    }

    public void addChild(MCTSNode node) {
        node.parent = this;
        children.add(node);
    }

    public MCTSNode getChild(int index) {
        return children.get(index);
    }

    public MCTSNode getParent() {
        return parent;
    }

}