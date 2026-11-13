"""
============================================================================
 MODULE 02 : CIRCULAR SINGLY LINKED LIST (CSLL)
============================================================================

 The last node points back to the FIRST node instead of None, so there is no
 end. Traversal must stop when we come back around to head.

       +------------------------------------+
       v                                    |
     [10|*] --> [20|*] --> [30|*] --> [40|*]-+
      head                             tail

 Invariant maintained by every operation:   tail.next is head

 REAL-WORLD USE CASES: round-robin CPU scheduling, turn order in multiplayer
 games, Josephus problem, repeating slideshows.

 TIME COMPLEXITY
  | Insert at head / tail | O(1) |  (we keep a tail reference)
  | Insert at position k  | O(n) |
  | Delete head           | O(1) |
  | Delete tail           | O(n) |  (need tail's predecessor)
  | Search / traverse     | O(n) |
  Space: O(n)

 HOW TO RUN:  python circular_singly_linked_list.py
============================================================================
"""


class Node:
    __slots__ = ("value", "next")

    def __init__(self, value):
        self.value, self.next = value, None


class CircularSinglyLinkedList:
    def __init__(self):
        self.head = self.tail = None
        self.size = 0

    def insert(self, v, location: int) -> None:
        n = Node(v)
        if self.head is None:
            self.head = self.tail = n
            n.next = n                              # a ring of one
        elif location <= 0:
            n.next = self.head
            self.head = n
            self.tail.next = self.head              # keep circle closed
        elif location >= self.size:
            n.next = self.head
            self.tail.next = n
            self.tail = n
        else:
            prev = self.head
            for _ in range(location - 1):
                prev = prev.next
            n.next, prev.next = prev.next, n
        self.size += 1

    def add_last(self, v) -> None:
        self.insert(v, self.size)

    def __iter__(self):
        """Emulates do-while: yield head first, stop on wrap-around."""
        if self.head is None:
            return
        cur = self.head
        while True:
            yield cur.value
            cur = cur.next
            if cur is self.head:
                break

    def traverse(self) -> str:
        if self.head is None:
            return "  (empty)"
        return "  " + " -> ".join(map(str, self)) + f" -> (back to {self.head.value})"

    def search(self, v) -> int:
        for i, x in enumerate(self):
            if x == v:
                return i
        return -1

    def delete(self, location: int):
        if self.head is None:
            return None
        if self.size == 1:
            removed = self.head.value
            self.head.next = None                   # break the self loop
            self.head = self.tail = None
        elif location <= 0:
            removed = self.head.value
            self.head = self.head.next
            self.tail.next = self.head
        else:
            location = min(location, self.size - 1)
            prev = self.head
            for _ in range(location - 1):
                prev = prev.next
            removed = prev.next.value
            if prev.next is self.tail:
                self.tail = prev
            prev.next = prev.next.next
        self.size -= 1
        return removed

    def unlink_after(self, prev: Node):
        """O(1) removal of prev.next - used by the scheduler/Josephus scenarios."""
        victim = prev.next
        if self.size == 1:
            self.delete_all()
            return victim.value
        prev.next = victim.next
        if victim is self.head:
            self.head = victim.next
        if victim is self.tail:
            self.tail = prev
        self.size -= 1
        return victim.value

    def delete_all(self) -> None:
        """Break the cycle first (essential in C/C++, good hygiene in Python)."""
        if self.tail:
            self.tail.next = None
        self.head = self.tail = None
        self.size = 0


# --------------------------------------------------------------------------
#  SCENARIO 1: Round-robin CPU scheduler (quantum = 3)
# --------------------------------------------------------------------------
def round_robin_scenario() -> None:
    print("\n=== SCENARIO 1: Round-Robin CPU Scheduler (quantum = 3) ===")
    ready = CircularSinglyLinkedList()
    for name, burst in [("Chrome", 5), ("VSCode", 3), ("Spotify", 7), ("Terminal", 2)]:
        ready.add_last({"name": name, "remaining": burst})
    print("  ready queue: " + " -> ".join(f"{p['name']}({p['remaining']})" for p in ready))

    quantum, clock = 3, 0
    prev, cur = ready.tail, ready.head
    while ready.size > 0:
        p = cur.value
        run = min(quantum, p["remaining"])
        p["remaining"] -= run
        clock += run
        if p["remaining"] == 0:
            print(f"  t={clock:2d}  {p['name']:<8} ran {run} -> FINISHED")
            nxt = cur.next
            ready.unlink_after(prev)                # O(1) using predecessor
            cur = nxt
        else:
            print(f"  t={clock:2d}  {p['name']:<8} ran {run} -> {p['remaining']} left")
            prev, cur = cur, cur.next               # circle continues naturally
    print(f"  all processes completed at t={clock}")


# --------------------------------------------------------------------------
#  SCENARIO 2: Josephus problem (n=7, k=3)
# --------------------------------------------------------------------------
def josephus_scenario(n: int = 7, k: int = 3) -> None:
    print(f"\n=== SCENARIO 2: Josephus Problem (n={n}, k={k}) ===")
    circle = CircularSinglyLinkedList()
    for i in range(1, n + 1):
        circle.add_last(i)
    prev, order = circle.tail, []
    while circle.size > 1:
        for _ in range(k - 1):
            prev = prev.next
        order.append(circle.unlink_after(prev))
    print("  elimination order: " + " ".join(map(str, order)))
    print(f"  survivor: person {circle.head.value}")


def main() -> None:
    print("=== CIRCULAR SINGLY LINKED LIST OPERATIONS ===")
    c = CircularSinglyLinkedList()
    c.insert(10, 0); c.insert(20, 1); c.insert(40, 2); c.insert(5, 0); c.insert(30, 3)
    print(c.traverse())
    print(f"  tail.next is head ? {c.tail.next is c.head}")
    print(f"  search(30) = {c.search(30)}")
    print(f"  delete(0)  = {c.delete(0)}")
    print(f"  delete(99) = {c.delete(99)}  (clamped to tail)")
    print(c.traverse())
    print(f"  tail.next is head ? {c.tail.next is c.head}")
    c.delete_all()
    print(c.traverse())
    round_robin_scenario()
    josephus_scenario()


if __name__ == "__main__":
    main()
