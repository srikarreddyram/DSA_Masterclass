/*
 * ============================================================================
 *  MODULE 08 : SEARCHING ALGORITHMS - Linear Search & Binary Search
 * ============================================================================
 *
 *  LINEAR SEARCH
 *  -------------
 *  Check every element left to right. Works on ANY array (sorted or not),
 *  linked lists, streams.             Best O(1) | Average/Worst O(n) | Space O(1)
 *
 *  BINARY SEARCH  (requires SORTED data)
 *  -------------
 *  Compare the target with the MIDDLE element; discard the half that cannot
 *  contain it. Each step halves the search space:
 *        n -> n/2 -> n/4 -> ... -> 1      => at most floor(log2 n) + 1 steps
 *  1,000,000 items -> ~20 comparisons. 1 billion -> ~30.
 *                                     Best O(1) | Average/Worst O(log n)
 *                                     Space O(1) iterative, O(log n) recursive
 *
 *      lo                mid                hi
 *      [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]      target 23
 *       23 > 16  -> lo = mid + 1
 *                         [23, 38, 56, 72, 91]
 *                          23 < 56 -> hi = mid - 1  ... found at index 5
 *
 *  PITFALLS
 *   - mid = (lo + hi) / 2 can overflow for huge arrays -> use lo + (hi-lo)/2
 *   - Off-by-one: loop while lo <= hi when hi is inclusive
 *   - Duplicates: plain binary search returns ANY match; use lower_bound /
 *     upper_bound variants to get the first / last occurrence.
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Looking up a record in a sorted index (ISBN, roll number, timestamp)
 *   - git bisect (binary search over commits to find a bug)
 *   - "Binary search on the answer" (minimum capacity, maximum speed...)
 *
 *  HOW TO RUN
 *  ----------
 *   javac SearchingAlgorithms.java && java SearchingAlgorithms
 * ============================================================================
 */

import java.util.*;

public class SearchingAlgorithms {

    static long comparisons;            // instrumentation for the scenario

    // ---------------- LINEAR SEARCH ----------------
    static int linearSearch(int[] a, int target) {
        for (int i = 0; i < a.length; i++) {
            comparisons++;
            if (a[i] == target) return i;
        }
        return -1;
    }

    // ---------------- BINARY SEARCH (iterative) ----------------
    static int binarySearch(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;          // overflow-safe midpoint
            comparisons++;
            if (a[mid] == target) return mid;
            if (a[mid] < target) lo = mid + 1;     // discard left half
            else hi = mid - 1;                     // discard right half
        }
        return -1;
    }

    // ---------------- BINARY SEARCH (recursive) ----------------
    static int binarySearchRecursive(int[] a, int target, int lo, int hi) {
        if (lo > hi) return -1;                    // base case: empty range
        int mid = lo + (hi - lo) / 2;
        if (a[mid] == target) return mid;
        return a[mid] < target ? binarySearchRecursive(a, target, mid + 1, hi)
                               : binarySearchRecursive(a, target, lo, mid - 1);
    }

    /** First index with a[i] >= target (insertion point). */
    static int lowerBound(int[] a, int target) {
        int lo = 0, hi = a.length;                 // half-open [lo, hi)
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] < target) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    /** First index with a[i] > target. */
    static int upperBound(int[] a, int target) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] <= target) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    /** Generic binary search over sorted objects by a key. */
    static <T, K extends Comparable<K>> int binarySearchBy(List<T> list, K key, java.util.function.Function<T, K> keyOf) {
        int lo = 0, hi = list.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            comparisons++;
            int c = keyOf.apply(list.get(mid)).compareTo(key);
            if (c == 0) return mid;
            if (c < 0) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Library database sorted by ISBN-13
    // ------------------------------------------------------------------------
    static class Book {
        final long isbn; final String title, author; final int copies;
        Book(long i, String t, String a, int c) { isbn = i; title = t; author = a; copies = c; }
    }

    static void libraryScenario() {
        System.out.println("\n=== SCENARIO: Library ISBN Lookup (sorted database of 200,000 books) ===");
        List<Book> db = new ArrayList<>();
        Random r = new Random(2026);
        Set<Long> used = new HashSet<>();
        while (db.size() < 199_996) {
            long isbn = 978_0_000_00000_0L + (long) (r.nextDouble() * 1_999_999_999L);
            if (used.add(isbn)) db.add(new Book(isbn, "Generated Title #" + db.size(), "Various", 1 + r.nextInt(5)));
        }
        db.add(new Book(9780262046305L, "Introduction to Algorithms (4th ed.)", "Cormen et al.", 3));
        db.add(new Book(9780984782857L, "Cracking the Coding Interview", "G. L. McDowell", 5));
        db.add(new Book(9780134685991L, "Effective Java (3rd ed.)", "Joshua Bloch", 2));
        db.add(new Book(9781492051367L, "Fluent Python (2nd ed.)", "Luciano Ramalho", 1));
        db.sort(Comparator.comparingLong(b -> b.isbn));          // index must be sorted ONCE

        long[] queries = {9780262046305L, 9781492051367L, 9780000000001L};
        for (long q : queries) {
            comparisons = 0;
            int idx = -1;
            for (int i = 0; i < db.size(); i++) { comparisons++; if (db.get(i).isbn == q) { idx = i; break; } }
            long linear = comparisons;

            comparisons = 0;
            int bIdx = binarySearchBy(db, q, b -> b.isbn);
            long binary = comparisons;

            String result = bIdx >= 0 ? "\"" + db.get(bIdx).title + "\" by " + db.get(bIdx).author + ", copies " + db.get(bIdx).copies
                                      : "NOT IN CATALOGUE";
            System.out.printf("  ISBN %d -> %s%n", q, result);
            System.out.printf("      linear search: %,7d comparisons | binary search: %d comparisons (same answer: %b)%n",
                    linear, binary, idx == bIdx);
        }
        System.out.printf("  log2(%,d) = %.1f  -> binary search never needs more than %d probes%n",
                db.size(), Math.log(db.size()) / Math.log(2), (int) (Math.log(db.size()) / Math.log(2)) + 1);
    }

    public static void main(String[] args) {
        System.out.println("=== LINEAR vs BINARY SEARCH ===");
        int[] unsorted = {38, 5, 91, 12, 72, 2, 23, 56, 16, 8};
        System.out.println("  unsorted " + Arrays.toString(unsorted));
        System.out.println("  linearSearch(23) = " + linearSearch(unsorted, 23) + "  (binary search would be WRONG here)");

        int[] sorted = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};
        System.out.println("  sorted   " + Arrays.toString(sorted));
        System.out.println("  binarySearch(23)          = " + binarySearch(sorted, 23));
        System.out.println("  binarySearchRecursive(72) = " + binarySearchRecursive(sorted, 72, 0, sorted.length - 1));
        System.out.println("  binarySearch(40)          = " + binarySearch(sorted, 40) + "  (absent)");
        System.out.println("  insertion point for 40    = " + lowerBound(sorted, 40));

        int[] dup = {1, 3, 3, 3, 3, 7, 9, 9, 12};
        System.out.println("\n=== DUPLICATES: lower / upper bound ===");
        System.out.println("  array " + Arrays.toString(dup));
        int first = lowerBound(dup, 3), last = upperBound(dup, 3) - 1;
        System.out.println("  first 3 at " + first + ", last 3 at " + last + ", count = " + (last - first + 1));
        System.out.println("  count of 9 = " + (upperBound(dup, 9) - lowerBound(dup, 9)) + ", count of 5 = " + (upperBound(dup, 5) - lowerBound(dup, 5)));

        libraryScenario();
    }
}
