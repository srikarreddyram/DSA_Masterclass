/*
 * ============================================================================
 *  MODULE 01 : STANDARD ARRAYS (1D and 2D)
 * ============================================================================
 *
 *  WHAT IS AN ARRAY?
 *  -----------------
 *  An array is a FIXED-SIZE, CONTIGUOUS block of memory holding elements of
 *  the SAME type. Because every slot has the same width, the address of any
 *  element can be computed directly:
 *
 *        address(arr[i])       = baseAddress + i * elementSize
 *        address(grid[r][c])   = baseAddress + (r * numCols + c) * elementSize
 *                                (conceptually; in Java a 2D array is an
 *                                 "array of arrays" - each row is its own
 *                                 object on the heap)
 *
 *  That arithmetic is why random access is O(1).
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Lookup tables (days in each month, sensor calibration constants)
 *   - Image pixels / game boards / spreadsheets (2D arrays)
 *   - Buffers for I/O, audio samples, network packets
 *   - The backing store for higher-level structures (ArrayList, Heap, HashMap)
 *
 *  TIME COMPLEXITY (1D array of size n)
 *  ------------------------------------
 *   | Operation                   | Time  | Notes                            |
 *   |-----------------------------|-------|----------------------------------|
 *   | Create                      | O(n)  | JVM zero-fills every slot        |
 *   | Insert at given index       | O(1)  | if slot is empty, no shifting    |
 *   | Insert with shifting        | O(n)  | elements after i move right      |
 *   | Access arr[i]               | O(1)  | address arithmetic               |
 *   | Traverse                    | O(n)  |                                  |
 *   | Linear search               | O(n)  |                                  |
 *   | Delete (mark empty)         | O(1)  |                                  |
 *   | Delete (with shifting)      | O(n)  |                                  |
 *   Space: O(n)
 *
 *  TIME COMPLEXITY (2D array of m rows x n cols)
 *  ---------------------------------------------
 *   Create O(mn) | Insert O(1) | Access O(1) | Traverse O(mn) | Search O(mn)
 *   Delete O(1)  | Space O(mn)
 *
 *  WHEN TO USE / AVOID
 *  -------------------
 *   USE  : size known in advance, need fast index access, same-type data.
 *   AVOID: size changes often (use a dynamic array), frequent middle
 *          insertions/deletions (use a linked list), sparse data (use a map).
 *
 *  HOW TO RUN
 *  ----------
 *   javac StandardArrays.java && java StandardArrays
 * ============================================================================
 */

import java.util.Arrays;

public class StandardArrays {

    // ------------------------------------------------------------------------
    //  1D ARRAY WRAPPER
    // ------------------------------------------------------------------------
    // We use Integer.MIN_VALUE as a sentinel meaning "this cell is empty".
    // A raw int[] cannot hold null, so a sentinel is the classic trick.
    static class SingleDimensionArray {
        private static final int EMPTY = Integer.MIN_VALUE;
        private final int[] arr;

        SingleDimensionArray(int size) {
            arr = new int[size];               // O(n): JVM zero-fills
            Arrays.fill(arr, EMPTY);           // O(n): mark every slot empty
        }

        /** Insert into an EMPTY slot - O(1). No shifting, no overwrite. */
        boolean insert(int index, int value) {
            if (index < 0 || index >= arr.length) {
                System.out.println("  [insert] index " + index + " out of bounds");
                return false;
            }
            if (arr[index] != EMPTY) {
                System.out.println("  [insert] cell " + index + " already occupied");
                return false;
            }
            arr[index] = value;
            return true;
        }

        /**
         * Insert with SHIFTING - O(n).
         * Every element from 'index' to the end moves one place right; the last
         * element "falls off" if the array is full. This is exactly what
         * ArrayList.add(index, value) does internally (via System.arraycopy).
         */
        void insertWithShift(int index, int value) {
            for (int i = arr.length - 1; i > index; i--) {
                arr[i] = arr[i - 1];           // walk backwards so we don't overwrite
            }
            arr[index] = value;
        }

        /** O(1) random access thanks to address arithmetic. */
        Integer get(int index) {
            if (index < 0 || index >= arr.length || arr[index] == EMPTY) return null;
            return arr[index];
        }

