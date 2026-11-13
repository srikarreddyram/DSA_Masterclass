"""
============================================================================
 MODULE 04 : BINARY TREE - Array implementation (complete binary tree)
============================================================================

 INDEX ARITHMETIC (1-based; index 0 unused for cleaner math)
     left child = 2i    right child = 2i + 1    parent = i // 2

           [1]A
         /      \
      [2]B      [3]C          arr: [ - | A | B | C | D | E | F | G ]
      /  \      /  \                 0   1   2   3   4   5   6   7
    [4]D [5]E [6]F [7]G

 (0-based variant: left = 2i+1, right = 2i+2, parent = (i-1)//2)

 WHY AN ARRAY?
  + No child references, great cache locality, O(1) parent/child jumps
  - Only efficient for COMPLETE trees (a skewed tree wastes 2^h slots)
  - Fixed capacity

 USE CASES: binary heaps, segment trees, Fenwick trees, tournament trees.

 TIME COMPLEXITY
  | Insert (next slot) O(1) | Search O(n) | Traversals O(n) |
  | Level order = linear scan O(n) | Delete O(n) | Delete tree O(1) |
  Space: O(n)

 HOW TO RUN:  python binary_tree_array.py
============================================================================
"""


class ArrayBinaryTree:
    def __init__(self, capacity: int):
        self.arr = [None] * (capacity + 1)
        self.last = 0                            # last used index (0 = empty)

    def is_full(self) -> bool:
        return self.last == len(self.arr) - 1

    def insert(self, v) -> bool:
        """O(1): next free slot in level order is simply last + 1."""
        if self.is_full():
            print(f"  tree is full, cannot insert {v}")
            return False
        self.last += 1
        self.arr[self.last] = v
        return True

    def search(self, v) -> int:
        for i in range(1, self.last + 1):
            if self.arr[i] == v:
                return i
        return -1

    def pre_order(self, i=1, out=None):
        out = [] if out is None else out
        if i <= self.last:                       # beyond last = "null child"
            out.append(self.arr[i])
            self.pre_order(2 * i, out)
            self.pre_order(2 * i + 1, out)
        return out

    def in_order(self, i=1, out=None):
        out = [] if out is None else out
        if i <= self.last:
            self.in_order(2 * i, out)
            out.append(self.arr[i])
            self.in_order(2 * i + 1, out)
        return out

    def post_order(self, i=1, out=None):
        out = [] if out is None else out
        if i <= self.last:
            self.post_order(2 * i, out)
            self.post_order(2 * i + 1, out)
            out.append(self.arr[i])
        return out

    def level_order(self):
        return self.arr[1 : self.last + 1]       # it's just the array, in order!

    def delete(self, v) -> bool:
        idx = self.search(v)
        if idx == -1:
            return False
        self.arr[idx] = self.arr[self.last]      # replace with last element
        self.arr[self.last] = None
        self.last -= 1
        return True

    def parent_of(self, v):
        i = self.search(v)
        return self.arr[i // 2] if i > 1 else None

    def delete_tree(self) -> None:
        self.arr = [None] * len(self.arr)
        self.last = 0

    def print_levels(self) -> None:
        start, level = 1, 0
        while start <= self.last:
            nodes = [f"{self.arr[i]}[{i}]" for i in range(start, min(2 * start, self.last + 1))]
            print(f"  level {level}: " + " ".join(nodes))
            start, level = start * 2, level + 1


# --------------------------------------------------------------------------
#  SCENARIO: Knock-out tournament bracket. 8 players at leaves [8..15];
#  node i stores the winner of children 2i and 2i+1; champion at index 1.
# --------------------------------------------------------------------------
def tournament_scenario() -> None:
    print("\n=== SCENARIO: Knock-out Tournament Bracket (array tree) ===")
    players = ["Anand", "Carlsen", "Gukesh", "Nakamura", "Pragg", "Caruana", "Ding", "Arjun"]
    rating = [2750, 2830, 2790, 2800, 2760, 2795, 2740, 2780]
    n = len(players)                             # power of two
    tree = [0] * (2 * n)                         # stores PLAYER INDEX per node
    for i in range(n):
        tree[n + i] = i
    for i in range(n - 1, 0, -1):                # bottom-up
        a, b = tree[2 * i], tree[2 * i + 1]
        tree[i] = a if rating[a] >= rating[b] else b

    round_names = {4: "Quarter-finals", 2: "Semi-finals", 1: "Final"}
    start = n // 2
    while start >= 1:
        print(f"  {round_names[start]}:")
        for i in range(start, 2 * start):
            a, b = tree[2 * i], tree[2 * i + 1]
            print(f"    {players[a]:<9} vs {players[b]:<9} -> {players[tree[i]]}")
        start //= 2
    champ = tree[1]
    print(f"  CHAMPION: {players[champ]}")

    saved = rating[champ]
    rating[champ] = -10 ** 9                     # champion forfeits
    i = (n + champ) // 2
    while i >= 1:                                # replay ONE leaf->root path: O(log n)
        a, b = tree[2 * i], tree[2 * i + 1]
        tree[i] = a if rating[a] >= rating[b] else b
        i //= 2
    rating[champ] = saved
    print(f"  If {players[champ]} withdraws, replaying ONE path (O(log n)) -> new winner: {players[tree[1]]}")


def main() -> None:
    print("=== ARRAY BINARY TREE OPERATIONS ===")
    t = ArrayBinaryTree(9)
    for s in ["Drinks", "Hot", "Cold", "Tea", "Coffee", "Cola", "Juice", "Green", "Masala", "Overflow"]:
        t.insert(s)
    t.print_levels()
    print(f"  pre_order  : {t.pre_order()}")
    print(f"  in_order   : {t.in_order()}")
    print(f"  post_order : {t.post_order()}")
    print(f"  level_order: {t.level_order()}")
    print(f"  search(Cola) -> index {t.search('Cola')}, parent = {t.parent_of('Cola')}")
    t.delete("Hot")
    print(f"  delete(Hot) -> {t.level_order()}")
    t.delete_tree()
    print(f"  after delete_tree -> {t.level_order()}")
    tournament_scenario()


if __name__ == "__main__":
    main()
