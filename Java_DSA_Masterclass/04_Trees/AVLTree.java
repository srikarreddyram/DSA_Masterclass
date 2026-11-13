/*
 * ============================================================================
 *  MODULE 04 : AVL TREE (self-balancing Binary Search Tree)
 * ============================================================================
 *
 *  THE PROBLEM IT SOLVES
 *  ---------------------
 *  A plain BST built from sorted data degenerates into a linked list (height
 *  n-1), making every operation O(n). An AVL tree (Adelson-Velsky & Landis,
 *  1962) keeps itself balanced so height stays ~1.44*log2(n).
 *
 *  BALANCE FACTOR
 *  --------------
 *      bf(node) = height(left) - height(right)
 *  AVL invariant: bf is -1, 0 or +1 for EVERY node. After an insert/delete we
 *  walk back up the path and fix any node with |bf| > 1 using ROTATIONS.
 *
 *  THE FOUR IMBALANCE CASES
 *  ------------------------
 *  1) LEFT-LEFT (LL)   bf > 1 and new key went into left child's LEFT
 *                      -> single RIGHT rotation
 *            z                y
 *           / \             /   \
 *          y   T4          x     z
 *         / \      ==>    / \   / \
 *        x   T3          T1 T2 T3 T4
 *       / \
 *      T1 T2
 *
 *  2) RIGHT-RIGHT (RR) mirror of LL -> single LEFT rotation
 *
 *  3) LEFT-RIGHT (LR)  bf > 1 and key went into left child's RIGHT
 *                      -> LEFT rotate the child, then RIGHT rotate the node
 *          z              z               x
 *         /              /              /   \
 *        y      ==>     x      ==>     y     z
 *         \            /
 *          x          y
 *
 *  4) RIGHT-LEFT (RL)  mirror of LR -> RIGHT rotate child, LEFT rotate node
 *
 *  Each rotation is O(1): it only re-wires 3 references and updates 2 heights.
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - In-memory indexes needing guaranteed O(log n) lookups (lookup-heavy:
 *     AVL is more strictly balanced than Red-Black trees -> faster search)
 *   - Leaderboards, interval scheduling, order books, symbol tables
 *
 *  TIME COMPLEXITY  (all guaranteed - no "worst case O(n)")
 *  ---------------
 *   | Insert  O(log n) | Search O(log n) | Delete O(log n) | Traverse O(n) |
 *   | Rotation O(1)    | Delete entire tree O(1)                           |
 *   Space: O(n) nodes + O(log n) recursion stack
 *
 *  HOW TO RUN
 *  ----------
 *   javac AVLTree.java && java AVLTree
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AVLTree {

    static class Node {
        int key, height = 0;       // a leaf has height 0
        String data;
        Node left, right;
        Node(int k, String d) { key = k; data = d; }
    }

    static class AVL {
        Node root;
        int rotations = 0;
        List<String> rotationLog = new ArrayList<>();

        int height(Node n) { return n == null ? -1 : n.height; }   // null height = -1
        int balance(Node n) { return n == null ? 0 : height(n.left) - height(n.right); }
        void update(Node n) { n.height = 1 + Math.max(height(n.left), height(n.right)); }

        /**
         * RIGHT rotation around z (fixes LL).
         *      z            y
         *     /            / \
         *    y     ==>    x   z
         *   / \              /
         *  x   T3           T3     <- y's right subtree becomes z's left
         */
        Node rotateRight(Node z) {
            Node y = z.left;
            Node t3 = y.right;
            y.right = z;           // z moves down to the right
            z.left = t3;           // re-attach orphaned subtree
            update(z);             // z first: it is now BELOW y
            update(y);
            rotations++;
            return y;              // y is the new root of this subtree
        }

        /** LEFT rotation around z (fixes RR). Mirror image of rotateRight. */
        Node rotateLeft(Node z) {
            Node y = z.right;
            Node t2 = y.left;
            y.left = z;
            z.right = t2;
            update(z);
            update(y);
            rotations++;
            return y;
        }

        /**
         * Restore balance at node n (shared by insert and delete).
         * We decide the case using the CHILD's balance factor instead of the
         * inserted key, which makes the same code correct for deletion too.
         */
        Node rebalance(Node n) {
            update(n);
            int bf = balance(n);
            if (bf > 1) {                                   // left heavy
                if (balance(n.left) < 0) {                  // LR: child is right heavy
                    rotationLog.add("LR at " + n.key);
                    n.left = rotateLeft(n.left);            //   step 1: convert LR -> LL
                } else rotationLog.add("LL at " + n.key);
                return rotateRight(n);                      //   step 2 (or only step for LL)
            }
            if (bf < -1) {                                  // right heavy
                if (balance(n.right) > 0) {                 // RL: child is left heavy
                    rotationLog.add("RL at " + n.key);
                    n.right = rotateRight(n.right);         //   convert RL -> RR
                } else rotationLog.add("RR at " + n.key);
                return rotateLeft(n);
            }
            return n;                                       // already balanced
        }

        void insert(int key, String data) { root = insert(root, key, data); }

        private Node insert(Node n, int key, String data) {
            if (n == null) return new Node(key, data);     // standard BST insert...
            if (key < n.key) n.left = insert(n.left, key, data);
            else if (key > n.key) n.right = insert(n.right, key, data);
            else { n.data = data; return n; }
            return rebalance(n);                            // ...then fix on the way UP
        }

        void delete(int key) { root = delete(root, key); }

        private Node delete(Node n, int key) {
            if (n == null) return null;
            if (key < n.key) n.left = delete(n.left, key);
            else if (key > n.key) n.right = delete(n.right, key);
            else {
                if (n.left == null) return n.right;
                if (n.right == null) return n.left;
                Node succ = n.right;                        // in-order successor
                while (succ.left != null) succ = succ.left;
                n.key = succ.key; n.data = succ.data;
                n.right = delete(n.right, succ.key);
            }
            return rebalance(n);                            // deletion can need rotations too
        }

        Node search(int key) {
            Node c = root;
            while (c != null && c.key != key) c = key < c.key ? c.left : c.right;
            return c;
        }

        void inOrder(Node n, List<Node> out) { if (n == null) return; inOrder(n.left, out); out.add(n); inOrder(n.right, out); }

        String levelOrder() {
            StringBuilder sb = new StringBuilder();
            if (root == null) return "(empty)";
            Queue<Node> q = new LinkedList<>();
            q.add(root);
            while (!q.isEmpty()) {
                int cnt = q.size();
                sb.append("[");
                for (int i = 0; i < cnt; i++) {
                    Node n = q.poll();
                    sb.append(n.key).append(i < cnt - 1 ? " " : "");
                    if (n.left != null) q.add(n.left);
                    if (n.right != null) q.add(n.right);
                }
                sb.append("] ");
            }
            return sb.toString();
        }

        boolean isBalanced(Node n) {
            if (n == null) return true;
            return Math.abs(balance(n)) <= 1 && isBalanced(n.left) && isBalanced(n.right);
        }

        void deleteAll() { root = null; }
    }

    /** Plain (unbalanced) BST height, for comparison in the scenario. */
    static int plainBstHeight(int[] keys) {
        Node root = null;
        for (int k : keys) {
            Node n = new Node(k, "");
            if (root == null) { root = n; continue; }
            Node c = root;
            while (true) {
                if (k < c.key) { if (c.left == null) { c.left = n; break; } c = c.left; }
                else { if (c.right == null) { c.right = n; break; } c = c.right; }
            }
        }
        return heightOf(root);
    }

    static int heightOf(Node n) { return n == null ? -1 : 1 + Math.max(heightOf(n.left), heightOf(n.right)); }

    // ------------------------------------------------------------------------
    //  SCENARIO: Live game leaderboard keyed by player ID that arrives SORTED
    //  (new signups have increasing IDs - the worst case for a plain BST)
    // ------------------------------------------------------------------------
    static void leaderboardScenario() {
        System.out.println("\n=== SCENARIO: Player Registry with Sorted Signups ===");
        int n = 1000;
        int[] ids = new int[n];
        AVL registry = new AVL();
        for (int i = 0; i < n; i++) {
            ids[i] = 10_000 + i;                          // sequential IDs
            registry.insert(ids[i], "player_" + i);
        }
        System.out.println("  " + n + " players inserted in increasing ID order");
        System.out.println("  plain BST height : " + plainBstHeight(ids) + "  -> ~" + n + " comparisons per lookup");
        System.out.println("  AVL tree height  : " + registry.root.height + "    -> ~" + (registry.root.height + 1) + " comparisons per lookup");
        System.out.println("  rotations used   : " + registry.rotations);
        System.out.println("  lookup 10777     : " + registry.search(10_777).data);
        for (int id = 10_000; id < 10_500; id++) registry.delete(id);   // half the players leave
        System.out.println("  after 500 deletions height=" + registry.root.height
                + " balanced=" + registry.isBalanced(registry.root));
    }

    public static void main(String[] args) {
        System.out.println("=== AVL ROTATION CASES ===");
        int[][] cases = {{30, 20, 10}, {10, 20, 30}, {30, 10, 20}, {10, 30, 20}};
        String[] names = {"LL", "RR", "LR", "RL"};
        for (int i = 0; i < cases.length; i++) {
            AVL t = new AVL();
            for (int k : cases[i]) t.insert(k, "");
            System.out.printf("  insert %-12s (%s case) -> %-14s log=%s%n",
                    java.util.Arrays.toString(cases[i]), names[i], t.levelOrder(), t.rotationLog);
        }

        System.out.println("\n=== AVL OPERATIONS ===");
        AVL avl = new AVL();
        for (int k : new int[]{70, 50, 90, 30, 60, 80, 100, 20, 10, 95, 85, 87})
            avl.insert(k, "v" + k);
        System.out.println("  levels   : " + avl.levelOrder());
        System.out.println("  rotations: " + avl.rotationLog);
        List<Node> in = new ArrayList<>();
        avl.inOrder(avl.root, in);
        StringBuilder sb = new StringBuilder();
        for (Node n : in) sb.append(n.key).append("(h").append(n.height).append(",bf").append(avl.balance(n)).append(") ");
        System.out.println("  inOrder  : " + sb);
        System.out.println("  search 85: " + (avl.search(85) != null) + ", search 33: " + (avl.search(33) != null));
        avl.rotationLog.clear();
        avl.delete(10); avl.delete(20); avl.delete(30); avl.delete(60);
        System.out.println("  after deleting 10,20,30,60 -> " + avl.levelOrder() + " log=" + avl.rotationLog);
        System.out.println("  balanced? " + avl.isBalanced(avl.root));
        avl.deleteAll();
        System.out.println("  after deleteAll -> " + avl.levelOrder());

        leaderboardScenario();
    }
}
