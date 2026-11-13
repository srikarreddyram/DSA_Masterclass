/*
 * ============================================================================
 *  MODULE 02 : DOUBLY LINKED LIST
 * ============================================================================
 *
 *  WHAT IS IT?
 *  -----------
 *  Like a singly linked list, but every node ALSO stores a reference to the
 *  PREVIOUS node. That lets us walk in both directions and delete a node in
 *  O(1) when we already hold a reference to it (no need to find its parent).
 *
 *   null <-[prev|10|next]<->[prev|20|next]<->[prev|30|next]-> null
 *            ^ head                             ^ tail
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Browser back/forward navigation
 *   - LRU cache (java.util.LinkedHashMap is a hashmap + doubly linked list)
 *   - java.util.LinkedList, deque implementations
 *   - Text editor cursor movement over lines
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   | Operation                        | Time |
 *   |----------------------------------|------|
 *   | Insert / delete at head or tail  | O(1) |  <- tail delete is now O(1)!
 *   | Insert / delete at position k    | O(n) (walk from the nearer end) |
 *   | Delete a given node reference    | O(1) |
 *   | Search                           | O(n) |
 *   | Reverse traversal                | O(n) |
 *   Space: O(n), ~1 extra reference per node compared to SLL
 *
 *  HOW TO RUN
 *  ----------
 *   javac DoublyLinkedList.java && java DoublyLinkedList
 * ============================================================================
 */

public class DoublyLinkedList {

    static class Node<T> {
        T value;
        Node<T> prev, next;
        Node(T value) { this.value = value; }
    }

    static class DLL<T> {
        Node<T> head, tail;
        int size;

        void addFirst(T v) {
            Node<T> n = new Node<>(v);
            if (head == null) { head = tail = n; }
            else {
                n.next = head;          // new <-> old head
                head.prev = n;
                head = n;
            }
            size++;
        }

        void addLast(T v) {
            Node<T> n = new Node<>(v);
            if (tail == null) { head = tail = n; }
            else {
                n.prev = tail;
                tail.next = n;
                tail = n;
            }
            size++;
        }

        /**
         * Walk to index from whichever end is closer. Halves average cost.
         */
        Node<T> nodeAt(int index) {
            if (index < 0 || index >= size) return null;
            Node<T> cur;
            if (index < size / 2) {
                cur = head;
                for (int i = 0; i < index; i++) cur = cur.next;
            } else {
                cur = tail;
                for (int i = size - 1; i > index; i--) cur = cur.prev;
            }
            return cur;
        }

        void insert(T v, int location) {
            if (location <= 0) { addFirst(v); return; }
            if (location >= size) { addLast(v); return; }
            Node<T> after = nodeAt(location);       // node currently at 'location'
            Node<T> before = after.prev;
            Node<T> n = new Node<>(v);
            // Four links to set:  before <-> n <-> after
            n.prev = before;
            n.next = after;
            before.next = n;
            after.prev = n;
            size++;
        }

        /** O(1) unlink of a node we already hold - the superpower of a DLL. */
        T unlink(Node<T> n) {
            if (n.prev != null) n.prev.next = n.next; else head = n.next;
            if (n.next != null) n.next.prev = n.prev; else tail = n.prev;
            n.prev = n.next = null;     // detach fully
            size--;
            return n.value;
        }

        T delete(int location) {
            Node<T> n = nodeAt(Math.max(0, Math.min(location, size - 1)));
            return n == null ? null : unlink(n);
        }

        int search(T v) {
            int i = 0;
            for (Node<T> cur = head; cur != null; cur = cur.next, i++)
                if (cur.value.equals(v)) return i;
            return -1;
        }

        void traverse() {
            StringBuilder sb = new StringBuilder("  null <- ");
            for (Node<T> c = head; c != null; c = c.next)
                sb.append(c.value).append(c.next != null ? " <-> " : "");
            System.out.println(sb.append(" -> null"));
        }

        void reverseTraverse() {
            StringBuilder sb = new StringBuilder("  (tail->head) ");
            for (Node<T> c = tail; c != null; c = c.prev)
                sb.append(c.value).append(c.prev != null ? " <- " : "");
            System.out.println(sb);
        }

        /**
         * Delete the whole list. In a DLL every node references its neighbours,
         * so we break prev links explicitly (helps GC / mirrors the course).
         */
        void deleteAll() {
            for (Node<T> c = head; c != null; ) {
                Node<T> next = c.next;
                c.prev = c.next = null;
                c = next;
            }
            head = tail = null;
            size = 0;
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Browser history with Back / Forward buttons
    // ------------------------------------------------------------------------
    static class BrowserHistory {
        private final DLL<String> pages = new DLL<>();
        private Node<String> current;

        BrowserHistory(String homepage) {
            pages.addLast(homepage);
            current = pages.tail;
        }

        /** Visiting a new page wipes all "forward" history - just like Chrome. */
        void visit(String url) {
            while (pages.tail != current) pages.unlink(pages.tail);   // O(1) each
            pages.addLast(url);
            current = pages.tail;
            System.out.println("  visit   -> " + url);
        }

        void back(int steps) {
            while (steps-- > 0 && current.prev != null) current = current.prev;
            System.out.println("  back    -> " + current.value);
        }

        void forward(int steps) {
            while (steps-- > 0 && current.next != null) current = current.next;
            System.out.println("  forward -> " + current.value);
        }
    }

    static void browserScenario() {
        System.out.println("\n=== SCENARIO: Browser Back/Forward Navigation ===");
        BrowserHistory b = new BrowserHistory("google.com");
        b.visit("leetcode.com");
        b.visit("leetcode.com/problems/two-sum");
        b.visit("github.com/srikarreddyram");
        b.back(1);
        b.back(1);
        b.forward(1);
        b.visit("stackoverflow.com");      // forward history (github) is discarded
        b.forward(2);                      // nothing ahead - stays put
        b.back(10);                        // clamps at the first page
        System.out.print("  full history:");
        b.pages.traverse();
    }

    public static void main(String[] args) {
        System.out.println("=== DOUBLY LINKED LIST OPERATIONS ===");
        DLL<Integer> dll = new DLL<>();
        dll.addLast(20);
        dll.addLast(30);
        dll.addFirst(10);
        dll.addLast(50);
        dll.insert(40, 3);
        dll.traverse();
        dll.reverseTraverse();
        System.out.println("  search(40)  = " + dll.search(40));
        System.out.println("  delete(0)   = " + dll.delete(0));
        System.out.println("  delete(tail)= " + dll.delete(dll.size - 1) + "   <- O(1) thanks to prev link");
        System.out.println("  delete(1)   = " + dll.delete(1));
        dll.traverse();
        dll.deleteAll();
        System.out.println("  after deleteAll size=" + dll.size + " head=" + dll.head);

        browserScenario();
    }
}
