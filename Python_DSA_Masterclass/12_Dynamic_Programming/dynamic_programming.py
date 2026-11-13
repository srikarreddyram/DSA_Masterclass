"""
============================================================================
 MODULE 12 : DYNAMIC PROGRAMMING
 Top-Down Memoization vs Bottom-Up Tabulation  +  0/1 Knapsack
============================================================================

 DP APPLIES WHEN
   1) OPTIMAL SUBSTRUCTURE     - best answer built from best sub-answers
   2) OVERLAPPING SUB-PROBLEMS - the same states repeat (Module 11)
 Solve each distinct state ONCE and reuse it. ("Dynamic programming" was
 named by Richard Bellman; "programming" meant planning/tabulating.)

 TWO STYLES
  TOP-DOWN (memoization)               | BOTTOM-UP (tabulation)
  -------------------------------------|-------------------------------------
  recursion + cache (dict / lru_cache) | loops fill a table from base cases
  + easy to derive from recursion      | + no recursion limit issues
  + only visits needed states          | + often O(1)/O(row) space possible
  - recursion overhead / depth limit   | - computes every state

 Merge sort is NOT DP: its halves never overlap, nothing to cache.

 0/1 KNAPSACK: each item taken whole or not at all; capacity W.
   best(i, cap) = max(best(i+1, cap), v[i] + best(i+1, cap - w[i]))
   recursion O(2^n) | memo/table O(n*W) | 1-D array O(W) space
   (iterate capacity DOWNWARDS in 1-D so each item is used once)

 COMPLEXITY (DP versions)
  | Fibonacci     | O(n)   | O(1) optimised |
  | Number factor | O(n)   | O(n)           |
  | House robber  | O(n)   | O(n)           |
  | Edit distance | O(m*n) | O(m*n)         |
  | LCS           | O(m*n) | O(m*n)         |
  | 0/1 knapsack  | O(n*W) | O(W) optimised |

 HOW TO RUN:  python dynamic_programming.py
============================================================================
"""
import random
import sys
import time
from functools import lru_cache
from itertools import combinations

sys.setrecursionlimit(10_000)
calls = 0


# ======================= FIBONACCI: 4 versions ============================
def fib_naive(n):
    global calls
    calls += 1
    return n if n < 2 else fib_naive(n - 1) + fib_naive(n - 2)


def fib_memo(n, memo=None):                      # TOP-DOWN with an explicit dict
    global calls
    calls += 1
    memo = {} if memo is None else memo
    if n < 2:
        return n
    if n not in memo:                            # compute only on a cache miss
        memo[n] = fib_memo(n - 1, memo) + fib_memo(n - 2, memo)
    return memo[n]


@lru_cache(maxsize=None)                         # Python's built-in memoizer
def fib_lru(n):
    return n if n < 2 else fib_lru(n - 1) + fib_lru(n - 2)


def fib_tab(n):                                  # BOTTOM-UP
    if n < 2:
        return n
    dp = [0] * (n + 1)
    dp[1] = 1
    for i in range(2, n + 1):
        dp[i] = dp[i - 1] + dp[i - 2]
    return dp[n]


def fib_o1(n):
    a, b = 0, 1
    for _ in range(n):
        a, b = b, a + b
    return a


# ======================= NUMBER FACTOR {1,3,4} ============================
def number_factor_memo(n, memo=None):
    memo = {} if memo is None else memo
    if n <= 2: return 1
    if n == 3: return 2
    if n not in memo:
        memo[n] = number_factor_memo(n - 1, memo) + number_factor_memo(n - 3, memo) + number_factor_memo(n - 4, memo)
    return memo[n]


def number_factor_tab(n):
    dp = [1, 1, 1, 2] + [0] * max(0, n - 3)
    for i in range(4, n + 1):
        dp[i] = dp[i - 1] + dp[i - 3] + dp[i - 4]
    return dp[n]


# ======================= HOUSE ROBBER =====================================
def rob_memo(h, i=0, memo=None):
    memo = {} if memo is None else memo
    if i >= len(h):
        return 0
    if i not in memo:
        memo[i] = max(h[i] + rob_memo(h, i + 2, memo), rob_memo(h, i + 1, memo))
    return memo[i]


def rob_tab(h):
    dp = [0] * (len(h) + 2)                      # dp[i] = best from house i onward
    for i in range(len(h) - 1, -1, -1):
        dp[i] = max(h[i] + dp[i + 2], dp[i + 1])
    return dp[0]


# ======================= STRING DP ========================================
def edit_distance(a, b):
    m, n = len(a), len(b)
    dp = [[0] * (n + 1) for _ in range(m + 1)]   # dp[i][j]: a[:i] -> b[:j]
    for i in range(m + 1): dp[i][0] = i
    for j in range(n + 1): dp[0][j] = j
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if a[i - 1] == b[j - 1]:
                dp[i][j] = dp[i - 1][j - 1]
            else:
                dp[i][j] = 1 + min(dp[i - 1][j - 1], dp[i - 1][j], dp[i][j - 1])  # replace, delete, insert
    return dp[m][n]


