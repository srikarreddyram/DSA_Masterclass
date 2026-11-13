"""
============================================================================
 MODULE 04 : AVL TREE (self-balancing Binary Search Tree)
============================================================================

 PROBLEM: a BST fed sorted data becomes a linked list (height n-1).
 AVL keeps   balance_factor = height(left) - height(right)   in {-1, 0, +1}
 for EVERY node, fixing violations with O(1) ROTATIONS on the way back up.
 Height stays <= ~1.44 log2(n).

 THE FOUR CASES
 1) LL  (bf > 1,  left child left-heavy/even)   -> right rotation
          z               y
         / \            /   \
        y   T4         x     z
       / \      ==>   / \   / \
      x   T3         T1 T2 T3 T4
 2) RR  mirror of LL                            -> left rotation
 3) LR  (bf > 1,  left child RIGHT-heavy)       -> left-rotate child, then right-rotate node
         z            z             x
        /            /            /   \
       y     ==>    x     ==>    y     z
        \          /
         x        y
 4) RL  mirror of LR                            -> right-rotate child, then left-rotate node

 Deciding the case from the CHILD's balance factor (not from the inserted
 key) lets the same rebalance() serve both insert and delete.

 USE CASES: lookup-heavy indexes with guaranteed O(log n), leaderboards,
 order books, symbol tables.

 TIME COMPLEXITY (guaranteed): insert/search/delete O(log n), rotation O(1),
 traversal O(n). Space O(n).

 HOW TO RUN:  python avl_tree.py
============================================================================
"""
import sys
from collections import deque

sys.setrecursionlimit(5000)        # the plain-BST comparison below gets deep


class Node:
    __slots__ = ("key", "data", "left", "right", "height")

    def __init__(self, key, data=""):
        self.key, self.data = key, data
        self.left = self.right = None
        self.height = 0                          # leaf height 0


class AVL:
    def __init__(self):
        self.root = None
        self.rotations = 0
        self.log = []

    @staticmethod
    def h(n) -> int:
        return -1 if n is None else n.height     # empty subtree height = -1

    def bf(self, n) -> int:
        return 0 if n is None else self.h(n.left) - self.h(n.right)

    def update(self, n) -> None:
        n.height = 1 + max(self.h(n.left), self.h(n.right))

    def rotate_right(self, z):
        """y = z.left becomes the root; y's right subtree (T3) moves under z."""
        y = z.left
        z.left, y.right = y.right, z
        self.update(z)                           # z first: it is now below y
        self.update(y)
        self.rotations += 1
        return y

    def rotate_left(self, z):
        y = z.right
        z.right, y.left = y.left, z
        self.update(z)
        self.update(y)
        self.rotations += 1
        return y

    def rebalance(self, n):
        self.update(n)
        b = self.bf(n)
        if b > 1:                                # left heavy
            if self.bf(n.left) < 0:              # LR -> convert to LL first
                self.log.append(f"LR at {n.key}")
                n.left = self.rotate_left(n.left)
            else:
                self.log.append(f"LL at {n.key}")
            return self.rotate_right(n)
        if b < -1:                               # right heavy
            if self.bf(n.right) > 0:             # RL -> convert to RR first
                self.log.append(f"RL at {n.key}")
                n.right = self.rotate_right(n.right)
            else:
                self.log.append(f"RR at {n.key}")
            return self.rotate_left(n)
        return n

    def insert(self, key, data="") -> None:
        self.root = self._insert(self.root, key, data)

    def _insert(self, n, key, data):
        if n is None:
            return Node(key, data)
        if key < n.key:   n.left = self._insert(n.left, key, data)
        elif key > n.key: n.right = self._insert(n.right, key, data)
        else:             n.data = data; return n
        return self.rebalance(n)                 # fix on the way UP

    def delete(self, key) -> None:
        self.root = self._delete(self.root, key)

    def _delete(self, n, key):
        if n is None:
            return None
        if key < n.key:   n.left = self._delete(n.left, key)
        elif key > n.key: n.right = self._delete(n.right, key)
        else:
            if n.left is None: return n.right
            if n.right is None: return n.left
            succ = n.right
            while succ.left: succ = succ.left
            n.key, n.data = succ.key, succ.data
            n.right = self._delete(n.right, succ.key)
        return self.rebalance(n)                 # deletions can need rotations too

    def search(self, key):
        c = self.root
        while c and c.key != key:
            c = c.left if key < c.key else c.right
        return c

    def in_order(self, n="root", out=None):
        if n == "root": n, out = self.root, []
        if n: self.in_order(n.left, out); out.append(n); self.in_order(n.right, out)
        return out

    def level_order(self) -> str:
        if not self.root: return "(empty)"
        parts, q = [], deque([self.root])
        while q:
            level = []
            for _ in range(len(q)):
                n = q.popleft(); level.append(str(n.key))
                if n.left: q.append(n.left)
                if n.right: q.append(n.right)
            parts.append("[" + " ".join(level) + "]")
        return " ".join(parts)

    def is_balanced(self, n="root") -> bool:
        if n == "root": n = self.root
        if n is None: return True
        return abs(self.bf(n)) <= 1 and self.is_balanced(n.left) and self.is_balanced(n.right)

    def delete_all(self) -> None:
        self.root = None


