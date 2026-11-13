/*
 * ============================================================================
 *  MODULE 10 : GREEDY ALGORITHMS
 *  Activity Selection | Coin Change | Fractional Knapsack
 * ============================================================================
 *
 *  WHAT IS GREEDY?
 *  ---------------
 *  Build the solution step by step, always taking the choice that looks BEST
 *  RIGHT NOW, and never reconsidering it. Greedy is correct only when the
 *  problem has:
 *    1) GREEDY-CHOICE PROPERTY : a locally optimal choice is part of some
 *                                globally optimal solution
 *    2) OPTIMAL SUBSTRUCTURE   : after the choice, what's left is a smaller
 *                                instance of the same problem
 *  When those fail (0/1 knapsack, coin change with odd denominations) greedy
 *  gives WRONG answers - use Dynamic Programming (Module 12) instead.
 *
 *  WELL-KNOWN GREEDY ALGORITHMS: Dijkstra, Prim, Kruskal, Huffman coding,
 *  activity selection, fractional knapsack, job sequencing with deadlines.
 *
 *  THIS FILE
 *  ---------
 *   | Problem             | Greedy rule                          | Time       |
 *   |---------------------|--------------------------------------|------------|
 *   | Activity selection  | pick the activity that FINISHES first| O(n log n) |
 *   | Coin change         | take the largest coin that fits      | O(k + ans) |
 *   | Fractional knapsack | take highest value/weight ratio first| O(n log n) |
 *
 *  HOW TO RUN
 *  ----------
 *   javac GreedyAlgorithms.java && java GreedyAlgorithms
 * ============================================================================
 */

import java.util.*;

public class GreedyAlgorithms {

    // ------------------------------------------------------------------------
    //  1) ACTIVITY SELECTION - max number of non-overlapping activities
    //  Why "earliest finish"? Finishing first leaves the MOST room for the
    //  rest; any optimal schedule can swap its first activity for ours.
    // ------------------------------------------------------------------------
    static class Activity {
        final String name; final int start, end;
        Activity(String n, int s, int e) { name = n; start = s; end = e; }
        @Override public String toString() { return String.format("%s[%02d:00-%02d:00]", name, start, end); }
    }

    static List<Activity> selectActivities(List<Activity> acts) {
        List<Activity> sorted = new ArrayList<>(acts);
        sorted.sort(Comparator.comparingInt(a -> a.end));       // O(n log n)
        List<Activity> chosen = new ArrayList<>();
        int lastEnd = Integer.MIN_VALUE;
        for (Activity a : sorted) {
            if (a.start >= lastEnd) {                            // compatible with previous pick
                chosen.add(a);
                lastEnd = a.end;
            }
        }
        return chosen;
    }

    // ------------------------------------------------------------------------
    //  2) COIN CHANGE (greedy) - fewest coins/notes for an amount.
    //  Optimal for "canonical" systems like INR {1,2,5,10,20,50,100,200,500,2000}
    //  but NOT for arbitrary denominations (demo below).
    // ------------------------------------------------------------------------
    static Map<Integer, Integer> greedyChange(int[] denominations, int amount) {
        int[] d = denominations.clone();
        Arrays.sort(d);
        Map<Integer, Integer> used = new LinkedHashMap<>();
        for (int i = d.length - 1; i >= 0 && amount > 0; i--) {  // largest first
            int count = amount / d[i];
            if (count > 0) { used.put(d[i], count); amount -= count * d[i]; }
        }
        if (amount != 0) return null;                            // cannot make exact change
        return used;
    }

    /** DP reference answer (minimum coins) - used to expose greedy's failure. */
    static int minCoinsDP(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        for (int a = 1; a <= amount; a++)
            for (int c : coins)
                if (c <= a && dp[a - c] != Integer.MAX_VALUE) dp[a] = Math.min(dp[a], dp[a - c] + 1);
        return dp[amount];
    }

    // ------------------------------------------------------------------------
    //  3) FRACTIONAL KNAPSACK - items can be split; maximise value within
    //  capacity. Sort by value density (value / weight) and fill greedily; the
    //  last item may be taken partially.
    // ------------------------------------------------------------------------
    static class Item {
        final String name; final double weight, value;
        Item(String n, double w, double v) { name = n; weight = w; value = v; }
        double ratio() { return value / weight; }
    }

