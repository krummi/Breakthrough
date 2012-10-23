/**
 *
 * State Interface
 *
 */
import java.util.ArrayList;

public interface State {

    enum Result { Unknown, Loss, Win };

    // Win/Loss values
    public static int WIN_VALUE  =  10000;
    public static int LOSS_VALUE = -10000;

    // Get all possible actions in the state. If action 'first' is part of list, it will be
    // place first in the list.
    ArrayList<Move> getActions( Move first );

    // Make the provided action.
    void make( Move move );

    // Undo the last action.
    void retract( Move move );

    // Check if a state is terminal.
    boolean isTerminal();

    // Returns game outcome (Unknown, Loss, Win), relative to side to move.
    Result getResult();

    // Return side to move
    int getPlayerToMove();

    // Reset state to initial position.
    void reset();

    // Display state;
    void display();

    // Evaluate "goodness" of state.
    int getEvaluation();

    // Returns move is strMove represents legal move, otherwise null.
    Move isLegalMove( String strMove );

    // Returns string representation of the state (FEN-like).
    String toString();

    // Set up a game position (from a FEN-like string). True on success, false otherwise
    // (and the state is reset to the start state).
    boolean setup( String strFEN );

}
