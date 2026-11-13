/*
 * ============================================================================
 *  MODULE 03 : STACK (Array-based implementation)
 * ============================================================================
 *
 *  WHAT IS A STACK?
 *  ----------------
 *  A LIFO (Last-In, First-Out) collection. Think of a pile of plates: you can
 *  only add to or take from the TOP.
 *
 *        push(30)          pop() -> 30
 *        |    |            |    |
 *        | 30 | <- top     |    |
 *        | 20 |            | 20 | <- top
 *        | 10 |            | 10 |
 *        +----+            +----+
 *
 *  ARRAY IMPLEMENTATION
 *  --------------------
 *  We keep an int 'top' = index of the top element (-1 when empty).
 *    push : arr[++top] = v
 *    pop  : return arr[top--]
 *  Fixed capacity -> isFull() exists. (See StackLinkedList for unbounded.)
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Function call stack, recursion
 *   - Undo/redo, browser back button
 *   - Expression evaluation (postfix), bracket matching, syntax parsing
 *   - DFS traversal, backtracking (maze, sudoku)
 *
 *  TIME COMPLEXITY  (all O(1) except delete-all in some languages)
 *  ---------------
 *   | create O(1)* | push O(1) | pop O(1) | peek O(1) | isEmpty O(1) |
 *   | isFull O(1)  | delete O(1) |
 *   * allocating an array of capacity n is O(n) in the JVM (zero fill)
 *   Space: O(n) for capacity n
 *
 *  HOW TO RUN
 *  ----------
 *   javac StackArray.java && java StackArray
 * ============================================================================
 */

public class StackArray {

    static class ArrayStack<T> {
        private Object[] arr;
        private int top = -1;           // -1 means empty

        ArrayStack(int capacity) { arr = new Object[capacity]; }

        boolean isEmpty() { return top == -1; }
        boolean isFull()  { return top == arr.length - 1; }
        int size()        { return top + 1; }

        void push(T v) {
            if (isFull()) throw new IllegalStateException("Stack overflow (capacity " + arr.length + ")");
            arr[++top] = v;             // pre-increment: move top up, then store
        }

        @SuppressWarnings("unchecked")
        T pop() {
            if (isEmpty()) throw new IllegalStateException("Stack underflow");
            T v = (T) arr[top];
            arr[top--] = null;          // clear slot (avoid loitering references)
            return v;
        }

        @SuppressWarnings("unchecked")
        T peek() {
            if (isEmpty()) throw new IllegalStateException("Stack is empty");
            return (T) arr[top];
        }

        /** Delete the whole stack. */
        void deleteStack() { arr = new Object[arr.length]; top = -1; }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder("[bottom] ");
            for (int i = 0; i <= top; i++) sb.append(arr[i]).append(' ');
            return sb.append("[top]").toString();
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 1: Code editor bracket validator  "{ [ ( ) ] }"
    // ------------------------------------------------------------------------
    static String validateBrackets(String code) {
        ArrayStack<int[]> stack = new ArrayStack<>(code.length());   // store {char, index}
        String open = "([{", close = ")]}";
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (open.indexOf(c) >= 0) {
                stack.push(new int[]{c, i});
            } else if (close.indexOf(c) >= 0) {
                if (stack.isEmpty()) return "unexpected '" + c + "' at col " + i;
                int[] top = stack.pop();
                // matching pair has the same index in 'open' and 'close'
                if (open.indexOf((char) top[0]) != close.indexOf(c))
                    return "'" + (char) top[0] + "' at col " + top[1] + " closed by '" + c + "' at col " + i;
            }
        }
        return stack.isEmpty() ? "VALID" : "unclosed '" + (char) stack.peek()[0] + "' at col " + stack.peek()[1];
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 2: Postfix (Reverse Polish) calculator  "5 1 2 + 4 * + 3 -"
    // ------------------------------------------------------------------------
    static double evaluatePostfix(String expr) {
        String[] tokens = expr.trim().split("\\s+");
        ArrayStack<Double> st = new ArrayStack<>(tokens.length);
        for (String t : tokens) {
            if (t.matches("-?\\d+(\\.\\d+)?")) {
                st.push(Double.parseDouble(t));
            } else {
                double b = st.pop(), a = st.pop();      // NOTE order: b is popped first
                switch (t) {
                    case "+": st.push(a + b); break;
                    case "-": st.push(a - b); break;
                    case "*": st.push(a * b); break;
                    case "/": st.push(a / b); break;
                    default: throw new IllegalArgumentException("bad token " + t);
                }
            }
        }
        return st.pop();
    }

    public static void main(String[] args) {
        System.out.println("=== ARRAY STACK OPERATIONS ===");
        ArrayStack<Integer> s = new ArrayStack<>(4);
        System.out.println("  isEmpty: " + s.isEmpty());
        for (int i = 1; i <= 4; i++) { s.push(i * 10); System.out.println("  push " + i * 10 + " -> " + s); }
        System.out.println("  isFull : " + s.isFull());
        try { s.push(50); } catch (IllegalStateException e) { System.out.println("  push 50 -> " + e.getMessage()); }
        System.out.println("  peek   : " + s.peek());
        System.out.println("  pop    : " + s.pop() + " -> " + s);
        System.out.println("  pop    : " + s.pop() + " -> " + s);
        s.deleteStack();
        System.out.println("  after deleteStack isEmpty=" + s.isEmpty());
        try { s.pop(); } catch (IllegalStateException e) { System.out.println("  pop on empty -> " + e.getMessage()); }

        System.out.println("\n=== SCENARIO 1: Bracket Validator ===");
        String[] samples = {"int f() { return a[(b+c)]; }", "if (x > 0 { y = [1, 2); }", "while (true) {", "a = b)"};
        for (String code : samples) System.out.printf("  %-32s => %s%n", code, validateBrackets(code));

        System.out.println("\n=== SCENARIO 2: Postfix Calculator ===");
        String[] exprs = {"5 1 2 + 4 * + 3 -", "2 3 4 * +", "100 5 / 3 -"};
        for (String e : exprs) System.out.printf("  %-20s = %.2f%n", e, evaluatePostfix(e));
    }
}
