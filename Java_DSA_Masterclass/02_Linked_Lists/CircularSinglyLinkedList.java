/*
 * ============================================================================
 *  MODULE 02 : CIRCULAR SINGLY LINKED LIST (CSLL)
 * ============================================================================
 *
 *  WHAT IS IT?
 *  -----------
 *  A singly linked list where the LAST node points back to the FIRST node
 *  instead of null. There is no "end" - you can loop forever, so traversal
 *  must stop when we arrive back at head.
 *
 *        +------------------------------------+
 *        v                                    |
 *      [10|*] --> [20|*] --> [30|*] --> [40|*]-+
 *       head                             tail
 *
 *  Invariant we maintain:  tail.next == head  (always!)
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Round-robin CPU scheduling (each process gets a time slice in turn)
 *   - Multiplayer turn order (board/card games)
 *   - Josephus problem, circular buffers, repeating slideshows
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   | Operation              | Time |
 *   |------------------------|------|
 *   | Insert at head / tail  | O(1) (we keep a tail reference) |
 *   | Insert at position k   | O(n) |
 *   | Delete head            | O(1) |
 *   | Delete tail            | O(n) (need tail's predecessor)  |
 *   | Search / traverse      | O(n) |
 *   Space: O(n)
 *
 *  HOW TO RUN
 *  ----------
 *   javac CircularSinglyLinkedList.java && java CircularSinglyLinkedList
 * ============================================================================
 */

public class CircularSinglyLinkedList {

    static class Node<T> {
        T value;
        Node<T> next;
        Node(T v) { value = v; }
    }

    static class CSLL<T> {
        Node<T> head, tail;
        int size;

        void insert(T v, int location) {
            Node<T> n = new Node<>(v);
            if (head == null) {
                head = tail = n;
                n.next = n;                     // single node points to itself
            } else if (location <= 0) {         // new head
                n.next = head;
                head = n;
                tail.next = head;               // keep the circle closed
            } else if (location >= size) {      // new tail
                n.next = head;
                tail.next = n;
                tail = n;
            } else {
                Node<T> prev = head;
                for (int i = 0; i < location - 1; i++) prev = prev.next;
                n.next = prev.next;
                prev.next = n;
            }
            size++;
        }

        void addLast(T v) { insert(v, size); }

        /** do-while: visit head first, stop when we wrap back to head. */
        void traverse() {
            if (head == null) { System.out.println("  (empty)"); return; }
            StringBuilder sb = new StringBuilder("  ");
            Node<T> cur = head;
            do {
                sb.append(cur.value).append(" -> ");
                cur = cur.next;
            } while (cur != head);
            System.out.println(sb.append("(back to ").append(head.value).append(")"));
        }

        int search(T v) {
            if (head == null) return -1;
            Node<T> cur = head;
            int i = 0;
            do {
                if (cur.value.equals(v)) return i;
                cur = cur.next;
                i++;
            } while (cur != head);
            return -1;
        }

        T delete(int location) {
            if (head == null) return null;
            T removed;
            if (size == 1) {                    // last node standing
                removed = head.value;
                head.next = null;               // break self-loop for GC
                head = tail = null;
            } else if (location <= 0) {
                removed = head.value;
                head = head.next;
                tail.next = head;
            } else {
                if (location >= size) location = size - 1;
                Node<T> prev = head;
                for (int i = 0; i < location - 1; i++) prev = prev.next;
                removed = prev.next.value;
                if (prev.next == tail) tail = prev;
                prev.next = prev.next.next;
            }
            size--;
            return removed;
        }

        /**
         * Deleting a circular list: because tail.next -> head, the nodes keep
         * referencing each other. Java's GC handles cycles, but breaking the
         * loop first is the "textbook" step (and is required in C/C++).
         */
        void deleteAll() {
            if (tail != null) tail.next = null;
            head = tail = null;
            size = 0;
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 1: Round-robin CPU scheduler (time quantum = 3 units)
    // ------------------------------------------------------------------------
    static class Process {
        final String name; int remaining;
        Process(String name, int burst) { this.name = name; this.remaining = burst; }
        @Override public String toString() { return name + "(" + remaining + ")"; }
    }

    static void roundRobinScenario() {
        System.out.println("\n=== SCENARIO 1: Round-Robin CPU Scheduler (quantum = 3) ===");
        CSLL<Process> ready = new CSLL<>();
        ready.addLast(new Process("Chrome", 5));
        ready.addLast(new Process("VSCode", 3));
        ready.addLast(new Process("Spotify", 7));
        ready.addLast(new Process("Terminal", 2));
        System.out.print("  ready queue:");
        ready.traverse();

        final int quantum = 3;
        int clock = 0;
        Node<Process> prev = ready.tail;          // track predecessor for O(1) removal
        Node<Process> cur = ready.head;
        while (ready.size > 0) {
            Process p = cur.value;
            int run = Math.min(quantum, p.remaining);
            p.remaining -= run;
            clock += run;
            if (p.remaining == 0) {
                System.out.printf("  t=%2d  %-8s ran %d -> FINISHED%n", clock, p.name, run);
                // unlink cur in O(1) using prev
                if (ready.size == 1) { ready.deleteAll(); break; }
                prev.next = cur.next;
                if (cur == ready.head) ready.head = cur.next;
                if (cur == ready.tail) ready.tail = prev;
                ready.size--;
                cur = prev.next;
            } else {
                System.out.printf("  t=%2d  %-8s ran %d -> %d left%n", clock, p.name, run, p.remaining);
                prev = cur;
                cur = cur.next;                    // circle continues naturally
            }
        }
        System.out.println("  all processes completed at t=" + clock);
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 2: Josephus problem - every k-th person is eliminated
    // ------------------------------------------------------------------------
    static void josephusScenario() {
        System.out.println("\n=== SCENARIO 2: Josephus Problem (n=7, k=3) ===");
        CSLL<Integer> circle = new CSLL<>();
        for (int i = 1; i <= 7; i++) circle.addLast(i);
        Node<Integer> prev = circle.tail;
        StringBuilder order = new StringBuilder("  elimination order: ");
        while (circle.size > 1) {
            for (int i = 0; i < 2; i++) prev = prev.next;    // move k-1 steps
            Node<Integer> victim = prev.next;
            order.append(victim.value).append(" ");
            prev.next = victim.next;
            if (victim == circle.head) circle.head = victim.next;
            if (victim == circle.tail) circle.tail = prev;
            circle.size--;
        }
        System.out.println(order);
        System.out.println("  survivor: person " + circle.head.value);
    }

    public static void main(String[] args) {
        System.out.println("=== CIRCULAR SINGLY LINKED LIST OPERATIONS ===");
        CSLL<Integer> list = new CSLL<>();
        list.insert(10, 0);
        list.insert(20, 1);
        list.insert(40, 2);
        list.insert(5, 0);
        list.insert(30, 3);
        list.traverse();
        System.out.println("  tail.next == head ? " + (list.tail.next == list.head));
        System.out.println("  search(30) = " + list.search(30));
        System.out.println("  delete(0)  = " + list.delete(0));
        System.out.println("  delete(99) = " + list.delete(99) + "  (clamped to tail)");
        list.traverse();
        System.out.println("  tail.next == head ? " + (list.tail.next == list.head));
        list.deleteAll();
        list.traverse();

        roundRobinScenario();
        josephusScenario();
    }
}
