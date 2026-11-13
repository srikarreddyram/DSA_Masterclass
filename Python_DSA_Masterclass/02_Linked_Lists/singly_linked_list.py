"""
============================================================================
 MODULE 02 : SINGLY LINKED LIST
============================================================================

 A chain of NODES scattered across the heap. Each node holds a value and a
 reference to the NEXT node. We keep HEAD (first) and TAIL (last, for O(1)
 appends).

     head                                   tail
      |                                      |
     [10|*]-->[20|*]-->[30|*]-->[40|None]<---+

 LINKED LIST vs ARRAY
  + No resizing / shifting; O(1) insert/delete at a known position
  - No random access (reaching index i walks i links); extra memory per node

 REAL-WORLD USE CASES: undo history, hash-table buckets, adjacency lists,
 LRU caches (with a dict), polynomial arithmetic.

 TIME COMPLEXITY
  | Insert at head            | O(1) |
  | Insert at tail (has tail) | O(1) |
  | Insert at position k      | O(n) |
  | Delete head               | O(1) |
  | Delete tail               | O(n) (need the node BEFORE tail) |
  | Search / index access     | O(n) |
  | Reverse (in place)        | O(n) time, O(1) space |
  Space: O(n)

 HOW TO RUN:  python singly_linked_list.py
============================================================================
"""


class Node:
    __slots__ = ("value", "next")        # saves memory per node

    def __init__(self, value, next=None):
        self.value, self.next = value, next


class SinglyLinkedList:
    def __init__(self):
        self.head = self.tail = None
        self.size = 0

    def insert(self, value, location: int) -> None:
        node = Node(value)
        if self.head is None:                         # CASE 1: empty
            self.head = self.tail = node
        elif location <= 0:                           # CASE 2: new head
            node.next = self.head
            self.head = node
        elif location >= self.size:                   # CASE 3: new tail
            self.tail.next = node
            self.tail = node
        else:                                         # CASE 4: middle
            prev = self.head
            for _ in range(location - 1):             # stop one BEFORE the spot
                prev = prev.next
            node.next = prev.next                     # 1) link to successor first
            prev.next = node                          # 2) then predecessor -> node
        self.size += 1

    def add_first(self, v): self.insert(v, 0)
    def add_last(self, v): self.insert(v, self.size)

    def __iter__(self):
        cur = self.head
        while cur:
            yield cur.value
            cur = cur.next

    def traverse(self) -> str:
        return "  " + (" -> ".join(map(str, self)) + " -> None" if self.head else "(empty)")

    def search(self, value) -> int:
        for i, v in enumerate(self):
            if v == value:
                return i
        return -1

    def get(self, index: int):
        if not 0 <= index < self.size:
            return None
        cur = self.head
        for _ in range(index):
            cur = cur.next
        return cur.value

    def delete(self, location: int):
        if self.head is None:
            return None
        if location <= 0:                             # delete head - O(1)
            removed = self.head.value
            self.head = self.head.next
            if self.head is None:
                self.tail = None
        else:
            location = min(location, self.size - 1)
            prev = self.head
            for _ in range(location - 1):
                prev = prev.next
            removed = prev.next.value
            prev.next = prev.next.next                # bypass -> garbage collected
            if prev.next is None:
                self.tail = prev                      # removed the tail
        self.size -= 1
        return removed

    def delete_all(self) -> None:
        self.head = self.tail = None
        self.size = 0

    def reverse(self) -> None:
        """Three-pointer in-place reversal: flip each arrow backwards."""
        prev, cur = None, self.head
        self.tail = self.head
        while cur:
            nxt = cur.next            # save the rest
            cur.next = prev           # flip the arrow
            prev, cur = cur, nxt      # advance both
        self.head = prev

    def middle(self):
        """Floyd's tortoise & hare."""
        slow = fast = self.head
        while fast and fast.next:
            slow, fast = slow.next, fast.next.next
        return slow.value if slow else None


# --------------------------------------------------------------------------
#  SCENARIO: Text editor UNDO history (newest action at head)
# --------------------------------------------------------------------------
def undo_scenario() -> None:
    print("\n=== SCENARIO: Text Editor Undo History ===")
    document = ""
    history = SinglyLinkedList()
    for t in ["Hello", ", world", "! This is", " a DSA demo."]:
        document += t
        history.add_first(t)                          # O(1)
        print(f"  typed  '{t}' -> doc = \"{document}\"")
    for _ in range(2):
        last = history.delete(0)                      # O(1) pop newest
        document = document[: len(document) - len(last)]
        print(f"  UNDO   '{last}' -> doc = \"{document}\"")
    print("  remaining history (newest first):" + history.traverse())


def main() -> None:
    print("=== SINGLY LINKED LIST OPERATIONS ===")
    sll = SinglyLinkedList()
    sll.add_last(10); sll.add_last(20); sll.add_last(40)
    sll.add_first(5)
    sll.insert(30, 3)
    print(sll.traverse())
    print(f"  size={sll.size} head={sll.head.value} tail={sll.tail.value}")
    print(f"  search(30) = index {sll.search(30)}")
    print(f"  get(2)     = {sll.get(2)}")
    print(f"  middle     = {sll.middle()}")
    print(f"  delete head -> removed {sll.delete(0)}")
    print(f"  delete tail -> removed {sll.delete(sll.size - 1)}")
    print(sll.traverse())
    print(f"  tail is now {sll.tail.value}")
    sll.reverse()
    print("  reversed:" + sll.traverse())
    sll.delete_all()
    print("  after delete_all:" + sll.traverse())
    undo_scenario()


if __name__ == "__main__":
    main()
