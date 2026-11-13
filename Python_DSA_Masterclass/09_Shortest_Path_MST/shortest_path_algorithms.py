"""
============================================================================
 MODULE 09 : SHORTEST PATH ALGORITHMS
 Dijkstra  |  Bellman-Ford  |  Floyd-Warshall
============================================================================

 SSSP = single source -> all vertices.  APSP = all pairs.

 RELAXATION (shared by all three):
     if dist[u] + w(u,v) < dist[v]: dist[v] = dist[u] + w(u,v); parent[v] = u

 1) DIJKSTRA (greedy, SSSP, weights >= 0)
    Expand the closest unsettled vertex (min-heap); once settled, final.
    Time O((V+E) log V)  Space O(V)

 2) BELLMAN-FORD (SSSP, negative weights OK)
    Relax EVERY edge V-1 times (a simple shortest path has <= V-1 edges).
    If a V-th pass still improves something -> NEGATIVE CYCLE.
    Time O(V*E)  Space O(V)

 3) FLOYD-WARSHALL (APSP, negative edges OK, no negative cycles)
    for k: for i: for j: dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])
    After iteration k, paths may use vertices 0..k as stop-overs.
    dist[i][i] < 0 at the end => negative cycle.   Time O(V^3)  Space O(V^2)

 WHICH ONE?
  | Unweighted                          | BFS            |
  | Non-negative weights, one source    | Dijkstra       |
  | Negative weights / detect neg cycle | Bellman-Ford   |
  | All pairs, small/dense graph        | Floyd-Warshall |

 HOW TO RUN:  python shortest_path_algorithms.py
============================================================================
"""
import heapq

INF = float("inf")


class Graph:
    """Directed weighted graph: edge list + adjacency list, vertices by name."""

    def __init__(self):
        self.names, self.id = [], {}
        self.edges = []                          # (u, v, w)
        self.adj = []                            # adj[u] = [(v, w)]

    def vertex(self, name) -> int:
        if name not in self.id:
            self.id[name] = len(self.names)
            self.names.append(name)
            self.adj.append([])
        return self.id[name]

    def add_edge(self, a, b, w) -> None:
        u, v = self.vertex(a), self.vertex(b)
        self.edges.append((u, v, w))
        self.adj[u].append((v, w))

    def add_undirected(self, a, b, w) -> None:
        self.add_edge(a, b, w)
        self.add_edge(b, a, w)

    def __len__(self):
        return len(self.names)

    def path(self, parent, dst) -> str:
        out = []
        while dst != -1:
            out.append(self.names[dst]); dst = parent[dst]
        return " -> ".join(reversed(out))


def dijkstra(g: Graph, src: int):
    dist, parent = [INF] * len(g), [-1] * len(g)
    dist[src] = 0
    pq, settled = [(0, src)], [False] * len(g)
    while pq:
        d, u = heapq.heappop(pq)
        if settled[u]:
            continue                             # stale heap entry
        settled[u] = True                        # final - only valid if w >= 0
        for v, w in g.adj[u]:
            if d + w < dist[v]:                  # relax
                dist[v], parent[v] = d + w, u
                heapq.heappush(pq, (dist[v], v))
    return dist, parent


def bellman_ford(g: Graph, src: int):
    """Returns (dist, parent, cycle). cycle is a list of names if one exists."""
    n = len(g)
    dist, parent = [INF] * n, [-1] * n
    dist[src] = 0
    for _ in range(n - 1):
        changed = False
        for u, v, w in g.edges:
            if dist[u] != INF and dist[u] + w < dist[v]:
                dist[v], parent[v] = dist[u] + w, u
                changed = True
        if not changed:
            break                                # converged early
    for u, v, w in g.edges:                      # V-th pass: still improving?
        if dist[u] != INF and dist[u] + w < dist[v]:
            parent[v] = u
            x = v
            for _ in range(n):                   # step back n times to land inside the cycle
                x = parent[x]
            cyc, y = [g.names[x]], parent[x]
            while y != x:
                cyc.append(g.names[y]); y = parent[y]
            cyc.append(g.names[x])
            return None, parent, list(reversed(cyc))
    return dist, parent, None


def floyd_warshall(g: Graph):
    n = len(g)
    d = [[INF] * n for _ in range(n)]
    nxt = [[-1] * n for _ in range(n)]
    for i in range(n):
        d[i][i], nxt[i][i] = 0, i
    for u, v, w in g.edges:
        if w < d[u][v]:
            d[u][v], nxt[u][v] = w, v
    for k in range(n):                           # allow k as a stop-over
        for i in range(n):
            if d[i][k] == INF:
                continue
            for j in range(n):
                if d[i][k] + d[k][j] < d[i][j]:
                    d[i][j] = d[i][k] + d[k][j]
                    nxt[i][j] = nxt[i][k]        # first hop toward j = first hop toward k
    return d, nxt


