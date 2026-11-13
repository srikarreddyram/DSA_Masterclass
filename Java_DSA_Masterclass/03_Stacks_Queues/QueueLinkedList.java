/*
 * ============================================================================
 *  MODULE 03 : QUEUE (Linked-List / Node-based implementation)
 * ============================================================================
 *
 *  HOW IT MAPS ONTO A LINKED LIST
 *  ------------------------------
 *  front = head, rear = tail of a singly linked list.
 *    enqueue -> append at tail   O(1)  (we keep a tail reference)
 *    dequeue -> remove head      O(1)
 *  Why not dequeue from the tail? Removing a singly-linked tail is O(n),
 *  so we always remove from the head and add at the tail.
 *
 *     front                       rear
 *       |                          |
 *     [10|*] -> [20|*] -> [30|*] -> [40|null]
 *
 *  ARRAY vs LINKED-LIST QUEUE
 *  --------------------------
 *   | Aspect     | Circular array            | Linked list              |
 *   |------------|---------------------------|--------------------------|
 *   | Capacity   | fixed (bounded buffer)    | unbounded                |
 *   | Memory     | preallocated, compact     | per-node overhead        |
 *   | Use when   | max size known, hot loop  | size unpredictable       |
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Task/job queues, message brokers, BFS traversal
 *   - Customer support ticketing, ride-hailing request matching
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   create O(1) | enqueue O(1) | dequeue O(1) | peek O(1) | isEmpty O(1)
 *   delete O(1) | Space O(n)
 *
 *  HOW TO RUN
 *  ----------
 *   javac QueueLinkedList.java && java QueueLinkedList
 * ============================================================================
 */

public class QueueLinkedList {

    static class Node<T> {
        T value; Node<T> next;
        Node(T v) { value = v; }
    }

    static class LinkedQueue<T> {
        private Node<T> front, rear;
        private int size;

        boolean isEmpty() { return front == null; }
        int size() { return size; }

        void enqueue(T v) {
            Node<T> n = new Node<>(v);
            if (rear == null) front = rear = n;   // first node is both ends
            else { rear.next = n; rear = n; }
            size++;
        }

        T dequeue() {
            if (isEmpty()) throw new IllegalStateException("Queue underflow");
            T v = front.value;
            front = front.next;
            if (front == null) rear = null;      // IMPORTANT: queue became empty
            size--;
            return v;
        }

        T peek() {
            if (isEmpty()) throw new IllegalStateException("Queue is empty");
            return front.value;
        }

        void deleteQueue() { front = rear = null; size = 0; }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder("front-> ");
            for (Node<T> c = front; c != null; c = c.next) sb.append(c.value).append(' ');
            return sb.append("<-rear").toString();
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Customer support ticket system with SLA-aware simulation
    // ------------------------------------------------------------------------
    static class Ticket {
        final int id; final String issue; final int arrivedAt;
        Ticket(int id, String issue, int t) { this.id = id; this.issue = issue; this.arrivedAt = t; }
        @Override public String toString() { return "#" + id; }
    }

    static void supportScenario() {
        System.out.println("\n=== SCENARIO: Customer Support Ticket Queue (2 agents) ===");
        LinkedQueue<Ticket> queue = new LinkedQueue<>();
        String[] issues = {"Login failed", "Refund status", "App crash", "Change address",
                "Payment declined", "Order missing", "OTP not received"};
        // tickets arrive one per minute; 2 agents each resolve a ticket every 3 minutes
        int nextId = 1001, served = 0, totalWait = 0;
        for (int minute = 0; minute <= 12; minute++) {
            if (minute < issues.length) {
                queue.enqueue(new Ticket(nextId, issues[nextId - 1001], minute));
                nextId++;
            }
            if (minute % 3 == 0 && minute > 0) {
                for (int agent = 1; agent <= 2 && !queue.isEmpty(); agent++) {
                    Ticket t = queue.dequeue();
                    int wait = minute - t.arrivedAt;
                    totalWait += wait; served++;
                    System.out.printf("  min %2d: agent %d resolves %s %-18s (waited %d min)%n",
                            minute, agent, t, "'" + t.issue + "'", wait);
                }
            }
        }
        System.out.printf("  served %d tickets, average wait %.1f min, still waiting: %s%n",
                served, (double) totalWait / served, queue);
    }

    public static void main(String[] args) {
        System.out.println("=== LINKED-LIST QUEUE OPERATIONS ===");
        LinkedQueue<String> q = new LinkedQueue<>();
        q.enqueue("A"); q.enqueue("B"); q.enqueue("C");
        System.out.println("  " + q + "  size=" + q.size());
        System.out.println("  peek    = " + q.peek());
        System.out.println("  dequeue = " + q.dequeue() + " -> " + q);
        System.out.println("  dequeue = " + q.dequeue() + " -> " + q);
        System.out.println("  dequeue = " + q.dequeue() + " -> " + q + " isEmpty=" + q.isEmpty());
        q.enqueue("D");     // proves rear was reset correctly
        System.out.println("  enqueue D after empty -> " + q);
        q.deleteQueue();
        System.out.println("  after deleteQueue isEmpty=" + q.isEmpty());

        supportScenario();
    }
}
