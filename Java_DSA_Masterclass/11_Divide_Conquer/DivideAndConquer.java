/*
 * ============================================================================
 *  MODULE 11 : DIVIDE AND CONQUER
 *  Fibonacci | Number Factor | House Robber | Convert String (Edit Distance)
 *  Longest Common Subsequence | Longest Palindromic Subsequence | Min-Cost Path
 * ============================================================================
 *
 *  THE PARADIGM
 *  ------------
 *   1) DIVIDE  the problem into smaller sub-problems of the same type
 *   2) CONQUER each sub-problem recursively (base case = trivially small)
 *   3) COMBINE the sub-answers into the answer for the original problem
 *
 *  Classic examples: merge sort, quick sort, binary search (Modules 07/08).
 *
 *  THE CATCH - OVERLAPPING SUB-PROBLEMS
 *  ------------------------------------
 *  In merge sort the two halves never overlap, so D&C is efficient. In the
 *  problems below the SAME sub-problem is solved again and again:
 *
 *                     fib(5)
 *                 /          \
 *            fib(4)          fib(3)       <- fib(3) computed twice,
 *           /     \          /    \          fib(2) three times...
 *       fib(3)  fib(2)   fib(2)  fib(1)
 *
 *  That makes pure D&C EXPONENTIAL. Every function here counts its calls so
 *  you can see the blow-up; Module 12 (Dynamic Programming) fixes it by
 *  caching (memoization) or building a table (tabulation).
 *
 *  COMPLEXITY OF THE PURE RECURSIVE SOLUTIONS
 *   | Problem                    | Time        | Space (stack) |
 *   |----------------------------|-------------|---------------|
 *   | Fibonacci                  | O(2^n)      | O(n)          |
 *   | Number factor {1,3,4}      | O(3^n)      | O(n)          |
 *   | House robber               | O(2^n)      | O(n)          |
 *   | Convert string (edit dist) | O(3^(m+n))  | O(m+n)        |
 *   | LCS                        | O(2^(m+n))  | O(m+n)        |
 *   | Longest palindromic subseq | O(2^n)      | O(n)          |
 *   | Min-cost path in grid      | O(2^(r+c))  | O(r+c)        |
 *
 *  HOW TO RUN
 *  ----------
 *   javac DivideAndConquer.java && java DivideAndConquer
 * ============================================================================
 */

public class DivideAndConquer {

    static long calls;                      // recursion counter

    // ------------------------------------------------------------------------
    //  FIBONACCI:  fib(n) = fib(n-1) + fib(n-2),  fib(0)=0, fib(1)=1
    // ------------------------------------------------------------------------
    static long fib(int n) {
        calls++;
        if (n < 2) return n;
        return fib(n - 1) + fib(n - 2);
    }

    // ------------------------------------------------------------------------
    //  NUMBER FACTOR: number of ways to write n as an ORDERED sum of 1, 3, 4.
    //   e.g. n = 4 -> {4}, {1,3}, {3,1}, {1,1,1,1}  => 4 ways
    //  Divide on the LAST number used:  f(n) = f(n-1) + f(n-3) + f(n-4)
    //  Base: f(0)=1 (empty sum), f(1)=1, f(2)=1, f(3)=2
    // ------------------------------------------------------------------------
    static long numberFactor(int n) {
        calls++;
        if (n == 0 || n == 1 || n == 2) return 1;
        if (n == 3) return 2;
        return numberFactor(n - 1) + numberFactor(n - 3) + numberFactor(n - 4);
    }

    // ------------------------------------------------------------------------
    //  HOUSE ROBBER: max loot from a street of houses without robbing two
    //  ADJACENT houses (the alarm links neighbours).
    //  At house i: either ROB it (+ value, skip to i+2) or SKIP it (go to i+1).
    //     rob(i) = max(house[i] + rob(i+2), rob(i+1))
    // ------------------------------------------------------------------------
    static int houseRobber(int[] houses, int i) {
        calls++;
        if (i >= houses.length) return 0;
        int robIt = houses[i] + houseRobber(houses, i + 2);
        int skipIt = houseRobber(houses, i + 1);
        return Math.max(robIt, skipIt);
    }

    // ------------------------------------------------------------------------
    //  CONVERT STRING (Edit / Levenshtein distance): min insert/delete/replace
    //  operations to turn s1 into s2. Compare characters from the front:
    //    equal      -> no cost, advance both
    //    different  -> 1 + min(insert, delete, replace)
    // ------------------------------------------------------------------------
    static int editDistance(String s1, String s2, int i, int j) {
        calls++;
        if (i == s1.length()) return s2.length() - j;       // insert the rest of s2
        if (j == s2.length()) return s1.length() - i;       // delete the rest of s1
        if (s1.charAt(i) == s2.charAt(j)) return editDistance(s1, s2, i + 1, j + 1);
        int insert  = 1 + editDistance(s1, s2, i, j + 1);
        int delete  = 1 + editDistance(s1, s2, i + 1, j);
        int replace = 1 + editDistance(s1, s2, i + 1, j + 1);
        return Math.min(insert, Math.min(delete, replace));
    }

