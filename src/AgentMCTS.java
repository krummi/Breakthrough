import java.util.ArrayList;
import java.util.Random;

/**
 *
 * Agent using MCTS to think ahead.
 *
 */
public class AgentMCTS implements Agent {
    private static final Random random = new Random();
    private static final double EXPLORATION_CONSTANT = 0.5;
    private static final int WHITE = 0;
    private static final int BLACK = 1;

    private boolean m_silent;
    private long    m_depthLimit;
    private long    m_nodeLimit;
    private long    m_timeLimit;

    private long m_nodes;
    private long m_msec;
    private long m_simulations;

    AgentMCTS () {
        m_silent = true;
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

        Move move = UCTSearch(state);
        System.out.println(move.toStr());
        return move;
    }

    private Move UCTSearch(State state) {
        MCTSNode v0 = new MCTSNode();
        String stateStr = state.toString();
        // TODO: Fix this stupidity.
        while (!reachedALimit()) {
            MCTSNode v1 = treePolicy(v0, state);
            int delta = defaultPolicy(v1, state);
            backup(v1, delta);
            state.setup(stateStr);
        }
        state.setup(stateStr);

        System.out.println(bestChild(v0, 0).value);
        return bestChild(v0, 0).move;
    }

    private MCTSNode treePolicy(MCTSNode node, State state) {
        while (!state.isTerminal()) {
            // Generates moves if it hasn't been done.
            if (node.untried == 0) {
                assert node.getChildCount() == 0;
                ArrayList<Move> moves = state.getActions(null);
                for (Move m : moves) {
                    MCTSNode newNode = new MCTSNode(m);
                    assert !newNode.tried;
                    assert newNode.untried == 0;
                    node.addChild(newNode);
                }
            }

            // Checks whether the node IS FULLY EXPANDED.
            if (node.untried < node.getChildCount()) {
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

            double value = child.value / child.visits; // TODO: child.visits.
            value += c * Math.sqrt((2 * Math.log(parent.visits)) / child.visits);

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
            if (!testNode.tried) {
                next = testNode;
                break;
            }
        }

        // This particular node has been tried out:
        assert next != null;
        next.tried = true;

        // ... and a single more node has been tried out in the context of the parent.
        node.untried += 1;

        //int randInt = random.nextInt(moves.size());
        return next;
    }

    private int defaultPolicy(MCTSNode node, State state) {
        while (!state.isTerminal()) {
            ArrayList<Move> moves = state.getActions(null);

            // Choose a move at random.
            Move move = moves.get(random.nextInt(moves.size()));
            state.make(move);
        }

        // Increase the number of simulations
        m_simulations++;

        return state.getEvaluation();
    }

    // TODO: General utility functions.

    private boolean reachedALimit()
    {
        if (m_depthLimit > 0 && m_simulations >= m_depthLimit) {
            return true;
        }
        if ( (m_nodeLimit > 0) && (m_nodes >= m_nodeLimit) ) {
            return true;
        }
        if ( (m_timeLimit > 0) && ((m_nodes % 1000) == 0) ) {
            if ( (java.lang.System.currentTimeMillis() - m_msec) >= m_timeLimit ) {
                return true;
            }
        }
        return false;
    }

}



class MCTSNode {

    // Members

    // TODO: Move to getters/setters.
    public Move move;
    public long value;
    public int visits;

    // Has this been tried?
    public boolean tried;

    // Number of untried actions.
    public int untried;

    public ArrayList<MCTSNode> children;
    public MCTSNode parent;

    // Constructors

    public MCTSNode() {
        this.children = new ArrayList<MCTSNode>();
        this.visits = 0;
        this.value = 0;
        this.untried = 0;
        this.tried = false;
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