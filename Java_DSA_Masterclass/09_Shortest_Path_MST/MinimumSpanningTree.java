/*
 * ============================================================================
 *  MODULE 09 : MINIMUM SPANNING TREE - Disjoint Set, Kruskal, Prim
 * ============================================================================
 *
 *  SPANNING TREE: a subset of edges that connects ALL V vertices with no
 *  cycles -> exactly V-1 edges.   MINIMUM spanning tree: smallest total weight.
 *  (Undirected, connected, weighted graphs only.)
 *
 *  MST != shortest-path tree. MST minimises the TOTAL cable laid; Dijkstra
 *  minimises the distance from ONE source to each vertex.
 *
 *  DISJOINT SET (Union-Find)
 *  -------------------------
 *  Tracks which vertices are already connected.
 *    find(x)      : representative ("root") of x's set
 *    union(a, b)  : merge two sets
 *  Optimisations:
 *    - Path compression: during find, point nodes straight at the root
 *    - Union by rank   : attach the shorter tree under the taller one
 *  Together: ~O(alpha(n)) per op (inverse Ackermann, effectively constant).
 *
 *  KRUSKAL (edge-centric, greedy)
 *    sort all edges by weight; take each edge if it joins two DIFFERENT sets
 *    (i.e. doesn't create a cycle).          Time O(E log E)   Space O(V + E)
 *
 *  PRIM (vertex-centric, greedy)
 *    start at any vertex; repeatedly add the cheapest edge leaving the tree
 *    (min-heap of frontier edges).           Time O(E log V)   Space O(V + E)
 *
 *  Kruskal shines on sparse graphs / edge lists; Prim on dense graphs.
 *
 *  REAL-WORLD USE CASES: laying fibre / power / water networks, cluster
 *  analysis (stop Kruskal early = k clusters), approximation for TSP.
 *
 *  HOW TO RUN
 *  ----------
 *   javac MinimumSpanningTree.java && java MinimumSpanningTree
 * ============================================================================
 */

import java.util.*;

public class MinimumSpanningTree {

    static class DisjointSet {
        final int[] parent, rank;
        int sets;

