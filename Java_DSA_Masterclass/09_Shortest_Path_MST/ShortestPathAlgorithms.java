/*
 * ============================================================================
 *  MODULE 09 : SHORTEST PATH ALGORITHMS
 *  Dijkstra  |  Bellman-Ford  |  Floyd-Warshall
 * ============================================================================
 *
 *  SINGLE SOURCE SHORTEST PATH (SSSP): shortest distance from ONE source to
 *  every vertex.   ALL PAIRS SHORTEST PATH (APSP): between EVERY pair.
 *
 *  RELAXATION - the core step shared by all three algorithms:
 *        if dist[u] + w(u,v) < dist[v]:  dist[v] = dist[u] + w(u,v); parent[v] = u
 *
 *  1) DIJKSTRA (greedy, SSSP, weights >= 0)
 *     Always expand the closest unsettled vertex (min-heap). Once settled, a
 *     vertex's distance is final - that is only true when no edge is negative.
 *     Time O((V + E) log V)   Space O(V)
 *
 *  2) BELLMAN-FORD (SSSP, negative weights allowed)
 *     Relax EVERY edge, V-1 times. Why V-1? A shortest path without cycles has
 *     at most V-1 edges, and pass i guarantees all shortest paths using <= i
 *     edges are correct. A V-th pass that STILL improves something proves a
 *     NEGATIVE CYCLE exists (distances could drop forever).
 *     Time O(V * E)   Space O(V)
 *
 *  3) FLOYD-WARSHALL (APSP, negative edges ok, no negative cycles)
 *     Dynamic programming over "intermediate" vertices:
 *        for k in V: for i in V: for j in V:
 *           dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])
 *     After iteration k, dist[i][j] is the best path using only vertices
 *     0..k as stop-overs. dist[i][i] < 0 at the end => negative cycle.
 *     Time O(V^3)   Space O(V^2)
 *
 *  WHICH ONE?
 *   | Situation                                  | Use            |
 *   |--------------------------------------------|----------------|
 *   | Unweighted graph                           | BFS            |
 *   | Non-negative weights, one source           | Dijkstra       |
 *   | Negative weights / detect negative cycles  | Bellman-Ford   |
 *   | All pairs, small/dense graph (V <= ~500)   | Floyd-Warshall |
 *
 *  HOW TO RUN
 *  ----------
 *   javac ShortestPathAlgorithms.java && java ShortestPathAlgorithms
 * ============================================================================
 */

import java.util.*;

public class ShortestPathAlgorithms {

    static final long INF = Long.MAX_VALUE / 4;     // /4 so INF + w never overflows

    /** Directed weighted graph stored as an edge list + adjacency list. */
    static class Graph {
        final List<String> names = new ArrayList<>();
        final Map<String, Integer> id = new HashMap<>();
        final List<int[]> edges = new ArrayList<>();             // {u, v, w}
        final List<List<int[]>> adj = new ArrayList<>();         // adj[u] = {v, w}

        int vertex(String n) {
            return id.computeIfAbsent(n, k -> { names.add(k); adj.add(new ArrayList<>()); return names.size() - 1; });
        }

        void addEdge(String a, String b, int w) {
            int u = vertex(a), v = vertex(b);
            edges.add(new int[]{u, v, w});
            adj.get(u).add(new int[]{v, w});
        }

        void addUndirected(String a, String b, int w) { addEdge(a, b, w); addEdge(b, a, w); }

        int size() { return names.size(); }

        String path(int[] parent, int dst) {
            LinkedList<String> p = new LinkedList<>();
            for (int at = dst; at != -1; at = parent[at]) p.addFirst(names.get(at));
            return String.join(" -> ", p);
        }
    }

