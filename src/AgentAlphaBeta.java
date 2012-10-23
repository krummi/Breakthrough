/**
 *
 * Agent using alpha-beta-search to think ahead.
 *
 */
public class AgentAlphaBeta implements Agent
{
    private boolean m_silent;
    private long    m_depthLimit;
    private long    m_nodeLimit;
    private long    m_timeLimit;

    AgentAlphaBeta( )
    {
        m_silent = true;
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
        Move bestMove = null;

        return bestMove;
    }

}
