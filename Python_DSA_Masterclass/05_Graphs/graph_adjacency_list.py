"""
============================================================================
 MODULE 05 : GRAPH - Adjacency LIST (Node / reference-based) implementation
============================================================================

 Each vertex is a GraphNode object holding a list of REFERENCES to its
 neighbour nodes. Memory is proportional to the edges that exist.

    A : [B, C]          A --- B
    B : [A, D]          |     |
    C : [A, E]          C     D --- E
    D : [B, E]           \_________/
    E : [C, D]

 Space O(V + E). A billion-user social graph is impossible as a matrix
 (10^18 cells) but fine as lists.

 ALGORITHMS (all O(V + E))
  - BFS / DFS
  - BFS shortest path (unweighted) -> degrees of separation
  - Topological sort: DFS-based and Kahn's (BFS, with cycle detection)
  - Friend recommendation: friends-of-friends ranked by mutual friends

 USE CASES: social networks, web crawling, build/dependency systems,
 recommendation engines, road networks.

 COMPLEXITY: add vertex O(1) | add edge O(1) | remove edge O(deg)
             BFS/DFS/topo O(V+E) | Space O(V+E)

 HOW TO RUN:  python graph_adjacency_list.py
============================================================================
"""
from collections import deque


class GraphNode:
    def __init__(self, name: str):
        self.name = name
        self.neighbours = []                     # references to other GraphNodes

    def __repr__(self) -> str:
        return self.name


class ListGraph:
    def __init__(self, directed: bool = False):
        self.nodes = {}                          # dicts keep insertion order
        self.directed = directed

    def add_vertex(self, name) -> GraphNode:
        if name not in self.nodes:
            self.nodes[name] = GraphNode(name)   # O(1)
        return self.nodes[name]

    def add_edge(self, u, v) -> None:
        a, b = self.add_vertex(u), self.add_vertex(v)
        a.neighbours.append(b)
        if not self.directed:
            b.neighbours.append(a)

    def remove_edge(self, u, v) -> None:
        a, b = self.nodes[u], self.nodes[v]
        a.neighbours.remove(b)
        if not self.directed:
            b.neighbours.remove(a)

    def print(self) -> None:
        for n in self.nodes.values():
            print(f"  {n.name} : {n.neighbours}")

    def bfs(self, start):
        s = self.nodes[start]
        visited, q, order = {s}, deque([s]), []
        while q:
            u = q.popleft()
            order.append(u.name)
            for v in u.neighbours:               # only real edges: O(deg)
                if v not in visited:
                    visited.add(v)
                    q.append(v)
        return order

    def dfs(self, start):
        order, visited = [], set()

        def visit(u):
            visited.add(u)
            order.append(u.name)
            for v in u.neighbours:
                if v not in visited:
                    visit(v)

        visit(self.nodes[start])
        return order

    def bfs_distances(self, src: GraphNode):
        dist, q = {src: 0}, deque([src])          # dict keeps discovery order
        while q:
            u = q.popleft()
            for v in u.neighbours:
                if v not in dist:
                    dist[v] = dist[u] + 1
                    q.append(v)
        return dist

    def shortest_path(self, src, dst):
        s, d = self.nodes[src], self.nodes[dst]
        parent, q = {s: None}, deque([s])
        while q and d not in parent:
            u = q.popleft()
            for v in u.neighbours:
                if v not in parent:
                    parent[v] = u
                    q.append(v)
        if d not in parent:
            return []
        path, at = [], d
        while at:
            path.append(at.name); at = parent[at]
        return path[::-1]

    def topological_sort_dfs(self):
        """Post-order push: a vertex goes on the stack after everything it
        leads to, so popping yields prerequisites first."""
        visited, stack = set(), []

        def visit(u):
            visited.add(u)
            for v in u.neighbours:
                if v not in visited:
                    visit(v)
            stack.append(u.name)

        for n in self.nodes.values():
            if n not in visited:
                visit(n)
        return stack[::-1]

    def kahn_topo_sort(self):
        """In-degree counting. Output shorter than V => a CYCLE exists."""
        indeg = {n: 0 for n in self.nodes.values()}
        for n in self.nodes.values():
            for v in n.neighbours:
                indeg[v] += 1
        ready = deque(n for n in self.nodes.values() if indeg[n] == 0)
        order = []
        while ready:
            u = ready.popleft()
            order.append(u.name)
            for v in u.neighbours:
                indeg[v] -= 1
                if indeg[v] == 0:
                    ready.append(v)
        return order if len(order) == len(self.nodes) else None