def lcs(a, b):
    m, n = len(a), len(b)
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            dp[i][j] = dp[i - 1][j - 1] + 1 if a[i - 1] == b[j - 1] else max(dp[i - 1][j], dp[i][j - 1])
    out, i, j = [], m, n                         # walk back to recover the string
    while i and j:
        if a[i - 1] == b[j - 1]:
            out.append(a[i - 1]); i -= 1; j -= 1
        elif dp[i - 1][j] >= dp[i][j - 1]:
            i -= 1
        else:
            j -= 1
    return "".join(reversed(out))


# ======================= 0/1 KNAPSACK =====================================
def knap_naive(w, v, cap, i=0):
    global calls
    calls += 1
    if i == len(w) or cap == 0:
        return 0
    best = knap_naive(w, v, cap, i + 1)                                # skip
    if w[i] <= cap:
        best = max(best, v[i] + knap_naive(w, v, cap - w[i], i + 1))   # take
    return best


def knap_memo(w, v, cap, i=0, memo=None):        # TOP-DOWN
    global calls
    calls += 1
    memo = {} if memo is None else memo
    if i == len(w) or cap == 0:
        return 0
    if (i, cap) not in memo:
        best = knap_memo(w, v, cap, i + 1, memo)
        if w[i] <= cap:
            best = max(best, v[i] + knap_memo(w, v, cap - w[i], i + 1, memo))
        memo[(i, cap)] = best
    return memo[(i, cap)]


def knap_tab(w, v, W):
    """BOTTOM-UP: dp[i][c] = best value using the first i items, capacity c.
    Returns (best, chosen item indices)."""
    n = len(w)
    dp = [[0] * (W + 1) for _ in range(n + 1)]
    for i in range(1, n + 1):
        for c in range(W + 1):
            dp[i][c] = dp[i - 1][c]                                     # skip item i-1
            if w[i - 1] <= c:
                dp[i][c] = max(dp[i][c], v[i - 1] + dp[i - 1][c - w[i - 1]])   # take it
    chosen, c = [], W
    for i in range(n, 0, -1):                    # value changed => item i-1 was taken
        if dp[i][c] != dp[i - 1][c]:
            chosen.append(i - 1)
            c -= w[i - 1]
    return dp[n][W], chosen[::-1]


def knap_1d(w, v, W):
    dp = [0] * (W + 1)
    for wi, vi in zip(w, v):
        for c in range(W, wi - 1, -1):           # DOWNWARD = 0/1 (upward = unbounded)
            dp[c] = max(dp[c], vi + dp[c - wi])
    return dp[W]


