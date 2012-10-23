/**
 *
 * BreakthroughState
 *
 */
import java.util.ArrayList;

public class BreakthroughState implements State
{
    enum Square { Empty, White, Black }

    private static String s_columns = "abcdefghijklmnopqrstuvxyz";
    private static String s_rows    = "123456789ABCDEFGHIJKLMNOP";

    private int         m_col;
    private int         m_row;
    private Square [][] m_board;
    private int         m_turn;
    private int []      m_countPces;
    private Result      m_result;

    public BreakthroughState( int row, int col )
    {
        m_row = Math.max( 4, Math.min( row, s_rows.length() ) );
        m_col = Math.max( 2, Math.min( col, s_columns.length() ) );
        m_board = new Square[m_row][m_col];
        m_countPces = new int[2];
        reset();
    }

    private void empty()
    {
        for ( int row=0; row<m_row; ++row ) {
            for ( int col=0; col<m_col; ++col ) {
                m_board[row][col] = Square.Empty;
            }
        }
        m_turn  = 0;
        m_countPces[0] = m_countPces[1] = 0;
        m_result = Result.Unknown;
    }

    public void reset()
    {
        empty();

        for ( int row=0; row<2; ++row ) {
            for ( int col=0; col<m_col; ++col ) {
                m_board[row][col] = Square.White;
                m_countPces[0]++;
            }
        }
        for ( int row=m_row-2; row<m_row; ++row ) {
            for ( int col=0; col<m_col; ++col ) {
                m_board[row][col] = Square.Black;
                m_countPces[1]++;
            }
        }
    }

    public boolean setup( String strFEN )
    {
        boolean error = false;
        boolean exit = false;
        int i  = 0;
        int n = strFEN.length();
        empty();
        for ( int row=m_row-1; row>=0 && !exit; --row ) {
            for ( int col=0; col<m_col && !exit; ++col ) {
                if ( i >= n ) {
                    exit = true;
                }
                else {
                    switch ( strFEN.charAt(i) ) {
                        case 'w':
                            m_board[row][col] = Square.White;
                            m_countPces[0]++;
                            break;
                        case 'b':
                            m_board[row][col] = Square.Black;
                            m_countPces[1]++;
                            break;
                        case '.':
                            break;
                        case ' ':
                            exit = true;
                            break;
                        default:
                            error = true;
                            exit = true;
                            break;
                    }
                    ++i;
                }
            }
        }

        if ( !error && (i < n) ) {
            int not_turn;
            if ( strFEN.charAt(i) == ' ' ) {
                ++i;
            }
            if ( i < n && strFEN.charAt(i) == '1' ) {
                m_turn = 1;
                not_turn = 0;
            }
            else {
                m_turn = 0;
                not_turn = 1;
            }

            // Check if a legal position:
            //  - side not-to-move must have at least one piece.
            //  - maximum one piece on backrank, and not for side-to-move.
            m_result =  Result.Unknown;
            int [] cntOnBackrank = { 0, 0 };
            for ( int col=0; col<m_col; ++col ) {
                if ( m_board[0][col] == Square.Black ) {
                    cntOnBackrank[1]++;
                }
                if ( m_board[m_row-1][col] == Square.White ) {
                    cntOnBackrank[0]++;
                }
            }
            if ( m_countPces[not_turn] == 0 ||
                 cntOnBackrank[m_turn] != 0 ||
                 cntOnBackrank[not_turn] > 1 ) {
                error = true;
            }
            else if ( cntOnBackrank[not_turn] == 1 || m_countPces[m_turn] == 0 ) {
                m_result = Result.Loss;
            }

        }

        if ( error ) {
            reset();
        }
        return !error;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        for ( int row=m_row-1; row>=0; --row ) {
            for ( int col=0; col<m_col; ++col ) {
                char c = ' ';
                switch ( m_board[row][col] ) {
                    case Empty: c='.'; break;
                    case White: c='w'; break;
                    case Black: c='b'; break;
                }
                buffer.append(c);
            }
        }
        buffer.append(' ');
        buffer.append( m_turn == 0 ? '0' : '1' );
        return buffer.toString();
    }

    public void display( )
    {
        ArrayList<Move> moves = new ArrayList<Move>();

        for ( int row=m_row-1; row>=0; --row ) {
            System.out.print( ' ' );
            System.out.print( row+1 );
            System.out.print( ' ' );
            for ( int col=0; col<m_col; ++col ) {
                char c = ' ';
                switch ( m_board[row][col] ) {
                    case Empty: c='.'; break;
                    case White: c='w'; break;
                    case Black: c='b'; break;
                }
                System.out.print(c);
            }
            System.out.println();
        }
        System.out.print( "   " );
        for ( int col=0; col<m_col; ++col ) {
             System.out.print( s_columns.charAt(col) );
        }
        System.out.println();
        System.out.println( m_turn == 0 ? " White" : " Black" );
        System.out.print( " Pieces: ");
        System.out.print( m_countPces[0] );
        System.out.print( " " );
        System.out.println( m_countPces[1] );
        System.out.print( " Terminal: ");
        System.out.println( isTerminal() );
        System.out.print( " Result: ");
        System.out.println( getResult() );
        System.out.print( " Evaluation: ");
        System.out.println( getEvaluation() );
        System.out.print( " FEN-str: ");
        System.out.println( toString() );
        System.out.print( " Moves: ");
        moves = getActions( null );
        for ( Move move : moves ) {
            System.out.print( ' ' );
            System.out.print( move.toStr() );
        }
        System.out.println();
    }


