public class TranspositionTable {

    // Types

    public class HashEntry {
        public long key;
        public int type;
        public int depth;
        public int eval;
        public int move;

        HashEntry(long key, int type, int depth, int eval, int move) {
            this.key = key;
            this.type = type;
            this.depth = depth;
            this.eval = eval;
            this.move = move;
        }
    }

    // Constants

    private static final int NO_OF_TABLES = 2;

    // Member variables

    private HashEntry[] table;
    private int size;

    // Functions

    public TranspositionTable(int size) {
        this.size = size / NO_OF_TABLES;
        clear();
    }

    public void clear() {
        table = new HashEntry[size * NO_OF_TABLES];
    }

    public HashEntry get(long key) {
        int hashKey = (int) (key % size);

        for (int i = 0; i < NO_OF_TABLES; i++) {
            HashEntry entry = table[i * size + hashKey];
            if (entry != null && entry.key == key) {
                return entry;
            }
        }

        return null;
    }

    public void put(long key, int type, int depth, int eval, int move) {

        int hashKey = (int) (key % size);
        HashEntry entry = table[hashKey];

        if (entry == null) {
            // The depth entry is empty.
            table[hashKey] = new HashEntry(key, type, depth, eval, move);
        } else if (entry.depth <= depth) {
            // The depth is lower; replace.
            table[hashKey] = new HashEntry(key, type, depth, eval, move);
        } else if (entry.key == key && entry.move == DiscoveryMove.MOVE_NONE) {
            // An entry was found with this key, but it did not contain any move.
            entry.move = move;
        } else {
            // Put the entry into the always-replace spot.
            table[hashKey + size] = new HashEntry(key, type, depth, eval, move);
        }
    }

    public void putLeaf(long key, int eval, int alpha, int beta) {
        if (eval >= beta) {
            put(key, AgentDiscovery.SCORE_UPPER, 0, eval, DiscoveryMove.MOVE_NONE);
        } else if (eval <= alpha) {
            put(key, AgentDiscovery.SCORE_LOWER, 0, eval, DiscoveryMove.MOVE_NONE);
        } else {
            put(key, AgentDiscovery.SCORE_EXACT, 0, eval, DiscoveryMove.MOVE_NONE);
        }
    }

}
