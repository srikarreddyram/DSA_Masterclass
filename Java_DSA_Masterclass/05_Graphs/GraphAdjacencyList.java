/*
 * ============================================================================
 *  MODULE 05 : GRAPH - Adjacency LIST (Node / reference-based) implementation
 * ============================================================================
 *
 *  ADJACENCY LIST
 *  --------------
 *  Each vertex is a GraphNode object holding a LIST OF REFERENCES to its
 *  neighbour nodes. Memory is proportional to the edges that actually exist.
 *
 *     A : [B, C]          A --- B
 *     B : [A, D]          |     |
 *     C : [A, E]          C     D --- E
 *     D : [B, E]           \_________/
 *     E : [C, D]
 *
 *  Space O(V + E) instead of O(V^2): a social network with 1 billion users
 *  and ~200 friends each is impossible as a matrix (10^18 cells!) but fine
 *  as a list (~2*10^11 references).
 *
 *  ALGORITHMS IN THIS FILE  (all O(V + E))
 *  -----------------------
 *   - BFS / DFS traversal
 *   - BFS shortest path (unweighted) -> "degrees of separation"
 *   - Topological sort (DFS-based) on a DAG -> course scheduling
 *   - Kahn's algorithm (BFS-based topo sort) with cycle detection
 *   - Friend recommendation via "friends of friends" + mutual count
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Social networks (friend graphs), web crawling (page links)
 *   - Build systems / package managers (dependency DAGs, topo order)
 *   - Road networks, recommendation engines, knowledge graphs
 *
 *  TIME / SPACE COMPLEXITY
 *  -----------------------
 *   | Add vertex O(1) | Add edge O(1) | Remove edge O(deg) | Edge check O(deg) |
 *   | BFS/DFS O(V+E)  | Topo sort O(V+E) | Space O(V+E)                         |
 *
 *  HOW TO RUN
 *  ----------
 *   javac GraphAdjacencyList.java && java GraphAdjacencyList
 * ============================================================================
 */

import java.util.*;

public class GraphAdjacencyList {

