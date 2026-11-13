# Java DSA Masterclass

Every `.java` file is a single self-contained program (helper classes are
nested), so files never clash and each one runs on its own.

```bash
cd 04_Trees
javac AVLTree.java && java AVLTree      # classic
java AVLTree.java                       # Java 11+ single-file launcher
```

Run everything (bash):

```bash
for f in */*.java; do d=$(dirname "$f"); c=$(basename "$f" .java); \
  (cd "$d" && javac "$c.java" && echo "=== $c ===" && java "$c"); done
```

Each module folder also contains `<folder>_Notes.pdf` with illustrated theory notes.

| Module | Topics | Scenarios | Files |
|---|---|---|---|
| `01_Arrays` | Standard Arrays (1D/2D) & Dynamic Array (custom ArrayList) | Temperature log, cinema seat booking, shopping cart | DynamicArrayList.java, StandardArrays.java |
| `02_Linked_Lists` | Singly, Doubly, Circular Singly, Circular Doubly Linked Lists | Undo history, browser back/forward, round-robin CPU, Josephus, music playlist | CircularDoublyLinkedList.java, CircularSinglyLinkedList.java, DoublyLinkedList.java, SinglyLinkedList.java |
| `03_Stacks_Queues` | Stacks (array & node) and Queues (linear, circular, node) | Bracket validator, postfix calculator, undo/redo, stock span, bank tokens, sensor window, printer spooler, support tickets | CircularQueueArray.java, LinearQueueArray.java, QueueLinkedList.java, StackArray.java, StackLinkedList.java |
| `04_Trees` | Binary Tree (array & node), BST, AVL, Binary Heap | Expression tree, tournament bracket, price index, player registry, ER triage, trending hashtags | AVLTree.java, BinaryHeap.java, BinarySearchTree.java, BinaryTreeArray.java, BinaryTreeLinked.java |
| `05_Graphs` | Graphs (adjacency matrix & list), BFS/DFS, topo sort, Dijkstra | Metro fewest-stops, friend recommendation, course planner, city navigation | GraphAdjacencyList.java, GraphAdjacencyMatrix.java, WeightedGraphDijkstra.java |
| `06_Hashing` | Hash tables: separate chaining & open addressing | User login system, duplicate payment detector | HashTableChaining.java, HashTableOpenAddressing.java |
| `07_Sorting` | Bubble, Selection, Insertion, Bucket, Merge, Quick, Heap sort | E-commerce product catalogue (price, rating, stability) | SortingAlgorithms.java |
| `08_Searching` | Linear search, binary search, lower/upper bound | Library ISBN lookup over 200,000 books | SearchingAlgorithms.java |
| `09_Shortest_Path_MST` | Dijkstra, Bellman-Ford, Floyd-Warshall, Kruskal, Prim | Network routing delay optimisation, fibre backbone design | MinimumSpanningTree.java, ShortestPathAlgorithms.java |
| `10_Greedy` | Activity selection, coin change, fractional knapsack | Conference room booking, cash change, shipping truck cargo | GreedyAlgorithms.java |
| `11_Divide_Conquer` | Number factor, house robber, edit distance, LCS, LPS | Version diff, spell checker | DivideAndConquer.java |
| `12_Dynamic_Programming` | Memoization vs tabulation, 0/1 knapsack | Cloud server CPU/RAM allocation within budget | DynamicProgramming.java |
