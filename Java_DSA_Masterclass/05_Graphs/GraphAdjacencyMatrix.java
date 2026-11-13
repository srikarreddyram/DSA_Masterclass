/*
 * ============================================================================
 *  MODULE 05 : GRAPH - Adjacency MATRIX (2D array) implementation
 * ============================================================================
 *
 *  WHAT IS A GRAPH?
 *  ----------------
 *  A set of VERTICES (nodes) connected by EDGES. Unlike trees there is no
 *  root and cycles are allowed.
 *   - Directed vs Undirected  (one-way streets vs two-way roads)
 *   - Weighted vs Unweighted  (distances vs simple connections)
 *   - Cyclic vs Acyclic (DAG), Connected vs Disconnected
 *
 *  ADJACENCY MATRIX
 *  ----------------
 *  A V x V grid. matrix[i][j] = 1 (or the weight) if an edge i -> j exists.
 *
 *          A  B  C  D  E
 *      A [ 0  1  1  0  0 ]        A --- B
 *      B [ 1  0  0  1  0 ]        |     |
 *      C [ 1  0  0  0  1 ]        C     D --- E
 *      D [ 0  1  0  0  1 ]         \_________/
 *      E [ 0  0  1  1  0 ]
 *
 *  Undirected graph -> the matrix is SYMMETRIC (matrix[i][j] == matrix[j][i]).
 *
 *  MATRIX vs LIST - when to choose which
 *  -------------------------------------
 *   | Operation          | Matrix   | List       |
 *   |--------------------|----------|------------|
 *   | Space              | O(V^2)   | O(V + E)   |
 *   | Add edge           | O(1)     | O(1)       |
 *   | Edge exists (u,v)? | O(1)     | O(deg u)   |
 *   | All neighbours of u| O(V)     | O(deg u)   |
 *   | BFS / DFS          | O(V^2)   | O(V + E)   |
 *   Matrix wins for DENSE graphs and constant-time edge lookups (e.g.
 *   Floyd-Warshall). List wins for SPARSE graphs (most real networks).
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Small dense networks: flight connections between hub cities
 *   - Image/grid adjacency, game boards, distance tables
 *   - Metro maps where "is there a direct line?" must be O(1)
 *
 *  ALGORITHMS IN THIS FILE
 *  -----------------------
 *   BFS (queue, level by level), DFS (stack/recursion, go deep first),
 *   BFS single-source shortest path for UNWEIGHTED graphs, path existence,
 *   connected components. All O(V^2) with a matrix.
 *
 *  HOW TO RUN
 *  ----------
 *   javac GraphAdjacencyMatrix.java && java GraphAdjacencyMatrix
 * ============================================================================
 */

import java.util.*;

public class GraphAdjacencyMatrix {

    static class MatrixGraph {
        final List<String> names = new ArrayList<>();      // index -> vertex name
        final Map<String, Integer> index = new HashMap<>(); // vertex name -> index
        int[][] adj;
        final boolean directed;

        MatrixGraph(boolean directed, String... vertices) {
            this.directed = directed;
            for (String v : vertices) { index.put(v, names.size()); names.add(v); }
            adj = new int[vertices.length][vertices.length];  // O(V^2) space up front
        }

        int id(String v) {
            Integer i = index.get(v);
            if (i == null) throw new IllegalArgumentException("unknown vertex " + v);
            return i;
        }

        /** Adding a vertex means growing the matrix - O(V^2). (A list does this in O(1).) */
        void addVertex(String v) {
            index.put(v, names.size()); names.add(v);
            int n = names.size();
            int[][] bigger = new int[n][n];
            for (int i = 0; i < n - 1; i++) System.arraycopy(adj[i], 0, bigger[i], 0, n - 1);
            adj = bigger;
        }

        void addEdge(String u, String v) {
            adj[id(u)][id(v)] = 1;
            if (!directed) adj[id(v)][id(u)] = 1;    // mirror for undirected
        }

        void removeEdge(String u, String v) {
            adj[id(u)][id(v)] = 0;
            if (!directed) adj[id(v)][id(u)] = 0;
        }

