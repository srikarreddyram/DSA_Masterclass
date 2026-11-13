/*
 * ============================================================================
 *  MODULE 04 : BINARY SEARCH TREE (BST)
 * ============================================================================
 *
 *  THE BST PROPERTY
 *  ----------------
 *  For EVERY node:   all keys in LEFT subtree  <  node.key  <  all keys in RIGHT
 *  Consequence: an in-order traversal visits keys in SORTED order, and each
 *  comparison lets us discard half of the remaining tree (like binary search).
 *
 *                  70
 *               /      \
 *             50        90
 *            /  \      /  \
 *          30    60  80   100
 *         /  \
 *       20    40
 *
 *  DELETION - THE THREE CASES
 *  --------------------------
 *   1) Leaf         -> just remove it.
 *   2) One child    -> replace the node with its only child.
 *   3) Two children -> copy the IN-ORDER SUCCESSOR (smallest key in the right
 *                      subtree) into the node, then delete that successor
 *                      (which has at most one child -> case 1 or 2).
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Ordered maps/sets (java.util.TreeMap is a Red-Black BST)
 *   - Range queries: "all products priced 500..2000", auto-complete ranges
 *   - Database indexes (B-Trees generalise BSTs to many keys per node)
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   | Operation            | Average  | Worst (skewed) |
 *   |----------------------|----------|----------------|
 *   | Insert/Search/Delete | O(log n) | O(n)           |
 *   | Min / Max            | O(log n) | O(n)           |
 *   | Floor / Ceiling      | O(log n) | O(n)           |
 *   | Traversal            | O(n)     | O(n)           |
 *   | Range query          | O(log n + k)  (k = results) |
 *   Space: O(n) for nodes, O(h) recursion stack.
 *   Worst case happens with SORTED input - the tree becomes a linked list.
 *   Fix: self-balancing trees (AVL - see AVLTree.java).
 *
 *  HOW TO RUN
 *  ----------
 *   javac BinarySearchTree.java && java BinarySearchTree
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BinarySearchTree {

    /** Generic key/value BST - essentially a hand-written TreeMap. */
    static class BST<K extends Comparable<K>, V> {
        class Node {
            K key; V val; Node left, right;
            Node(K k, V v) { key = k; val = v; }
        }

        Node root;
        int size;

        // ---------------- insert ----------------
        void put(K key, V val) { root = put(root, key, val); }

        private Node put(Node n, K key, V val) {
            if (n == null) { size++; return new Node(key, val); }   // found the empty spot
            int cmp = key.compareTo(n.key);
            if (cmp < 0)      n.left  = put(n.left, key, val);
            else if (cmp > 0) n.right = put(n.right, key, val);
            else              n.val = val;                           // duplicate key: update
            return n;
        }

        // ---------------- search (iterative - no stack needed) ----------------
        V get(K key) {
            Node cur = root;
            while (cur != null) {
                int cmp = key.compareTo(cur.key);
                if (cmp == 0) return cur.val;
                cur = cmp < 0 ? cur.left : cur.right;   // discard half the tree
            }
            return null;
        }

        K min() { Node n = root; while (n.left != null) n = n.left; return n.key; }
        K max() { Node n = root; while (n.right != null) n = n.right; return n.key; }

        /** Largest key <= target (e.g. "best item I can afford"). */
        K floor(K target) {
            Node cur = root; K best = null;
            while (cur != null) {
                int cmp = target.compareTo(cur.key);
                if (cmp == 0) return cur.key;
                if (cmp < 0) cur = cur.left;
                else { best = cur.key; cur = cur.right; }   // candidate; try bigger
            }
            return best;
        }

        /** Smallest key >= target. */
        K ceiling(K target) {
            Node cur = root; K best = null;
            while (cur != null) {
                int cmp = target.compareTo(cur.key);
                if (cmp == 0) return cur.key;
                if (cmp > 0) cur = cur.right;
                else { best = cur.key; cur = cur.left; }
            }
            return best;
        }

        /** Collect keys in [lo, hi], pruning subtrees that can't contain matches. */
        void range(Node n, K lo, K hi, List<Node> out) {
            if (n == null) return;
            if (lo.compareTo(n.key) < 0) range(n.left, lo, hi, out);     // left may have matches
            if (lo.compareTo(n.key) <= 0 && hi.compareTo(n.key) >= 0) out.add(n);
            if (hi.compareTo(n.key) > 0) range(n.right, lo, hi, out);    // right may have matches
        }

        // ---------------- delete ----------------
        void delete(K key) { root = delete(root, key); }

        private Node delete(Node n, K key) {
            if (n == null) return null;                    // key not found
            int cmp = key.compareTo(n.key);
            if (cmp < 0) { n.left = delete(n.left, key); return n; }
            if (cmp > 0) { n.right = delete(n.right, key); return n; }

            // found it
            if (n.left == null)  { size--; return n.right; }   // case 1 & 2
            if (n.right == null) { size--; return n.left; }    // case 2
            // case 3: two children -> in-order successor
            Node succ = n.right;
            while (succ.left != null) succ = succ.left;
            n.key = succ.key;
            n.val = succ.val;
            n.right = delete(n.right, succ.key);           // successor has <= 1 child
            return n;
        }

        // ---------------- traversals ----------------
        void inOrder(Node n, List<K> out)   { if (n == null) return; inOrder(n.left, out); out.add(n.key); inOrder(n.right, out); }
        void preOrder(Node n, List<K> out)  { if (n == null) return; out.add(n.key); preOrder(n.left, out); preOrder(n.right, out); }
        void postOrder(Node n, List<K> out) { if (n == null) return; postOrder(n.left, out); postOrder(n.right, out); out.add(n.key); }

        List<K> levelOrder() {
            List<K> out = new ArrayList<>();
            if (root == null) return out;
            Queue<Node> q = new LinkedList<>();
            q.add(root);
            while (!q.isEmpty()) {
                Node n = q.poll(); out.add(n.key);
                if (n.left != null) q.add(n.left);
                if (n.right != null) q.add(n.right);
            }
            return out;
        }

        int height(Node n) { return n == null ? -1 : 1 + Math.max(height(n.left), height(n.right)); }

        /** Validate the BST property using min/max bounds (classic interview Q). */
        boolean isValid(Node n, K lo, K hi) {
            if (n == null) return true;
            if ((lo != null && n.key.compareTo(lo) <= 0) || (hi != null && n.key.compareTo(hi) >= 0)) return false;
            return isValid(n.left, lo, n.key) && isValid(n.right, n.key, hi);
        }

        /** Lowest Common Ancestor - exploits ordering, O(h). */
        K lca(K a, K b) {
            Node cur = root;
            while (cur != null) {
                if (a.compareTo(cur.key) < 0 && b.compareTo(cur.key) < 0) cur = cur.left;
                else if (a.compareTo(cur.key) > 0 && b.compareTo(cur.key) > 0) cur = cur.right;
                else return cur.key;      // split point
            }
            return null;
        }

        void deleteAll() { root = null; size = 0; }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Product catalogue indexed by price (range filter, budget pick)
    // ------------------------------------------------------------------------
    static void productCatalogueScenario() {
        System.out.println("\n=== SCENARIO: E-commerce Price Index ===");
        BST<Integer, String> byPrice = new BST<>();
        Object[][] products = {
                {1299, "Wireless Mouse"}, {4999, "Mechanical Keyboard"}, {899, "USB Cable"},
                {15999, "27in Monitor"}, {2499, "Webcam 1080p"}, {7499, "Noise-Cancel Headphones"},
                {349, "Mouse Pad"}, {3299, "Laptop Stand"}, {54999, "Gaming GPU"}, {1999, "USB-C Hub"}};
        for (Object[] p : products) byPrice.put((Integer) p[0], (String) p[1]);

        List<BST<Integer, String>.Node> filter = new ArrayList<>();
        byPrice.range(byPrice.root, 1000, 5000, filter);
        System.out.println("  Filter Rs 1000 - 5000 (sorted, pruned search):");
        for (BST<Integer, String>.Node n : filter) System.out.printf("    Rs %6d  %s%n", n.key, n.val);

        int budget = 7000;
        Integer f = byPrice.floor(budget);
        System.out.println("  Best item within budget Rs " + budget + " : " + byPrice.get(f) + " (Rs " + f + ")");
        Integer c = byPrice.ceiling(10000);
        System.out.println("  Cheapest item at/above Rs 10000        : " + byPrice.get(c) + " (Rs " + c + ")");
        System.out.println("  Cheapest overall: Rs " + byPrice.min() + " | Priciest: Rs " + byPrice.max());
        byPrice.delete(54999);
        System.out.println("  GPU sold out -> priciest now Rs " + byPrice.max() + " (" + byPrice.get(byPrice.max()) + ")");
    }

    public static void main(String[] args) {
        System.out.println("=== BST OPERATIONS ===");
        BST<Integer, String> bst = new BST<>();
        for (int k : new int[]{70, 50, 90, 30, 60, 80, 100, 20, 40}) bst.put(k, "v" + k);
        List<Integer> in = new ArrayList<>(), pre = new ArrayList<>(), post = new ArrayList<>();
        bst.inOrder(bst.root, in); bst.preOrder(bst.root, pre); bst.postOrder(bst.root, post);
        System.out.println("  inOrder (sorted!): " + in);
        System.out.println("  preOrder         : " + pre);
        System.out.println("  postOrder        : " + post);
        System.out.println("  levelOrder       : " + bst.levelOrder());
        System.out.println("  size=" + bst.size + " height=" + bst.height(bst.root) + " valid=" + bst.isValid(bst.root, null, null));
        System.out.println("  get(60)=" + bst.get(60) + " get(65)=" + bst.get(65));
        System.out.println("  floor(65)=" + bst.floor(65) + " ceiling(65)=" + bst.ceiling(65));
        System.out.println("  LCA(20,40)=" + bst.lca(20, 40) + " LCA(20,60)=" + bst.lca(20, 60) + " LCA(40,100)=" + bst.lca(40, 100));

        bst.delete(20);  System.out.println("  delete 20 (leaf)        -> " + bst.levelOrder());
        bst.delete(30);  System.out.println("  delete 30 (one child)   -> " + bst.levelOrder());
        bst.delete(70);  System.out.println("  delete 70 (two children, root; successor 80 moves up) -> " + bst.levelOrder());

        BST<Integer, String> skewed = new BST<>();
        for (int i = 1; i <= 10; i++) skewed.put(i, "");
        System.out.println("  sorted insert 1..10 -> height " + skewed.height(skewed.root) + "  (degenerates into a list: O(n) search!)");

        productCatalogueScenario();
    }
}
