/*
 * ============================================================================
 *  MODULE 12 : DYNAMIC PROGRAMMING
 *  Top-Down Memoization vs Bottom-Up Tabulation  +  0/1 Knapsack
 * ============================================================================
 *
 *  WHEN DOES DP APPLY?
 *  -------------------
 *   1) OPTIMAL SUBSTRUCTURE     : the best answer is built from best answers
 *                                 to sub-problems
 *   2) OVERLAPPING SUB-PROBLEMS : the same sub-problems repeat (Module 11)
 *  DP solves each distinct sub-problem ONCE and reuses it.
 *  (The name "dynamic programming" was coined by Richard Bellman in the
 *   1950s; "programming" meant planning/tabulating, not coding.)
 *
 *  TWO STYLES
 *  ----------
 *   TOP-DOWN (Memoization)            | BOTTOM-UP (Tabulation)
 *   ----------------------------------|-----------------------------------
 *   Recursive D&C + a cache           | Iterative loops fill a table from
 *   (HashMap/array). Check the cache  | the base cases upward. Every cell's
 *   before computing.                 | dependencies are already filled.
 *   + Easy to write from recursion    | + No recursion -> no stack overflow
 *   + Only computes states it needs   | + Often allows O(1)/O(row) space
 *   - Recursion overhead / depth      | - Computes every state, needed or not
 *
 *  IS MERGE SORT DP? No - its sub-problems never overlap, so there is
 *  nothing to cache. It's plain divide & conquer.
 *
 *  0/1 KNAPSACK
 *  ------------
 *  n items, item i has weight w[i] and value v[i]; capacity W; each item is
 *  taken WHOLE or not at all. Greedy by value/weight fails (Module 10).
 *     best(i, cap) = max( best(i+1, cap),                       // skip item i
 *                         v[i] + best(i+1, cap - w[i]) )         // take item i (if it fits)
 *   Recursive: O(2^n)  |  Memo / Tabulation: O(n*W) time, O(n*W) space
 *   1-D rolling array: O(W) space (iterate capacity DOWNWARDS so each item
 *   is used at most once).
 *
 *  COMPLEXITY SUMMARY (DP versions)
 *   | Problem           | Time    | Space            |
 *   |-------------------|---------|------------------|
 *   | Fibonacci         | O(n)    | O(1) optimised   |
 *   | Number factor     | O(n)    | O(n)             |
 *   | House robber      | O(n)    | O(1) optimised   |
 *   | Edit distance     | O(m*n)  | O(m*n)           |
 *   | LCS               | O(m*n)  | O(m*n)           |
 *   | 0/1 knapsack      | O(n*W)  | O(W) optimised   |
 *
 *  HOW TO RUN
 *  ----------
 *   javac DynamicProgramming.java && java DynamicProgramming
 * ============================================================================
 */

import java.util.*;

public class DynamicProgramming {

    static long calls;

    // ======================= FIBONACCI: 4 versions ==========================
    static long fibNaive(int n) { calls++; return n < 2 ? n : fibNaive(n - 1) + fibNaive(n - 2); }

    static long fibMemo(int n, long[] memo) {                    // TOP-DOWN
        calls++;
        if (n < 2) return n;
        if (memo[n] != 0) return memo[n];                        // cache hit
        return memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
    }

    static long fibTab(int n) {                                  // BOTTOM-UP
        if (n < 2) return n;
        long[] dp = new long[n + 1];
        dp[1] = 1;
        for (int i = 2; i <= n; i++) dp[i] = dp[i - 1] + dp[i - 2];
        return dp[n];
    }

    static long fibO1(int n) {                                   // only the last 2 cells matter
        long a = 0, b = 1;
        for (int i = 0; i < n; i++) { long t = a + b; a = b; b = t; }
        return a;
    }

