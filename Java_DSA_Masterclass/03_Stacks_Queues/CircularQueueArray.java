/*
 * ============================================================================
 *  MODULE 03 : CIRCULAR QUEUE (Array-based ring buffer)
 * ============================================================================
 *
 *  THE FIX FOR THE LINEAR QUEUE
 *  ----------------------------
 *  Treat the array as a RING: when an index walks off the end, wrap it to 0
 *  using modulo arithmetic:
 *
 *        next = (i + 1) % capacity
 *
 *         index:  0    1    2    3    4
 *               [ 60 ][ 70 ][    ][ 40 ][ 50 ]
 *                       ^rear        ^front
 *         Dequeue order: 40, 50, 60, 70  (front wraps 4 -> 0)
 *
 *  We track 'size' explicitly. That removes the classic ambiguity where
 *  front == rear could mean either "empty" or "full".
 *  (Alternative textbook trick: leave one slot permanently unused.)
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Keyboard / network / audio buffers (producer-consumer)
 *   - CPU scheduling ready queues
 *   - Streaming "last N readings" sliding windows (sensor smoothing)
 *   - java.util.ArrayDeque is a resizable circular array
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   create O(1)* | enqueue O(1) | dequeue O(1) | peek O(1) | isEmpty O(1)
 *   isFull O(1)  | delete O(1)  | Space O(n)
 *
 *  HOW TO RUN
 *  ----------
 *   javac CircularQueueArray.java && java CircularQueueArray
 * ============================================================================
 */

public class CircularQueueArray {

    static class CircularQueue<T> {
        private final Object[] arr;
        private int front = 0;      // index of the oldest element
        private int rear = -1;      // index of the newest element
        private int size = 0;

        CircularQueue(int capacity) { arr = new Object[capacity]; }

        boolean isEmpty() { return size == 0; }
        boolean isFull()  { return size == arr.length; }
        int size()        { return size; }

        boolean enqueue(T v) {
            if (isFull()) return false;
            rear = (rear + 1) % arr.length;   // wrap around!
            arr[rear] = v;
            size++;
            return true;
        }

        @SuppressWarnings("unchecked")
        T dequeue() {
            if (isEmpty()) throw new IllegalStateException("Queue underflow");
            T v = (T) arr[front];
            arr[front] = null;
            front = (front + 1) % arr.length; // wrap around!
            size--;
            return v;
        }

        @SuppressWarnings("unchecked")
        T peek() {
            if (isEmpty()) throw new IllegalStateException("Queue is empty");
            return (T) arr[front];
        }

        void deleteQueue() {
            for (int i = 0; i < arr.length; i++) arr[i] = null;
            front = 0; rear = -1; size = 0;
        }

        /** Iterate in logical (FIFO) order: i-th element is at (front + i) % cap. */
        @SuppressWarnings("unchecked")
        T get(int i) { return (T) arr[(front + i) % arr.length]; }

        String rawLayout() {
            StringBuilder sb = new StringBuilder("raw[");
            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i] == null ? "  ." : String.format("%3s", arr[i]));
                if (i < arr.length - 1) sb.append('|');
            }
            return sb.append("] front=").append(front).append(" rear=").append(rear).toString();
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 1: Moving average of the last N sensor readings (ring buffer)
    // ------------------------------------------------------------------------
    static void sensorScenario() {
        System.out.println("\n=== SCENARIO 1: Sensor Moving Average (window = 4) ===");
        CircularQueue<Double> window = new CircularQueue<>(4);
        double[] readings = {22.1, 22.4, 23.0, 25.8, 24.9, 23.3, 22.7};
        double runningSum = 0;
        for (double r : readings) {
            if (window.isFull()) runningSum -= window.dequeue();  // evict oldest - O(1)
            window.enqueue(r);
            runningSum += r;
            System.out.printf("  reading %.1f -> avg of last %d = %.2f%n", r, window.size(), runningSum / window.size());
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 2: Printer spooler with a bounded job buffer
    // ------------------------------------------------------------------------
    static void printerScenario() {
        System.out.println("\n=== SCENARIO 2: Printer Spooler (capacity 3) ===");
        CircularQueue<String> spool = new CircularQueue<>(3);
        String[] events = {"+Resume.pdf", "+Report.docx", "+Ticket.png", "+Notes.txt", "print", "+Notes.txt", "print", "print", "print", "print"};
        for (String e : events) {
            if (e.startsWith("+")) {
                String job = e.substring(1);
                System.out.println(spool.enqueue(job)
                        ? "  queued   " + job + "   " + spool.rawLayout()
                        : "  REJECTED " + job + " (spooler full)");
            } else if (spool.isEmpty()) {
                System.out.println("  printer idle - no jobs");
            } else {
                System.out.println("  printing " + spool.dequeue() + "   " + spool.rawLayout());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== CIRCULAR QUEUE OPERATIONS ===");
        CircularQueue<Integer> q = new CircularQueue<>(5);
        for (int i = 1; i <= 5; i++) q.enqueue(i * 10);
        System.out.println("  full?  " + q.isFull() + "  " + q.rawLayout());
        System.out.println("  dequeue " + q.dequeue() + ", " + q.dequeue());
        q.enqueue(60);
        q.enqueue(70);                  // these wrap to index 0 and 1
        System.out.println("  after wrap: " + q.rawLayout());
        System.out.print("  FIFO order: ");
        for (int i = 0; i < q.size(); i++) System.out.print(q.get(i) + " ");
        System.out.println();
        System.out.println("  peek = " + q.peek());
        q.deleteQueue();
        System.out.println("  after deleteQueue: isEmpty=" + q.isEmpty());

        sensorScenario();
        printerScenario();
    }
}
