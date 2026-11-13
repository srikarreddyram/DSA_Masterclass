/*
 * ============================================================================
 *  MODULE 05 : WEIGHTED GRAPH + DIJKSTRA'S SHORTEST PATH (dual implementation)
 * ============================================================================
 *
 *  WHY NOT BFS?
 *  ------------
 *  BFS finds the path with the FEWEST EDGES. With weights (km, minutes, cost)
 *  a route with more edges can be cheaper:   A--10--C   vs   A--2--B--3--C
 *
 *  DIJKSTRA'S ALGORITHM (non-negative weights only)
 *  ------------------------------------------------
 *   dist[src] = 0, every other dist = INFINITY
 *   repeat:
 *     pick the UNVISITED vertex u with the smallest dist      <- greedy choice
 *     mark u final (its dist can never improve again)
 *     RELAX every edge u->v:  if dist[u] + w < dist[v] then dist[v] = dist[u] + w
 *
 *  Why is the greedy choice safe? All weights are >= 0, so any other route
 *  to u would pass through a vertex whose dist is already >= dist[u] and can
 *  only add more. (A negative edge breaks this -> use Bellman-Ford.)
 *
 *  TWO IMPLEMENTATIONS IN THIS FILE
 *  --------------------------------
 *   1) Adjacency MATRIX + linear scan for the min vertex
 *        Time O(V^2)            - best for DENSE graphs (E ~ V^2)
 *   2) Adjacency LIST + binary-heap PriorityQueue
 *        Time O((V + E) log V)  - best for SPARSE graphs (road networks)
 *   Space: O(V^2) for the matrix version, O(V + E) for the list version.
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - GPS navigation (with A* / contraction hierarchies on top)
 *   - Network routing protocols (OSPF), flight cost search, game AI pathing
 *
 *  HOW TO RUN
 *  ----------
 *   javac WeightedGraphDijkstra.java && java WeightedGraphDijkstra
 * ============================================================================
 */

import java.util.*;

public class WeightedGraphDijkstra {

    static final int INF = Integer.MAX_VALUE;

    // ========================================================================
    //  VERSION 1: ADJACENCY MATRIX - O(V^2)
    // ========================================================================
    static class MatrixWeightedGraph {
        final String[] names;
        final int[][] w;                 // w[i][j] = weight, 0 = no edge

        MatrixWeightedGraph(String... names) { this.names = names; w = new int[names.length][names.length]; }

        int id(String n) { for (int i = 0; i < names.length; i++) if (names[i].equals(n)) return i; return -1; }

        void addEdge(String u, String v, int weight) { w[id(u)][id(v)] = weight; w[id(v)][id(u)] = weight; }

        int[] dijkstra(String src, int[] parent) {
            int n = names.length;
            int[] dist = new int[n];
            boolean[] done = new boolean[n];
            Arrays.fill(dist, INF);
            Arrays.fill(parent, -1);
            dist[id(src)] = 0;
            for (int iter = 0; iter < n; iter++) {
                // 1) linear scan for the closest unfinished vertex - O(V)
                int u = -1;
                for (int i = 0; i < n; i++) if (!done[i] && (u == -1 || dist[i] < dist[u])) u = i;
                if (u == -1 || dist[u] == INF) break;         // remaining vertices unreachable
                done[u] = true;
                // 2) relax all neighbours by scanning the row - O(V)
                for (int v = 0; v < n; v++) {
                    if (w[u][v] > 0 && !done[v] && dist[u] + w[u][v] < dist[v]) {
                        dist[v] = dist[u] + w[u][v];
                        parent[v] = u;
                    }
                }
            }
            return dist;
        }
    }

    // ========================================================================
    //  VERSION 2: ADJACENCY LIST (node-based) + PRIORITY QUEUE - O((V+E) log V)
    // ========================================================================
    static class Edge {
        final Vertex to; final int weight;
        Edge(Vertex to, int weight) { this.to = to; this.weight = weight; }
    }

