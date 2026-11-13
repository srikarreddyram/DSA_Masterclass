/*
 * ============================================================================
 *  MODULE 04 : BINARY HEAP (array-based complete binary tree)
 * ============================================================================
 *
 *  WHAT IS A BINARY HEAP?
 *  ----------------------
 *  A COMPLETE binary tree that satisfies the HEAP PROPERTY:
 *    Min-Heap: every parent <= its children  -> smallest at the root
 *    Max-Heap: every parent >= its children  -> largest at the root
 *  Siblings are NOT ordered (so a heap is not sorted - it's "partially ordered").
 *
 *  Because it is always complete, it is stored in an ARRAY with no gaps
 *  (0-based here):  left = 2i+1, right = 2i+2, parent = (i-1)/2
 *
 *           5                 arr: [5, 10, 8, 20, 15, 9]
 *         /   \
 *       10     8
 *      /  \   /
 *    20   15 9
 *
 *  CORE OPERATIONS
 *  ---------------
 *   insert      : put at the end, then SIFT UP (swap with parent while smaller)
 *   extract     : take root, move LAST element to root, then SIFT DOWN
 *                 (swap with the smaller child while larger)
 *   buildHeap   : sift down every non-leaf from n/2-1 down to 0 -> O(n)!
 *                 (most nodes are near the bottom and sift only a little)
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Priority queues: OS schedulers, ER triage, Dijkstra / Prim (Module 05)
 *   - Top-K problems ("10 most viewed videos"), streaming median
 *   - Heap sort, merging K sorted lists
 *   - java.util.PriorityQueue is a binary min-heap
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   | create O(1)   | peek O(1)       | size O(1)                     |
 *   | insert O(log n) | extract O(log n) | buildHeap O(n)            |
 *   | heap sort O(n log n), in place, not stable | delete heap O(1)   |
 *   Space: O(n)
 *
 *  HOW TO RUN
 *  ----------
 *   javac BinaryHeap.java && java BinaryHeap
 * ============================================================================
 */

import java.util.Arrays;
import java.util.Comparator;

public class BinaryHeap {

    /**
     * Generic heap. The Comparator decides min vs max:
     *   Comparator.naturalOrder()  -> min-heap
     *   Comparator.reverseOrder()  -> max-heap
     */
    static class Heap<T> {
        private Object[] arr;
        private int size;
        private final Comparator<? super T> cmp;

        Heap(int capacity, Comparator<? super T> cmp) {
            this.arr = new Object[Math.max(1, capacity)];
            this.cmp = cmp;
        }

        int size() { return size; }
        boolean isEmpty() { return size == 0; }

        @SuppressWarnings("unchecked") private T at(int i) { return (T) arr[i]; }
        private boolean higherPriority(int i, int j) { return cmp.compare(at(i), at(j)) < 0; }
        private void swap(int i, int j) { Object t = arr[i]; arr[i] = arr[j]; arr[j] = t; }

        T peek() {
            if (isEmpty()) throw new IllegalStateException("heap is empty");
            return at(0);
        }

        void insert(T v) {
            if (size == arr.length) arr = Arrays.copyOf(arr, arr.length * 2);   // grow like ArrayList
            arr[size] = v;
            siftUp(size++);
        }

        /** Bubble element at i up while it beats its parent. O(log n). */
        private void siftUp(int i) {
            while (i > 0) {
                int parent = (i - 1) / 2;
                if (!higherPriority(i, parent)) break;    // heap property holds
                swap(i, parent);
                i = parent;
            }
        }

        T extract() {
            if (isEmpty()) throw new IllegalStateException("heap is empty");
            T top = at(0);
            arr[0] = arr[--size];          // move last leaf to the root
            arr[size] = null;
            siftDown(0, size);
            return top;
        }

        /** Push element at i down, always swapping with the BETTER child. O(log n). */
        private void siftDown(int i, int n) {
            while (true) {
                int left = 2 * i + 1, right = left + 1, best = i;
                if (left < n && higherPriority(left, best)) best = left;
                if (right < n && higherPriority(right, best)) best = right;
                if (best == i) return;     // both children are worse -> done
                swap(i, best);
                i = best;
            }
        }

        /** Floyd's bottom-up heap construction - O(n), not O(n log n). */
        static <T> Heap<T> buildHeap(T[] items, Comparator<? super T> cmp) {
            Heap<T> h = new Heap<>(items.length, cmp);
            System.arraycopy(items, 0, h.arr, 0, items.length);
            h.size = items.length;
            for (int i = items.length / 2 - 1; i >= 0; i--) h.siftDown(i, h.size);  // skip leaves
            return h;
        }