def fw_path(g, nxt, u, v) -> str:
    if nxt[u][v] == -1:
        return "unreachable"
    out = [g.names[u]]
    while u != v:
        u = nxt[u][v]
        out.append(g.names[u])
    return " -> ".join(out)


def network_routing_scenario() -> None:
    print("\n=== SCENARIO: Network Routing Delay Optimisation ===")
    net = Graph()
    for a, b, w in [("Mumbai", "Pune", 4), ("Mumbai", "Ahmedabad", 9), ("Mumbai", "Bangalore", 18),
                    ("Pune", "Hyderabad", 11), ("Pune", "Bangalore", 14), ("Ahmedabad", "Delhi", 12),
                    ("Delhi", "Kolkata", 22), ("Hyderabad", "Chennai", 9), ("Bangalore", "Chennai", 6),
                    ("Hyderabad", "Kolkata", 20), ("Chennai", "Kolkata", 25), ("Delhi", "Hyderabad", 21)]:
        net.add_undirected(a, b, w)
    src = net.id["Mumbai"]
    dist, parent = dijkstra(net, src)
    print("  [Dijkstra] Routing table for router Mumbai:")
    print("    destination  latency  next-hop   full path")
    for v in range(len(net)):
        if v == src:
            continue
        hop = v
        while parent[hop] != src:                # walk back to the first hop
            hop = parent[hop]
        print(f"    {net.names[v]:<11} {dist[v]:5d} ms  {net.names[hop]:<9}  {net.path(parent, v)}")

    d, nxt = floyd_warshall(net)
    print("  [Floyd-Warshall] All-pairs latency matrix (ms):")
    print("              " + "".join(f" {n[:6]:>6}" for n in net.names))
    best_hub, best_worst = None, INF
    for i, row in enumerate(d):
        print(f"    {net.names[i]:<10}" + "".join(f" {x:6d}" for x in row))
        if max(row) < best_worst:
            best_worst, best_hub = max(row), i
    print(f"  -> Best location for a central DNS/CDN server (min worst-case latency): "
          f"{net.names[best_hub]} ({best_worst} ms to the farthest router)")
    dl, ch = net.id["Delhi"], net.id["Chennai"]
    print(f"     Delhi -> Chennai route: {fw_path(net, nxt, dl, ch)} ({d[dl][ch]} ms)")

    print("  [Bellman-Ford] Transit cost with peering rebates (negative edges):")
    cost = Graph()
    for a, b, w in [("Mumbai", "Pune", 5), ("Mumbai", "Ahmedabad", 12), ("Pune", "Hyderabad", 3),
                    ("Ahmedabad", "Hyderabad", -8), ("Hyderabad", "Chennai", 3), ("Pune", "Chennai", 10),
                    ("Chennai", "Kolkata", 7), ("Hyderabad", "Kolkata", -2)]:
        cost.add_edge(a, b, w)
    bf, par, _ = bellman_ford(cost, cost.id["Mumbai"])
    for v in range(len(cost)):
        print(f"    Mumbai -> {cost.names[v]:<10} cost {bf[v]:3d}  via {cost.path(par, v)}")
    dj, _ = dijkstra(cost, cost.id["Mumbai"])
    k = cost.id["Kolkata"]
    print(f"    Dijkstra on the same graph says Mumbai->Kolkata = {dj[k]}  <- WRONG: Hyderabad was "
          f"settled before the -8 rebate was discovered ({bf[k]} is correct)")
    print("  !! New link Kolkata -> Ahmedabad (cost 4) closes a loop with total cost 4 - 8 - 2 = -6:")
    cost.add_edge("Kolkata", "Ahmedabad", 4)
    _, _, cycle = bellman_ford(cost, cost.id["Mumbai"])
    print("    Bellman-Ford: " + (f"NEGATIVE CYCLE detected: {' -> '.join(cycle)}" if cycle else "no cycle"))


def main() -> None:
    print("=== SHORTEST PATH ALGORITHMS ON ONE GRAPH ===")
    g = Graph()
    for a, b, w in [("A", "B", 6), ("A", "D", 1), ("D", "B", 2), ("D", "E", 1),
                    ("B", "E", 2), ("B", "C", 5), ("E", "C", 5)]:
        g.add_edge(a, b, w)
    d1, p1 = dijkstra(g, 0)
    d2, _, _ = bellman_ford(g, 0)
    d3, _ = floyd_warshall(g)
    print("  vertex  Dijkstra  BellmanFord  FloydWarshall  path")
    for v in range(len(g)):
        print(f"  {g.names[v]:<6}  {d1[v]:8d}  {d2[v]:11d}  {d3[0][v]:13d}  {g.path(p1, v)}")
    network_routing_scenario()


if __name__ == "__main__":
    main()
