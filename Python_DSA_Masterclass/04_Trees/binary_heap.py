"""
============================================================================
 MODULE 04 : BINARY HEAP (array-based complete binary tree)
============================================================================

 HEAP PROPERTY
   Min-heap: parent <= children (smallest at root)
   Max-heap: parent >= children (largest at root)
 Always COMPLETE -> stored in an array with no gaps (0-based):
   left = 2i+1   right = 2i+2   parent = (i-1)//2

          5                arr: [5, 10, 8, 20, 15, 9]
        /   \
      10     8
     /  \   /
   20   15 9

 OPERATIONS
   insert   : append at end, SIFT UP while better than parent
   extract  : take root, move last leaf to root, SIFT DOWN to better child
   heapify  : sift down every non-leaf from n//2-1 to 0 -> O(n) total

 USE CASES: priority queues (schedulers, ER triage, Dijkstra, Prim), top-K,
 streaming median, heap sort, merging K sorted lists. Python's `heapq`
 module is a min-heap on a plain list.

 TIME COMPLEXITY
  peek O(1) | insert O(log n) | extract O(log n) | build O(n)
  heap sort O(n log n) in place | Space O(n)

 HOW TO RUN:  python binary_heap.py
============================================================================
"""


class Heap:
    """Generic heap. `key` maps an item to its priority; smaller = served first.
    For a max-heap pass key=lambda x: -x (or set max_heap=True)."""

    def __init__(self, key=lambda x: x, max_heap: bool = False):
        self.arr = []
        self._key = (lambda x: -key(x)) if max_heap else key

    def __len__(self) -> int:
        return len(self.arr)

    def is_empty(self) -> bool:
        return not self.arr

    def _better(self, i: int, j: int) -> bool:
        return self._key(self.arr[i]) < self._key(self.arr[j])

    def peek(self):
        if not self.arr:
            raise IndexError("heap is empty")
        return self.arr[0]

    def insert(self, v) -> None:
        self.arr.append(v)
        self._sift_up(len(self.arr) - 1)

    def _sift_up(self, i: int) -> None:
        while i > 0:
            parent = (i - 1) // 2
            if not self._better(i, parent):      # heap property satisfied
                break
            self.arr[i], self.arr[parent] = self.arr[parent], self.arr[i]
            i = parent

    def extract(self):
        if not self.arr:
            raise IndexError("heap is empty")
        top = self.arr[0]
        last = self.arr.pop()                    # remove last leaf
        if self.arr:
            self.arr[0] = last                   # move it to the root
            self._sift_down(0, len(self.arr))
        return top

    def _sift_down(self, i: int, n: int) -> None:
        while True:
            left, right, best = 2 * i + 1, 2 * i + 2, i
            if left < n and self._better(left, best): best = left
            if right < n and self._better(right, best): best = right
            if best == i:
                return
            self.arr[i], self.arr[best] = self.arr[best], self.arr[i]
            i = best

    @classmethod
    def build(cls, items, **kw) -> "Heap":
        """Floyd's bottom-up construction - O(n)."""
        h = cls(**kw)
        h.arr = list(items)
        for i in range(len(h.arr) // 2 - 1, -1, -1):   # leaves already are heaps
            h._sift_down(i, len(h.arr))
        return h

    def heap_sort(self):
        """In place on a copy: swap root to the end, shrink, sift down."""
        saved = self.arr
        self.arr = list(saved)
        for end in range(len(self.arr) - 1, 0, -1):
            self.arr[0], self.arr[end] = self.arr[end], self.arr[0]
            self._sift_down(0, end)
        result, self.arr = self.arr, saved
        return result                            # max-heap -> ascending

    def delete_heap(self) -> None:
        self.arr = []

    def __repr__(self) -> str:
        return str(self.arr)


def emergency_room_scenario() -> None:
    print("\n=== SCENARIO 1: ER Triage Priority Queue ===")
    # priority = (severity, arrival) -> FIFO among equal severity
    er = Heap(key=lambda p: (p["severity"], p["arrival"]))
    arrivals = [("Ravi", "sprained ankle", 4), ("Meera", "chest pain", 1), ("Arjun", "high fever", 3),
                ("Kavya", "fracture", 2), ("Sam", "minor cut", 5), ("Priya", "breathing difficulty", 1)]
    for t, (name, complaint, sev) in enumerate(arrivals):
        er.insert({"name": name, "complaint": complaint, "severity": sev, "arrival": t})
        print(f"  arrives: {name:<6} severity {sev}  ({complaint})")
    print("  --- doctor treats in priority order ---")
    while not er.is_empty():
        p = er.extract()
        print(f"  treating {p['name']:<6} [sev {p['severity']}] {p['complaint']}")


def top_k_scenario(k: int = 3) -> None:
    print(f"\n=== SCENARIO 2: Top-{k} Trending Hashtags (streaming) ===")
    stream = [("#java", 5400), ("#python", 8900), ("#dsa", 3100), ("#ai", 12000),
              ("#cricket", 15000), ("#leetcode", 2800), ("#cloud", 7600)]
    # A size-k MIN-heap keeps the k largest; its root is the weakest of them.
    h = Heap(key=lambda x: x[1])
    for tag in stream:
        if len(h) < k:
            h.insert(tag)
        elif tag[1] > h.peek()[1]:
            h.extract()
            h.insert(tag)
    result = [h.extract() for _ in range(k)][::-1]
    print("  top 3: " + ", ".join(f"{t}({c})" for t, c in result))


def main() -> None:
    print("=== MIN-HEAP OPERATIONS ===")
    mn = Heap()
    for v in [20, 15, 30, 5, 10, 8, 25]:
        mn.insert(v)
        print(f"  insert {v} -> {mn}")
    print(f"  peek = {mn.peek()}, size = {len(mn)}")
    print("  extract all: " + " ".join(str(mn.extract()) for _ in range(len(mn))))

    print("\n=== MAX-HEAP via build (O(n)) + HEAP SORT ===")
    data = [4, 10, 3, 5, 1, 15, 7, 9]
    mx = Heap.build(data, max_heap=True)
    print(f"  input     : {data}")
    print(f"  max-heap  : {mx}")
    print(f"  heap sort : {mx.heap_sort()}")
    mx.delete_heap()
    print(f"  after delete_heap size={len(mx)}")
    emergency_room_scenario()
    top_k_scenario()


if __name__ == "__main__":
    main()
