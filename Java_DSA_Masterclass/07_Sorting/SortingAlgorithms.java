/*
 * ============================================================================
 *  MODULE 07 : SORTING ALGORITHMS
 *  Bubble, Selection, Insertion, Bucket, Merge, Quick, Heap
 * ============================================================================
 *
 *  TERMINOLOGY
 *  -----------
 *   In-place : uses O(1) (or O(log n) stack) extra memory
 *   Stable   : equal keys keep their original relative order. Matters when you
 *              sort by a SECOND key after a first one (scenario below).
 *   Adaptive : runs faster on nearly-sorted input (insertion, bubble w/ flag)
 *
 *  COMPARISON TABLE
 *  ----------------
 *   | Algorithm | Best       | Average    | Worst      | Space    | Stable | In-place |
 *   |-----------|------------|------------|------------|----------|--------|----------|
 *   | Bubble    | O(n)       | O(n^2)     | O(n^2)     | O(1)     | yes    | yes      |
 *   | Selection | O(n^2)     | O(n^2)     | O(n^2)     | O(1)     | NO     | yes      |
 *   | Insertion | O(n)       | O(n^2)     | O(n^2)     | O(1)     | yes    | yes      |
 *   | Bucket    | O(n+k)     | O(n+k)     | O(n^2)     | O(n+k)   | yes*   | no       |
 *   | Merge     | O(n log n) | O(n log n) | O(n log n) | O(n)     | yes    | no       |
 *   | Quick     | O(n log n) | O(n log n) | O(n^2)     | O(log n) | NO     | yes      |
 *   | Heap      | O(n log n) | O(n log n) | O(n log n) | O(1)     | NO     | yes      |
 *   * if each bucket is sorted with a stable algorithm
 *
 *  WHICH ONE IN PRACTICE?
 *   - Small or nearly sorted arrays : insertion sort
 *   - Need stability / linked lists / external (disk) sorting : merge sort
 *   - General in-memory, fastest on average : quick sort (random pivot)
 *   - Guaranteed O(n log n) with O(1) memory : heap sort
 *   - Uniformly distributed numbers in a known range : bucket sort
 *   Java's Arrays.sort uses Dual-Pivot Quicksort for primitives and TimSort
 *   (merge + insertion, stable) for objects. Python's sorted() is TimSort.
 *
 *  All comparison sorts here are GENERIC: they take T[] and a Comparator, so
 *  the same code sorts ints or Product objects by any field.
 *
 *  HOW TO RUN
 *  ----------
 *   javac SortingAlgorithms.java && java SortingAlgorithms
 * ============================================================================
 */

import java.util.*;

public class SortingAlgorithms {

    static <T> void swap(T[] a, int i, int j) { T t = a[i]; a[i] = a[j]; a[j] = t; }

    // ------------------------------------------------------------------------
    //  BUBBLE SORT - repeatedly swap adjacent out-of-order pairs. After pass p
    //  the largest p elements have "bubbled" to the end. Early exit if a pass
    //  makes no swaps -> O(n) on already-sorted input.
    // ------------------------------------------------------------------------
    static <T> void bubbleSort(T[] a, Comparator<? super T> c) {
        for (int pass = 0; pass < a.length - 1; pass++) {
            boolean swapped = false;
            for (int j = 0; j < a.length - 1 - pass; j++) {      // last 'pass' items are final
                if (c.compare(a[j], a[j + 1]) > 0) { swap(a, j, j + 1); swapped = true; }
            }
            if (!swapped) break;                                  // already sorted
        }
    }

