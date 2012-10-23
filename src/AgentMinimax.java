/**
 *
 * Agent using MiniMax search for thinking ahead.
 *
 */
import java.util.ArrayList;
import java.util.Random;

public class AgentMinimax implements Agent
{
    final int MAX_SEARCH_DEPTH = 100;

    private boolean m_silent;
    private long    m_depthLimit;
    private long    m_nodeLimit;
    private long    m_timeLimit;
    private Random  m_rng;
    private long    m_nodes;
    private long    m_msec;
    private boolean m_abort;
    private PV      m_pv;

    AgentMinimax( )
    {
        m_silent = true;
        m_rng = new Random();
        m_pv = new PV( MAX_SEARCH_DEPTH + 1 );
        m_depthLimit = m_nodeLimit = m_timeLimit = 0;
    }

    public void setSilence( boolean on )
    {
        m_silent = on;
    }

    public void setThinklimit( long maxLimit, long maxNodes, long maxTimeMsec )
    {
        m_depthLimit = maxLimit;
        m_nodeLimit = maxNodes;
        m_timeLimit = maxTimeMsec;
    }

    public Move playMove( State state )
    {
        m_msec = System.currentTimeMillis();
        m_nodes = 0;
        m_abort = false;

        Move bestMove = null;

        // We add some non-determinism to the player by randomizing the first action it looks at
        // (assuming given the crude evaluation function many actions will have the best-value).
        ArrayList<Move> moves = state.getActions( null );
        if ( moves.size() == 0 ) {
            return bestMove;
        }
        bestMove = moves.get( m_rng.nextInt(moves.size()) );

        long maxDepth = m_depthLimit;
        if ( maxDepth == 0 || maxDepth > MAX_SEARCH_DEPTH ) {
            maxDepth = MAX_SEARCH_DEPTH;
        }
        for ( int depth = 1 ; depth <= maxDepth && !m_abort; ++depth ) {
            // Make sure the best move from previous iteration is search first in this iteration,
            // this is necessary to ensure that in case of a timeout, the best move currently found
            // is returned.
            int value = minimax( 0, depth, state, bestMove );
            ArrayList<Move> pv = m_pv.getPV();
            long msec = java.lang.System.currentTimeMillis() - m_msec;
            if ( !m_silent ) { System.out.printf( "\t%2d %10d %7d", depth, m_nodes, msec ); }
            if ( !pv.isEmpty() && pv.get(0) != null ) {
                if ( !m_silent ) { System.out.printf( " %+6d ", value ); }
                for ( Move move : pv ) {
                    if ( !m_silent ) { System.out.print( ' ' + move.toStr() ); }
                }
                bestMove = pv.get( 0 );
            }
            if ( !m_silent ) { System.out.println(); }
         }

        return bestMove;
    }

    private boolean reachedALimit()
    {
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

    private int minimax( int ply, int depth, State state, Move firstMoveToLookAt )
    {
        m_pv.set( ply );
        m_nodes++;

        if ( depth <=0 || state.isTerminal() ) {
            m_abort = reachedALimit();
            return state.getEvaluation();
        }

        int bestValue = Integer.MIN_VALUE;
        ArrayList<Move> moves = state.getActions( firstMoveToLookAt );
        for ( Move move : moves ) {
            state.make( move );
            int value = -minimax( ply+1, depth-1, state, null );
            state.retract( move );
            if ( m_abort ) { break; }
            if ( value > bestValue ) {
                bestValue = value;
                m_pv.set( ply, move );
            }

        }
        return bestValue;
    }

}
