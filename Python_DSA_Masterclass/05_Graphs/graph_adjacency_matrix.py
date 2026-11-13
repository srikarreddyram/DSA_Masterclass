"""
============================================================================
 MODULE 05 : GRAPH - Adjacency MATRIX (2D array) implementation
============================================================================

 GRAPH = vertices + edges. Directed/undirected, weighted/unweighted,
 cyclic/acyclic (DAG), connected/disconnected.

 ADJACENCY MATRIX: V x V grid, matrix[i][j] = 1 (or weight) if edge i -> j.

         A  B  C  D  E
     A [ 0  1  1  0  0 ]        A --- B
     B [ 1  0  0  1  0 ]        |     |
     C [ 1  0  0  0  1 ]        C     D --- E
     D [ 0  1  0  0  1 ]         \_________/
     E [ 0  0  1  1  0 ]
 Undirected -> symmetric matrix.

 MATRIX vs LIST
  | Operation          | Matrix | List     |
  |--------------------|--------|----------|
  | Space              | O(V^2) | O(V+E)   |
  | Edge exists (u,v)? | O(1)   | O(deg u) |
  | Neighbours of u    | O(V)   | O(deg u) |
  | BFS / DFS          | O(V^2) | O(V+E)   |
  Matrix: dense graphs, O(1) edge checks, Floyd-Warshall.
  List  : sparse graphs (most real networks).

 ALGORITHMS: BFS, DFS (iterative + recursive), BFS shortest path
 (unweighted), connected components - all O(V^2) here.

 HOW TO RUN:  python graph_adjacency_matrix.py
============================================================================
"""
from collections import deque


class MatrixGraph:
    def __init__(self, vertices, directed: bool = False):
        self.names = list(vertices)
        self.index = {v: i for i, v in enumerate(self.names)}
        self.directed = directed
        n = len(self.names)
        self.adj = [[0] * n for _ in range(n)]   # O(V^2) space up front

    def id(self, v) -> int:
        return self.index[v]

    def add_vertex(self, v) -> None:
        """Growing a matrix is O(V^2) (a list does it in O(1))."""
        self.index[v] = len(self.names)
        self.names.append(v)
        for row in self.adj:
            row.append(0)
        self.adj.append([0] * len(self.names))

    def add_edge(self, u, v) -> None:
        self.adj[self.id(u)][self.id(v)] = 1
        if not self.directed:
            self.adj[self.id(v)][self.id(u)] = 1  # mirror

    def remove_edge(self, u, v) -> None:
        self.adj[self.id(u)][self.id(v)] = 0
        if not self.directed:
            self.adj[self.id(v)][self.id(u)] = 0

    def has_edge(self, u, v) -> bool:
        return self.adj[self.id(u)][self.id(v)] != 0   # O(1)!

    def neighbours(self, u: int):
        return [v for v, w in enumerate(self.adj[u]) if w]   # O(V) row scan

    def degree(self, v) -> int:
        return len(self.neighbours(self.id(v)))

    def print(self) -> None:
        short = [n[:3] for n in self.names]
        print("       " + "".join(f"{s:<4}" for s in short))
        for i, row in enumerate(self.adj):
            print(f"  {short[i]:<4}[" + "".join(f" {x}  " for x in row) + "]")

    def bfs(self, start):
        """Mark visited WHEN ENQUEUED so a vertex never enters the queue twice."""
        visited = [False] * len(self.adj)
        s = self.id(start)
        q, order = deque([s]), []
        visited[s] = True
        while q:
            u = q.popleft()
            order.append(self.names[u])
            for v in self.neighbours(u):
                if not visited[v]:
                    visited[v] = True
                    q.append(v)
        return order

    def dfs(self, start):
        visited, stack, order = [False] * len(self.adj), [self.id(start)], []
        while stack:
            u = stack.pop()
            if visited[u]:
                continue                         # may have been pushed twice
            visited[u] = True
            order.append(self.names[u])
            for v in reversed(self.neighbours(u)):   # reverse -> smallest first
                if not visited[v]:
                    stack.append(v)
        return order

    def dfs_recursive(self, u: int, visited, order):
        visited[u] = True
        order.append(self.names[u])
        for v in self.neighbours(u):
            if not visited[v]:
                self.dfs_recursive(v, visited, order)
        return order

    def shortest_path(self, src, dst):
        """BFS reaches each vertex first via the fewest edges -> record parents."""
        s, d = self.id(src), self.id(dst)
        parent = [-1] * len(self.adj)
        visited = [False] * len(self.adj)
        q = deque([s]); visited[s] = True
        while q:
            u = q.popleft()
            if u == d:
                break
            for v in self.neighbours(u):
                if not visited[v]:
                    visited[v], parent[v] = True, u
                    q.append(v)
        if not visited[d]:
            return []
        path, at = [], d
        while at != -1:
            path.append(self.names[at]); at = parent[at]
        return path[::-1]

    def components(self):
        seen, comps = [False] * len(self.adj), []
        for i in range(len(self.adj)):
            if not seen[i]:
                comps.append(self.dfs_recursive(i, seen, []))
        return comps