        DisjointSet(int n) {
            parent = new int[n]; rank = new int[n]; sets = n;
            for (int i = 0; i < n; i++) parent[i] = i;          // everyone is their own set
        }

        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);    // PATH COMPRESSION
            return parent[x];
        }

        boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;                         // already connected -> cycle
            if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;                                    // UNION BY RANK
            if (rank[ra] == rank[rb]) rank[ra]++;
            sets--;
            return true;
        }
    }

    static class Edge {
        final int u, v, w;
        Edge(int u, int v, int w) { this.u = u; this.v = v; this.w = w; }
    }

    static class Network {
        final String[] names;
        final List<Edge> edges = new ArrayList<>();
        Network(String... names) { this.names = names; }
        int id(String n) { for (int i = 0; i < names.length; i++) if (names[i].equals(n)) return i; throw new IllegalArgumentException(n); }
        void add(String a, String b, int w) { edges.add(new Edge(id(a), id(b), w)); }
        String fmt(Edge e) { return names[e.u] + " - " + names[e.v] + " (" + e.w + ")"; }
    }

    static List<Edge> kruskal(Network g, List<String> log) {
        List<Edge> sorted = new ArrayList<>(g.edges);
        sorted.sort(Comparator.comparingInt(e -> e.w));         // cheapest first
        DisjointSet ds = new DisjointSet(g.names.length);
        List<Edge> mst = new ArrayList<>();
        for (Edge e : sorted) {
            if (ds.union(e.u, e.v)) { mst.add(e); log.add("take " + g.fmt(e)); }
            else log.add("skip " + g.fmt(e) + "  (would form a cycle)");
            if (mst.size() == g.names.length - 1) break;        // V-1 edges -> done
        }
        return mst;
    }

    static List<Edge> prim(Network g, int start) {
        int n = g.names.length;
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (Edge e : g.edges) { adj.get(e.u).add(e); adj.get(e.v).add(new Edge(e.v, e.u, e.w)); }

        boolean[] inTree = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.w));
        List<Edge> mst = new ArrayList<>();
        inTree[start] = true;
        pq.addAll(adj.get(start));                              // frontier edges
        while (!pq.isEmpty() && mst.size() < n - 1) {
            Edge e = pq.poll();                                 // cheapest edge leaving the tree
            if (inTree[e.v]) continue;                          // both ends already inside
            inTree[e.v] = true;
            mst.add(e);
            for (Edge out : adj.get(e.v)) if (!inTree[out.v]) pq.add(out);
        }
        return mst;
    }

    static int total(List<Edge> es) { int t = 0; for (Edge e : es) t += e.w; return t; }

    // ------------------------------------------------------------------------
    //  SCENARIO: connecting data centres with fibre at minimum cost (Rs crore)
    // ------------------------------------------------------------------------
    static void fibreScenario() {
        System.out.println("\n=== SCENARIO: Fibre Backbone Between Data Centres (cost in Rs crore) ===");
        Network dc = new Network("Mumbai", "Pune", "Hyderabad", "Bangalore", "Chennai", "Delhi", "Kolkata");
        Object[][] quotes = {{"Mumbai", "Pune", 12}, {"Mumbai", "Delhi", 95}, {"Mumbai", "Hyderabad", 48},
                {"Pune", "Hyderabad", 40}, {"Pune", "Bangalore", 58}, {"Hyderabad", "Bangalore", 39},
                {"Hyderabad", "Chennai", 41}, {"Bangalore", "Chennai", 22}, {"Hyderabad", "Delhi", 101},
                {"Delhi", "Kolkata", 98}, {"Hyderabad", "Kolkata", 93}, {"Chennai", "Kolkata", 105}};
        for (Object[] q : quotes) dc.add((String) q[0], (String) q[1], (Integer) q[2]);

        List<String> log = new ArrayList<>();
        List<Edge> k = kruskal(dc, log);
        System.out.println("  Kruskal's decisions:");
        for (String s : log) System.out.println("    " + s);
        List<Edge> p = prim(dc, dc.id("Delhi"));
        int allLinks = total(dc.edges);
        System.out.println("  Kruskal total = Rs " + total(k) + " cr | Prim (from Delhi) total = Rs " + total(p) + " cr");
        System.out.println("  Building every quoted link would cost Rs " + allLinks + " cr -> saving Rs "
                + (allLinks - total(k)) + " cr with " + k.size() + " links");
    }

    public static void main(String[] args) {
        System.out.println("=== DISJOINT SET (UNION-FIND) ===");
        DisjointSet ds = new DisjointSet(6);
        ds.union(0, 1); ds.union(2, 3); ds.union(1, 3);
        System.out.println("  after union(0,1), union(2,3), union(1,3): sets=" + ds.sets);
        System.out.println("  find(0)==find(2)? " + (ds.find(0) == ds.find(2)) + " | find(4)==find(5)? " + (ds.find(4) == ds.find(5)));
        System.out.println("  union(0,2) again -> " + ds.union(0, 2) + " (already connected => cycle)");

        System.out.println("\n=== KRUSKAL vs PRIM ===");
        Network g = new Network("A", "B", "C", "D", "E");
        g.add("A", "B", 15); g.add("A", "C", 20); g.add("B", "C", 13); g.add("B", "D", 5);
        g.add("C", "D", 10); g.add("C", "E", 6); g.add("D", "E", 8);
        List<Edge> k = kruskal(g, new ArrayList<>());
        List<Edge> p = prim(g, 0);
        StringJoiner kj = new StringJoiner(", "), pj = new StringJoiner(", ");
        for (Edge e : k) kj.add(g.fmt(e));
        for (Edge e : p) pj.add(g.fmt(e));
        System.out.println("  Kruskal: " + kj + "  total=" + total(k));
        System.out.println("  Prim   : " + pj + "  total=" + total(p));

        fibreScenario();
    }
}