    // ------------------------------------------------------------------------
    //  LONGEST COMMON SUBSEQUENCE: longest sequence appearing in BOTH strings
    //  in the same order (not necessarily contiguous).
    //    "ABCBDAB" & "BDCABA" -> length 4  (e.g. "BCBA")
    //  If first chars match -> 1 + LCS(rest, rest)
    //  else                 -> max(LCS(skip s1 char), LCS(skip s2 char))
    // ------------------------------------------------------------------------
    static int lcs(String a, String b, int i, int j) {
        calls++;
        if (i == a.length() || j == b.length()) return 0;
        if (a.charAt(i) == b.charAt(j)) return 1 + lcs(a, b, i + 1, j + 1);
        return Math.max(lcs(a, b, i + 1, j), lcs(a, b, i, j + 1));
    }

    // ------------------------------------------------------------------------
    //  LONGEST PALINDROMIC SUBSEQUENCE  ("ELRMENMET" -> "EMEME", length 5)
    // ------------------------------------------------------------------------
    static int lps(String s, int i, int j) {
        calls++;
        if (i > j) return 0;
        if (i == j) return 1;
        if (s.charAt(i) == s.charAt(j)) return 2 + lps(s, i + 1, j - 1);
        return Math.max(lps(s, i + 1, j), lps(s, i, j - 1));
    }

    // ------------------------------------------------------------------------
    //  MIN-COST PATH: from top-left to bottom-right moving only RIGHT or DOWN.
    //  Solved backwards from the target: cost(r,c) = grid[r][c] + min(from up, from left)
    // ------------------------------------------------------------------------
    static int minCost(int[][] g, int r, int c) {
        calls++;
        if (r < 0 || c < 0) return Integer.MAX_VALUE;
        if (r == 0 && c == 0) return g[0][0];
        int up = minCost(g, r - 1, c), left = minCost(g, r, c - 1);
        return g[r][c] + Math.min(up, left);
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Code-review diff - how similar are two versions of a function?
    //  LCS on lines powers the classic `diff` utility; edit distance on
    //  characters powers spell-checkers ("did you mean ...?").
    // ------------------------------------------------------------------------
    static void diffScenario() {
        System.out.println("\n=== SCENARIO: Version Diff & Spell Check ===");
        String v1 = "ABCDEFG", v2 = "ABXDEYG";
        System.out.println("  Each letter stands for one line of code:");
        System.out.println("    old version : " + v1 + "\n    new version : " + v2);
        calls = 0;
        int common = lcs(v1, v2, 0, 0);
        System.out.println("  LCS = " + common + " unchanged lines -> " + (v1.length() - common) + " removed, "
                + (v2.length() - common) + " added   (" + calls + " recursive calls)");

        String[] dictionary = {"algorithm", "logarithm", "altruism", "rhythm", "allegory"};
        String typo = "algoritm";
        String best = null; int bestDist = Integer.MAX_VALUE;
        for (String w : dictionary) {
            calls = 0;
            int d = editDistance(typo, w, 0, 0);
            System.out.printf("    edit('%s', '%s') = %d   (%,d calls)%n", typo, w, d, calls);
            if (d < bestDist) { bestDist = d; best = w; }
        }
        System.out.println("  Did you mean \"" + best + "\"?");
    }

    public static void main(String[] args) {
        System.out.println("=== DIVIDE & CONQUER (watch the call counts explode) ===");
        for (int n : new int[]{10, 20, 30}) { calls = 0; long f = fib(n); System.out.printf("  fib(%d) = %,d   calls = %,d%n", n, f, calls); }

        System.out.println();
        for (int n : new int[]{4, 5, 6, 20}) { calls = 0; long w = numberFactor(n); System.out.printf("  numberFactor(%d) = %,d ways   calls = %,d%n", n, w, calls); }

        System.out.println();
        int[] street = {6, 7, 1, 30, 8, 2, 4};
        calls = 0;
        System.out.println("  houses " + java.util.Arrays.toString(street) + " -> max loot " + houseRobber(street, 0) + "  (calls " + calls + ")");

        System.out.println();
        calls = 0;
        System.out.println("  editDistance(\"table\", \"tbrltt\") = " + editDistance("table", "tbrltt", 0, 0) + "  (calls " + calls + ")");
        calls = 0;
        System.out.println("  LCS(\"ABCBDAB\", \"BDCABA\")       = " + lcs("ABCBDAB", "BDCABA", 0, 0) + "  (calls " + calls + ")");
        calls = 0;
        System.out.println("  LPS(\"ELRMENMET\")               = " + lps("ELRMENMET", 0, 8) + "  (calls " + calls + ")");
        int[][] grid = {{4, 7, 8, 6, 4}, {6, 7, 3, 9, 2}, {3, 8, 1, 2, 4}, {7, 1, 7, 3, 7}, {2, 9, 8, 9, 3}};
        calls = 0;
        System.out.println("  min-cost path in 5x5 grid      = " + minCost(grid, 4, 4) + "  (calls " + calls + ")");

        diffScenario();
        System.out.println("\n  -> All of these recompute the same sub-problems. See Module 12 for the DP fix.");
    }
}
