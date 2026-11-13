"""
============================================================================
 MODULE 11 : DIVIDE AND CONQUER
 Fibonacci | Number Factor | House Robber | Convert String (Edit Distance)
 Longest Common Subsequence | Longest Palindromic Subsequence | Min-Cost Path
============================================================================

 THE PARADIGM
   1) DIVIDE  into smaller sub-problems of the same type
   2) CONQUER each recursively (base case = trivially small)
   3) COMBINE the sub-answers
 Classic examples: merge sort, quick sort, binary search (Modules 07/08).

 THE CATCH - OVERLAPPING SUB-PROBLEMS
                    fib(5)
                /          \
           fib(4)          fib(3)     <- fib(3) twice, fib(2) three times...
          /     \          /    \
      fib(3)  fib(2)   fib(2)  fib(1)
 Merge sort's halves never overlap, so it's efficient. The problems below
 recompute the same states over and over -> EXPONENTIAL time. Every function
 counts its calls so you can watch the blow-up. Module 12 fixes it with DP.

 COMPLEXITY (pure recursion)
  | Fibonacci                  | O(2^n)      | stack O(n)    |
  | Number factor {1,3,4}      | O(3^n)      | stack O(n)    |
  | House robber               | O(2^n)      | stack O(n)    |
  | Edit distance              | O(3^(m+n))  | stack O(m+n)  |
  | LCS                        | O(2^(m+n))  | stack O(m+n)  |
  | Longest palindromic subseq | O(2^n)      | stack O(n)    |
  | Min-cost grid path         | O(2^(r+c))  | stack O(r+c)  |

 HOW TO RUN:  python divide_and_conquer.py
============================================================================
"""

calls = 0


def fib(n: int) -> int:
    global calls
    calls += 1
    return n if n < 2 else fib(n - 1) + fib(n - 2)


def number_factor(n: int) -> int:
    """Ways to write n as an ORDERED sum of 1, 3, 4 (e.g. 4 -> {4},{1,3},{3,1},{1,1,1,1}).
    Divide on the LAST number used: f(n) = f(n-1) + f(n-3) + f(n-4)."""
    global calls
    calls += 1
    if n in (0, 1, 2):
        return 1
    if n == 3:
        return 2
    return number_factor(n - 1) + number_factor(n - 3) + number_factor(n - 4)


def house_robber(houses, i: int = 0) -> int:
    """Max loot without robbing adjacent houses: rob i (skip to i+2) or skip i."""
    global calls
    calls += 1
    if i >= len(houses):
        return 0
    return max(houses[i] + house_robber(houses, i + 2), house_robber(houses, i + 1))


def edit_distance(s1: str, s2: str, i: int = 0, j: int = 0) -> int:
    """Min insert/delete/replace operations to convert s1 into s2."""
    global calls
    calls += 1
    if i == len(s1):
        return len(s2) - j                       # insert the rest
    if j == len(s2):
        return len(s1) - i                       # delete the rest
    if s1[i] == s2[j]:
        return edit_distance(s1, s2, i + 1, j + 1)
    return 1 + min(edit_distance(s1, s2, i, j + 1),       # insert
                   edit_distance(s1, s2, i + 1, j),       # delete
                   edit_distance(s1, s2, i + 1, j + 1))   # replace


def lcs(a: str, b: str, i: int = 0, j: int = 0) -> int:
    """Longest common subsequence length ("ABCBDAB","BDCABA" -> 4)."""
    global calls
    calls += 1
    if i == len(a) or j == len(b):
        return 0
    if a[i] == b[j]:
        return 1 + lcs(a, b, i + 1, j + 1)
    return max(lcs(a, b, i + 1, j), lcs(a, b, i, j + 1))


def lps(s: str, i: int, j: int) -> int:
    """Longest palindromic subsequence ("ELRMENMET" -> "EMEME", 5)."""
    global calls
    calls += 1
    if i > j:
        return 0
    if i == j:
        return 1
    if s[i] == s[j]:
        return 2 + lps(s, i + 1, j - 1)
    return max(lps(s, i + 1, j), lps(s, i, j - 1))


def min_cost(g, r: int, c: int) -> float:
    """Cheapest path from (0,0) to (r,c) moving only right/down (solved backwards)."""
    global calls
    calls += 1
    if r < 0 or c < 0:
        return float("inf")
    if r == 0 and c == 0:
        return g[0][0]
    return g[r][c] + min(min_cost(g, r - 1, c), min_cost(g, r, c - 1))


def diff_scenario() -> None:
    global calls
    print("\n=== SCENARIO: Version Diff & Spell Check ===")
    v1, v2 = "ABCDEFG", "ABXDEYG"
    print("  Each letter stands for one line of code:")
    print(f"    old version : {v1}\n    new version : {v2}")
    calls = 0
    common = lcs(v1, v2)
    print(f"  LCS = {common} unchanged lines -> {len(v1) - common} removed, {len(v2) - common} added   "
          f"({calls} recursive calls)")
    typo, best = "algoritm", None
    for w in ["algorithm", "logarithm", "altruism", "rhythm", "allegory"]:
        calls = 0
        d = edit_distance(typo, w)
        print(f"    edit('{typo}', '{w}') = {d}   ({calls:,} calls)")
        if best is None or d < best[0]:
            best = (d, w)
    print(f"  Did you mean \"{best[1]}\"?")


def main() -> None:
    global calls
    print("=== DIVIDE & CONQUER (watch the call counts explode) ===")
    for n in (10, 20, 25):
        calls = 0
        f = fib(n)
        print(f"  fib({n}) = {f:,}   calls = {calls:,}")
    print()
    for n in (4, 5, 6, 20):
        calls = 0
        w = number_factor(n)
        print(f"  number_factor({n}) = {w:,} ways   calls = {calls:,}")
    print()
    street = [6, 7, 1, 30, 8, 2, 4]
    calls = 0
    print(f"  houses {street} -> max loot {house_robber(street)}  (calls {calls})")
    print()
    calls = 0; print(f"  edit_distance('table', 'tbrltt') = {edit_distance('table', 'tbrltt')}  (calls {calls})")
    calls = 0; print(f"  LCS('ABCBDAB', 'BDCABA')         = {lcs('ABCBDAB', 'BDCABA')}  (calls {calls})")
    calls = 0; print(f"  LPS('ELRMENMET')                 = {lps('ELRMENMET', 0, 8)}  (calls {calls})")
    grid = [[4, 7, 8, 6, 4], [6, 7, 3, 9, 2], [3, 8, 1, 2, 4], [7, 1, 7, 3, 7], [2, 9, 8, 9, 3]]
    calls = 0; print(f"  min-cost path in 5x5 grid        = {min_cost(grid, 4, 4)}  (calls {calls})")
    diff_scenario()
    print("\n  -> All of these recompute the same sub-problems. See Module 12 for the DP fix.")


if __name__ == "__main__":
    main()
