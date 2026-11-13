/*
 * ============================================================================
 *  MODULE 06 : HASH TABLE with OPEN ADDRESSING (Linear / Quadratic / Double)
 * ============================================================================
 *
 *  COLLISION RESOLUTION #2 : OPEN ADDRESSING
 *  -----------------------------------------
 *  All entries live directly IN the array (no chains). On a collision we
 *  PROBE other slots following a deterministic sequence until we find an
 *  empty one:
 *
 *   Linear probing    : idx = (h + i)        mod m     i = 0, 1, 2, ...
 *   Quadratic probing : idx = (h + i*i)      mod m
 *   Double hashing    : idx = (h + i*h2(k))  mod m     h2(k) never 0
 *
 *  PRIMARY CLUSTERING: with linear probing, occupied slots form long runs;
 *  any key hashing into a run must walk to its end, making the run longer.
 *  Quadratic probing jumps out of runs; double hashing gives each key its
 *  own step size, which is best at high load.
 *
 *  DELETION NEEDS TOMBSTONES
 *  -------------------------
 *  If we simply emptied a slot, a later search could stop early at that hole
 *  and miss a key that was placed AFTER it during probing. So deletion writes
 *  a special DELETED marker: searches skip over it, inserts may reuse it.
 *
 *  WHAT IF THE TABLE IS FULL?
 *  --------------------------
 *  Open addressing must resize before alpha gets close to 1 (we resize at
 *  0.5). Chaining never becomes "full" but slows down as chains grow.
 *
 *  CHAINING vs OPEN ADDRESSING
 *   | Aspect            | Chaining              | Open addressing           |
 *   |-------------------|-----------------------|---------------------------|
 *   | Memory            | extra node pointers   | compact single array      |
 *   | Cache behaviour   | poor (pointer chase)  | great (linear probing)    |
 *   | Load factor       | can exceed 1          | must stay < 1 (~0.5-0.7)  |
 *   | Deletion          | easy                  | tombstones                |
 *   Python's dict and Java's IdentityHashMap use open addressing.
 *
 *  TIME COMPLEXITY: put/get/remove O(1) average, O(n) worst. Space O(m).
 *
 *  HOW TO RUN
 *  ----------
 *   javac HashTableOpenAddressing.java && java HashTableOpenAddressing
 * ============================================================================
 */

import java.util.Random;

public class HashTableOpenAddressing {

    enum Probing { LINEAR, QUADRATIC, DOUBLE }

    static class OpenHashTable {
        private static final String DELETED = "<deleted>";   // tombstone marker (compared by reference)
        private String[] keys;
        private int[] values;
        private int size;               // live keys
        private int used;               // live keys + tombstones (both block probing)
        private final Probing mode;
        private final double maxLoad;
        long totalProbes;               // statistics for the comparison demo

        OpenHashTable(int capacity, Probing mode, double maxLoad) {
            keys = new String[capacity];
            values = new int[capacity];
            this.mode = mode;
            this.maxLoad = maxLoad;
        }

        private int h1(String k, int m) { return (k.hashCode() & 0x7fffffff) % m; }

        /** Secondary hash for double hashing: must be non-zero and ideally coprime with m. */
        private int h2(String k, int m) {
            int h = (k.hashCode() * 0x9E3779B1) >>> 1;          // different mixing
            return 1 + (h % (m - 1));
        }

        /** i-th probe position for key k in a table of size m. */
        private int probe(String k, int i, int m) {
            int h = h1(k, m);
            switch (mode) {
                case LINEAR:    return (h + i) % m;
                case QUADRATIC: return (int) ((h + (long) i * i) % m);
                default:        return (int) ((h + (long) i * h2(k, m)) % m);
            }
        }

        void put(String key, int value) {
            if ((double) (used + 1) / keys.length > maxLoad) resize(keys.length * 2);
            int firstTombstone = -1;
            for (int i = 0; i < keys.length; i++) {
                int idx = probe(key, i, keys.length);
                totalProbes++;
                if (keys[idx] == null) {                      // empty: key not present
                    int target = firstTombstone >= 0 ? firstTombstone : idx;   // reuse a tombstone
                    if (target == idx) used++;
                    keys[target] = key; values[target] = value; size++;
                    return;
                }
                if (keys[idx] == DELETED) { if (firstTombstone < 0) firstTombstone = idx; continue; }
                if (keys[idx].equals(key)) { values[idx] = value; return; }    // update
            }
            if (firstTombstone >= 0) { keys[firstTombstone] = key; values[firstTombstone] = value; size++; return; }
            resize(keys.length * 2);                          // quadratic probing may not visit every slot
            put(key, value);
        }