        boolean hasEdge(String u, String v) { return adj[id(u)][id(v)] != 0; }   // O(1)!

        /** Neighbours require scanning a whole row - O(V). */
        List<Integer> neighbours(int u) {
            List<Integer> out = new ArrayList<>();
            for (int v = 0; v < adj.length; v++) if (adj[u][v] != 0) out.add(v);
            return out;
        }

        int degree(String v) { return neighbours(id(v)).size(); }

        void print() {
            StringBuilder sb = new StringBuilder("       ");
            for (String n : names) sb.append(String.format("%-4s", n.length() > 3 ? n.substring(0, 3) : n));
            System.out.println(sb);
            for (int i = 0; i < adj.length; i++) {
                sb = new StringBuilder(String.format("  %-4s[", names.get(i).length() > 3 ? names.get(i).substring(0, 3) : names.get(i)));
                for (int j = 0; j < adj.length; j++) sb.append(String.format(" %d  ", adj[i][j]));
                System.out.println(sb.append("]"));
            }
        }

        // ---------------- BFS ----------------
        /**
         * Breadth First Search: explore all neighbours at distance 1, then 2...
         * We mark a vertex visited WHEN ENQUEUED (not when dequeued) so it is
         * never added to the queue twice.
         */
        List<String> bfs(String start) {
            List<String> order = new ArrayList<>();
            boolean[] visited = new boolean[adj.length];
            Queue<Integer> q = new ArrayDeque<>();
            q.add(id(start));
            visited[id(start)] = true;
            while (!q.isEmpty()) {
                int u = q.poll();
                order.add(names.get(u));
                for (int v : neighbours(u)) {          // O(V) row scan per vertex -> O(V^2)
                    if (!visited[v]) { visited[v] = true; q.add(v); }
                }
            }
            return order;
        }

        // ---------------- DFS (iterative) ----------------
        List<String> dfs(String start) {
            List<String> order = new ArrayList<>();
            boolean[] visited = new boolean[adj.length];
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(id(start));
            while (!stack.isEmpty()) {
                int u = stack.pop();
                if (visited[u]) continue;              // may have been pushed twice
                visited[u] = true;
                order.add(names.get(u));
                List<Integer> nb = neighbours(u);
                // push in reverse so the smallest-index neighbour is explored first
                for (int i = nb.size() - 1; i >= 0; i--) if (!visited[nb.get(i)]) stack.push(nb.get(i));
            }
            return order;
        }

        // ---------------- DFS (recursive) ----------------
        void dfsRecursive(int u, boolean[] visited, List<String> order) {
            visited[u] = true;
            order.add(names.get(u));
            for (int v : neighbours(u)) if (!visited[v]) dfsRecursive(v, visited, order);
        }

        /**
         * BFS shortest path in an UNWEIGHTED graph.
         * The first time BFS reaches a vertex is via the fewest edges, so we
         * record parent[] and walk it backwards from the destination.
         */
        List<String> shortestPath(String src, String dst) {
            int s = id(src), d = id(dst);
            int[] parent = new int[adj.length];
            Arrays.fill(parent, -1);
            boolean[] visited = new boolean[adj.length];
            Queue<Integer> q = new ArrayDeque<>();
            q.add(s); visited[s] = true;
            while (!q.isEmpty()) {
                int u = q.poll();
                if (u == d) break;                     // early exit
                for (int v : neighbours(u)) if (!visited[v]) { visited[v] = true; parent[v] = u; q.add(v); }
            }
            if (!visited[d]) return Collections.emptyList();
            LinkedList<String> path = new LinkedList<>();
            for (int at = d; at != -1; at = parent[at]) path.addFirst(names.get(at));
            return path;
        }