def social_network_scenario() -> None:
    print("\n=== SCENARIO 1: Social Network Friend Recommendation ===")
    net = ListGraph()
    for u, v in [("Srikar", "Himesh"), ("Srikar", "Krrish"), ("Srikar", "Sathvik"),
                 ("Himesh", "Krrish"), ("Himesh", "Ananya"), ("Krrish", "Ananya"),
                 ("Krrish", "Rahul"), ("Sathvik", "Praveen"), ("Praveen", "Rahul"),
                 ("Ananya", "Meera"), ("Rahul", "Meera"), ("Meera", "Zoya")]:
        net.add_edge(u, v)
    net.print()

    me = net.nodes["Srikar"]
    print("  Degrees of separation from Srikar:")
    for person, d in net.bfs_distances(me).items():
        if d > 0:
            print(f"    {person} : {d}")

    direct, mutual = set(me.neighbours), {}
    for friend in me.neighbours:
        for fof in friend.neighbours:
            if fof is not me and fof not in direct:
                mutual.setdefault(fof, []).append(friend.name)
    ranked = sorted(mutual.items(), key=lambda kv: (-len(kv[1]), kv[0].name))
    print("  People you may know:")
    for person, via in ranked:
        print(f"    {person.name:<8} {len(via)} mutual friend(s): {via}")
    print("  Intro chain Srikar -> Zoya: " + " -> ".join(net.shortest_path("Srikar", "Zoya")))


def course_schedule_scenario() -> None:
    print("\n=== SCENARIO 2: Course Prerequisite Planner (Topological Sort) ===")
    courses = ListGraph(directed=True)          # u -> v means "take u before v"
    for u, v in [("Programming", "DataStructures"), ("DiscreteMath", "DataStructures"),
                 ("DataStructures", "Algorithms"), ("DataStructures", "Databases"),
                 ("Algorithms", "MachineLearning"), ("LinearAlgebra", "MachineLearning"),
                 ("Databases", "BigData"), ("MachineLearning", "DeepLearning")]:
        courses.add_edge(u, v)
    print(f"  DFS topo order : {courses.topological_sort_dfs()}")
    print(f"  Kahn topo order: {courses.kahn_topo_sort()}")
    print("  Admin mistakenly adds DeepLearning -> Programming ...")
    courses.add_edge("DeepLearning", "Programming")
    plan = courses.kahn_topo_sort()
    print(f"  Kahn result    : {plan if plan else 'CYCLE DETECTED - no valid schedule exists'}")


def main() -> None:
    print("=== ADJACENCY LIST GRAPH (undirected) ===")
    g = ListGraph()
    for u, v in ["AB", "AC", "BD", "CE", "DE"]:
        g.add_edge(u, v)
    g.print()
    print(f"  BFS from A      : {g.bfs('A')}")
    print(f"  DFS from A      : {g.dfs('A')}")
    print(f"  shortest A -> E : {g.shortest_path('A', 'E')}")
    g.remove_edge("C", "E")
    print(f"  removed C-E -> shortest A -> E : {g.shortest_path('A', 'E')}")

    print("\n=== ADJACENCY LIST GRAPH (directed) ===")
    dg = ListGraph(directed=True)
    for u, v in ["AC", "BC", "CE", "BD", "DF", "EF", "FG"]:
        dg.add_edge(u, v)
    dg.print()
    print(f"  BFS from B : {dg.bfs('B')}")
    print(f"  topo (DFS) : {dg.topological_sort_dfs()}")
    social_network_scenario()
    course_schedule_scenario()


if __name__ == "__main__":
    main()
