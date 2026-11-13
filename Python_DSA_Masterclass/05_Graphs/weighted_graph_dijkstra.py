"""
============================================================================
 MODULE 05 : WEIGHTED GRAPH + DIJKSTRA (dual implementation)
============================================================================

 WHY NOT BFS? BFS minimises the NUMBER of edges. With weights, more edges
 can be cheaper:   A --10-- C   vs   A --2-- B --3-- C

 DIJKSTRA (non-negative weights)
   dist[src] = 0, others = infinity
   repeat: take the unfinished vertex u with smallest dist (greedy),
           mark it final, RELAX each edge: dist[v] = min(dist[v], dist[u] + w)
 Greedy is safe because weights >= 0: any detour can only add cost.
 Negative edges break it -> Bellman-Ford (Module 09).

 TWO IMPLEMENTATIONS
  1) Adjacency MATRIX + linear min-scan     O(V^2)          dense graphs
  2) Adjacency LIST + binary heap (heapq)   O((V+E) log V)  sparse graphs

 USE CASES: GPS navigation, network routing (OSPF), flight cost search,
 game pathfinding.

 HOW TO RUN:  python weighted_graph_dijkstra.py
============================================================================
"""
import heapq
import itertools

INF = float("inf")


# ==========================================================================
#  VERSION 1: ADJACENCY MATRIX - O(V^2)
# ==========================================================================
class MatrixWeightedGraph:
    def __init__(self, names):
        self.names = list(names)
        n = len(self.names)
        self.w = [[0] * n for _ in range(n)]     # 0 = no edge

    def id(self, v): return self.names.index(v)

    def add_edge(self, u, v, weight) -> None:
        self.w[self.id(u)][self.id(v)] = self.w[self.id(v)][self.id(u)] = weight

    def dijkstra(self, src):
        n = len(self.names)
        dist, parent, done = [INF] * n, [-1] * n, [False] * n
        dist[self.id(src)] = 0
        for _ in range(n):
            # 1) linear scan for the closest unfinished vertex - O(V)
            u = min((i for i in range(n) if not done[i]), key=lambda i: dist[i], default=None)
            if u is None or dist[u] == INF:
                break
            done[u] = True
            # 2) relax by scanning the row - O(V)
            for v in range(n):
                if self.w[u][v] and not done[v] and dist[u] + self.w[u][v] < dist[v]:
                    dist[v] = dist[u] + self.w[u][v]
                    parent[v] = u
        return dist, parent


# ==========================================================================
#  VERSION 2: ADJACENCY LIST (node-based) + PRIORITY QUEUE
# ==========================================================================
class Vertex:
    def __init__(self, name):
        self.name = name
        self.edges = []                          # list of (Vertex, weight)
        self.dist, self.parent = INF, None

    def __repr__(self): return self.name


class ListWeightedGraph:
    def __init__(self):
        self.vertices = {}
        self.relaxations = self.stale_skips = 0

    def v(self, name) -> Vertex:
        if name not in self.vertices:
            self.vertices[name] = Vertex(name)
        return self.vertices[name]

    def add_edge(self, a, b, weight) -> None:   # undirected road
        self.v(a).edges.append((self.v(b), weight))
        self.v(b).edges.append((self.v(a), weight))

    def remove_edge(self, a, b) -> None:
        A, B = self.v(a), self.v(b)
        A.edges = [(x, w) for x, w in A.edges if x is not B]
        B.edges = [(x, w) for x, w in B.edges if x is not A]

    def dijkstra(self, src) -> None:
        for x in self.vertices.values():
            x.dist, x.parent = INF, None
        self.relaxations = self.stale_skips = 0
        s = self.v(src)
        s.dist = 0
        # heapq has no decrease-key, so we use LAZY DELETION: push a NEW entry
        # whenever a distance improves, and skip outdated entries when popped.
        # The counter breaks ties so Vertex objects are never compared.
        tie = itertools.count()
        pq = [(0, next(tie), s)]
        settled = set()
        while pq:
            d, _, u = heapq.heappop(pq)          # O(log V): closest frontier vertex
            if u in settled:
                self.stale_skips += 1            # stale entry -> ignore
                continue
            settled.add(u)
            for nb, w in u.edges:                # each edge relaxed once: O(E)
                if nb in settled:
                    continue
                cand = d + w
                if cand < nb.dist:               # RELAXATION
                    nb.dist, nb.parent = cand, u
                    heapq.heappush(pq, (cand, next(tie), nb))
                    self.relaxations += 1

    def path_to(self, dst):
        d = self.v(dst)
        if d.dist == INF:
            return []
        path = []
        while d:
            path.append(d.name); d = d.parent
        return path[::-1]


ROADS = [("SRM_KTR", "Tambaram", 18), ("SRM_KTR", "Vandalur", 12), ("Vandalur", "Tambaram", 10),
         ("Tambaram", "Airport", 15), ("Tambaram", "Velachery", 25), ("Airport", "Guindy", 12),
         ("Velachery", "Guindy", 10), ("Guindy", "T_Nagar", 14), ("Velachery", "OMR_Tidel", 15),
         ("OMR_Tidel", "Adyar", 12), ("Adyar", "Marina", 15), ("T_Nagar", "Marina", 20),
         ("T_Nagar", "Central", 22), ("Marina", "Central", 10), ("SRM_KTR", "OMR_Tidel", 45)]


def city_navigation_scenario() -> None:
    print("\n=== SCENARIO: Chennai City Navigation (Dijkstra) ===")
    city = ListWeightedGraph()
    for a, b, w in ROADS:
        city.add_edge(a, b, w)
    city.dijkstra("SRM_KTR")
    print("  Fastest travel times from SRM_KTR:")
    for x in city.vertices.values():
        print(f"    {x.name:<10} {x.dist:3d} min   via {' > '.join(city.path_to(x.name))}")
    print(f"  (heap work: {city.relaxations} relaxations, {city.stale_skips} stale entries skipped)")
    print("  !! Traffic alert: Airport-Guindy road blocked")
    city.remove_edge("Airport", "Guindy")
    city.dijkstra("SRM_KTR")
    print(f"  New route to Central: {' > '.join(city.path_to('Central'))} ({city.v('Central').dist} min)")


def main() -> None:
    edges = [("A", "B", 2), ("A", "C", 10), ("B", "C", 3), ("B", "D", 8),
             ("C", "D", 2), ("D", "E", 4), ("C", "E", 9), ("E", "F", 1)]

    print("=== DIJKSTRA - MATRIX VERSION O(V^2) ===")
    mg = MatrixWeightedGraph("ABCDEF")
    for e in edges:
        mg.add_edge(*e)
    dist, parent = mg.dijkstra("A")
    for i, name in enumerate(mg.names):
        path, at = [], i
        while at != -1:
            path.append(mg.names[at]); at = parent[at]
        print(f"  A -> {name} : {dist[i]:2d}  path {path[::-1]}")
    print("  note: A->C direct costs 10, but A->B->C costs 5 (BFS would pick the direct edge)")

    print("\n=== DIJKSTRA - ADJACENCY LIST + HEAP O((V+E) log V) ===")
    lg = ListWeightedGraph()
    for e in edges:
        lg.add_edge(*e)
    lg.dijkstra("A")
    for x in lg.vertices.values():
        print(f"  A -> {x.name} : {x.dist:2d}  path {lg.path_to(x.name)}")
    print(f"  both versions agree: {lg.v('F').dist == dist[5]}")
    city_navigation_scenario()


if __name__ == "__main__":
    main()
