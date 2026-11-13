# DSA Masterclass

Hands-on implementations of the full Data Structures & Algorithms curriculum in **Java** and **Python**: 28 programs per language, each standalone and runnable, with illustrated notes for every module.

## Highlights

- **Two languages, same curriculum**: every topic is implemented in both Java and Python.
- **Multiple implementations per structure**: array-based and node-based stacks, queues and binary trees; graphs as adjacency matrix and adjacency list.
- **Built from scratch**: a custom dynamic array (ArrayList) with resizing logic, hash tables, heaps, AVL trees.
- **Teaching-style code**: each file has a concept header, Big-O table, commented code, a test run and a real-world scenario.
- **Illustrated notes**: each module folder has a PDF (e.g. `04_Trees_Notes.pdf`) with diagrams, theory, Big-O tables and pitfalls.

## Repository structure

```
DSA_Masterclass/
├── Java_DSA_Masterclass/       # .java sources (one self-contained program per file)
│   ├── 01_Arrays/ ... 12_Dynamic_Programming/
│   └── README.md
└── Python_DSA_Masterclass/     # .py sources (standard library only)
    ├── 01_Arrays/ ... 12_Dynamic_Programming/
    └── README.md
```

## Quick start

**Python** (3.8+, no dependencies):

```bash
cd Python_DSA_Masterclass/04_Trees
python avl_tree.py
```

**Java** (11+ for the single-file launcher):

```bash
cd Java_DSA_Masterclass/04_Trees
java AVLTree.java
# or: javac AVLTree.java && java AVLTree
```

See each language's README for commands to run every file at once.

## Modules

1. **01_Arrays**: Standard Arrays (1D/2D) & Dynamic Array (custom ArrayList)
2. **02_Linked_Lists**: Singly, Doubly, Circular Singly, Circular Doubly Linked Lists
3. **03_Stacks_Queues**: Stacks (array & node) and Queues (linear, circular, node)
4. **04_Trees**: Binary Tree (array & node), BST, AVL, Binary Heap
5. **05_Graphs**: Graphs (adjacency matrix & list), BFS/DFS, topo sort, Dijkstra
6. **06_Hashing**: Hash tables: separate chaining & open addressing
7. **07_Sorting**: Bubble, Selection, Insertion, Bucket, Merge, Quick, Heap sort
8. **08_Searching**: Linear search, binary search, lower/upper bound
9. **09_Shortest_Path_MST**: Dijkstra, Bellman-Ford, Floyd-Warshall, Kruskal, Prim
10. **10_Greedy**: Activity selection, coin change, fractional knapsack
11. **11_Divide_Conquer**: Number factor, house robber, edit distance, LCS, LPS
12. **12_Dynamic_Programming**: Memoization vs tabulation, 0/1 knapsack

## Contributing

Issues and pull requests are welcome, especially for additional topics, clearer explanations or bug fixes.

## License

Released under the [MIT License](LICENSE).
