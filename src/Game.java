import java.util.ArrayList;

public class Game {

    private State           m_initialState;
    private ArrayList<Move> m_moves;

    public Game( State init ) {
        m_initialState = init;
        m_moves = new ArrayList<Move>();
    }
    public State getInitialState() {
        return m_initialState;
    }

    public ArrayList<Move> getMoveHistory() {
        return m_moves;
    }

    public Move getLastMove() {
        Move move = null;
        if ( !m_moves.isEmpty() ) {
            move = m_moves.get( m_moves.size() - 1 );
        }
        return move;
    }

    public void removeLastMove() {
        if ( !m_moves.isEmpty() ) {
            m_moves.remove( m_moves.size() - 1 );
        }
    }

    public void reset( State init ) {
        m_initialState.setup( init.toString() );
        m_moves.clear();
    }

    public void addMove( Move move ) {
        m_moves.add( move );
    }

    void makeStateCurrent( State state ) {
        state.setup( getInitialState().toString() );
        for ( Move move : m_moves ) {
            state.make( move );
        }
    }
}