    // ======================= NUMBER FACTOR {1,3,4} ==========================
    static long numberFactorMemo(int n, Map<Integer, Long> memo) {
        if (n <= 2) return 1;
        if (n == 3) return 2;
        Long hit = memo.get(n);
        if (hit != null) return hit;
        long r = numberFactorMemo(n - 1, memo) + numberFactorMemo(n - 3, memo) + numberFactorMemo(n - 4, memo);
        memo.put(n, r);
        return r;
    }

    static long numberFactorTab(int n) {
        long[] dp = new long[Math.max(n + 1, 4)];
        dp[0] = dp[1] = dp[2] = 1; dp[3] = 2;
        for (int i = 4; i <= n; i++) dp[i] = dp[i - 1] + dp[i - 3] + dp[i - 4];
        return dp[n];
    }

    // ======================= HOUSE ROBBER ===================================
    static int robMemo(int[] h, int i, Integer[] memo) {
        if (i >= h.length) return 0;
        if (memo[i] != null) return memo[i];
        return memo[i] = Math.max(h[i] + robMemo(h, i + 2, memo), robMemo(h, i + 1, memo));
    }

    static int robTab(int[] h) {
        // dp[i] = best loot from houses i..n-1; fill right-to-left
        int[] dp = new int[h.length + 2];
        for (int i = h.length - 1; i >= 0; i--) dp[i] = Math.max(h[i] + dp[i + 2], dp[i + 1]);
        return dp[0];
    }

