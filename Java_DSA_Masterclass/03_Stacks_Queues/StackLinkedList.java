/*
 * ============================================================================
 *  MODULE 03 : STACK (Linked-List / Node-based implementation)
 * ============================================================================
 *
 *  WHY A LINKED-LIST STACK?
 *  ------------------------
 *  The TOP of the stack is simply the HEAD of a singly linked list.
 *    push -> insert at head  (O(1))
 *    pop  -> remove head     (O(1))
 *  No capacity limit, so there is no isFull(); memory grows one node at a time.
 *
 *        top
 *         |
 *       [30|*] -> [20|*] -> [10|null]
 *
 *  ARRAY STACK vs LINKED-LIST STACK
 *  --------------------------------
 *   | Aspect          | Array             | Linked List             |
 *   |-----------------|-------------------|-------------------------|
 *   | Capacity        | fixed (or resize) | unbounded               |
 *   | Memory/element  | 1 slot            | 1 slot + 1 reference    |
 *   | Cache locality  | excellent         | poor                    |
 *   | push/pop        | O(1)              | O(1)                    |
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Undo/redo where history length is unpredictable
 *   - Iterative DFS / backtracking with unknown depth
 *   - Browser back stack
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   create O(1) | push O(1) | pop O(1) | peek O(1) | isEmpty O(1) | delete O(1)
 *   Space: O(n)
 *
 *  HOW TO RUN
 *  ----------
 *   javac StackLinkedList.java && java StackLinkedList
 * ============================================================================
 */

public class StackLinkedList {

    static class Node<T> {
        T value; Node<T> next;
        Node(T v, Node<T> next) { this.value = v; this.next = next; }
    }

    static class LinkedStack<T> {
        private Node<T> top;
        private int size;

        boolean isEmpty() { return top == null; }
        int size() { return size; }

        void push(T v) { top = new Node<>(v, top); size++; }   // new node points to old top

        T pop() {
            if (isEmpty()) throw new IllegalStateException("Stack underflow");
            T v = top.value;
            top = top.next;        // old top is now unreachable -> GC
            size--;
            return v;
        }

        T peek() {
            if (isEmpty()) throw new IllegalStateException("Stack is empty");
            return top.value;
        }

        void deleteStack() { top = null; size = 0; }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder("[top] ");
            for (Node<T> c = top; c != null; c = c.next) sb.append(c.value).append(' ');
            return sb.append("[bottom]").toString();
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Text editor with UNDO and REDO (two stacks)
    // ------------------------------------------------------------------------
    static class Editor {
        private String text = "";
        private final LinkedStack<String> undo = new LinkedStack<>();
        private final LinkedStack<String> redo = new LinkedStack<>();

        void type(String s) {
            undo.push(text);          // snapshot BEFORE the change
            redo.deleteStack();       // new edit invalidates redo history
            text += s;
            System.out.printf("  type   %-10s -> \"%s\"%n", "'" + s + "'", text);
        }

        void undo() {
            if (undo.isEmpty()) { System.out.println("  undo   (nothing to undo)"); return; }
            redo.push(text);
            text = undo.pop();
            System.out.printf("  undo              -> \"%s\"%n", text);
        }

        void redo() {
            if (redo.isEmpty()) { System.out.println("  redo   (nothing to redo)"); return; }
            undo.push(text);
            text = redo.pop();
            System.out.printf("  redo              -> \"%s\"%n", text);
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 2: Stock span / "Next Greater Element" using a monotonic stack
    // ------------------------------------------------------------------------
    static int[] nextGreater(int[] prices) {
        int[] ans = new int[prices.length];
        LinkedStack<Integer> st = new LinkedStack<>();   // holds INDICES, values decreasing
        for (int i = prices.length - 1; i >= 0; i--) {
            // pop everything not greater than current - they can never be an answer
            while (!st.isEmpty() && prices[st.peek()] <= prices[i]) st.pop();
            ans[i] = st.isEmpty() ? -1 : prices[st.peek()];
            st.push(i);
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println("=== LINKED-LIST STACK OPERATIONS ===");
        LinkedStack<String> s = new LinkedStack<>();
        for (String x : new String[]{"A", "B", "C", "D"}) s.push(x);
        System.out.println("  after pushes : " + s + " size=" + s.size());
        System.out.println("  peek         : " + s.peek());
        System.out.println("  pop          : " + s.pop() + " -> " + s);
        s.deleteStack();
        System.out.println("  after delete : isEmpty=" + s.isEmpty());

        System.out.println("\n=== SCENARIO 1: Editor Undo / Redo ===");
        Editor e = new Editor();
        e.type("Hello");
        e.type(" DSA");
        e.type(" world");
        e.undo();
        e.undo();
        e.redo();
        e.type("!");
        e.redo();

        System.out.println("\n=== SCENARIO 2: Next Greater Stock Price (monotonic stack) ===");
        int[] prices = {100, 80, 60, 70, 60, 75, 85};
        int[] ng = nextGreater(prices);
        for (int i = 0; i < prices.length; i++)
            System.out.printf("  day %d price %3d -> next higher price: %s%n", i, prices[i], ng[i] == -1 ? "none" : ng[i]);
    }
}
