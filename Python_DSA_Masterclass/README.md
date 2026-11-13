# Python DSA Masterclass

Every `.py` file is standalone (standard library only, Python 3.8+).

```bash
cd 04_Trees
python avl_tree.py
```

Run everything:

```bash
for f in */*.py; do echo "=== $f ==="; (cd "$(dirname "$f")" && python "$(basename "$f")"); done
```

Each module folder also contains `<folder>_Notes.pdf` with illustrated theory notes.

| Module | Topics | Scenarios | Files |
|---|---|---|---|
| `01_Arrays` | Standard Arrays (1D/2D) & Dynamic Array (custom ArrayList) | Temperature log, cinema seat booking, shopping cart | dynamic_array_list.py, standard_arrays.py |
| `02_Linked_Lists` | Singly, Doubly, Circular Singly, Circular Doubly Linked Lists | Undo history, browser back/forward, round-robin CPU, Josephus, music playlist | circular_doubly_linked_list.py, circular_singly_linked_list.py, doubly_linked_list.py, singly_linked_list.py |
| `03_Stacks_Queues` | Stacks (array & node) and Queues (linear, circular, node) | Bracket validator, postfix calculator, undo/redo, stock span, bank tokens, sensor window, printer spooler, support tickets | circular_queue_array.py, linear_queue_array.py, queue_linked_list.py, stack_array.py, stack_linked_list.py |
| `04_Trees` | Binary Tree (array & node), BST, AVL, Binary Heap | Expression tree, tournament bracket, price index, player registry, ER triage, trending hashtags | avl_tree.py, binary_heap.py, binary_search_tree.py, binary_tree_array.py, binary_tree_linked.py |
| `05_Graphs` | Graphs (adjacency matrix & list), BFS/DFS, topo sort, Dijkstra | Metro fewest-stops, friend recommendation, course planner, city navigation | graph_adjacency_list.py, graph_adjacency_matrix.py, weighted_graph_dijkstra.py |
| `06_Hashing` | Hash tables: separate chaining & open addressing | User login system, duplicate payment detector | hash_table_chaining.py, hash_table_open_addressing.py |
| `07_Sorting` | Bubble, Selection, Insertion, Bucket, Merge, Quick, Heap sort | E-commerce product catalogue (price, rating, stability) | sorting_algorithms.py |
| `08_Searching` | Linear search, binary search, lower/upper bound | Library ISBN lookup over 200,000 books | searching_algorithms.py |
| `09_Shortest_Path_MST` | Dijkstra, Bellman-Ford, Floyd-Warshall, Kruskal, Prim | Network routing delay optimisation, fibre backbone design | minimum_spanning_tree.py, shortest_path_algorithms.py |
| `10_Greedy` | Activity selection, coin change, fractional knapsack | Conference room booking, cash change, shipping truck cargo | greedy_algorithms.py |
| `11_Divide_Conquer` | Number factor, house robber, edit distance, LCS, LPS | Version diff, spell checker | divide_and_conquer.py |
| `12_Dynamic_Programming` | Memoization vs tabulation, 0/1 knapsack | Cloud server CPU/RAM allocation within budget | dynamic_programming.py |
