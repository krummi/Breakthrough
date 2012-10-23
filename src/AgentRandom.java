/**
 *
 * Agent playing (legal) moves at random.
 *
 */

import java.util.ArrayList;
import java.util.Random;

public class AgentRandom implements Agent
{
    AgentRandom( )
    {
        m_silent = true;
        m_rng = new Random();
    }


    public void setSilence( boolean on )
    {
        m_silent = on;
    }

    public void setThinklimit( long maxLimit, long maxNodes, long maxTimeMsec )
    {
        // Ignore.
    }

    public Move playMove( State state )
    {
        Move bestMove = null;

        ArrayList<Move> moves = state.getActions( null );
        if ( moves.size() > 0 ) {
            bestMove = moves.get( m_rng.nextInt(moves.size()));
        }
        return bestMove;
    }

    private boolean m_silent;
    private Random m_rng;
}
