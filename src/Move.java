/**
 *
 * Move
 *
 */

public class Move
{
    int from_col;
    int from_row;
    int to_col;
    int to_row;
    boolean capture;

    private static String s_columns = "abcdefghijklmnopqrstuvwxyz";
    private static String s_rows    = "1234567890ABCDEFGHIJKLMNOP";

    public Move( int fc, int fr, int tc, int tr, boolean c )
    {
       from_col = fc;
       from_row = fr;
       to_col   = tc;
       to_row   = tr;
       capture  = c;
    }

    public String toStr()
    {
        String str = "";
        str += s_columns.charAt(from_col);
        str += s_rows.charAt(from_row);
        str += (capture ? "x" : "-" );
        str += s_columns.charAt(to_col);
        str += s_rows.charAt(to_row);
        return str;
    }

    boolean equals ( Move other ) {
        return ( from_col == other.from_col )
            && ( from_row == other.from_row )
            && ( to_col   == other.to_col   )
            && ( to_row   == other.to_row   )
            && ( capture  == other.capture  );
    }

    public String toString() {
        return toStr();
    }

}

