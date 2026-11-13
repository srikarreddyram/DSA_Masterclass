"""
============================================================================
 MODULE 04 : BINARY SEARCH TREE (BST)
============================================================================

 BST PROPERTY: for EVERY node, left subtree keys < node.key < right subtree
 keys. In-order traversal is therefore SORTED, and each comparison discards
 half the remaining tree.

                 70
              /      \
            50        90
           /  \      /  \
         30    60  80   100
        /  \
      20    40

 DELETION CASES
  1) leaf          -> remove
  2) one child     -> replace node with that child
  3) two children  -> copy in-order SUCCESSOR (min of right subtree) into the
                      node, then delete the successor (<= 1 child)

 USE CASES: ordered maps/sets, range queries (price filters), auto-complete
 ranges, database indexes (B-trees generalise this).

 TIME COMPLEXITY
  | Operation              | Average  | Worst (skewed) |
  |------------------------|----------|----------------|
  | insert/search/delete   | O(log n) | O(n)           |
  | min/max/floor/ceiling  | O(log n) | O(n)           |
  | range query            | O(log n + k)              |
  | traversal              | O(n)                      |
  Sorted input produces the worst case -> see avl_tree.py.

 HOW TO RUN:  python binary_search_tree.py
============================================================================
"""
from collections import deque


class Node:
    __slots__ = ("key", "val", "left", "right")

    def __init__(self, key, val):
        self.key, self.val, self.left, self.right = key, val, None, None


class BST:
    def __init__(self):
        self.root = None
        self.size = 0

    def put(self, key, val) -> None:
        self.root = self._put(self.root, key, val)

    def _put(self, n, key, val):
        if n is None:
            self.size += 1
            return Node(key, val)
        if key < n.key:   n.left = self._put(n.left, key, val)
        elif key > n.key: n.right = self._put(n.right, key, val)
        else:             n.val = val                    # duplicate: update
        return n

    def get(self, key):
        cur = self.root
        while cur:                                       # iterative: O(h), O(1) space
            if key == cur.key:
                return cur.val
            cur = cur.left if key < cur.key else cur.right
        return None

    def min(self):
        n = self.root
        while n.left: n = n.left
        return n.key

    def max(self):
        n = self.root
        while n.right: n = n.right
        return n.key

    def floor(self, target):
        """Largest key <= target."""
        cur, best = self.root, None
        while cur:
            if target == cur.key: return cur.key
            if target < cur.key: cur = cur.left
            else: best, cur = cur.key, cur.right         # candidate; look for bigger
        return best

    def ceiling(self, target):
        """Smallest key >= target."""
        cur, best = self.root, None
        while cur:
            if target == cur.key: return cur.key
            if target > cur.key: cur = cur.right
            else: best, cur = cur.key, cur.left
        return best

    def range(self, lo, hi, n="root", out=None):
        """Keys in [lo, hi]; skip subtrees that cannot contain matches."""
        if n == "root": n, out = self.root, []
        if n is None: return out
        if lo < n.key: self.range(lo, hi, n.left, out)
        if lo <= n.key <= hi: out.append((n.key, n.val))
        if hi > n.key: self.range(lo, hi, n.right, out)
        return out

    def delete(self, key) -> None:
        self.root = self._delete(self.root, key)

    def _delete(self, n, key):
        if n is None:
            return None
        if key < n.key:
            n.left = self._delete(n.left, key); return n
        if key > n.key:
            n.right = self._delete(n.right, key); return n
        if n.left is None:  self.size -= 1; return n.right    # cases 1 & 2
        if n.right is None: self.size -= 1; return n.left     # case 2
        succ = n.right                                         # case 3
        while succ.left: succ = succ.left
        n.key, n.val = succ.key, succ.val
        n.right = self._delete(n.right, succ.key)
        return n

    # ---------------- traversals ----------------
    def in_order(self, n="root", out=None):
        if n == "root": n, out = self.root, []
        if n: self.in_order(n.left, out); out.append(n.key); self.in_order(n.right, out)
        return out

    def pre_order(self, n="root", out=None):
        if n == "root": n, out = self.root, []
        if n: out.append(n.key); self.pre_order(n.left, out); self.pre_order(n.right, out)
        return out

    def post_order(self, n="root", out=None):
        if n == "root": n, out = self.root, []
        if n: self.post_order(n.left, out); self.post_order(n.right, out); out.append(n.key)
        return out

    def level_order(self):
        out, q = [], deque([self.root] if self.root else [])
        while q:
            n = q.popleft(); out.append(n.key)
            if n.left: q.append(n.left)
            if n.right: q.append(n.right)
        return out

    def height(self, n="root"):
        if n == "root": n = self.root
        return -1 if n is None else 1 + max(self.height(n.left), self.height(n.right))

    def is_valid(self, n="root", lo=None, hi=None) -> bool:
        if n == "root": n = self.root
        if n is None: return True
        if (lo is not None and n.key <= lo) or (hi is not None and n.key >= hi): return False
        return self.is_valid(n.left, lo, n.key) and self.is_valid(n.right, n.key, hi)

    def lca(self, a, b):
        cur = self.root
        while cur:
            if a < cur.key and b < cur.key: cur = cur.left
            elif a > cur.key and b > cur.key: cur = cur.right
            else: return cur.key                         # split point
        return None

    def delete_all(self) -> None:
        self.root, self.size = None, 0


