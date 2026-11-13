/*
 * ============================================================================
 *  MODULE 04 : BINARY TREE - Linked (Node-based) implementation
 * ============================================================================
 *
 *  WHAT IS A BINARY TREE?
 *  ----------------------
 *  A hierarchical structure where each node has AT MOST two children (left,
 *  right). There is no ordering rule (that's what a BST adds).
 *
 *                 N1
 *               /    \
 *             N2      N3
 *            /  \    /  \
 *          N4   N5  N6   N7
 *
 *  TERMINOLOGY: root, parent, child, leaf (no children), sibling, depth of a
 *  node (edges from root), height of tree (edges on longest root-leaf path),
 *  level, subtree.
 *
 *  TYPES: Full (0 or 2 children), Perfect (all leaves same level), Complete
 *  (all levels full except last, filled left->right), Balanced, Skewed.
 *
 *  LINKED vs ARRAY representation
 *  ------------------------------
 *  Linked: each node holds references to its children. Flexible shape, no
 *          wasted slots for sparse/skewed trees. (This file.)
 *  Array : index arithmetic (left=2i, right=2i+1). Perfect for COMPLETE
 *          trees like heaps. (See BinaryTreeArray.java.)
 *
 *  TRAVERSALS
 *  ----------
 *   PreOrder  : Root, Left, Right  -> copy a tree, prefix expressions
 *   InOrder   : Left, Root, Right  -> sorted order in a BST, infix expr
 *   PostOrder : Left, Right, Root  -> delete a tree, evaluate expressions
 *   LevelOrder: breadth-first with a queue -> shortest depth, printing levels
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Expression trees in compilers/calculators (scenario below)
 *   - Decision trees in ML, Huffman coding trees, syntax trees
 *
 *  TIME / SPACE COMPLEXITY
 *  -----------------------
 *   | Operation                     | Time | Space                     |
 *   |-------------------------------|------|---------------------------|
 *   | Create                        | O(1) | O(1)                      |
 *   | Insert (first vacant, BFS)    | O(n) | O(n) queue                |
 *   | Search                        | O(n) | O(n)                      |
 *   | Pre/In/Post order (recursive) | O(n) | O(h) call stack           |
 *   | Level order                   | O(n) | O(n) queue                |
 *   | Delete node (swap w/ deepest) | O(n) | O(n)                      |
 *   | Delete entire tree            | O(1) | O(1)                      |
 *
 *  HOW TO RUN
 *  ----------
 *   javac BinaryTreeLinked.java && java BinaryTreeLinked
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

public class BinaryTreeLinked {

    static class Node<T> {
        T value;
        Node<T> left, right;
        Node(T v) { value = v; }
    }

    static class BinaryTree<T> {
        Node<T> root;

        // ---------------- traversals ----------------
        void preOrder(Node<T> n, List<T> out)  { if (n == null) return; out.add(n.value); preOrder(n.left, out); preOrder(n.right, out); }
        void inOrder(Node<T> n, List<T> out)   { if (n == null) return; inOrder(n.left, out); out.add(n.value); inOrder(n.right, out); }
        void postOrder(Node<T> n, List<T> out) { if (n == null) return; postOrder(n.left, out); postOrder(n.right, out); out.add(n.value); }

        /** BFS using a queue: visit level 0, then level 1, ... */
        List<List<T>> levelOrder() {
            List<List<T>> levels = new ArrayList<>();
            if (root == null) return levels;
            Queue<Node<T>> q = new LinkedList<>();
            q.add(root);
            while (!q.isEmpty()) {
                int count = q.size();               // nodes on the CURRENT level
                List<T> level = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    Node<T> n = q.poll();
                    level.add(n.value);
                    if (n.left != null) q.add(n.left);
                    if (n.right != null) q.add(n.right);
                }
                levels.add(level);
            }
            return levels;
        }

        /** Iterative in-order with an explicit stack (interview favourite). */
        List<T> inOrderIterative() {
            List<T> out = new ArrayList<>();
            Deque<Node<T>> st = new ArrayDeque<>();
            Node<T> cur = root;
            while (cur != null || !st.isEmpty()) {
                while (cur != null) { st.push(cur); cur = cur.left; }   // go far left
                cur = st.pop();
                out.add(cur.value);
                cur = cur.right;                                          // then right subtree
            }
            return out;
        }

        // ---------------- search ----------------
        boolean search(T v) {
            if (root == null) return false;
            Queue<Node<T>> q = new LinkedList<>();
            q.add(root);
            while (!q.isEmpty()) {
                Node<T> n = q.poll();
                if (n.value.equals(v)) return true;
                if (n.left != null) q.add(n.left);
                if (n.right != null) q.add(n.right);
            }
            return false;
        }

        // ---------------- insert ----------------
        /**
         * Insert at the FIRST vacant position in level order. This keeps the
         * tree as compact (complete) as possible.
         */
        void insert(T v) {
            Node<T> n = new Node<>(v);
            if (root == null) { root = n; return; }
            Queue<Node<T>> q = new LinkedList<>();
            q.add(root);
            while (!q.isEmpty()) {
                Node<T> cur = q.poll();
                if (cur.left == null)  { cur.left = n; return; }
                q.add(cur.left);
                if (cur.right == null) { cur.right = n; return; }
                q.add(cur.right);
            }
        }

        // ---------------- delete ----------------
        /**
         * Delete a node in a plain binary tree:
         *  1) find the node to delete AND the deepest (last level-order) node
         *  2) copy deepest value into the target
         *  3) remove the deepest node (it's always a leaf)
         * The tree stays compact and no subtree is orphaned.
         */
        boolean delete(T v) {
            if (root == null) return false;
            if (root.left == null && root.right == null) {
                if (root.value.equals(v)) { root = null; return true; }
                return false;
            }
            Queue<Node<T>> q = new LinkedList<>();
            q.add(root);
            Node<T> target = null, deepest = null, deepestParent = null;
            while (!q.isEmpty()) {
                Node<T> cur = q.poll();
                if (cur.value.equals(v) && target == null) target = cur;
                if (cur.left != null)  { deepestParent = cur; q.add(cur.left); }
                if (cur.right != null) { deepestParent = cur; q.add(cur.right); }
                deepest = cur;
            }
            if (target == null) return false;
            target.value = deepest.value;                 // overwrite
            if (deepestParent.right == deepest) deepestParent.right = null;
            else deepestParent.left = null;              // unlink the leaf
            return true;
        }

        int height(Node<T> n) { return n == null ? -1 : 1 + Math.max(height(n.left), height(n.right)); }

        int countLeaves(Node<T> n) {
            if (n == null) return 0;
            if (n.left == null && n.right == null) return 1;
            return countLeaves(n.left) + countLeaves(n.right);
        }

        Node<T> mirror(Node<T> n) {           // invert the tree (Google interview classic)
            if (n == null) return null;
            Node<T> t = n.left; n.left = mirror(n.right); n.right = mirror(t);
            return n;
        }

        void deleteTree() { root = null; }    // GC reclaims every unreachable node

        /** Sideways pretty-printer: right subtree on top, root at left margin. */
        void print(Node<T> n, String indent, StringBuilder sb) {
            if (n == null) return;
            print(n.right, indent + "      ", sb);
            sb.append("  ").append(indent).append(n.value).append('\n');
            print(n.left, indent + "      ", sb);
        }

        void print() { StringBuilder sb = new StringBuilder(); print(root, "", sb); System.out.print(sb); }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Expression Tree - parse postfix, print infix, evaluate
    // ------------------------------------------------------------------------
    static Node<String> buildExpressionTree(String postfix) {
        Deque<Node<String>> st = new ArrayDeque<>();
        for (String tok : postfix.split("\\s+")) {
            Node<String> n = new Node<>(tok);
            if ("+-*/^".contains(tok) && tok.length() == 1) {
                n.right = st.pop();      // operator: second operand first!
                n.left = st.pop();
            }
            st.push(n);
        }
        return st.pop();
    }

    static String toInfix(Node<String> n) {          // in-order with brackets
        if (n.left == null) return n.value;
        return "(" + toInfix(n.left) + " " + n.value + " " + toInfix(n.right) + ")";
    }

    static double evaluate(Node<String> n) {         // post-order evaluation
        if (n.left == null) return Double.parseDouble(n.value);
        double a = evaluate(n.left), b = evaluate(n.right);
        switch (n.value) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return a / b;
            default:  return Math.pow(a, b);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== LINKED BINARY TREE OPERATIONS ===");
        BinaryTree<String> t = new BinaryTree<>();
        for (int i = 1; i <= 9; i++) t.insert("N" + i);
        t.print();
        List<String> pre = new ArrayList<>(), in = new ArrayList<>(), post = new ArrayList<>();
        t.preOrder(t.root, pre); t.inOrder(t.root, in); t.postOrder(t.root, post);
        System.out.println("  preOrder   : " + pre);
        System.out.println("  inOrder    : " + in);
        System.out.println("  inOrder(it): " + t.inOrderIterative());
        System.out.println("  postOrder  : " + post);
        System.out.println("  levelOrder : " + t.levelOrder());
        System.out.println("  height=" + t.height(t.root) + " leaves=" + t.countLeaves(t.root));
        System.out.println("  search N5 -> " + t.search("N5") + ", search N42 -> " + t.search("N42"));
        t.delete("N3");
        System.out.println("  delete N3 (replaced by deepest N9): " + t.levelOrder());
        t.mirror(t.root);
        System.out.println("  mirrored   : " + t.levelOrder());
        t.deleteTree();
        System.out.println("  after deleteTree root=" + t.root);

        System.out.println("\n=== SCENARIO: Expression Tree Calculator ===");
        String[] exprs = {"3 4 + 2 *", "5 1 2 + 4 * + 3 -", "2 3 ^ 4 2 / -"};
        for (String e : exprs) {
            Node<String> root = buildExpressionTree(e);
            BinaryTree<String> tree = new BinaryTree<>();
            tree.root = root;
            List<String> prefix = new ArrayList<>();
            tree.preOrder(root, prefix);
            System.out.printf("  postfix %-20s infix %-28s prefix %-24s = %.2f%n",
                    e, toInfix(root), String.join(" ", prefix), evaluate(root));
        }
    }
}
