"""
============================================================================
 MODULE 10 : GREEDY ALGORITHMS
 Activity Selection | Coin Change | Fractional Knapsack
============================================================================

 GREEDY = always take the choice that looks best right now, never undo it.
 Correct only when the problem has:
   1) GREEDY-CHOICE PROPERTY : a locally best choice belongs to some optimal
                               solution
   2) OPTIMAL SUBSTRUCTURE   : the remainder is a smaller instance of the
                               same problem
 When these fail (0/1 knapsack, odd coin systems) greedy is WRONG -> use DP
 (Module 12).

 KNOWN GREEDY ALGORITHMS: Dijkstra, Prim, Kruskal, Huffman coding, activity
 selection, fractional knapsack, job sequencing.

  | Problem             | Greedy rule                         | Time       |
  |---------------------|-------------------------------------|------------|
  | Activity selection  | earliest FINISH time first          | O(n log n) |
  | Coin change         | largest coin that fits              | O(k + ans) |
  | Fractional knapsack | highest value/weight ratio first    | O(n log n) |

 HOW TO RUN:  python greedy_algorithms.py
============================================================================
"""


def select_activities(acts):
    """acts: list of (name, start, end). Earliest finish leaves the most room."""
    chosen, last_end = [], float("-inf")
    for name, s, e in sorted(acts, key=lambda a: a[2]):   # O(n log n)
        if s >= last_end:                                 # compatible
            chosen.append((name, s, e))
            last_end = e
    return chosen


def fmt_act(a):
    return f"{a[0]}[{a[1]:02d}:00-{a[2]:02d}:00]"


def greedy_change(denominations, amount):
    used = {}
    for d in sorted(denominations, reverse=True):         # largest first
        if amount >= d:
            used[d], amount = divmod(amount, d)
    return used if amount == 0 else None


def min_coins_dp(coins, amount) -> int:
    """DP reference answer to expose where greedy fails."""
    dp = [0] + [float("inf")] * amount
    for a in range(1, amount + 1):
        for c in coins:
            if c <= a:
                dp[a] = min(dp[a], dp[a - c] + 1)
    return dp[amount]


def fractional_knapsack(items, capacity):
    """items: list of (name, weight, value). Returns (total_value, plan)."""
    total, remaining, plan = 0.0, capacity, []
    for name, w, v in sorted(items, key=lambda it: it[2] / it[1], reverse=True):
        if remaining <= 0:
            break
        take = min(w, remaining)
        frac = take / w
        total += v * frac
        remaining -= take
        plan.append(f"{name:<16} {take:7.1f} kg of {w:7.1f} ({frac * 100:5.1f}%)  "
                    f"ratio {v / w:8.1f}/kg  -> Rs {v * frac:10,.0f}")
    return total, plan


def truck_scenario() -> None:
    print("\n=== SCENARIO: Maximising Cargo Value for a Shipping Truck (1,000 kg) ===")
    cargo = [("Saffron", 15, 375_000), ("Cardamom", 120, 360_000), ("Basmati rice", 600, 72_000),
             ("Cashew nuts", 300, 270_000), ("Black pepper", 250, 162_500), ("Turmeric", 400, 64_000)]
    best, plan = fractional_knapsack(cargo, 1000)
    print("  Load plan (highest value per kg first):")
    for line in plan:
        print("    " + line)
    print(f"  Maximum cargo value = Rs {best:,.0f}")
    print("  Why greedy fails for INDIVISIBLE items (0/1 knapsack, capacity 50 kg):")
    print("    items: A 10kg Rs60 (6/kg), B 20kg Rs100 (5/kg), C 30kg Rs120 (4/kg)")
    print("    greedy by ratio takes A+B = Rs160 (C no longer fits)")
    print("    optimal is B+C = Rs220 -> solved correctly with DP in Module 12")


def main() -> None:
    print("=== ACTIVITY SELECTION: Conference Room Booking ===")
    meetings = [("Standup", 9, 10), ("Design review", 9, 12), ("1:1", 10, 11), ("Client call", 11, 13),
                ("Lunch talk", 12, 13), ("Sprint plan", 13, 15), ("Interview", 14, 15),
                ("Retro", 15, 16), ("All hands", 10, 16)]
    chosen = select_activities(meetings)
    print("  requests : " + ", ".join(map(fmt_act, meetings)))
    print("  booked   : " + ", ".join(map(fmt_act, chosen)) + f"  -> {len(chosen)} meetings")

    print("\n=== COIN CHANGE ===")
    inr = [1, 2, 5, 10, 20, 50, 100, 200, 500, 2000]
    for amt in (2768, 1999, 93):
        c = greedy_change(inr, amt)
        print(f"  Rs {amt} -> {c}  ({sum(c.values())} notes/coins, optimal: {min_coins_dp(inr, amt)})")
    g = greedy_change([1, 3, 4], 6)
    print(f"  Non-canonical coins {{1,3,4}}, amount 6 -> greedy {g} = {sum(g.values())} coins, "
          f"but optimal is {min_coins_dp([1, 3, 4], 6)} (3+3)  <- greedy is NOT always optimal")
    truck_scenario()


if __name__ == "__main__":
    main()
