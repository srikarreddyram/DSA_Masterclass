/*
 * ============================================================================
 *  MODULE 04 : BINARY TREE - Array implementation (complete binary tree)
 * ============================================================================
 *
 *  INDEX ARITHMETIC (1-based, index 0 left unused for cleaner math)
 *  -----------------------------------------------------------------
 *     node at index i
 *       left child  = 2i
 *       right child = 2i + 1
 *       parent      = i / 2   (integer division)
 *
 *            [1]A
 *          /      \
 *       [2]B      [3]C          arr: [ - | A | B | C | D | E | F | G ]
 *       /  \      /  \                 0   1   2   3   4   5   6   7
 *     [4]D [5]E [6]F [7]G
 *
 *  (0-based variant: left = 2i+1, right = 2i+2, parent = (i-1)/2.)
 *
 *  WHY AN ARRAY?
 *  -------------
 *   + No child references -> less memory, excellent cache locality
 *   + O(1) jump to parent/child (great for heaps & segment trees)
 *   - Only efficient for COMPLETE trees. A skewed tree of height h needs
 *     2^(h+1) slots even though it has h+1 nodes.
 *   - Fixed capacity (unless you resize)
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Binary heaps / priority queues (see BinaryHeap.java)
 *   - Segment trees, Fenwick trees, tournament trees (scenario below)
 *
 *  TIME / SPACE COMPLEXITY
 *  -----------------------
 *   | Operation             | Time | Space |
 *   |-----------------------|------|-------|
 *   | Create (capacity n)   | O(1) | O(n)  |
 *   | Insert (next slot)    | O(1) | O(1)  |  <- faster than linked version!
 *   | Search                | O(n) | O(1)  |
 *   | Traversals            | O(n) | O(h) recursion |
 *   | Delete (swap w/ last) | O(n) (search) | O(1) |
 *   | Delete entire tree    | O(1) | O(1)  |
 *
 *  HOW TO RUN
 *  ----------
 *   javac BinaryTreeArray.java && java BinaryTreeArray
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;

public class BinaryTreeArray {

    static class ArrayBinaryTree {
        private String[] arr;
        private int lastUsedIndex = 0;      // index 0 unused; tree starts at 1

        ArrayBinaryTree(int capacity) { arr = new String[capacity + 1]; }

        boolean isFull() { return lastUsedIndex == arr.length - 1; }

        /** O(1): the next free slot in level order is simply lastUsedIndex+1. */
        boolean insert(String v) {
            if (isFull()) { System.out.println("  tree is full, cannot insert " + v); return false; }
            arr[++lastUsedIndex] = v;
            return true;
        }

        int search(String v) {
            for (int i = 1; i <= lastUsedIndex; i++) if (arr[i].equals(v)) return i;
            return -1;
        }

        void preOrder(int i, List<String> out) {
            if (i > lastUsedIndex) return;              // past the last node = null child
            out.add(arr[i]);
            preOrder(2 * i, out);
            preOrder(2 * i + 1, out);
        }

        void inOrder(int i, List<String> out) {
            if (i > lastUsedIndex) return;
            inOrder(2 * i, out);
            out.add(arr[i]);
            inOrder(2 * i + 1, out);
        }

        void postOrder(int i, List<String> out) {
            if (i > lastUsedIndex) return;
            postOrder(2 * i, out);
            postOrder(2 * i + 1, out);
            out.add(arr[i]);
        }

        /** Level order is just a linear scan of the array! No queue needed. */
        List<String> levelOrder() {
            List<String> out = new ArrayList<>();
            for (int i = 1; i <= lastUsedIndex; i++) out.add(arr[i]);
            return out;
        }

        /** Replace the target with the LAST element, then shrink. */
        boolean delete(String v) {
            int idx = search(v);
            if (idx == -1) return false;
            arr[idx] = arr[lastUsedIndex];
            arr[lastUsedIndex--] = null;
            return true;
        }

        String parentOf(String v) { int i = search(v); return i > 1 ? arr[i / 2] : null; }

        void deleteTree() { arr = new String[arr.length]; lastUsedIndex = 0; }

        void printLevels() {
            int level = 0;
            for (int start = 1; start <= lastUsedIndex; start *= 2, level++) {
                StringBuilder sb = new StringBuilder("  level " + level + ": ");
                for (int i = start; i < start * 2 && i <= lastUsedIndex; i++)
                    sb.append(arr[i]).append("[").append(i).append("] ");
                System.out.println(sb);
            }
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Knock-out tournament bracket as an array tree.
    //  8 players occupy leaves at indices 8..15. Each internal node i stores the
    //  winner of its children 2i and 2i+1. The champion ends up at index 1.
    // ------------------------------------------------------------------------
    static void tournamentScenario() {
        System.out.println("\n=== SCENARIO: Knock-out Tournament Bracket (array tree) ===");
        String[] players = {"Anand", "Carlsen", "Gukesh", "Nakamura", "Pragg", "Caruana", "Ding", "Arjun"};
        int[] rating = {2750, 2830, 2790, 2800, 2760, 2795, 2740, 2780};
        int n = players.length;              // must be a power of two
        int[] tree = new int[2 * n];         // stores PLAYER INDEX at each node

        for (int i = 0; i < n; i++) tree[n + i] = i;          // leaves
        String[] roundNames = {"", "Final", "Semi-finals", "", "Quarter-finals"};
        for (int i = n - 1; i >= 1; i--) {                     // bottom-up: parents
            int a = tree[2 * i], b = tree[2 * i + 1];
            tree[i] = rating[a] >= rating[b] ? a : b;           // higher rating wins
        }
        // print round by round: internal nodes [n/2..n-1], then [n/4..n/2-1], ...
        for (int start = n / 2; start >= 1; start /= 2) {
            System.out.println("  " + roundNames[start] + ":");
            for (int i = start; i < 2 * start; i++) {
                int a = tree[2 * i], b = tree[2 * i + 1];
                System.out.printf("    %-9s vs %-9s -> %s%n", players[a], players[b], players[tree[i]]);
            }
        }
        System.out.println("  CHAMPION: " + players[tree[1]]);

        // Bonus: who would win if the champion withdrew? Replay only one path - O(log n)
        int champ = tree[1];
        int saved = rating[champ];
        rating[champ] = Integer.MIN_VALUE;                     // champion forfeits
        for (int i = (n + champ) / 2; i >= 1; i /= 2) {        // walk leaf -> root only
            int a = tree[2 * i], b = tree[2 * i + 1];
            tree[i] = rating[a] >= rating[b] ? a : b;
        }
        rating[champ] = saved;
        System.out.println("  If " + players[champ] + " withdraws, replaying ONE path (O(log n)) -> new winner: "
                + players[tree[1]]);
    }

    public static void main(String[] args) {
        System.out.println("=== ARRAY BINARY TREE OPERATIONS ===");
        ArrayBinaryTree t = new ArrayBinaryTree(9);
        for (String s : new String[]{"Drinks", "Hot", "Cold", "Tea", "Coffee", "Cola", "Juice", "Green", "Masala"}) t.insert(s);
        t.insert("Overflow");
        t.printLevels();
        List<String> pre = new ArrayList<>(), in = new ArrayList<>(), post = new ArrayList<>();
        t.preOrder(1, pre); t.inOrder(1, in); t.postOrder(1, post);
        System.out.println("  preOrder  : " + pre);
        System.out.println("  inOrder   : " + in);
        System.out.println("  postOrder : " + post);
        System.out.println("  levelOrder: " + t.levelOrder());
        System.out.println("  search(Cola) -> index " + t.search("Cola") + ", parent = " + t.parentOf("Cola"));
        t.delete("Hot");
        System.out.println("  delete(Hot) -> " + t.levelOrder());
        t.deleteTree();
        System.out.println("  after deleteTree -> " + t.levelOrder());

        tournamentScenario();
    }
}
