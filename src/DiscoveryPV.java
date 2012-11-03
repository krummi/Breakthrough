/**
 * PV
 *
 * Keeps track of the Principal Variation (for minimax-based search ).
 *
 */
import java.util.ArrayList;

public class DiscoveryPV
{
    int       m_n;
    int  [][] m_pv;

    DiscoveryPV( int n )
    {
        m_n  = n;
        m_pv = new int[m_n][m_n];

        for ( int i=0; i<m_n; ++i ) {
            for ( int j=0; j<m_n; ++j ) {
                m_pv[i][j] = -1;
            }
        }
    }

    void set( int ply )
    {
        m_pv[ply][ply] = -1;
    }

    void set( int ply, int move )
    {
        m_pv[ply][ply] = move;
        for ( int j=ply+1; j<m_n ; ++j ) {
            m_pv[ply][j] = m_pv[ply+1][j];
            if ( -1 == m_pv[ply+1][j] ) break;
        }
    }

    public ArrayList<Integer> getPV()
    {
        ArrayList<Integer> moves = new ArrayList<Integer>();
        for ( int j=0; j<m_n && m_pv[0][j] != -1 ; ++j ) {
            moves.add( m_pv[0][j] );
        }
        return moves;
    }
}