        /**
         * In-place heap sort: repeatedly swap root to the end and shrink the heap.
         * With a MAX-heap comparator this produces ascending order.
         */
        Object[] heapSort() {
            Object[] copy = Arrays.copyOf(arr, size);
            Heap<T> h = new Heap<>(size, cmp);
            h.arr = copy; h.size = size;
            for (int end = size - 1; end > 0; end--) {
                h.swap(0, end);            // best element goes to its final slot
                h.siftDown(0, end);        // restore heap in the shrunken prefix
            }
            return copy;
        }

        void deleteHeap() { arr = new Object[arr.length]; size = 0; }

        @Override public String toString() { return Arrays.toString(Arrays.copyOf(arr, size)); }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 1: Hospital Emergency Room triage (priority queue)
    //  Lower severity number = more critical. Ties broken by arrival order.
    // ------------------------------------------------------------------------
    static class Patient {
        final String name, complaint; final int severity, arrival;
        Patient(String n, String c, int s, int a) { name = n; complaint = c; severity = s; arrival = a; }
        @Override public String toString() { return name; }
    }

    static void emergencyRoomScenario() {
        System.out.println("\n=== SCENARIO 1: ER Triage Priority Queue ===");
        Comparator<Patient> triage = Comparator.<Patient>comparingInt(p -> p.severity)
                .thenComparingInt(p -> p.arrival);           // FIFO among equal severity
        Heap<Patient> er = new Heap<>(4, triage);
        Object[][] arrivals = {
                {"Ravi", "sprained ankle", 4}, {"Meera", "chest pain", 1}, {"Arjun", "high fever", 3},
                {"Kavya", "fracture", 2}, {"Sam", "minor cut", 5}, {"Priya", "breathing difficulty", 1}};
        int t = 0;
        for (Object[] a : arrivals) {
            er.insert(new Patient((String) a[0], (String) a[1], (Integer) a[2], t++));
            System.out.printf("  arrives: %-6s severity %d  (%s)%n", a[0], a[2], a[1]);
        }
        System.out.println("  --- doctor treats in priority order ---");
        while (!er.isEmpty()) {
            Patient p = er.extract();
            System.out.printf("  treating %-6s [sev %d] %s%n", p.name, p.severity, p.complaint);
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 2: Top-K trending hashtags using a size-K MIN-heap
    //  Keep the K largest seen so far; the root is the weakest of them.
    //  O(n log k) time, O(k) space - works on an endless stream.
    // ------------------------------------------------------------------------
    static void topKScenario() {
        System.out.println("\n=== SCENARIO 2: Top-3 Trending Hashtags (streaming) ===");
        String[] tags = {"#java", "#python", "#dsa", "#ai", "#cricket", "#leetcode", "#cloud"};
        int[] mentions = {5400, 8900, 3100, 12000, 15000, 2800, 7600};
        int k = 3;
        Heap<int[]> minHeap = new Heap<>(k, Comparator.comparingInt(x -> x[1]));   // {tagIndex, count}
        for (int i = 0; i < tags.length; i++) {
            if (minHeap.size() < k) minHeap.insert(new int[]{i, mentions[i]});
            else if (mentions[i] > minHeap.peek()[1]) {    // beats the weakest of the top-K
                minHeap.extract();
                minHeap.insert(new int[]{i, mentions[i]});
            }
        }
        String[] result = new String[k];
        for (int i = k - 1; i >= 0; i--) {                 // extract ascending, fill from back
            int[] e = minHeap.extract();
            result[i] = tags[e[0]] + "(" + e[1] + ")";
        }
        System.out.println("  top " + k + ": " + String.join(", ", result));
    }

    public static void main(String[] args) {
        System.out.println("=== MIN-HEAP OPERATIONS ===");
        Heap<Integer> min = new Heap<>(4, Comparator.naturalOrder());
        for (int v : new int[]{20, 15, 30, 5, 10, 8, 25}) {
            min.insert(v);
            System.out.println("  insert " + v + " -> " + min);
        }
        System.out.println("  peek = " + min.peek() + ", size = " + min.size());
        System.out.print("  extract all: ");
        while (!min.isEmpty()) System.out.print(min.extract() + " ");
        System.out.println();

        System.out.println("\n=== MAX-HEAP via buildHeap (O(n)) + HEAP SORT ===");
        Integer[] data = {4, 10, 3, 5, 1, 15, 7, 9};
        Heap<Integer> max = Heap.buildHeap(data, Comparator.reverseOrder());
        System.out.println("  input     : " + Arrays.toString(data));
        System.out.println("  max-heap  : " + max);
        System.out.println("  heap sort : " + Arrays.toString(max.heapSort()));
        max.deleteHeap();
        System.out.println("  after deleteHeap size=" + max.size());

        emergencyRoomScenario();
        topKScenario();
    }
}