    // ------------------------------------------------------------------------
    //  SELECTION SORT - find the minimum of the unsorted part, swap it to the
    //  front. Exactly n-1 swaps (good when writes are expensive). Unstable:
    //  the long-distance swap can jump an element over its equal twin.
    // ------------------------------------------------------------------------
    static <T> void selectionSort(T[] a, Comparator<? super T> c) {
        for (int i = 0; i < a.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) if (c.compare(a[j], a[min]) < 0) min = j;
            if (min != i) swap(a, i, min);
        }
    }

    // ------------------------------------------------------------------------
    //  INSERTION SORT - grow a sorted prefix; take the next element and shift
    //  larger elements right until its slot is found (like sorting cards).
    // ------------------------------------------------------------------------
    static <T> void insertionSort(T[] a, Comparator<? super T> c) {
        for (int i = 1; i < a.length; i++) {
            T key = a[i];
            int j = i - 1;
            while (j >= 0 && c.compare(a[j], key) > 0) { a[j + 1] = a[j]; j--; }   // shift right
            a[j + 1] = key;
        }
    }

    // ------------------------------------------------------------------------
    //  BUCKET SORT (non-negative ints) - scatter values into k buckets by range,
    //  sort each bucket (insertion sort), then concatenate.
    //    bucket index = value * k / (max + 1)
    // ------------------------------------------------------------------------
    static void bucketSort(int[] a) {
        if (a.length == 0) return;
        int k = (int) Math.ceil(Math.sqrt(a.length));            // number of buckets
        int max = Arrays.stream(a).max().getAsInt();
        List<List<Integer>> buckets = new ArrayList<>();
        for (int i = 0; i < k; i++) buckets.add(new ArrayList<>());
        for (int v : a) buckets.get((int) ((long) v * k / (max + 1))).add(v);
        int idx = 0;
        for (List<Integer> b : buckets) {
            Integer[] arr = b.toArray(new Integer[0]);
            insertionSort(arr, Comparator.naturalOrder());       // buckets are small
            for (int v : arr) a[idx++] = v;
        }
    }

    // ------------------------------------------------------------------------
    //  MERGE SORT - divide in half, sort each half recursively, MERGE the two
    //  sorted halves. T(n) = 2T(n/2) + O(n) = O(n log n). Using "<=" when the
    //  left element ties makes it STABLE.
    // ------------------------------------------------------------------------
    static <T> void mergeSort(T[] a, Comparator<? super T> c) {
        @SuppressWarnings("unchecked") T[] tmp = (T[]) new Object[a.length];
        mergeSort(a, tmp, 0, a.length - 1, c);
    }

    private static <T> void mergeSort(T[] a, T[] tmp, int lo, int hi, Comparator<? super T> c) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;                           // avoids int overflow
        mergeSort(a, tmp, lo, mid, c);
        mergeSort(a, tmp, mid + 1, hi, c);
        if (c.compare(a[mid], a[mid + 1]) <= 0) return;         // halves already in order
        System.arraycopy(a, lo, tmp, lo, hi - lo + 1);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) a[k++] = c.compare(tmp[i], tmp[j]) <= 0 ? tmp[i++] : tmp[j++];  // <= : stable
        while (i <= mid) a[k++] = tmp[i++];                     // leftovers (right side already in place)
        while (j <= hi) a[k++] = tmp[j++];
    }

    // ------------------------------------------------------------------------
    //  QUICK SORT - choose a pivot, PARTITION so smaller items go left and
    //  larger go right, recurse on both sides. A RANDOM pivot makes the O(n^2)
    //  worst case (already-sorted input with a fixed pivot) astronomically
    //  unlikely. Lomuto partition shown below.
    // ------------------------------------------------------------------------
    private static final Random RNG = new Random(42);

    static <T> void quickSort(T[] a, Comparator<? super T> c) { quickSort(a, 0, a.length - 1, c); }

    private static <T> void quickSort(T[] a, int lo, int hi, Comparator<? super T> c) {
        while (lo < hi) {
            int p = partition(a, lo, hi, c);
            // Recurse into the SMALLER side, loop on the larger: stack depth O(log n)
            if (p - lo < hi - p) { quickSort(a, lo, p - 1, c); lo = p + 1; }
            else                 { quickSort(a, p + 1, hi, c); hi = p - 1; }
        }
    }

    private static <T> int partition(T[] a, int lo, int hi, Comparator<? super T> c) {
        swap(a, lo + RNG.nextInt(hi - lo + 1), hi);             // random pivot moved to the end
        T pivot = a[hi];
        int boundary = lo;                                      // a[lo..boundary-1] < pivot
        for (int j = lo; j < hi; j++) {
            if (c.compare(a[j], pivot) < 0) swap(a, boundary++, j);
        }
        swap(a, boundary, hi);                                  // pivot lands in its final slot
        return boundary;
    }

    // ------------------------------------------------------------------------
    //  HEAP SORT - build a MAX-heap in O(n), then repeatedly move the max to
    //  the end and sift the new root down. (Full heap details in Module 04.)
    // ------------------------------------------------------------------------
    static <T> void heapSort(T[] a, Comparator<? super T> c) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) siftDown(a, i, n, c);   // heapify
        for (int end = n - 1; end > 0; end--) {
            swap(a, 0, end);                                    // max to its final place
            siftDown(a, 0, end, c);
        }
    }

    private static <T> void siftDown(T[] a, int i, int n, Comparator<? super T> c) {
        while (true) {
            int l = 2 * i + 1, r = l + 1, big = i;
            if (l < n && c.compare(a[l], a[big]) > 0) big = l;
            if (r < n && c.compare(a[r], a[big]) > 0) big = r;
            if (big == i) return;
            swap(a, i, big);
            i = big;
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: E-commerce product catalogue
    // ------------------------------------------------------------------------
    static class Product {
        final String name; final int price; final double rating; final int reviews;
        Product(String n, int p, double r, int rv) { name = n; price = p; rating = r; reviews = rv; }
        @Override public String toString() { return String.format("%-22s Rs %6d  %.1f*  (%d reviews)", name, price, rating, reviews); }
    }

    static Product[] catalogue() {
        return new Product[]{
                new Product("Boat Rockerz 450", 1499, 4.1, 120_000), new Product("Sony WH-1000XM5", 29990, 4.6, 8_200),
                new Product("JBL Tune 760NC", 5999, 4.3, 21_000), new Product("Noise Buds VS104", 999, 3.9, 64_000),
                new Product("Sennheiser HD 450BT", 9990, 4.3, 5_400), new Product("OnePlus Buds 3", 5499, 4.3, 30_500),
                new Product("Realme Buds Air 5", 3699, 4.1, 41_000), new Product("Apple AirPods Pro 2", 24900, 4.6, 15_000)};
    }

    static void catalogueScenario() {
        System.out.println("\n=== SCENARIO: Sorting an E-commerce Product Catalogue ===");
        Product[] items = catalogue();

        quickSort(items, Comparator.comparingInt(p -> p.price));
        System.out.println("  Price: Low to High (quick sort)");
        for (Product p : items) System.out.println("    " + p);

        // Customer-review ranking: rating desc, ties broken by more reviews
        mergeSort(items, Comparator.<Product>comparingDouble(p -> p.rating).reversed()
                .thenComparing(Comparator.<Product>comparingInt(p -> p.reviews).reversed()));
        System.out.println("  Customer Rating (merge sort, rating desc then #reviews desc)");
        for (Product p : items) System.out.println("    " + p);

        // STABILITY DEMO: first sort by price, then STABLE-sort by rating.
        // Equal-rated products stay in price order -> "best rated, cheapest first".
        Product[] stable = catalogue(), unstable = catalogue();
        insertionSort(stable, Comparator.comparingInt(p -> p.price));
        insertionSort(stable, Comparator.<Product>comparingDouble(p -> p.rating).reversed());
        selectionSort(unstable, Comparator.comparingInt(p -> p.price));
        selectionSort(unstable, Comparator.<Product>comparingDouble(p -> p.rating).reversed());
        System.out.println("  Two-pass sort (price, then rating) - look at the 4.1* products:");
        System.out.println("    stable (insertion) : " + names(stable, 4.1));
        System.out.println("    unstable(selection): " + names(unstable, 4.1) + "  <- price order lost");
    }

    static String names(Product[] arr, double rating) {
        StringJoiner j = new StringJoiner(", ");
        for (Product p : arr) if (p.rating == rating) j.add(p.name + "(" + p.price + ")");
        return j.toString();
    }

    // ------------------------------------------------------------------------
    //  Benchmark helper
    // ------------------------------------------------------------------------
    interface Sorter { void sort(Integer[] a); }

    static void benchmark() {
        System.out.println("\n=== BENCHMARK (n = 5,000 random ints; times are indicative only) ===");
        Random r = new Random(1);
        Integer[] base = new Integer[5_000];
        for (int i = 0; i < base.length; i++) base[i] = r.nextInt(1_000_000);
        Integer[] expected = base.clone();
        Arrays.sort(expected);
        Comparator<Integer> nat = Comparator.naturalOrder();
        Map<String, Sorter> sorters = new LinkedHashMap<>();
        sorters.put("Bubble", a -> bubbleSort(a, nat));
        sorters.put("Selection", a -> selectionSort(a, nat));
        sorters.put("Insertion", a -> insertionSort(a, nat));
        sorters.put("Bucket", a -> { int[] p = Arrays.stream(a).mapToInt(Integer::intValue).toArray(); bucketSort(p); for (int i = 0; i < p.length; i++) a[i] = p[i]; });
        sorters.put("Merge", a -> mergeSort(a, nat));
        sorters.put("Quick", a -> quickSort(a, nat));
        sorters.put("Heap", a -> heapSort(a, nat));
        for (Map.Entry<String, Sorter> e : sorters.entrySet()) {
            Integer[] a = base.clone();
            long t0 = System.nanoTime();
            e.getValue().sort(a);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            System.out.printf("  %-10s %5d ms   correct=%b%n", e.getKey(), ms, Arrays.equals(a, expected));
        }
    }

    public static void main(String[] args) {
        Integer[] data = {64, 25, 12, 22, 11, 90, 25, 3};
        System.out.println("=== SORTING ALGORITHMS ===");
        System.out.println("  input     : " + Arrays.toString(data));
        Comparator<Integer> nat = Comparator.naturalOrder();
        Integer[] a;
        a = data.clone(); bubbleSort(a, nat);    System.out.println("  bubble    : " + Arrays.toString(a));
        a = data.clone(); selectionSort(a, nat); System.out.println("  selection : " + Arrays.toString(a));
        a = data.clone(); insertionSort(a, nat); System.out.println("  insertion : " + Arrays.toString(a));
        int[] b = {64, 25, 12, 22, 11, 90, 25, 3}; bucketSort(b);
        System.out.println("  bucket    : " + Arrays.toString(b));
        a = data.clone(); mergeSort(a, nat);     System.out.println("  merge     : " + Arrays.toString(a));
        a = data.clone(); quickSort(a, nat);     System.out.println("  quick     : " + Arrays.toString(a));
        a = data.clone(); heapSort(a, nat);      System.out.println("  heap      : " + Arrays.toString(a));
        a = data.clone(); mergeSort(a, nat.reversed()); System.out.println("  merge desc: " + Arrays.toString(a));

        catalogueScenario();
        benchmark();
    }
}