# --------------------------------------------------------------------------
#  SCENARIO: Optimising a cloud server's CPU/RAM allocation within budget
#   Part A - budget (classic 0/1 knapsack): server add-ons, Rs 20,000/month
#   Part B - CPU + RAM (2-constraint knapsack): which workloads to host on a
#            16 vCPU / 64 GB node to maximise monthly revenue
# --------------------------------------------------------------------------
def cloud_scenario() -> None:
    print("\n=== SCENARIO: Cloud Server CPU/RAM Allocation Within Budget ===")
    addons = ["+8 vCPU", "+32 GB RAM", "NVMe SSD 1TB", "GPU T4", "Load balancer", "Redis cache 16GB", "DDoS shield"]
    cost = [7000, 5000, 3000, 12000, 2500, 4500, 3500]           # Rs / month
    score = [60, 45, 25, 70, 20, 40, 15]
    budget, unit = 20000, 500                                    # costs are multiples of 500 -> smaller table
    best, picks = knap_tab([c // unit for c in cost], score, budget // unit)
    print(f"  Part A: Rs {budget:,}/month budget for server add-ons")
    for i in picks:
        print(f"    [x] {addons[i]:<17} Rs {cost[i]:6,d}  score {score[i]}")
    spent = sum(cost[i] for i in picks)
    print(f"    total score {best}, spent Rs {spent:,} (Rs {budget - spent:,} left)")
    print(f"    1-D optimised DP agrees: {knap_1d([c // unit for c in cost], score, budget // unit) == best}")

    jobs = ["Postgres DB", "Kafka broker", "ML inference", "Web frontend", "CI runners", "Elasticsearch", "Video encoder"]
    cpu = [4, 4, 6, 2, 6, 4, 8]
    ram = [16, 12, 24, 4, 8, 24, 16]
    revenue = [9000, 7000, 13000, 3000, 6000, 9500, 11000]
    CPU, RAM = 16, 64
    dp = [[0] * (RAM + 1) for _ in range(CPU + 1)]               # dp[c][r]
    took = [[[False] * (RAM + 1) for _ in range(CPU + 1)] for _ in jobs]
    for i in range(len(jobs)):
        for c in range(CPU, cpu[i] - 1, -1):                     # both dimensions DOWNWARD
            for r in range(RAM, ram[i] - 1, -1):
                with_job = revenue[i] + dp[c - cpu[i]][r - ram[i]]
                if with_job > dp[c][r]:
                    dp[c][r] = with_job
                    took[i][c][r] = True
    hosted, c, r = [], CPU, RAM
    for i in range(len(jobs) - 1, -1, -1):                       # reconstruct backwards
        if took[i][c][r]:
            hosted.append(i)
            c, r = c - cpu[i], r - ram[i]
    hosted.reverse()
    print("  Part B: host client workloads on one 16 vCPU / 64 GB node")
    for i in hosted:
        print(f"    [x] {jobs[i]:<14} {cpu[i]:2d} vCPU {ram[i]:3d} GB  Rs {revenue[i]:6,d}/month")
    print(f"    revenue Rs {dp[CPU][RAM]:,}/month using {sum(cpu[i] for i in hosted)}/{CPU} vCPU "
          f"and {sum(ram[i] for i in hosted)}/{RAM} GB")

    # sanity check against brute force (2^7 = 128 subsets - fine for a demo)
    brute = max(sum(revenue[i] for i in s) for k in range(len(jobs) + 1) for s in combinations(range(len(jobs)), k)
                if sum(cpu[i] for i in s) <= CPU and sum(ram[i] for i in s) <= RAM)
    print(f"    brute-force check over all 128 subsets: {brute == dp[CPU][RAM]}")


def main() -> None:
    global calls
    print("=== FIBONACCI: naive vs memo vs tab ===")
    n = 27
    calls = 0; t0 = time.perf_counter(); f1 = fib_naive(n); tn = time.perf_counter() - t0; cn = calls
    calls = 0; t0 = time.perf_counter(); f2 = fib_memo(n); tm = time.perf_counter() - t0; cm = calls
    print(f"  naive D&C   fib({n})={f1}  calls={cn:,}  time={tn * 1000:.0f} ms")
    print(f"  memoized    fib({n})={f2}  calls={cm:,}  time={tm * 1e6:.0f} us")
    print(f"  lru_cache   fib(300)={fib_lru(300)}")
    print(f"  tabulated   fib({n})={fib_tab(n)}  (loop, O(n) space)")
    print(f"  O(1) space  fib(90)={fib_o1(90)}")

    print("\n=== NUMBER FACTOR & HOUSE ROBBER ===")
    print(f"  number_factor(30): memo={number_factor_memo(30)} tab={number_factor_tab(30)}")
    street = [6, 7, 1, 30, 8, 2, 4]
    print(f"  house_robber{street}: memo={rob_memo(street)} tab={rob_tab(street)}")

    print("\n=== STRING DP ===")
    print(f"  edit_distance('sunday','saturday') = {edit_distance('sunday', 'saturday')}")
    print(f"  LCS('ABCBDAB','BDCABA') = '{lcs('ABCBDAB', 'BDCABA')}'")
    print(f"  LCS('AGGTAB','GXTXAYB') = '{lcs('AGGTAB', 'GXTXAYB')}'")

    print("\n=== 0/1 KNAPSACK: three ways ===")
    w, v = [10, 20, 30], [60, 100, 120]
    print("  items (w,v): (10,60) (20,100) (30,120), capacity 50")
    calls = 0; a = knap_naive(w, v, 50); c1 = calls
    calls = 0; b = knap_memo(w, v, 50); c2 = calls
    best, items = knap_tab(w, v, 50)
    print(f"  recursive={a} ({c1} calls) | memo={b} ({c2} calls) | table={best} items {items} | 1-D={knap_1d(w, v, 50)}")
    print("  (greedy by ratio would return 160 - see Module 10)")

    rnd = random.Random(5)
    N = 20
    bw = [5 + rnd.randrange(40) for _ in range(N)]
    bv = [10 + rnd.randrange(90) for _ in range(N)]
    calls = 0; t0 = time.perf_counter(); r1 = knap_naive(bw, bv, 200); nt = time.perf_counter() - t0; nc = calls
    calls = 0; t0 = time.perf_counter(); r2 = knap_memo(bw, bv, 200); mt = time.perf_counter() - t0; mc = calls
    print(f"  {N} items, capacity 200: recursive {nc:,} calls ({nt * 1000:.0f} ms) vs memo {mc:,} calls "
          f"({mt * 1000:.0f} ms); same answer: {r1 == r2}")
    cloud_scenario()


if __name__ == "__main__":
    main()