    public int getPlayerToMove( )
    {
        return m_turn;
    }

    private boolean sqrOK( int row, int col ) {
        return row >= 0 && row < m_row && col >= 0 && col < m_col;
    }

    private void addMove( ArrayList<Move> moves, Move move, Move placeFirst )
    {
        if ( placeFirst != null && placeFirst.equals( move ) ) {
            moves.add( 0, move );
        }
        else {
            moves.add( move );
        }
    }

    public ArrayList<Move> getActions( Move first )
    {
        ArrayList<Move> moves = new ArrayList<Move>();

        if ( !isTerminal() ) {
            Move move;
            if ( m_turn == 0 ) { // White
                for ( int col=0; col<m_col; ++col ) {
                    for ( int row=0; row<m_row; ++row ) {
                        if ( m_board[row][col] == Square.White ) {
                            int c = col; int r=row + 1;
                            if ( sqrOK(r,c) && m_board[r][c] == Square.Empty ) {
                                addMove(moves, new Move(col, row, c, r, false), first);
                            }
                            c = col - 1;
                            if ( sqrOK(r,c) && m_board[r][c] != Square.White )  {
                                addMove(moves, new Move(col, row, c, r, m_board[r][c] != Square.Empty), first);
                            }
                            c = col + 1;
                            if ( sqrOK(r,c) && m_board[r][c] != Square.White )  {
                                addMove(moves, new Move(col, row, c, r, m_board[r][c] != Square.Empty), first);
                            }
                        }
                    }

                }
            }
            else {  // Black
                for ( int col=0; col<m_col; ++col ) {
                    for ( int row=0; row<m_row; ++row ) {
                        if ( m_board[row][col] == Square.Black ) {
                            int c = col; int r=row - 1;
                            if ( sqrOK(r,c) && m_board[r][c]== Square.Empty )  {
                                addMove(moves, new Move(col, row, c, r, false), first);
                            }
                            c = col - 1;
                            if ( sqrOK(r,c) && m_board[r][c] != Square.Black )  {
                                addMove(moves, new Move(col, row, c, r, m_board[r][c] != Square.Empty), first);
                            }
                            c = col + 1;
                            if ( sqrOK(r,c) && m_board[r][c] != Square.Black )  {
                                addMove(moves, new Move(col, row, c, r, m_board[r][c] != Square.Empty), first);
                            }
                        }
                    }

                }
            }
        }
        return moves;
    }


    public void make( Move move )
    {
        assert ( !isTerminal() );

        m_board[move.to_row][move.to_col] = m_board[move.from_row][move.from_col];
        m_board[move.from_row][move.from_col] = Square.Empty;

        if ( (move.to_row == m_row - 1 &&  m_turn == 0) || (move.to_row == 0 &&  m_turn == 1) ) {
            // Side that moved wins, the other side (to move now) loses.
            m_result = Result.Loss;
        }
        else {
            m_result = Result.Unknown;
        }
        m_turn = ( ( m_turn == 0 ) ? 1 : 0 );
        if ( move.capture ) {
            m_countPces[m_turn]--;
            if ( m_countPces[m_turn] == 0 ) {
                m_result = Result.Loss;
            }
        }

    }


    public void retract( Move move )
    {
        final Square[] captured = { Square.White, Square.Black };
        Square sqr = Square.Empty;

        if ( move.capture ) {
            m_countPces[m_turn]++;
            sqr = captured[m_turn];
        }
        m_turn = ( ( m_turn == 0 ) ? 1 : 0 );
        m_board[move.from_row][move.from_col] = m_board[move.to_row][move.to_col];
        m_board[move.to_row][move.to_col] = sqr;
        m_result = Result.Unknown;
    }


    public boolean isTerminal()
    {
        return m_result != Result.Unknown;
    }

    public Result getResult()
    {
       return m_result;
    }


    public Move isLegalMove( String strMove )
    {
        ArrayList<Move> moves = getActions( null );
        for( Move move : moves ) {
            if ( move.toStr().equals( strMove ) ) {
                return move;
            }
        }
        return null;
    }

    public int getEvaluation()
    {
        int value = 0;

        if ( isTerminal() ) {
            Result result = getResult();
            if ( result == Result.Win ) {
               value = State.WIN_VALUE;
            }
            else if ( result == Result.Loss ) {
               value = State.LOSS_VALUE;
            }
            else {
                System.out.println("Error: should not happen.");
                value = 0;
            }
        }
        else {
            // Evaluate a non-terminal position (from the perspective of the side to move).
            value = m_countPces[0] - m_countPces[1];
            value = ( ( m_turn == 0 ) ? value : -value );
        }

        return value;
    }

}