def metro_scenario() -> None:
    print("\n=== SCENARIO: City Metro - Fewest Stops Route (BFS on matrix) ===")
    metro = MatrixGraph(["Airport", "Guindy", "Saidapet", "Teynampet", "Central", "Egmore",
                         "Vadapalani", "Koyambedu", "AnnaNagar", "Thirumangalam"])
    for u, v in [("Airport", "Guindy"), ("Guindy", "Saidapet"), ("Saidapet", "Teynampet"),
                 ("Teynampet", "Central"), ("Guindy", "Vadapalani"), ("Vadapalani", "Koyambedu"),
                 ("Koyambedu", "Thirumangalam"), ("Thirumangalam", "AnnaNagar"),
                 ("AnnaNagar", "Egmore"), ("Egmore", "Central")]:
        metro.add_edge(u, v)
    print(f"  Direct link Guindy-Vadapalani? {metro.has_edge('Guindy', 'Vadapalani')}  (O(1) matrix lookup)")
    print("  Interchange stations (degree >= 3):")
    for s in metro.names:
        if metro.degree(s) >= 3:
            print(f"    {s} ({metro.degree(s)} links)")

    def show():
        r = metro.shortest_path("Airport", "Egmore")
        print("  Airport -> Egmore : " + (" -> ".join(r) + f"  [{len(r) - 1} stops]" if r else "NO ROUTE - network split"))

    show()
    print("  !! Maintenance: Teynampet-Central closed")
    metro.remove_edge("Teynampet", "Central")
    show()
    print("  !! Maintenance: Koyambedu-Thirumangalam ALSO closed")
    metro.remove_edge("Koyambedu", "Thirumangalam")
    show()
    print(f"  Network islands   : {metro.components()}")


def main() -> None:
    print("=== ADJACENCY MATRIX GRAPH (undirected) ===")
    g = MatrixGraph("ABCDE")
    for u, v in ["AB", "AC", "BD", "CE", "DE"]:
        g.add_edge(u, v)
    g.print()
    print(f"  BFS from A          : {g.bfs('A')}")
    print(f"  DFS from A (iter)   : {g.dfs('A')}")
    print(f"  DFS from A (recur)  : {g.dfs_recursive(0, [False] * 5, [])}")
    print(f"  shortest A -> E     : {g.shortest_path('A', 'E')}")
    g.add_vertex("F"); g.add_edge("E", "F")
    print(f"  added F, edge E-F -> BFS from A: {g.bfs('A')}")

    print("\n=== ADJACENCY MATRIX GRAPH (directed) ===")
    dg = MatrixGraph("ABCD", directed=True)
    for u, v in ["AB", "BC", "CA", "CD"]:
        dg.add_edge(u, v)
    dg.print()
    print(f"  A->B? {dg.has_edge('A', 'B')}  B->A? {dg.has_edge('B', 'A')}  (asymmetric)")
    print(f"  BFS from B: {dg.bfs('B')}   BFS from D: {dg.bfs('D')}")
    metro_scenario()


if __name__ == "__main__":
    main()