        /** O(n) - visit every slot once. */
        void traverse() {
            StringBuilder sb = new StringBuilder("  [");
            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i] == EMPTY ? "_" : String.valueOf(arr[i]));
                if (i < arr.length - 1) sb.append(", ");
            }
            System.out.println(sb.append("]"));
        }

        /** Linear search - O(n). Returns index or -1. */
        int linearSearch(int target) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == target) return i;
            }
            return -1;
        }

        /** Delete by marking empty - O(1). */
        void delete(int index) {
            if (index >= 0 && index < arr.length) arr[index] = EMPTY;
        }

        /** Delete with left shift (keeps data compact) - O(n). */
        void deleteWithShift(int index) {
            for (int i = index; i < arr.length - 1; i++) arr[i] = arr[i + 1];
            arr[arr.length - 1] = EMPTY;
        }
    }

    // ------------------------------------------------------------------------
    //  2D ARRAY WRAPPER
    // ------------------------------------------------------------------------
    static class TwoDimensionArray {
        private static final int EMPTY = Integer.MIN_VALUE;
        private final int[][] grid;

        TwoDimensionArray(int rows, int cols) {
            grid = new int[rows][cols];        // O(mn)
            for (int[] row : grid) Arrays.fill(row, EMPTY);
        }

        boolean insert(int r, int c, int value) {
            if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length) return false;
            if (grid[r][c] != EMPTY) return false;
            grid[r][c] = value;                // O(1)
            return true;
        }

        Integer get(int r, int c) {
            if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length) return null;
            return grid[r][c] == EMPTY ? null : grid[r][c];
        }

        /** Row-major traversal - O(mn). Row-major is cache friendly in Java. */
        void traverse() {
            for (int[] row : grid) {
                StringBuilder sb = new StringBuilder("  ");
                for (int v : row) sb.append(String.format("%4s", v == EMPTY ? "_" : v));
                System.out.println(sb);
            }
        }

        /** O(mn) search; returns {row, col} or null. */
        int[] search(int target) {
            for (int r = 0; r < grid.length; r++)
                for (int c = 0; c < grid[r].length; c++)
                    if (grid[r][c] == target) return new int[]{r, c};
            return null;
        }

        void delete(int r, int c) { grid[r][c] = EMPTY; }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 1: Weekly temperature log (1D) - average, hottest day
    // ------------------------------------------------------------------------
    static void temperatureScenario() {
        System.out.println("\n=== SCENARIO 1: Weekly Temperature Analysis (1D array) ===");
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] temps = {31.5, 33.0, 29.8, 35.2, 34.1, 30.0, 32.7};

        double sum = 0;
        int hottest = 0;
        for (int i = 0; i < temps.length; i++) {      // single O(n) pass
            sum += temps[i];
            if (temps[i] > temps[hottest]) hottest = i;
        }
        double avg = sum / temps.length;
        System.out.printf("  Average temperature : %.2f C%n", avg);
        System.out.printf("  Hottest day         : %s (%.1f C)%n", days[hottest], temps[hottest]);

        System.out.print("  Days above average  : ");
        for (int i = 0; i < temps.length; i++) if (temps[i] > avg) System.out.print(days[i] + " ");
        System.out.println();
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 2: Cinema seat booking (2D) - rows x seats grid
    // ------------------------------------------------------------------------
    static void cinemaScenario() {
        System.out.println("\n=== SCENARIO 2: Cinema Seat Booking (2D array) ===");
        int rows = 4, seatsPerRow = 6;
        char[][] hall = new char[rows][seatsPerRow];
        for (char[] row : hall) Arrays.fill(row, 'O');   // O = open

        int[][] bookings = {{0, 2}, {0, 3}, {2, 0}, {2, 1}, {3, 5}, {0, 2}};
        for (int[] b : bookings) {
            if (hall[b[0]][b[1]] == 'X') {
                System.out.printf("  Seat %c%d is ALREADY booked%n", (char) ('A' + b[0]), b[1] + 1);
            } else {
                hall[b[0]][b[1]] = 'X';                  // O(1) booking
                System.out.printf("  Booked seat %c%d%n", (char) ('A' + b[0]), b[1] + 1);
            }
        }

        System.out.println("  Screen this way --> [==============]");
        for (int r = 0; r < rows; r++) {
            System.out.print("  Row " + (char) ('A' + r) + ": ");
            for (char s : hall[r]) System.out.print(s + " ");
            System.out.println();
        }

        // Find the first row that still has 2 adjacent free seats (couple seating)
        outer:
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < seatsPerRow - 1; c++) {
                if (hall[r][c] == 'O' && hall[r][c + 1] == 'O') {
                    System.out.printf("  First pair of adjacent seats: %c%d-%c%d%n",
                            (char) ('A' + r), c + 1, (char) ('A' + r), c + 2);
                    break outer;
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 1D ARRAY OPERATIONS ===");
        SingleDimensionArray sda = new SingleDimensionArray(8);
        sda.insert(0, 10);
        sda.insert(1, 20);
        sda.insert(2, 30);
        sda.insert(4, 50);
        sda.insert(1, 99);          // occupied -> rejected
        sda.insert(12, 5);          // out of bounds -> rejected
        sda.traverse();

        System.out.println("  get(2)              = " + sda.get(2));
        System.out.println("  linearSearch(50)    = " + sda.linearSearch(50));
        System.out.println("  linearSearch(77)    = " + sda.linearSearch(77));

        sda.insertWithShift(1, 15);
        System.out.println("  after insertWithShift(1, 15):");
        sda.traverse();

        sda.delete(5);
        System.out.println("  after delete(5):");
        sda.traverse();

        sda.deleteWithShift(0);
        System.out.println("  after deleteWithShift(0):");
        sda.traverse();

        System.out.println("\n=== 2D ARRAY OPERATIONS ===");
        TwoDimensionArray tda = new TwoDimensionArray(3, 4);
        int v = 1;
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 4; c++)
                if ((r + c) % 2 == 0) tda.insert(r, c, v++ * 10);
        tda.traverse();
        System.out.println("  get(1,1)   = " + tda.get(1, 1));
        System.out.println("  search(50) = " + Arrays.toString(tda.search(50)));
        tda.delete(0, 0);
        System.out.println("  after delete(0,0):");
        tda.traverse();

        temperatureScenario();
        cinemaScenario();
    }
}
