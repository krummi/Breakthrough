/**
 * PV
 *
 * Keeps track of the Principal Variation (for minimax-based search ).
 *
 */
import java.util.ArrayList;

public class PV
{
    int       m_n;
    Move [][] m_pv;

    PV( int n )
    {
        m_n  = n;
        m_pv = new Move[m_n][m_n];

        for ( int i=0; i<m_n; ++i ) {
            for ( int j=0; j<m_n; ++j ) {
                m_pv[i][j] = null;
            }
        }
    }

    void set( int ply )
    {
         m_pv[ply][ply] = null;
    }

    void set( int ply, Move move )
    {
        m_pv[ply][ply] = move;
        for ( int j=ply+1; j<m_n ; ++j ) {
            m_pv[ply][j] = m_pv[ply+1][j];
            if ( null == m_pv[ply+1][j] ) break;
        }
    }    

    public ArrayList<Move> getPV()
    {
        ArrayList<Move> moves = new ArrayList<Move>();
        for ( int j=0; j<m_n && m_pv[0][j] != null ; ++j ) {
             moves.add( m_pv[0][j] );
        }
        return moves;
    }
}
