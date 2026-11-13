/*
 * ============================================================================
 *  MODULE 03 : LINEAR QUEUE (Array-based)
 * ============================================================================
 *
 *  WHAT IS A QUEUE?
 *  ----------------
 *  A FIFO (First-In, First-Out) collection - like a line at a ticket counter.
 *  Items join at the REAR (enqueue) and leave from the FRONT (dequeue).
 *
 *     dequeue <-  [10][20][30][40]  <- enqueue
 *                 front        rear
 *
 *  LINEAR ARRAY IMPLEMENTATION
 *  ---------------------------
 *  Two indices: 'front' (beginning of queue) and 'rear' (last element).
 *    enqueue : arr[++rear] = v
 *    dequeue : return arr[front++]
 *
 *  THE BIG FLAW (why Circular Queue exists)
 *  ----------------------------------------
 *  'front' only moves forward. After a few dequeues the slots at the start are
 *  dead space, so the queue can report FULL even though it holds 1 element:
 *
 *     [ x ][ x ][ x ][40]     front=3, rear=3  -> isFull() == true !!
 *       ^ wasted slots
 *
 *  This file demonstrates the flaw on purpose. See CircularQueueArray.java.
 *  (Variant: when the queue becomes empty we reset front/rear to -1, which
 *   reclaims space only in that special case.)
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   create O(1)* | enqueue O(1) | dequeue O(1) | peek O(1) | isEmpty O(1)
 *   isFull O(1)  | delete O(1)  | Space O(n)
 *
 *  HOW TO RUN
 *  ----------
 *   javac LinearQueueArray.java && java LinearQueueArray
 * ============================================================================
 */

public class LinearQueueArray {

    static class LinearQueue {
        private final int[] arr;
        private int front = -1, rear = -1;

        LinearQueue(int capacity) { arr = new int[capacity]; }

        boolean isEmpty() { return front == -1 || front > rear; }
        boolean isFull()  { return rear == arr.length - 1; }   // the flawed check

        void enqueue(int v) {
            if (isFull()) {
                System.out.println("  enqueue(" + v + ") FAILED - queue is full (rear at end)");
                return;
            }
            if (front == -1) front = 0;     // first ever element
            arr[++rear] = v;
            System.out.println("  enqueue(" + v + ") -> " + this);
        }

        int dequeue() {
            if (isEmpty()) throw new IllegalStateException("Queue underflow");
            int v = arr[front++];
            if (front > rear) front = rear = -1;  // became empty -> reset indices
            return v;
        }

        int peek() {
            if (isEmpty()) throw new IllegalStateException("Queue is empty");
            return arr[front];
        }

        void deleteQueue() { front = rear = -1; }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                boolean live = !isEmpty() && i >= front && i <= rear;
                sb.append(live ? String.format("%3d", arr[i]) : "  .");
                if (i < arr.length - 1) sb.append('|');
            }
            return sb.append("]  front=").append(front).append(" rear=").append(rear).toString();
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Bank token counter - customers served strictly in order
    // ------------------------------------------------------------------------
    static void bankScenario() {
        System.out.println("\n=== SCENARIO: Bank Token Counter ===");
        LinearQueue tokens = new LinearQueue(6);
        for (int t = 101; t <= 104; t++) tokens.enqueue(t);
        System.out.println("  Serving token " + tokens.dequeue());
        System.out.println("  Serving token " + tokens.dequeue());
        System.out.println("  Next in line : " + tokens.peek());
        tokens.enqueue(105);
        tokens.enqueue(106);
        System.out.println("  Two slots are free at the front, but...");
        tokens.enqueue(107);          // fails! demonstrates the linear-queue flaw
        System.out.println("  => wasted space: use a CIRCULAR queue to reuse freed slots.");
    }

    public static void main(String[] args) {
        System.out.println("=== LINEAR QUEUE OPERATIONS ===");
        LinearQueue q = new LinearQueue(4);
        System.out.println("  isEmpty: " + q.isEmpty());
        q.enqueue(10); q.enqueue(20); q.enqueue(30);
        System.out.println("  peek   : " + q.peek());
        System.out.println("  dequeue: " + q.dequeue() + " -> " + q);
        q.enqueue(40);
        System.out.println("  isFull : " + q.isFull());
        q.deleteQueue();
        System.out.println("  after deleteQueue -> " + q + " isEmpty=" + q.isEmpty());
        bankScenario();
    }
}