    // ------------------------------------------------------------------------
    //  1) DIJKSTRA with a binary heap (lazy deletion)
    // ------------------------------------------------------------------------
    static long[] dijkstra(Graph g, int src, int[] parent) {
        long[] dist = new long[g.size()];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[src] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(x -> x[0]));  // {dist, vertex}
        pq.add(new long[]{0, src});
        boolean[] settled = new boolean[g.size()];
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            int u = (int) top[1];
            if (settled[u]) continue;           // stale heap entry
            settled[u] = true;                  // dist[u] is now FINAL (needs w >= 0)
            for (int[] e : g.adj.get(u)) {
                int v = e[0], w = e[1];
                if (dist[u] + w < dist[v]) {    // relax
                    dist[v] = dist[u] + w;
                    parent[v] = u;
                    pq.add(new long[]{dist[v], v});
                }
            }
        }
        return dist;
    }

    // ------------------------------------------------------------------------
    //  2) BELLMAN-FORD - returns null if a negative cycle is reachable
    // ------------------------------------------------------------------------
    static long[] bellmanFord(Graph g, int src, int[] parent, List<String> cycleOut) {
        int n = g.size();
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[src] = 0;
        for (int pass = 1; pass <= n - 1; pass++) {
            boolean changed = false;
            for (int[] e : g.edges) {
                if (dist[e[0]] != INF && dist[e[0]] + e[2] < dist[e[1]]) {
                    dist[e[1]] = dist[e[0]] + e[2];
                    parent[e[1]] = e[0];
                    changed = true;
                }
            }
            if (!changed) break;                // early exit: converged
        }
        // V-th pass: any further improvement means a negative cycle
        for (int[] e : g.edges) {
            if (dist[e[0]] != INF && dist[e[0]] + e[2] < dist[e[1]]) {
                // Walk parent pointers V times to land INSIDE the cycle, then collect it
                int x = e[1];
                parent[x] = e[0];
                for (int i = 0; i < n; i++) x = parent[x];
                LinkedList<String> cyc = new LinkedList<>();
                int y = x;
                do { cyc.addFirst(g.names.get(y)); y = parent[y]; } while (y != x);
                cyc.addFirst(g.names.get(x));
                cycleOut.addAll(cyc);
                return null;
            }
        }
        return dist;
    }

    // ------------------------------------------------------------------------
    //  3) FLOYD-WARSHALL with next-hop matrix for path reconstruction
    // ------------------------------------------------------------------------
    static long[][] floydWarshall(Graph g, int[][] next) {
        int n = g.size();
        long[][] d = new long[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(d[i], INF);
            Arrays.fill(next[i], -1);
            d[i][i] = 0;
            next[i][i] = i;
        }
        for (int[] e : g.edges) {
            if (e[2] < d[e[0]][e[1]]) { d[e[0]][e[1]] = e[2]; next[e[0]][e[1]] = e[1]; }
        }
        for (int k = 0; k < n; k++)             // allow vertex k as a stop-over
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] != INF && d[k][j] != INF && d[i][k] + d[k][j] < d[i][j]) {
                        d[i][j] = d[i][k] + d[k][j];
                        next[i][j] = next[i][k];    // first hop towards j goes towards k
                    }
        return d;
    }

    static String fwPath(Graph g, int[][] next, int u, int v) {
        if (next[u][v] == -1) return "unreachable";
        StringBuilder sb = new StringBuilder(g.names.get(u));
        while (u != v) { u = next[u][v]; sb.append(" -> ").append(g.names.get(u)); }
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Network Routing Delay Optimisation
    //  Routers connected by links; weights = one-way latency in ms.
    // ------------------------------------------------------------------------
    static Graph buildNetwork() {
        Graph net = new Graph();
        String[][] links = {
                {"Mumbai", "Pune", "4"}, {"Mumbai", "Ahmedabad", "9"}, {"Mumbai", "Bangalore", "18"},
                {"Pune", "Hyderabad", "11"}, {"Pune", "Bangalore", "14"}, {"Ahmedabad", "Delhi", "12"},
                {"Delhi", "Kolkata", "22"}, {"Hyderabad", "Chennai", "9"}, {"Bangalore", "Chennai", "6"},
                {"Hyderabad", "Kolkata", "20"}, {"Chennai", "Kolkata", "25"}, {"Delhi", "Hyderabad", "21"}};
        for (String[] l : links) net.addUndirected(l[0], l[1], Integer.parseInt(l[2]));
        return net;
    }

    static void networkRoutingScenario() {
        System.out.println("\n=== SCENARIO: Network Routing Delay Optimisation ===");
        Graph net = buildNetwork();
        int src = net.id.get("Mumbai");
        int[] parent = new int[net.size()];
        long[] dist = dijkstra(net, src, parent);

        System.out.println("  [Dijkstra] Routing table for router Mumbai:");
        System.out.println("    destination  latency  next-hop   full path");
        for (int v = 0; v < net.size(); v++) {
            if (v == src) continue;
            int hop = v;
            while (parent[hop] != src) hop = parent[hop];          // walk back to first hop
            System.out.printf("    %-11s %5d ms  %-9s  %s%n", net.names.get(v), dist[v], net.names.get(hop), net.path(parent, v));
        }

        int[][] next = new int[net.size()][net.size()];
        long[][] all = floydWarshall(net, next);
        System.out.println("  [Floyd-Warshall] All-pairs latency matrix (ms):");
        StringBuilder header = new StringBuilder("              ");
        for (String n : net.names) header.append(String.format(" %6.6s", n));
        System.out.println(header);
        int bestHub = -1; long bestWorst = INF;
        for (int i = 0; i < net.size(); i++) {
            StringBuilder row = new StringBuilder(String.format("    %-10s", net.names.get(i)));
            long worst = 0;
            for (int j = 0; j < net.size(); j++) { row.append(String.format(" %6d", all[i][j])); worst = Math.max(worst, all[i][j]); }
            System.out.println(row);
            if (worst < bestWorst) { bestWorst = worst; bestHub = i; }
        }
        System.out.println("  -> Best location for a central DNS/CDN server (min worst-case latency): "
                + net.names.get(bestHub) + " (" + bestWorst + " ms to the farthest router)");
        System.out.println("     Delhi -> Chennai route: " + fwPath(net, next, net.id.get("Delhi"), net.id.get("Chennai"))
                + " (" + all[net.id.get("Delhi")][net.id.get("Chennai")] + " ms)");

        // Bellman-Ford: transit COST graph where some peering links pay us a rebate (negative cost)
        System.out.println("  [Bellman-Ford] Transit cost with peering rebates (negative edges):");
        Graph cost = new Graph();
        String[][] c = {{"Mumbai", "Pune", "5"}, {"Mumbai", "Ahmedabad", "12"}, {"Pune", "Hyderabad", "3"},
                {"Ahmedabad", "Hyderabad", "-8"}, {"Hyderabad", "Chennai", "3"}, {"Pune", "Chennai", "10"},
                {"Chennai", "Kolkata", "7"}, {"Hyderabad", "Kolkata", "-2"}};
        for (String[] e : c) cost.addEdge(e[0], e[1], Integer.parseInt(e[2]));
        int[] par = new int[cost.size()];
        List<String> cycle = new ArrayList<>();
        long[] bf = bellmanFord(cost, cost.id.get("Mumbai"), par, cycle);
        for (int v = 0; v < cost.size(); v++)
            System.out.printf("    Mumbai -> %-10s cost %3d  via %s%n", cost.names.get(v), bf[v], cost.path(par, v));
        int[] dpar = new int[cost.size()];
        long[] dj = dijkstra(cost, cost.id.get("Mumbai"), dpar);
        System.out.println("    Dijkstra on the same graph says Mumbai->Kolkata = " + dj[cost.id.get("Kolkata")]
                + "  <- WRONG: Hyderabad was settled before the -8 rebate was discovered ("
                + bf[cost.id.get("Kolkata")] + " is correct)");

        System.out.println("  !! New link Kolkata -> Ahmedabad (cost 4) closes a loop with total cost 4 - 8 - 2 = -6:");
        cost.addEdge("Kolkata", "Ahmedabad", 4);
        cycle.clear();
        long[] bad = bellmanFord(cost, cost.id.get("Mumbai"), par, cycle);
        System.out.println("    Bellman-Ford: " + (bad == null ? "NEGATIVE CYCLE detected: " + String.join(" -> ", cycle) : "no cycle"));
    }

    public static void main(String[] args) {
        System.out.println("=== SHORTEST PATH ALGORITHMS ON ONE GRAPH ===");
        Graph g = new Graph();
        g.addEdge("A", "B", 6); g.addEdge("A", "D", 1); g.addEdge("D", "B", 2);
        g.addEdge("D", "E", 1); g.addEdge("B", "E", 2); g.addEdge("B", "C", 5); g.addEdge("E", "C", 5);
        int[] p1 = new int[g.size()], p2 = new int[g.size()];
        long[] d1 = dijkstra(g, 0, p1);
        long[] d2 = bellmanFord(g, 0, p2, new ArrayList<>());
        int[][] next = new int[g.size()][g.size()];
        long[][] d3 = floydWarshall(g, next);
        System.out.println("  vertex  Dijkstra  BellmanFord  FloydWarshall  path");
        for (int v = 0; v < g.size(); v++)
            System.out.printf("  %-6s  %8d  %11d  %13d  %s%n", g.names.get(v), d1[v], d2[v], d3[0][v], g.path(p1, v));

        networkRoutingScenario();
    }
}