def plain_bst_height(keys) -> int:
    """Unbalanced BST height for comparison (iterative insert)."""
    root = None
    for k in keys:
        n = Node(k)
        if root is None: root = n; continue
        c = root
        while True:
            if k < c.key:
                if c.left is None: c.left = n; break
                c = c.left
            else:
                if c.right is None: c.right = n; break
                c = c.right
    def height(x):
        return -1 if x is None else 1 + max(height(x.left), height(x.right))
    return height(root)


def leaderboard_scenario() -> None:
    print("\n=== SCENARIO: Player Registry with Sorted Signups ===")
    n = 1000
    ids = [10_000 + i for i in range(n)]         # sequential IDs = BST worst case
    registry = AVL()
    for i, pid in enumerate(ids):
        registry.insert(pid, f"player_{i}")
    print(f"  {n} players inserted in increasing ID order")
    print(f"  plain BST height : {plain_bst_height(ids)}  -> ~{n} comparisons per lookup")
    print(f"  AVL tree height  : {registry.root.height}    -> ~{registry.root.height + 1} comparisons per lookup")
    print(f"  rotations used   : {registry.rotations}")
    print(f"  lookup 10777     : {registry.search(10_777).data}")
    for pid in range(10_000, 10_500):
        registry.delete(pid)
    print(f"  after 500 deletions height={registry.root.height} balanced={registry.is_balanced()}")


def main() -> None:
    print("=== AVL ROTATION CASES ===")
    for keys, name in [([30, 20, 10], "LL"), ([10, 20, 30], "RR"), ([30, 10, 20], "LR"), ([10, 30, 20], "RL")]:
        t = AVL()
        for k in keys:
            t.insert(k)
        print(f"  insert {str(keys):<12} ({name} case) -> {t.level_order():<14} log={t.log}")

    print("\n=== AVL OPERATIONS ===")
    avl = AVL()
    for k in [70, 50, 90, 30, 60, 80, 100, 20, 10, 95, 85, 87]:
        avl.insert(k, f"v{k}")
    print(f"  levels   : {avl.level_order()}")
    print(f"  rotations: {avl.log}")
    print("  in_order : " + " ".join(f"{n.key}(h{n.height},bf{avl.bf(n)})" for n in avl.in_order()))
    print(f"  search 85: {avl.search(85) is not None}, search 33: {avl.search(33) is not None}")
    avl.log.clear()
    for k in (10, 20, 30, 60):
        avl.delete(k)
    print(f"  after deleting 10,20,30,60 -> {avl.level_order()}  log={avl.log}")
    print(f"  balanced? {avl.is_balanced()}")
    avl.delete_all()
    print(f"  after delete_all -> {avl.level_order()}")
    leaderboard_scenario()


if __name__ == "__main__":
    main()