def product_catalogue_scenario() -> None:
    print("\n=== SCENARIO: E-commerce Price Index ===")
    by_price = BST()
    for price, name in [(1299, "Wireless Mouse"), (4999, "Mechanical Keyboard"), (899, "USB Cable"),
                        (15999, "27in Monitor"), (2499, "Webcam 1080p"), (7499, "Noise-Cancel Headphones"),
                        (349, "Mouse Pad"), (3299, "Laptop Stand"), (54999, "Gaming GPU"), (1999, "USB-C Hub")]:
        by_price.put(price, name)
    print("  Filter Rs 1000 - 5000 (sorted, pruned search):")
    for price, name in by_price.range(1000, 5000):
        print(f"    Rs {price:6d}  {name}")
    f = by_price.floor(7000)
    print(f"  Best item within budget Rs 7000 : {by_price.get(f)} (Rs {f})")
    c = by_price.ceiling(10000)
    print(f"  Cheapest item at/above Rs 10000 : {by_price.get(c)} (Rs {c})")
    print(f"  Cheapest overall: Rs {by_price.min()} | Priciest: Rs {by_price.max()}")
    by_price.delete(54999)
    print(f"  GPU sold out -> priciest now Rs {by_price.max()} ({by_price.get(by_price.max())})")


def main() -> None:
    print("=== BST OPERATIONS ===")
    bst = BST()
    for k in [70, 50, 90, 30, 60, 80, 100, 20, 40]:
        bst.put(k, f"v{k}")
    print(f"  in_order (sorted!): {bst.in_order()}")
    print(f"  pre_order         : {bst.pre_order()}")
    print(f"  post_order        : {bst.post_order()}")
    print(f"  level_order       : {bst.level_order()}")
    print(f"  size={bst.size} height={bst.height()} valid={bst.is_valid()}")
    print(f"  get(60)={bst.get(60)} get(65)={bst.get(65)}")
    print(f"  floor(65)={bst.floor(65)} ceiling(65)={bst.ceiling(65)}")
    print(f"  LCA(20,40)={bst.lca(20, 40)} LCA(20,60)={bst.lca(20, 60)} LCA(40,100)={bst.lca(40, 100)}")
    bst.delete(20); print(f"  delete 20 (leaf)        -> {bst.level_order()}")
    bst.delete(30); print(f"  delete 30 (one child)   -> {bst.level_order()}")
    bst.delete(70); print(f"  delete 70 (two children; successor 80 moves up) -> {bst.level_order()}")
    skewed = BST()
    for i in range(1, 11):
        skewed.put(i, "")
    print(f"  sorted insert 1..10 -> height {skewed.height()}  (degenerates into a list!)")
    product_catalogue_scenario()


if __name__ == "__main__":
    main()
