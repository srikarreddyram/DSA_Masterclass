"""
============================================================================
 MODULE 04 : BINARY TREE - Linked (Node-based) implementation
============================================================================

 Each node has AT MOST two children. No ordering rule (that's the BST).

                N1
              /    \
            N2      N3
           /  \    /  \
         N4   N5  N6   N7

 TERMINOLOGY: root, parent, child, leaf, sibling, depth, height, level.
 TYPES: full, perfect, complete, balanced, skewed.

 LINKED vs ARRAY REPRESENTATION
  Linked (this file): child references -> any shape, no wasted slots.
  Array (binary_tree_array.py): index math -> ideal for COMPLETE trees.

 TRAVERSALS
  PreOrder  Root-Left-Right  (copy tree, prefix expression)
  InOrder   Left-Root-Right  (sorted order in a BST, infix)
  PostOrder Left-Right-Root  (delete tree, evaluate expression)
  LevelOrder BFS with a queue

 TIME / SPACE
  | Insert (first vacancy, BFS) | O(n) | O(n) |
  | Search                      | O(n) | O(n) |
  | Pre/In/Post order           | O(n) | O(h) recursion |
  | Level order                 | O(n) | O(n) queue |
  | Delete (swap with deepest)  | O(n) | O(n) |
  | Delete entire tree          | O(1) | O(1) |

 HOW TO RUN:  python binary_tree_linked.py
============================================================================
"""
from collections import deque


class Node:
    __slots__ = ("value", "left", "right")

    def __init__(self, value):
        self.value, self.left, self.right = value, None, None


class BinaryTree:
    def __init__(self):
        self.root = None

    # ---------------- traversals ----------------
    def pre_order(self, n, out):
        if n: out.append(n.value); self.pre_order(n.left, out); self.pre_order(n.right, out)
        return out

    def in_order(self, n, out):
        if n: self.in_order(n.left, out); out.append(n.value); self.in_order(n.right, out)
        return out

    def post_order(self, n, out):
        if n: self.post_order(n.left, out); self.post_order(n.right, out); out.append(n.value)
        return out

    def level_order(self):
        levels = []
        if not self.root:
            return levels
        q = deque([self.root])
        while q:
            level = []
            for _ in range(len(q)):              # exactly the nodes on this level
                n = q.popleft()
                level.append(n.value)
                if n.left: q.append(n.left)
                if n.right: q.append(n.right)
            levels.append(level)
        return levels

    def in_order_iterative(self):
        out, st, cur = [], [], self.root
        while cur or st:
            while cur:                           # go as far left as possible
                st.append(cur); cur = cur.left
            cur = st.pop()
            out.append(cur.value)
            cur = cur.right                      # then the right subtree
        return out

    # ---------------- search / insert / delete ----------------
    def search(self, v) -> bool:
        q = deque([self.root] if self.root else [])
        while q:
            n = q.popleft()
            if n.value == v:
                return True
            if n.left: q.append(n.left)
            if n.right: q.append(n.right)
        return False

    def insert(self, v) -> None:
        """Insert at the first vacant position in level order (keeps it compact)."""
        node = Node(v)
        if not self.root:
            self.root = node; return
        q = deque([self.root])
        while q:
            cur = q.popleft()
            if cur.left is None:
                cur.left = node; return
            q.append(cur.left)
            if cur.right is None:
                cur.right = node; return
            q.append(cur.right)

    def delete(self, v) -> bool:
        """Copy the deepest node's value into the target, then remove the deepest leaf."""
        if not self.root:
            return False
        if not self.root.left and not self.root.right:
            if self.root.value == v:
                self.root = None; return True
            return False
        q = deque([self.root])
        target = deepest = deepest_parent = None
        while q:
            cur = q.popleft()
            if cur.value == v and target is None:
                target = cur
            if cur.left: deepest_parent = cur; q.append(cur.left)
            if cur.right: deepest_parent = cur; q.append(cur.right)
            deepest = cur
        if target is None:
            return False
        target.value = deepest.value
        if deepest_parent.right is deepest: deepest_parent.right = None
        else: deepest_parent.left = None
        return True

    def height(self, n) -> int:
        return -1 if n is None else 1 + max(self.height(n.left), self.height(n.right))

    def count_leaves(self, n) -> int:
        if n is None: return 0
        if not n.left and not n.right: return 1
        return self.count_leaves(n.left) + self.count_leaves(n.right)

    def mirror(self, n):
        if n:
            n.left, n.right = self.mirror(n.right), self.mirror(n.left)
        return n

    def delete_tree(self) -> None:
        self.root = None

    def pretty(self, n=None, indent="", first=True) -> str:
        """Sideways print: right subtree on top."""
        if first: n = self.root
        if n is None: return ""
        return (self.pretty(n.right, indent + "      ", False)
                + f"  {indent}{n.value}\n"
                + self.pretty(n.left, indent + "      ", False))


# --------------------------------------------------------------------------
#  SCENARIO: Expression tree - parse postfix, print infix/prefix, evaluate
# --------------------------------------------------------------------------
def build_expression_tree(postfix: str) -> Node:
    st = []
    for tok in postfix.split():
        n = Node(tok)
        if tok in "+-*/^" and len(tok) == 1:
            n.right = st.pop()                   # second operand comes off first
            n.left = st.pop()
        st.append(n)
    return st.pop()


def to_infix(n: Node) -> str:
    if n.left is None:
        return n.value
    return f"({to_infix(n.left)} {n.value} {to_infix(n.right)})"


def evaluate(n: Node) -> float:                  # post-order evaluation
    if n.left is None:
        return float(n.value)
    a, b = evaluate(n.left), evaluate(n.right)
    return {"+": a + b, "-": a - b, "*": a * b, "/": a / b if b else float("nan"), "^": a ** b}[n.value]


def main() -> None:
    print("=== LINKED BINARY TREE OPERATIONS ===")
    t = BinaryTree()
    for i in range(1, 10):
        t.insert(f"N{i}")
    print(t.pretty(), end="")
    print(f"  pre_order   : {t.pre_order(t.root, [])}")
    print(f"  in_order    : {t.in_order(t.root, [])}")
    print(f"  in_order(it): {t.in_order_iterative()}")
    print(f"  post_order  : {t.post_order(t.root, [])}")
    print(f"  level_order : {t.level_order()}")
    print(f"  height={t.height(t.root)} leaves={t.count_leaves(t.root)}")
    print(f"  search N5 -> {t.search('N5')}, search N42 -> {t.search('N42')}")
    t.delete("N3")
    print(f"  delete N3 (replaced by deepest N9): {t.level_order()}")
    t.mirror(t.root)
    print(f"  mirrored    : {t.level_order()}")
    t.delete_tree()
    print(f"  after delete_tree root={t.root}")

    print("\n=== SCENARIO: Expression Tree Calculator ===")
    for e in ["3 4 + 2 *", "5 1 2 + 4 * + 3 -", "2 3 ^ 4 2 / -"]:
        root = build_expression_tree(e)
        tree = BinaryTree(); tree.root = root
        prefix = " ".join(tree.pre_order(root, []))
        print(f"  postfix {e:<20} infix {to_infix(root):<28} prefix {prefix:<24} = {evaluate(root):.2f}")


if __name__ == "__main__":
    main()
