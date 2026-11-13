"""
============================================================================
 MODULE 03 : QUEUE (Linked-List / Node-based)
============================================================================

 front = head, rear = tail of a singly linked list:
   enqueue -> append at tail (O(1), we keep a tail reference)
   dequeue -> remove head    (O(1))
 (Removing a singly-linked TAIL is O(n), so we never dequeue from there.)

    front                       rear
      |                          |
    [10|*] -> [20|*] -> [30|*] -> [40|None]

 CIRCULAR ARRAY vs LINKED QUEUE
  | Capacity | fixed (bounded buffer)  | unbounded          |
  | Memory   | compact, preallocated   | per-node overhead  |
  | Use when | max size known          | size unpredictable |

 REAL-WORLD USE CASES: job/task queues, message brokers, BFS, support
 ticketing, ride-hailing request matching.

 TIME COMPLEXITY: all operations O(1); Space O(n)

 HOW TO RUN:  python queue_linked_list.py
============================================================================
"""


class Node:
    __slots__ = ("value", "next")

    def __init__(self, value):
        self.value, self.next = value, None


class LinkedQueue:
    def __init__(self):
        self.front = self.rear = None
        self.size = 0

    def is_empty(self) -> bool:
        return self.front is None

    def enqueue(self, v) -> None:
        n = Node(v)
        if self.rear is None:
            self.front = self.rear = n
        else:
            self.rear.next = n
            self.rear = n
        self.size += 1

    def dequeue(self):
        if self.is_empty():
            raise IndexError("Queue underflow")
        v = self.front.value
        self.front = self.front.next
        if self.front is None:
            self.rear = None              # IMPORTANT: queue became empty
        self.size -= 1
        return v

    def peek(self):
        if self.is_empty():
            raise IndexError("Queue is empty")
        return self.front.value

    def delete_queue(self) -> None:
        self.front = self.rear = None
        self.size = 0

    def __repr__(self) -> str:
        vals, cur = [], self.front
        while cur:
            vals.append(str(cur.value)); cur = cur.next
        return "front-> " + " ".join(vals) + " <-rear"


def support_scenario() -> None:
    print("\n=== SCENARIO: Customer Support Ticket Queue (2 agents) ===")
    queue = LinkedQueue()
    issues = ["Login failed", "Refund status", "App crash", "Change address",
              "Payment declined", "Order missing", "OTP not received"]
    served = total_wait = 0
    # one ticket arrives per minute; 2 agents each resolve a ticket every 3 minutes
    for minute in range(13):
        if minute < len(issues):
            queue.enqueue({"id": 1001 + minute, "issue": issues[minute], "arrived": minute})
        if minute % 3 == 0 and minute > 0:
            for agent in (1, 2):
                if queue.is_empty():
                    break
                t = queue.dequeue()
                wait = minute - t["arrived"]
                served += 1
                total_wait += wait
                print(f"  min {minute:2d}: agent {agent} resolves #{t['id']} "
                      f"{repr(t['issue']):<18} (waited {wait} min)")
    print(f"  served {served} tickets, average wait {total_wait / served:.1f} min, still waiting: {queue}")


def main() -> None:
    print("=== LINKED-LIST QUEUE OPERATIONS ===")
    q = LinkedQueue()
    for x in "ABC":
        q.enqueue(x)
    print(f"  {q}  size={q.size}")
    print(f"  peek    = {q.peek()}")
    for _ in range(3):
        print(f"  dequeue = {q.dequeue()} -> {q}")
    print(f"  is_empty={q.is_empty()}")
    q.enqueue("D")                        # proves rear was reset
    print(f"  enqueue D after empty -> {q}")
    q.delete_queue()
    print(f"  after delete_queue is_empty={q.is_empty()}")
    support_scenario()


if __name__ == "__main__":
    main()
