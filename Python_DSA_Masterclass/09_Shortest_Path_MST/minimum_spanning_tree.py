"""
============================================================================
 MODULE 09 : MINIMUM SPANNING TREE - Disjoint Set, Kruskal, Prim
============================================================================

 SPANNING TREE: V-1 edges connecting every vertex with no cycle.
 MST: the spanning tree with the minimum total weight (undirected,
 connected, weighted graphs). MST != shortest-path tree: the MST minimises
 TOTAL cable, Dijkstra minimises distance from one source.

 DISJOINT SET (Union-Find)
   find(x)     -> representative of x's set
   union(a, b) -> merge two sets (False if already together = would be a cycle)
   Path compression + union by rank => ~O(alpha(n)), effectively constant.

 KRUSKAL: sort edges by weight; add each edge that joins two different sets.
          Time O(E log E)  Space O(V + E)   (great for sparse edge lists)
 PRIM   : grow one tree; repeatedly add the cheapest edge leaving it (heap).
          Time O(E log V)  Space O(V + E)   (great for dense graphs)

 USE CASES: fibre/power/water network design, clustering, TSP approximation.

 HOW TO RUN:  python minimum_spanning_tree.py
============================================================================
"""
import heapq


class DisjointSet:
    def __init__(self, n: int):
        self.parent = list(range(n))             # everyone starts alone
        self.rank = [0] * n
        self.sets = n

    def find(self, x: int) -> int:
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])   # PATH COMPRESSION
        return self.parent[x]

    def union(self, a: int, b: int) -> bool:
        ra, rb = self.find(a), self.find(b)
        if ra == rb:
            return False                         # same set -> cycle
        if self.rank[ra] < self.rank[rb]:
            ra, rb = rb, ra
        self.parent[rb] = ra                     # UNION BY RANK
        if self.rank[ra] == self.rank[rb]:
            self.rank[ra] += 1
        self.sets -= 1
        return True


class Network:
    def __init__(self, names):
        self.names = list(names)
        self.edges = []                          # (u, v, w)

    def add(self, a, b, w) -> None:
        self.edges.append((self.names.index(a), self.names.index(b), w))

    def fmt(self, e) -> str:
        u, v, w = e
        return f"{self.names[u]} - {self.names[v]} ({w})"


def kruskal(g: Network, log=None):
    ds, mst = DisjointSet(len(g.names)), []
    for e in sorted(g.edges, key=lambda e: e[2]):        # cheapest first
        if ds.union(e[0], e[1]):
            mst.append(e)
            if log is not None: log.append("take " + g.fmt(e))
        elif log is not None:
            log.append("skip " + g.fmt(e) + "  (would form a cycle)")
        if len(mst) == len(g.names) - 1:
            break                                        # V-1 edges -> done
    return mst


def prim(g: Network, start: int = 0):
    n = len(g.names)
    adj = [[] for _ in range(n)]
    for u, v, w in g.edges:
        adj[u].append((w, u, v))
        adj[v].append((w, v, u))
    in_tree = [False] * n
    in_tree[start] = True
    pq = list(adj[start])                                # frontier edges
    heapq.heapify(pq)
    mst = []
    while pq and len(mst) < n - 1:
        w, u, v = heapq.heappop(pq)                      # cheapest edge leaving the tree
        if in_tree[v]:
            continue
        in_tree[v] = True
        mst.append((u, v, w))
        for e in adj[v]:
            if not in_tree[e[2]]:
                heapq.heappush(pq, e)
    return mst


def total(edges) -> int:
    return sum(w for _, _, w in edges)


def fibre_scenario() -> None:
    print("\n=== SCENARIO: Fibre Backbone Between Data Centres (cost in Rs crore) ===")
    dc = Network(["Mumbai", "Pune", "Hyderabad", "Bangalore", "Chennai", "Delhi", "Kolkata"])
    for a, b, w in [("Mumbai", "Pune", 12), ("Mumbai", "Delhi", 95), ("Mumbai", "Hyderabad", 48),
                    ("Pune", "Hyderabad", 40), ("Pune", "Bangalore", 58), ("Hyderabad", "Bangalore", 39),
                    ("Hyderabad", "Chennai", 41), ("Bangalore", "Chennai", 22), ("Hyderabad", "Delhi", 101),
                    ("Delhi", "Kolkata", 98), ("Hyderabad", "Kolkata", 93), ("Chennai", "Kolkata", 105)]:
        dc.add(a, b, w)
    log = []
    k = kruskal(dc, log)
    print("  Kruskal's decisions:")
    for line in log:
        print("    " + line)
    p = prim(dc, dc.names.index("Delhi"))
    all_links = total(dc.edges)
    print(f"  Kruskal total = Rs {total(k)} cr | Prim (from Delhi) total = Rs {total(p)} cr")
    print(f"  Building every quoted link would cost Rs {all_links} cr -> saving Rs "
          f"{all_links - total(k)} cr with {len(k)} links")


def main() -> None:
    print("=== DISJOINT SET (UNION-FIND) ===")
    ds = DisjointSet(6)
    ds.union(0, 1); ds.union(2, 3); ds.union(1, 3)
    print(f"  after union(0,1), union(2,3), union(1,3): sets={ds.sets}")
    print(f"  find(0)==find(2)? {ds.find(0) == ds.find(2)} | find(4)==find(5)? {ds.find(4) == ds.find(5)}")
    print(f"  union(0,2) again -> {ds.union(0, 2)} (already connected => cycle)")

    print("\n=== KRUSKAL vs PRIM ===")
    g = Network("ABCDE")
    for a, b, w in [("A", "B", 15), ("A", "C", 20), ("B", "C", 13), ("B", "D", 5),
                    ("C", "D", 10), ("C", "E", 6), ("D", "E", 8)]:
        g.add(a, b, w)
    k, p = kruskal(g), prim(g, 0)
    print(f"  Kruskal: {', '.join(g.fmt(e) for e in k)}  total={total(k)}")
    print(f"  Prim   : {', '.join(g.fmt(e) for e in p)}  total={total(p)}")
    fibre_scenario()


if __name__ == "__main__":
    main()
