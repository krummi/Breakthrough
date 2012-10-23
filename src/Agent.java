/**
 *
 * Agent Interface.
 *
 */
public interface Agent
{
    enum Thinklimit { Depth, Simulations, Nodes, Time };

    // Agent does not print any output.
    public void setSilence( boolean on );

    // Agent think limit.
    public void setThinklimit( long maxLimit, long maxNodes, long maxTimeMsec );

    // Pick a move to play, return null if terminal position.
    public Move playMove( State state );
}