    static double fractionalKnapsack(List<Item> items, double capacity, List<String> plan) {
        List<Item> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> Double.compare(b.ratio(), a.ratio()));   // best density first
        double total = 0, remaining = capacity;
        for (Item it : sorted) {
            if (remaining <= 0) break;
            double take = Math.min(it.weight, remaining);
            double fraction = take / it.weight;
            total += it.value * fraction;
            remaining -= take;
            plan.add(String.format("%-16s %7.1f kg of %7.1f (%5.1f%%)  ratio %8.1f/kg  -> Rs %,10.0f",
                    it.name, take, it.weight, fraction * 100, it.ratio(), it.value * fraction));
        }
        return total;
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Maximising cargo value for a shipping truck (capacity 1,000 kg)
    //  Bulk goods can be split (fractional knapsack). For indivisible items
    //  (e.g. a single machine) see 0/1 knapsack in Module 12 - greedy by ratio
    //  is NOT optimal there; we show the counterexample at the end.
    // ------------------------------------------------------------------------
    static void truckScenario() {
        System.out.println("\n=== SCENARIO: Maximising Cargo Value for a Shipping Truck (1,000 kg) ===");
        List<Item> cargo = List.of(
                new Item("Saffron", 15, 375_000), new Item("Cardamom", 120, 360_000),
                new Item("Basmati rice", 600, 72_000), new Item("Cashew nuts", 300, 270_000),
                new Item("Black pepper", 250, 162_500), new Item("Turmeric", 400, 64_000));
        List<String> plan = new ArrayList<>();
        double best = fractionalKnapsack(cargo, 1000, plan);
        System.out.println("  Load plan (highest value per kg first):");
        for (String s : plan) System.out.println("    " + s);
        System.out.printf("  Maximum cargo value = Rs %,.0f%n", best);

        System.out.println("  Why greedy fails for INDIVISIBLE items (0/1 knapsack, capacity 50 kg):");
        System.out.println("    items: A 10kg Rs60 (6/kg), B 20kg Rs100 (5/kg), C 30kg Rs120 (4/kg)");
        System.out.println("    greedy by ratio takes A+B = Rs160 (C no longer fits)");
        System.out.println("    optimal is B+C = Rs220 -> solved correctly with DP in Module 12");
    }

    public static void main(String[] args) {
        System.out.println("=== ACTIVITY SELECTION: Conference Room Booking ===");
        List<Activity> meetings = List.of(
                new Activity("Standup", 9, 10), new Activity("Design review", 9, 12),
                new Activity("1:1", 10, 11), new Activity("Client call", 11, 13),
                new Activity("Lunch talk", 12, 13), new Activity("Sprint plan", 13, 15),
                new Activity("Interview", 14, 15), new Activity("Retro", 15, 16), new Activity("All hands", 10, 16));
        List<Activity> chosen = selectActivities(meetings);
        System.out.println("  requests : " + meetings);
        System.out.println("  booked   : " + chosen + "  -> " + chosen.size() + " meetings");

        System.out.println("\n=== COIN CHANGE ===");
        int[] inr = {1, 2, 5, 10, 20, 50, 100, 200, 500, 2000};
        for (int amt : new int[]{2768, 1999, 93}) {
            Map<Integer, Integer> c = greedyChange(inr, amt);
            int n = c.values().stream().mapToInt(Integer::intValue).sum();
            System.out.println("  Rs " + amt + " -> " + c + "  (" + n + " notes/coins, optimal: " + minCoinsDP(inr, amt) + ")");
        }
        int[] odd = {1, 3, 4};
        Map<Integer, Integer> g = greedyChange(odd, 6);
        System.out.println("  Non-canonical coins {1,3,4}, amount 6 -> greedy " + g + " = "
                + g.values().stream().mapToInt(Integer::intValue).sum() + " coins, but optimal is "
                + minCoinsDP(odd, 6) + " (3+3)  <- greedy is NOT always optimal");

        truckScenario();
    }
}
