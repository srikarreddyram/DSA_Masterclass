/*
 * ============================================================================
 *  MODULE 02 : SINGLY LINKED LIST
 * ============================================================================
 *
 *  WHAT IS IT?
 *  -----------
 *  A chain of NODES scattered anywhere in heap memory. Each node stores a
 *  value and a reference ("pointer") to the NEXT node. The list keeps a
 *  reference to the HEAD (first) and, for O(1) appends, the TAIL (last).
 *
 *     head                                   tail
 *      |                                      |
 *     [10|*]-->[20|*]-->[30|*]-->[40|null]<---+
 *
 *  LINKED LIST vs ARRAY
 *  --------------------
 *   + No resizing, no shifting: insert/delete at a known node is O(1)
 *   + Memory is allocated per node, so it grows one element at a time
 *   - No random access: reaching index i requires walking i links -> O(n)
 *   - Extra memory per node for the reference; poor CPU-cache locality
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Undo history, task queues, adjacency lists in graphs
 *   - Hash table buckets (separate chaining), LRU caches (with a hashmap)
 *   - Polynomial arithmetic, big-integer digits
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   | Operation                    | Time |
 *   |------------------------------|------|
 *   | Insert at head               | O(1) |
 *   | Insert at tail (with tail)   | O(1) |
 *   | Insert at position k         | O(k) -> O(n) worst |
 *   | Delete head                  | O(1) |
 *   | Delete tail                  | O(n) (must find the node BEFORE tail) |
 *   | Delete at position k         | O(n) |
 *   | Search / access by index     | O(n) |
 *   | Reverse                      | O(n) time, O(1) extra space |
 *   Space: O(n)
 *
 *  HOW TO RUN
 *  ----------
 *   javac SinglyLinkedList.java && java SinglyLinkedList
 * ============================================================================
 */

public class SinglyLinkedList {

    static class Node<T> {
        T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    static class SLL<T> {
        Node<T> head, tail;
        int size;

        /** Insert at any position. location 0 = head, location >= size = tail. */
        void insert(T value, int location) {
            Node<T> node = new Node<>(value);
            if (head == null) {                       // CASE 1: empty list
                head = tail = node;
            } else if (location <= 0) {               // CASE 2: new head
                node.next = head;
                head = node;
            } else if (location >= size) {            // CASE 3: new tail - O(1)
                tail.next = node;
                tail = node;
            } else {                                  // CASE 4: middle
                Node<T> prev = head;
                for (int i = 0; i < location - 1; i++) prev = prev.next;  // stop one BEFORE
                node.next = prev.next;                // 1) new node points to successor
                prev.next = node;                     // 2) predecessor points to new node
            }                                         // (order matters! swap = lost list)
            size++;
        }

        void addFirst(T v) { insert(v, 0); }
        void addLast(T v)  { insert(v, size); }

        void traverse() {
            StringBuilder sb = new StringBuilder("  ");
            for (Node<T> cur = head; cur != null; cur = cur.next) {
                sb.append(cur.value).append(cur.next != null ? " -> " : " -> null");
            }
            if (head == null) sb.append("(empty)");
            System.out.println(sb);
        }

        /** Returns index of first match or -1. O(n). */
        int search(T value) {
            int idx = 0;
            for (Node<T> cur = head; cur != null; cur = cur.next, idx++)
                if (cur.value.equals(value)) return idx;
            return -1;
        }

        T get(int index) {
            if (index < 0 || index >= size) return null;
            Node<T> cur = head;
            for (int i = 0; i < index; i++) cur = cur.next;
            return cur.value;
        }

        /** Delete node at location. Handles head, tail and middle. */
        T delete(int location) {
            if (head == null) return null;
            T removed;
            if (location <= 0) {                       // delete head - O(1)
                removed = head.value;
                head = head.next;
                if (head == null) tail = null;         // list became empty
            } else {
                if (location >= size) location = size - 1;
                Node<T> prev = head;
                for (int i = 0; i < location - 1; i++) prev = prev.next;
                removed = prev.next.value;
                prev.next = prev.next.next;            // bypass the node -> GC frees it
                if (prev.next == null) tail = prev;    // we removed the tail
            }
            size--;
            return removed;
        }

        /** Delete entire list: dropping head/tail makes every node unreachable. */
        void deleteAll() { head = tail = null; size = 0; }

        /**
         * In-place reversal with three pointers - classic interview question.
         *   prev <- cur    next
         * At every step we flip cur.next to point backwards.
         */
        void reverse() {
            Node<T> prev = null, cur = head;
            tail = head;
            while (cur != null) {
                Node<T> next = cur.next;   // save the rest of the list
                cur.next = prev;           // flip the arrow
                prev = cur;                // advance prev
                cur = next;                // advance cur
            }
            head = prev;
        }

        /** Floyd's tortoise & hare: slow moves 1, fast moves 2. */
        T middle() {
            Node<T> slow = head, fast = head;
            while (fast != null && fast.next != null) {
                slow = slow.next;
                fast = fast.next.next;
            }
            return slow == null ? null : slow.value;
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Text editor UNDO history (most recent action at head)
    // ------------------------------------------------------------------------
    static void undoScenario() {
        System.out.println("\n=== SCENARIO: Text Editor Undo History ===");
        StringBuilder document = new StringBuilder();
        SLL<String> history = new SLL<>();          // each node = text that was typed

        String[] typed = {"Hello", ", world", "! This is", " a DSA demo."};
        for (String t : typed) {
            document.append(t);
            history.addFirst(t);                    // newest action goes to head - O(1)
            System.out.println("  typed  '" + t + "' -> doc = \"" + document + "\"");
        }

        for (int i = 0; i < 2; i++) {
            String last = history.delete(0);        // pop newest action - O(1)
            document.setLength(document.length() - last.length());
            System.out.println("  UNDO   '" + last + "' -> doc = \"" + document + "\"");
        }
        System.out.print("  remaining history (newest first):");
        history.traverse();
    }

    public static void main(String[] args) {
        System.out.println("=== SINGLY LINKED LIST OPERATIONS ===");
        SLL<Integer> list = new SLL<>();
        list.addLast(10);
        list.addLast(20);
        list.addLast(40);
        list.addFirst(5);
        list.insert(30, 3);          // middle insert
        list.traverse();
        System.out.println("  size=" + list.size + " head=" + list.head.value + " tail=" + list.tail.value);
        System.out.println("  search(30) = index " + list.search(30));
        System.out.println("  get(2)     = " + list.get(2));
        System.out.println("  middle     = " + list.middle());

        System.out.println("  delete head -> removed " + list.delete(0));
        System.out.println("  delete tail -> removed " + list.delete(list.size - 1));
        list.traverse();
        System.out.println("  tail is now " + list.tail.value);

        list.reverse();
        System.out.print("  reversed:");
        list.traverse();

        list.deleteAll();
        System.out.print("  after deleteAll:");
        list.traverse();

        undoScenario();
    }
}
