"""
============================================================================
 MODULE 02 : DOUBLY LINKED LIST
============================================================================

 Every node references BOTH its previous and next node, so we can walk in
 either direction and unlink a known node in O(1).

   None <-[prev|10|next]<->[prev|20|next]<->[prev|30|next]-> None
            ^ head                             ^ tail

 REAL-WORLD USE CASES: browser back/forward, LRU cache (OrderedDict is a
 dict + doubly linked list), collections.deque, editor cursors.

 TIME COMPLEXITY
  | Insert/delete at head or tail   | O(1) |  <- tail delete now O(1)
  | Insert/delete at position k     | O(n) (walk from nearer end) |
  | Unlink a node you already hold  | O(1) |
  | Search                          | O(n) |
  | Reverse traversal               | O(n) |
  Space: O(n) (+1 reference per node vs singly)

 HOW TO RUN:  python doubly_linked_list.py
============================================================================
"""


class Node:
    __slots__ = ("value", "prev", "next")

    def __init__(self, value):
        self.value, self.prev, self.next = value, None, None


class DoublyLinkedList:
    def __init__(self):
        self.head = self.tail = None
        self.size = 0

    def add_first(self, v) -> None:
        n = Node(v)
        if self.head is None:
            self.head = self.tail = n
        else:
            n.next, self.head.prev = self.head, n
            self.head = n
        self.size += 1

    def add_last(self, v) -> None:
        n = Node(v)
        if self.tail is None:
            self.head = self.tail = n
        else:
            n.prev, self.tail.next = self.tail, n
            self.tail = n
        self.size += 1

    def node_at(self, index: int):
        """Walk from whichever end is closer - halves the average cost."""
        if not 0 <= index < self.size:
            return None
        if index < self.size // 2:
            cur = self.head
            for _ in range(index):
                cur = cur.next
        else:
            cur = self.tail
            for _ in range(self.size - 1 - index):
                cur = cur.prev
        return cur

    def insert(self, v, location: int) -> None:
        if location <= 0:
            return self.add_first(v)
        if location >= self.size:
            return self.add_last(v)
        after = self.node_at(location)
        before = after.prev
        n = Node(v)
        # four links:  before <-> n <-> after
        n.prev, n.next = before, after
        before.next = after.prev = n
        self.size += 1

    def unlink(self, n: Node):
        """O(1) removal of a node we hold a reference to."""
        if n.prev: n.prev.next = n.next
        else:      self.head = n.next
        if n.next: n.next.prev = n.prev
        else:      self.tail = n.prev
        n.prev = n.next = None
        self.size -= 1
        return n.value

    def delete(self, location: int):
        n = self.node_at(max(0, min(location, self.size - 1)))
        return None if n is None else self.unlink(n)

    def search(self, v) -> int:
        cur, i = self.head, 0
        while cur:
            if cur.value == v:
                return i
            cur, i = cur.next, i + 1
        return -1

    def traverse(self) -> str:
        vals, cur = [], self.head
        while cur:
            vals.append(str(cur.value)); cur = cur.next
        return "  None <- " + " <-> ".join(vals) + " -> None"

    def reverse_traverse(self) -> str:
        vals, cur = [], self.tail
        while cur:
            vals.append(str(cur.value)); cur = cur.prev
        return "  (tail->head) " + " <- ".join(vals)

    def delete_all(self) -> None:
        cur = self.head
        while cur:                       # break prev/next links (mirrors the course)
            nxt = cur.next
            cur.prev = cur.next = None
            cur = nxt
        self.head = self.tail = None
        self.size = 0


# --------------------------------------------------------------------------
#  SCENARIO: Browser history with Back / Forward
# --------------------------------------------------------------------------
class BrowserHistory:
    def __init__(self, homepage: str):
        self.pages = DoublyLinkedList()
        self.pages.add_last(homepage)
        self.current = self.pages.tail

    def visit(self, url: str) -> None:
        while self.pages.tail is not self.current:      # wipe forward history
            self.pages.unlink(self.pages.tail)          # O(1) each
        self.pages.add_last(url)
        self.current = self.pages.tail
        print(f"  visit   -> {url}")

    def back(self, steps: int) -> None:
        while steps > 0 and self.current.prev:
            self.current, steps = self.current.prev, steps - 1
        print(f"  back    -> {self.current.value}")

    def forward(self, steps: int) -> None:
        while steps > 0 and self.current.next:
            self.current, steps = self.current.next, steps - 1
        print(f"  forward -> {self.current.value}")


def browser_scenario() -> None:
    print("\n=== SCENARIO: Browser Back/Forward Navigation ===")
    b = BrowserHistory("google.com")
    b.visit("leetcode.com")
    b.visit("leetcode.com/problems/two-sum")
    b.visit("github.com/srikarreddyram")
    b.back(1); b.back(1); b.forward(1)
    b.visit("stackoverflow.com")        # forward history discarded
    b.forward(2)                        # nothing ahead
    b.back(10)                          # clamps at first page
    print("  full history:" + b.pages.traverse())


def main() -> None:
    print("=== DOUBLY LINKED LIST OPERATIONS ===")
    dll = DoublyLinkedList()
    dll.add_last(20); dll.add_last(30); dll.add_first(10); dll.add_last(50)
    dll.insert(40, 3)
    print(dll.traverse())
    print(dll.reverse_traverse())
    print(f"  search(40)   = {dll.search(40)}")
    print(f"  delete(0)    = {dll.delete(0)}")
    print(f"  delete(tail) = {dll.delete(dll.size - 1)}   <- O(1) thanks to prev link")
    print(f"  delete(1)    = {dll.delete(1)}")
    print(dll.traverse())
    dll.delete_all()
    print(f"  after delete_all size={dll.size} head={dll.head}")
    browser_scenario()


if __name__ == "__main__":
    main()