        Integer get(String key) {
            for (int i = 0; i < keys.length; i++) {
                int idx = probe(key, i, keys.length);
                if (keys[idx] == null) return null;           // a true hole ends the search
                if (keys[idx] != DELETED && keys[idx].equals(key)) return values[idx];
            }
            return null;
        }

        boolean remove(String key) {
            for (int i = 0; i < keys.length; i++) {
                int idx = probe(key, i, keys.length);
                if (keys[idx] == null) return false;
                if (keys[idx] != DELETED && keys[idx].equals(key)) {
                    keys[idx] = DELETED;                      // tombstone, NOT null
                    size--;
                    return true;
                }
            }
            return false;
        }

        private void resize(int newCap) {
            String[] oldK = keys; int[] oldV = values;
            keys = new String[newCap]; values = new int[newCap];
            size = used = 0;
            for (int i = 0; i < oldK.length; i++)
                if (oldK[i] != null && oldK[i] != DELETED) put(oldK[i], oldV[i]);   // tombstones are dropped
        }

        int size() { return size; }
        int capacity() { return keys.length; }

        String layout() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keys.length; i++)
                sb.append(String.format("    [%2d] %s%n", i, keys[i] == null ? "." : keys[i] == DELETED ? "XX (tombstone)" : keys[i]));
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Payment gateway duplicate-transaction detector.
    //  Every transaction ID is checked against a hash set; repeats are blocked.
    //  We also compare probe counts of the three strategies at high load.
    // ------------------------------------------------------------------------
    static void duplicatePaymentScenario() {
        System.out.println("\n=== SCENARIO: Duplicate Payment Detector ===");
        OpenHashTable seen = new OpenHashTable(16, Probing.DOUBLE, 0.5);
        String[] stream = {"TXN-1001", "TXN-1002", "TXN-1003", "TXN-1002", "TXN-1004", "TXN-1001", "TXN-1005"};
        int blocked = 0;
        for (String t : stream) {
            if (seen.get(t) != null) { blocked++; System.out.println("  " + t + " -> DUPLICATE, payment blocked"); }
            else { seen.put(t, 1); System.out.println("  " + t + " -> processed"); }
        }
        System.out.println("  blocked " + blocked + " duplicate charge(s)");

        System.out.println("\n  Probe cost at load factor 0.9 (10,000 random IDs, table 11,113 slots):");
        Random rnd = new Random(7);
        String[] idsArr = new String[10_000];
        for (int i = 0; i < idsArr.length; i++) idsArr[i] = "TXN-" + Long.toHexString(rnd.nextLong());
        for (Probing p : Probing.values()) {
            OpenHashTable t = new OpenHashTable(11_113, p, 0.95);   // prime size, no resize
            for (String id : idsArr) t.put(id, 1);
            System.out.printf("    %-9s avg probes per insert = %.2f%n", p, (double) t.totalProbes / idsArr.length);
        }
        System.out.println("  -> linear probing suffers from primary clustering at high load.");
    }

    public static void main(String[] args) {
        System.out.println("=== LINEAR PROBING WITH TOMBSTONES ===");
        OpenHashTable t = new OpenHashTable(11, Probing.LINEAR, 0.8);
        String[] words = {"Aa", "BB", "C#", "java", "dsa", "heap"};   // "Aa" and "BB" share a hashCode!
        for (String w : words) t.put(w, w.length());
        System.out.println("  \"Aa\".hashCode()=" + "Aa".hashCode() + " == \"BB\".hashCode()=" + "BB".hashCode() + " -> guaranteed collision");
        System.out.print(t.layout());
        System.out.println("  get(BB)=" + t.get("BB") + " (found by probing past Aa)");
        t.remove("Aa");
        System.out.println("  remove(Aa) leaves a tombstone:");
        System.out.print(t.layout());
        System.out.println("  get(BB) after delete = " + t.get("BB") + "  <- still found thanks to the tombstone");
        t.put("Aa", 42);
        System.out.println("  put(Aa) again reuses the tombstone slot, get(Aa)=" + t.get("Aa"));

        System.out.println("\n=== QUADRATIC & DOUBLE HASHING ===");
        for (Probing p : new Probing[]{Probing.QUADRATIC, Probing.DOUBLE}) {
            OpenHashTable q = new OpenHashTable(8, p, 0.5);
            for (int i = 0; i < 20; i++) q.put("key" + i, i);
            System.out.printf("  %-9s 20 keys -> size=%d capacity=%d (auto-resized) get(key13)=%d%n",
                    p, q.size(), q.capacity(), q.get("key13"));
        }
        duplicatePaymentScenario();
    }
}