        /** Connected components (undirected): run BFS from every unvisited vertex. */
        List<List<String>> components() {
            boolean[] seen = new boolean[adj.length];
            List<List<String>> comps = new ArrayList<>();
            for (int i = 0; i < adj.length; i++) {
                if (seen[i]) continue;
                List<String> comp = new ArrayList<>();
                dfsRecursive(i, seen, comp);
                comps.add(comp);
            }
            return comps;
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Metro rail network - fewest-stops route & line closures
    // ------------------------------------------------------------------------
    static void metroScenario() {
        System.out.println("\n=== SCENARIO: City Metro - Fewest Stops Route (BFS on matrix) ===");
        MatrixGraph metro = new MatrixGraph(false,
                "Airport", "Guindy", "Saidapet", "Teynampet", "Central", "Egmore",
                "Vadapalani", "Koyambedu", "AnnaNagar", "Thirumangalam");
        String[][] lines = {
                {"Airport", "Guindy"}, {"Guindy", "Saidapet"}, {"Saidapet", "Teynampet"},
                {"Teynampet", "Central"}, {"Guindy", "Vadapalani"}, {"Vadapalani", "Koyambedu"},
                {"Koyambedu", "Thirumangalam"}, {"Thirumangalam", "AnnaNagar"}, {"AnnaNagar", "Egmore"},
                {"Egmore", "Central"}};
        for (String[] e : lines) metro.addEdge(e[0], e[1]);

        System.out.println("  Direct link Guindy-Vadapalani? " + metro.hasEdge("Guindy", "Vadapalani") + "  (O(1) matrix lookup)");
        System.out.println("  Interchange stations (degree >= 3): ");
        for (String s : metro.names) if (metro.degree(s) >= 3) System.out.println("    " + s + " (" + metro.degree(s) + " links)");

        List<String> route = metro.shortestPath("Airport", "Egmore");
        System.out.println("  Airport -> Egmore : " + String.join(" -> ", route) + "  [" + (route.size() - 1) + " stops]");

        System.out.println("  !! Maintenance: Teynampet-Central closed");
        metro.removeEdge("Teynampet", "Central");
        route = metro.shortestPath("Airport", "Egmore");
        System.out.println("  Airport -> Egmore : " + String.join(" -> ", route) + "  [" + (route.size() - 1) + " stops]");

        System.out.println("  !! Maintenance: Koyambedu-Thirumangalam ALSO closed");
        metro.removeEdge("Koyambedu", "Thirumangalam");
        route = metro.shortestPath("Airport", "Egmore");
        System.out.println("  Airport -> Egmore : " + (route.isEmpty() ? "NO ROUTE - network split" : String.join(" -> ", route)));
        System.out.println("  Network islands   : " + metro.components());
    }

    public static void main(String[] args) {
        System.out.println("=== ADJACENCY MATRIX GRAPH (undirected) ===");
        MatrixGraph g = new MatrixGraph(false, "A", "B", "C", "D", "E");
        g.addEdge("A", "B"); g.addEdge("A", "C"); g.addEdge("B", "D");
        g.addEdge("C", "E"); g.addEdge("D", "E");
        g.print();
        System.out.println("  BFS from A          : " + g.bfs("A"));
        System.out.println("  DFS from A (iter)   : " + g.dfs("A"));
        List<String> rec = new ArrayList<>();
        g.dfsRecursive(g.id("A"), new boolean[5], rec);
        System.out.println("  DFS from A (recur)  : " + rec);
        System.out.println("  shortest A -> E     : " + g.shortestPath("A", "E"));
        g.addVertex("F");
        g.addEdge("E", "F");
        System.out.println("  added F, edge E-F -> BFS from A: " + g.bfs("A"));

        System.out.println("\n=== ADJACENCY MATRIX GRAPH (directed) ===");
        MatrixGraph dg = new MatrixGraph(true, "A", "B", "C", "D");
        dg.addEdge("A", "B"); dg.addEdge("B", "C"); dg.addEdge("C", "A"); dg.addEdge("C", "D");
        dg.print();
        System.out.println("  A->B? " + dg.hasEdge("A", "B") + "  B->A? " + dg.hasEdge("B", "A") + "  (asymmetric)");
        System.out.println("  BFS from B: " + dg.bfs("B") + "   BFS from D: " + dg.bfs("D"));

        metroScenario();
    }
}