    /** A vertex that directly references its neighbours (true node-based graph). */
    static class GraphNode {
        final String name;
        final List<GraphNode> neighbours = new ArrayList<>();
        GraphNode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    static class ListGraph {
        final Map<String, GraphNode> nodes = new LinkedHashMap<>();   // keeps insertion order
        final boolean directed;

        ListGraph(boolean directed) { this.directed = directed; }

        GraphNode addVertex(String name) { return nodes.computeIfAbsent(name, GraphNode::new); } // O(1)

        void addEdge(String u, String v) {
            GraphNode a = addVertex(u), b = addVertex(v);
            a.neighbours.add(b);
            if (!directed) b.neighbours.add(a);
        }

        void removeEdge(String u, String v) {
            GraphNode a = nodes.get(u), b = nodes.get(v);
            a.neighbours.remove(b);
            if (!directed) b.neighbours.remove(a);
        }

        void print() {
            for (GraphNode n : nodes.values()) System.out.println("  " + n.name + " : " + n.neighbours);
        }

        // ---------------- BFS ----------------
        List<String> bfs(String start) {
            List<String> order = new ArrayList<>();
            Set<GraphNode> visited = new HashSet<>();
            Queue<GraphNode> q = new ArrayDeque<>();
            GraphNode s = nodes.get(start);
            q.add(s); visited.add(s);
            while (!q.isEmpty()) {
                GraphNode u = q.poll();
                order.add(u.name);
                for (GraphNode v : u.neighbours)        // only REAL edges: O(deg) not O(V)
                    if (visited.add(v)) q.add(v);       // Set.add returns false if present
            }
            return order;
        }

        // ---------------- DFS (recursive) ----------------
        List<String> dfs(String start) {
            List<String> order = new ArrayList<>();
            dfs(nodes.get(start), new HashSet<>(), order);
            return order;
        }

        private void dfs(GraphNode u, Set<GraphNode> visited, List<String> order) {
            visited.add(u);
            order.add(u.name);
            for (GraphNode v : u.neighbours) if (!visited.contains(v)) dfs(v, visited, order);
        }

        // ---------------- BFS distances (single source, unweighted) ----------------
        Map<GraphNode, Integer> bfsDistances(GraphNode src) {
            Map<GraphNode, Integer> dist = new LinkedHashMap<>();   // keeps BFS discovery order
            Queue<GraphNode> q = new ArrayDeque<>();
            dist.put(src, 0); q.add(src);
            while (!q.isEmpty()) {
                GraphNode u = q.poll();
                for (GraphNode v : u.neighbours)
                    if (!dist.containsKey(v)) { dist.put(v, dist.get(u) + 1); q.add(v); }
            }
            return dist;
        }

        List<String> shortestPath(String src, String dst) {
            Map<GraphNode, GraphNode> parent = new HashMap<>();
            GraphNode s = nodes.get(src), d = nodes.get(dst);
            Queue<GraphNode> q = new ArrayDeque<>();
            parent.put(s, null); q.add(s);
            while (!q.isEmpty() && !parent.containsKey(d)) {
                GraphNode u = q.poll();
                for (GraphNode v : u.neighbours) if (!parent.containsKey(v)) { parent.put(v, u); q.add(v); }
            }
            if (!parent.containsKey(d)) return Collections.emptyList();
            LinkedList<String> path = new LinkedList<>();
            for (GraphNode at = d; at != null; at = parent.get(at)) path.addFirst(at.name);
            return path;
        }

        // ---------------- Topological sort (DFS) ----------------
        /**
         * For a DAG, a vertex is pushed onto the stack only AFTER all vertices
         * reachable from it are pushed (post-order). Popping the stack therefore
         * yields every vertex before the vertices that depend on it.
         */
        List<String> topologicalSortDFS() {
            Deque<String> stack = new ArrayDeque<>();
            Set<GraphNode> visited = new HashSet<>();
            for (GraphNode n : nodes.values()) if (!visited.contains(n)) topoVisit(n, visited, stack);
            return new ArrayList<>(stack);             // ArrayDeque iterates from top
        }

        private void topoVisit(GraphNode u, Set<GraphNode> visited, Deque<String> stack) {
            visited.add(u);
            for (GraphNode v : u.neighbours) if (!visited.contains(v)) topoVisit(v, visited, stack);
            stack.push(u.name);                        // post-order push
        }

        /**
         * Kahn's algorithm (BFS topological sort):
         *  1) count incoming edges (in-degree) for every vertex
         *  2) start with all vertices that have in-degree 0
         *  3) "remove" a vertex: decrement its neighbours' in-degree; any that hit
         *     0 become available.
         * If we output fewer than V vertices, the remainder are stuck in a CYCLE.
         */
        List<String> kahnTopoSort() {
            Map<GraphNode, Integer> indeg = new HashMap<>();
            for (GraphNode n : nodes.values()) indeg.putIfAbsent(n, 0);
            for (GraphNode n : nodes.values()) for (GraphNode v : n.neighbours) indeg.merge(v, 1, Integer::sum);
            Queue<GraphNode> ready = new ArrayDeque<>();
            for (GraphNode n : nodes.values()) if (indeg.get(n) == 0) ready.add(n);
            List<String> order = new ArrayList<>();
            while (!ready.isEmpty()) {
                GraphNode u = ready.poll();
                order.add(u.name);
                for (GraphNode v : u.neighbours) if (indeg.merge(v, -1, Integer::sum) == 0) ready.add(v);
            }
            if (order.size() < nodes.size()) return null;   // cycle detected
            return order;
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 1: Social Network - degrees of separation & friend suggestions
    // ------------------------------------------------------------------------
    static void socialNetworkScenario() {
        System.out.println("\n=== SCENARIO 1: Social Network Friend Recommendation ===");
        ListGraph net = new ListGraph(false);
        String[][] friendships = {
                {"Srikar", "Himesh"}, {"Srikar", "Krrish"}, {"Srikar", "Sathvik"},
                {"Himesh", "Krrish"}, {"Himesh", "Ananya"}, {"Krrish", "Ananya"},
                {"Krrish", "Rahul"}, {"Sathvik", "Praveen"}, {"Praveen", "Rahul"},
                {"Ananya", "Meera"}, {"Rahul", "Meera"}, {"Meera", "Zoya"}};
        for (String[] f : friendships) net.addEdge(f[0], f[1]);
        net.print();

        GraphNode me = net.nodes.get("Srikar");
        Map<GraphNode, Integer> dist = net.bfsDistances(me);
        System.out.println("  Degrees of separation from Srikar:");
        for (Map.Entry<GraphNode, Integer> e : dist.entrySet())
            if (e.getValue() > 0) System.out.println("    " + e.getKey() + " : " + e.getValue());

        // Friends-of-friends who aren't already friends, ranked by mutual friends.
        Map<GraphNode, List<String>> mutual = new HashMap<>();
        Set<GraphNode> direct = new HashSet<>(me.neighbours);
        for (GraphNode friend : me.neighbours)
            for (GraphNode fof : friend.neighbours)
                if (fof != me && !direct.contains(fof))
                    mutual.computeIfAbsent(fof, k -> new ArrayList<>()).add(friend.name);

        List<Map.Entry<GraphNode, List<String>>> ranked = new ArrayList<>(mutual.entrySet());
        ranked.sort((a, b) -> b.getValue().size() != a.getValue().size()
                ? b.getValue().size() - a.getValue().size()
                : a.getKey().name.compareTo(b.getKey().name));
        System.out.println("  People you may know:");
        for (Map.Entry<GraphNode, List<String>> e : ranked)
            System.out.printf("    %-8s %d mutual friend(s): %s%n", e.getKey(), e.getValue().size(), e.getValue());

        System.out.println("  Intro chain Srikar -> Zoya: " + String.join(" -> ", net.shortestPath("Srikar", "Zoya")));
    }

    // ------------------------------------------------------------------------
    //  SCENARIO 2: University course scheduling (topological sort on a DAG)
    // ------------------------------------------------------------------------
    static void courseScheduleScenario() {
        System.out.println("\n=== SCENARIO 2: Course Prerequisite Planner (Topological Sort) ===");
        ListGraph courses = new ListGraph(true);       // edge u -> v means "u before v"
        String[][] prereqs = {
                {"Programming", "DataStructures"}, {"DiscreteMath", "DataStructures"},
                {"DataStructures", "Algorithms"}, {"DataStructures", "Databases"},
                {"Algorithms", "MachineLearning"}, {"LinearAlgebra", "MachineLearning"},
                {"Databases", "BigData"}, {"MachineLearning", "DeepLearning"}};
        for (String[] p : prereqs) courses.addEdge(p[0], p[1]);
        System.out.println("  DFS topo order : " + courses.topologicalSortDFS());
        System.out.println("  Kahn topo order: " + courses.kahnTopoSort());

        System.out.println("  Admin mistakenly adds DeepLearning -> Programming ...");
        courses.addEdge("DeepLearning", "Programming");
        List<String> plan = courses.kahnTopoSort();
        System.out.println("  Kahn result    : " + (plan == null ? "CYCLE DETECTED - no valid schedule exists" : plan));
    }

    public static void main(String[] args) {
        System.out.println("=== ADJACENCY LIST GRAPH (undirected) ===");
        ListGraph g = new ListGraph(false);
        g.addEdge("A", "B"); g.addEdge("A", "C"); g.addEdge("B", "D");
        g.addEdge("C", "E"); g.addEdge("D", "E");
        g.print();
        System.out.println("  BFS from A      : " + g.bfs("A"));
        System.out.println("  DFS from A      : " + g.dfs("A"));
        System.out.println("  shortest A -> E : " + g.shortestPath("A", "E"));
        g.removeEdge("C", "E");
        System.out.println("  removed C-E -> shortest A -> E : " + g.shortestPath("A", "E"));

        System.out.println("\n=== ADJACENCY LIST GRAPH (directed) ===");
        ListGraph dg = new ListGraph(true);
        dg.addEdge("A", "C"); dg.addEdge("B", "C"); dg.addEdge("C", "E");
        dg.addEdge("B", "D"); dg.addEdge("D", "F"); dg.addEdge("E", "F"); dg.addEdge("F", "G");
        dg.print();
        System.out.println("  BFS from B : " + dg.bfs("B"));
        System.out.println("  topo (DFS) : " + dg.topologicalSortDFS());

        socialNetworkScenario();
        courseScheduleScenario();
    }
}