    static class Vertex {
        final String name;
        final List<Edge> edges = new ArrayList<>();
        int dist = INF;
        Vertex parent;
        Vertex(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    static class ListWeightedGraph {
        final Map<String, Vertex> vertices = new LinkedHashMap<>();

        Vertex v(String name) { return vertices.computeIfAbsent(name, Vertex::new); }

        void addEdge(String a, String b, int weight) {        // undirected road
            v(a).edges.add(new Edge(v(b), weight));
            v(b).edges.add(new Edge(v(a), weight));
        }

        void removeEdge(String a, String b) {
            v(a).edges.removeIf(e -> e.to == v(b));
            v(b).edges.removeIf(e -> e.to == v(a));
        }

        int relaxations, staleSkips;

        void dijkstra(String src) {
            for (Vertex x : vertices.values()) { x.dist = INF; x.parent = null; }
            relaxations = staleSkips = 0;
            Vertex s = v(src);
            s.dist = 0;

            // The PQ holds {distance-at-push-time, vertex}. Java's PriorityQueue has
            // no "decrease-key", so we use LAZY DELETION: when a shorter distance is
            // found we simply push a NEW entry. Older, larger entries for the same
            // vertex stay in the heap and are skipped when polled ("stale").
            PriorityQueue<Object[]> pq = new PriorityQueue<>(Comparator.comparingInt(o -> (Integer) o[0]));
            pq.add(new Object[]{0, s});
            Set<Vertex> settled = new HashSet<>();

            while (!pq.isEmpty()) {
                Object[] top = pq.poll();                 // O(log V): closest frontier vertex
                Vertex u = (Vertex) top[1];
                if (!settled.add(u)) { staleSkips++; continue; }   // stale entry -> ignore
                for (Edge e : u.edges) {                  // each edge relaxed once overall: O(E)
                    if (settled.contains(e.to)) continue;
                    int candidate = u.dist + e.weight;
                    if (candidate < e.to.dist) {          // RELAXATION
                        e.to.dist = candidate;
                        e.to.parent = u;
                        pq.add(new Object[]{candidate, e.to});   // O(log V)
                        relaxations++;
                    }
                }
            }
        }

        List<String> pathTo(String dst) {
            LinkedList<String> path = new LinkedList<>();
            Vertex d = v(dst);
            if (d.dist == INF) return path;
            for (Vertex at = d; at != null; at = at.parent) path.addFirst(at.name);
            return path;
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: City navigation - fastest drive between Chennai landmarks
    //  Edge weights are travel times in minutes.
    // ------------------------------------------------------------------------
    static final String[][] ROADS = {
            {"SRM_KTR", "Tambaram", "18"}, {"SRM_KTR", "Vandalur", "12"}, {"Vandalur", "Tambaram", "10"},
            {"Tambaram", "Airport", "15"}, {"Tambaram", "Velachery", "25"}, {"Airport", "Guindy", "12"},
            {"Velachery", "Guindy", "10"}, {"Guindy", "T_Nagar", "14"}, {"Velachery", "OMR_Tidel", "15"},
            {"OMR_Tidel", "Adyar", "12"}, {"Adyar", "Marina", "15"}, {"T_Nagar", "Marina", "20"},
            {"T_Nagar", "Central", "22"}, {"Marina", "Central", "10"}, {"SRM_KTR", "OMR_Tidel", "45"}};

    static void cityNavigationScenario() {
        System.out.println("\n=== SCENARIO: Chennai City Navigation (Dijkstra) ===");
        ListWeightedGraph city = new ListWeightedGraph();
        for (String[] r : ROADS) city.addEdge(r[0], r[1], Integer.parseInt(r[2]));

        city.dijkstra("SRM_KTR");
        System.out.println("  Fastest travel times from SRM_KTR:");
        for (Vertex x : city.vertices.values())
            System.out.printf("    %-10s %3d min   via %s%n", x.name, x.dist, String.join(" > ", city.pathTo(x.name)));
        System.out.println("  (heap work: " + city.relaxations + " relaxations, " + city.staleSkips + " stale entries skipped)");

        System.out.println("  !! Traffic alert: Airport-Guindy road blocked");
        city.removeEdge("Airport", "Guindy");
        city.dijkstra("SRM_KTR");
        System.out.printf("  New route to Central: %s (%d min)%n",
                String.join(" > ", city.pathTo("Central")), city.v("Central").dist);
    }

    public static void main(String[] args) {
        System.out.println("=== DIJKSTRA - MATRIX VERSION O(V^2) ===");
        MatrixWeightedGraph mg = new MatrixWeightedGraph("A", "B", "C", "D", "E", "F");
        mg.addEdge("A", "B", 2); mg.addEdge("A", "C", 10); mg.addEdge("B", "C", 3);
        mg.addEdge("B", "D", 8); mg.addEdge("C", "D", 2); mg.addEdge("D", "E", 4);
        mg.addEdge("C", "E", 9); mg.addEdge("E", "F", 1);
        int[] parent = new int[6];
        int[] dist = mg.dijkstra("A", parent);
        for (int i = 0; i < dist.length; i++) {
            LinkedList<String> path = new LinkedList<>();
            for (int at = i; at != -1; at = parent[at]) path.addFirst(mg.names[at]);
            System.out.printf("  A -> %s : %2d  path %s%n", mg.names[i], dist[i], path);
        }
        System.out.println("  note: A->C direct costs 10, but A->B->C costs 5 (BFS would pick the direct edge)");

        System.out.println("\n=== DIJKSTRA - ADJACENCY LIST + PRIORITY QUEUE O((V+E) log V) ===");
        ListWeightedGraph lg = new ListWeightedGraph();
        lg.addEdge("A", "B", 2); lg.addEdge("A", "C", 10); lg.addEdge("B", "C", 3);
        lg.addEdge("B", "D", 8); lg.addEdge("C", "D", 2); lg.addEdge("D", "E", 4);
        lg.addEdge("C", "E", 9); lg.addEdge("E", "F", 1);
        lg.dijkstra("A");
        for (Vertex x : lg.vertices.values())
            System.out.printf("  A -> %s : %2d  path %s%n", x.name, x.dist, lg.pathTo(x.name));
        System.out.println("  both versions agree: " + (lg.v("F").dist == dist[5]));

        cityNavigationScenario();
    }
}