    // ======================= EDIT DISTANCE (bottom-up) ======================
    static int editDistance(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];          // dp[i][j]: a[0..i) -> b[0..j)
        for (int i = 0; i <= m; i++) dp[i][0] = i;   // delete everything
        for (int j = 0; j <= n; j++) dp[0][j] = j;   // insert everything
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? dp[i - 1][j - 1]
                        : 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));   // replace, delete, insert
        return dp[m][n];
    }

    // ======================= LCS with reconstruction ========================
    static String lcs(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1) ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
        // walk back from the bottom-right corner to recover the actual subsequence
        StringBuilder sb = new StringBuilder();
        for (int i = m, j = n; i > 0 && j > 0; ) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) { sb.append(a.charAt(i - 1)); i--; j--; }
            else if (dp[i - 1][j] >= dp[i][j - 1]) i--;
            else j--;
        }
        return sb.reverse().toString();
    }

    // ======================= 0/1 KNAPSACK ===================================
    static int knapNaive(int[] w, int[] v, int i, int cap) {
        calls++;
        if (i == w.length || cap == 0) return 0;
        int skip = knapNaive(w, v, i + 1, cap);
        int take = w[i] <= cap ? v[i] + knapNaive(w, v, i + 1, cap - w[i]) : 0;
        return Math.max(skip, take);
    }

    static int knapMemo(int[] w, int[] v, int i, int cap, Integer[][] memo) {   // TOP-DOWN
        calls++;
        if (i == w.length || cap == 0) return 0;
        if (memo[i][cap] != null) return memo[i][cap];
        int skip = knapMemo(w, v, i + 1, cap, memo);
        int take = w[i] <= cap ? v[i] + knapMemo(w, v, i + 1, cap - w[i], memo) : 0;
        return memo[i][cap] = Math.max(skip, take);
    }

    /** BOTTOM-UP table: dp[i][c] = best value using the first i items with capacity c. */
    static int knapTab(int[] w, int[] v, int W, List<Integer> chosen) {
        int n = w.length;
        int[][] dp = new int[n + 1][W + 1];
        for (int i = 1; i <= n; i++)
            for (int c = 0; c <= W; c++) {
                dp[i][c] = dp[i - 1][c];                                       // skip item i-1
                if (w[i - 1] <= c) dp[i][c] = Math.max(dp[i][c], v[i - 1] + dp[i - 1][c - w[i - 1]]);  // take it
            }
        // Reconstruct: if the value changed when item i-1 became available, it was taken
        for (int i = n, c = W; i > 0; i--)
            if (dp[i][c] != dp[i - 1][c]) { chosen.add(i - 1); c -= w[i - 1]; }
        Collections.reverse(chosen);
        return dp[n][W];
    }

    /** Space-optimised O(W): iterate capacity DOWNWARDS so each item is used once. */
    static int knap1D(int[] w, int[] v, int W) {
        int[] dp = new int[W + 1];
        for (int i = 0; i < w.length; i++)
            for (int c = W; c >= w[i]; c--)                    // downward = 0/1; upward would = unbounded
                dp[c] = Math.max(dp[c], v[i] + dp[c - w[i]]);
        return dp[W];
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Optimising a cloud server's CPU/RAM allocation within budget
    //
    //  Part A - BUDGET (classic 0/1 knapsack): which add-ons to buy for a
    //           production server with Rs 20,000/month? value = performance score.
    //  Part B - CPU + RAM (2-constraint knapsack): which client workloads to host
    //           on a 16 vCPU / 64 GB node to maximise monthly revenue.
    // ------------------------------------------------------------------------
    static void cloudScenario() {
        System.out.println("\n=== SCENARIO: Cloud Server CPU/RAM Allocation Within Budget ===");
        String[] addons = {"+8 vCPU", "+32 GB RAM", "NVMe SSD 1TB", "GPU T4", "Load balancer", "Redis cache 16GB", "DDoS shield"};
        int[] cost =      {7000,      5000,         3000,           12000,    2500,            4500,               3500};   // Rs/month
        int[] score =     {60,        45,           25,             70,       20,              40,                 15};
        int budget = 20000;

        // Scale costs by 500 to shrink the table: all costs are multiples of 500
        int unit = 500;
        int[] wUnits = Arrays.stream(cost).map(c -> c / unit).toArray();
        List<Integer> picks = new ArrayList<>();
        int best = knapTab(wUnits, score, budget / unit, picks);
        System.out.println("  Part A: Rs " + String.format("%,d", budget) + "/month budget for server add-ons");
        int spent = 0;
        for (int i : picks) { spent += cost[i]; System.out.printf("    [x] %-17s Rs %,6d  score %d%n", addons[i], cost[i], score[i]); }
        System.out.printf("    total score %d, spent Rs %,d (Rs %,d left)%n", best, spent, budget - spent);
        System.out.println("    1-D optimised DP agrees: " + (knap1D(wUnits, score, budget / unit) == best));

        // Part B: dp[c][r] = best revenue with c vCPUs and r GB still free.
        String[] jobs = {"Postgres DB", "Kafka broker", "ML inference", "Web frontend", "CI runners", "Elasticsearch", "Video encoder"};
        int[] cpu =     {4,             4,              6,              2,              6,            4,               8};
        int[] ram =     {16,            12,             24,             4,              8,            24,              16};
        int[] revenue = {9000,          7000,           13000,          3000,           6000,         9500,            11000};
        int CPU = 16, RAM = 64;
        int[][] dp = new int[CPU + 1][RAM + 1];
        boolean[][][] took = new boolean[jobs.length][CPU + 1][RAM + 1];
        for (int i = 0; i < jobs.length; i++)
            for (int c = CPU; c >= cpu[i]; c--)                 // both dimensions iterate DOWNWARDS
                for (int r = RAM; r >= ram[i]; r--) {
                    int withJob = revenue[i] + dp[c - cpu[i]][r - ram[i]];
                    if (withJob > dp[c][r]) { dp[c][r] = withJob; took[i][c][r] = true; }
                }
        System.out.println("  Part B: host client workloads on one 16 vCPU / 64 GB node");
        int c = CPU, r = RAM, usedCpu = 0, usedRam = 0;
        List<String> hosted = new ArrayList<>();
        for (int i = jobs.length - 1; i >= 0; i--)              // reconstruct from the last item backwards
            if (took[i][c][r]) { hosted.add(0, jobs[i]); usedCpu += cpu[i]; usedRam += ram[i]; c -= cpu[i]; r -= ram[i]; }
        for (String j : hosted) {
            int i = Arrays.asList(jobs).indexOf(j);
            System.out.printf("    [x] %-14s %2d vCPU %3d GB  Rs %,6d/month%n", j, cpu[i], ram[i], revenue[i]);
        }
        System.out.printf("    revenue Rs %,d/month using %d/%d vCPU and %d/%d GB%n", dp[CPU][RAM], usedCpu, CPU, usedRam, RAM);
    }

    public static void main(String[] args) {
        System.out.println("=== FIBONACCI: naive vs memo vs tab ===");
        int n = 35;
        calls = 0; long t0 = System.nanoTime(); long f1 = fibNaive(n); long tNaive = System.nanoTime() - t0; long cNaive = calls;
        calls = 0; t0 = System.nanoTime(); long f2 = fibMemo(n, new long[n + 1]); long tMemo = System.nanoTime() - t0; long cMemo = calls;
        System.out.printf("  naive D&C   fib(%d)=%d  calls=%,d  time=%d ms%n", n, f1, cNaive, tNaive / 1_000_000);
        System.out.printf("  memoized    fib(%d)=%d  calls=%,d  time<1 ms (%d us)%n", n, f2, cMemo, tMemo / 1000);
        System.out.printf("  tabulated   fib(%d)=%d  (loop, O(n) space)%n", n, fibTab(n));
        System.out.printf("  O(1) space  fib(90)=%d%n", fibO1(90));

        System.out.println("\n=== NUMBER FACTOR & HOUSE ROBBER ===");
        System.out.println("  numberFactor(30): memo=" + numberFactorMemo(30, new HashMap<>()) + " tab=" + numberFactorTab(30));
        int[] street = {6, 7, 1, 30, 8, 2, 4};
        System.out.println("  houseRobber" + Arrays.toString(street) + ": memo=" + robMemo(street, 0, new Integer[street.length]) + " tab=" + robTab(street));

        System.out.println("\n=== STRING DP ===");
        System.out.println("  editDistance(\"sunday\",\"saturday\") = " + editDistance("sunday", "saturday"));
        System.out.println("  LCS(\"ABCBDAB\",\"BDCABA\") = \"" + lcs("ABCBDAB", "BDCABA") + "\"");
        System.out.println("  LCS(\"AGGTAB\",\"GXTXAYB\") = \"" + lcs("AGGTAB", "GXTXAYB") + "\"");

        System.out.println("\n=== 0/1 KNAPSACK: three ways ===");
        int[] w = {10, 20, 30}, v = {60, 100, 120};
        List<Integer> pick = new ArrayList<>();
        System.out.println("  items (w,v): (10,60) (20,100) (30,120), capacity 50");
        calls = 0; int a = knapNaive(w, v, 0, 50); long c1 = calls;
        calls = 0; int b = knapMemo(w, v, 0, 50, new Integer[w.length][51]); long c2 = calls;
        int c = knapTab(w, v, 50, pick);
        System.out.println("  recursive=" + a + " (" + c1 + " calls) | memo=" + b + " (" + c2 + " calls) | table=" + c + " items " + pick
                + " | 1-D=" + knap1D(w, v, 50));
        System.out.println("  (greedy by ratio would return 160 - see Module 10)");

        Random rnd = new Random(5);
        int N = 25;
        int[] bw = new int[N], bv = new int[N];
        for (int i = 0; i < N; i++) { bw[i] = 5 + rnd.nextInt(40); bv[i] = 10 + rnd.nextInt(90); }
        calls = 0; t0 = System.nanoTime(); int r1 = knapNaive(bw, bv, 0, 200); long nt = System.nanoTime() - t0; long nc = calls;
        calls = 0; t0 = System.nanoTime(); int r2 = knapMemo(bw, bv, 0, 200, new Integer[N][201]); long mt = System.nanoTime() - t0; long mc = calls;
        System.out.printf("  25 items, capacity 200: recursive %,d calls (%d ms) vs memo %,d calls (%d ms); same answer: %b%n",
                nc, nt / 1_000_000, mc, mt / 1_000_000, r1 == r2);

        cloudScenario();
    }
}
