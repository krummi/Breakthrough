/**
 *
 * Agent Interface.
 *
 */
public interface Agent
{
    // Agent does not print any output.
    public void setSilence( boolean on );

    // Agent think limit.
    public void setThinklimit( long maxLimit, long maxNodes, long maxTimeMsec );


    public String getName();

    // Pick a move to play, return null if terminal position.
    public Move playMove( State state, Game gameHistory );

}
