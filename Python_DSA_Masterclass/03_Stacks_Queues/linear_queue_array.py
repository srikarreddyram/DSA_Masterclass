"""
============================================================================
 MODULE 03 : LINEAR QUEUE (Array-based)
============================================================================

 FIFO - First In, First Out (a ticket-counter line).
 Join at the REAR (enqueue), leave from the FRONT (dequeue).

    dequeue <-  [10][20][30][40]  <- enqueue
                front        rear

 THE BIG FLAW (why Circular Queue exists)
 ----------------------------------------
 `front` only moves forward, so freed slots at the start are never reused.
 The queue can report FULL while holding a single element:

    [ x ][ x ][ x ][40]     front=3, rear=3  -> is_full() == True !!

 This file shows the flaw on purpose. See circular_queue_array.py.

 PYTHON NOTE: never use list.pop(0) as a queue - it is O(n) because every
 element shifts. Use collections.deque (a doubly linked block list) instead.

 TIME COMPLEXITY: enqueue O(1) | dequeue O(1) | peek O(1) | is_empty O(1)
                  is_full O(1) | delete O(1)  | Space O(n)

 HOW TO RUN:  python linear_queue_array.py
============================================================================
"""


class LinearQueue:
    def __init__(self, capacity: int):
        self.arr = [None] * capacity
        self.front = self.rear = -1

    def is_empty(self) -> bool: return self.front == -1 or self.front > self.rear
    def is_full(self) -> bool: return self.rear == len(self.arr) - 1    # the flawed check

    def enqueue(self, v) -> None:
        if self.is_full():
            print(f"  enqueue({v}) FAILED - queue is full (rear at end)")
            return
        if self.front == -1:
            self.front = 0
        self.rear += 1
        self.arr[self.rear] = v
        print(f"  enqueue({v}) -> {self}")

    def dequeue(self):
        if self.is_empty():
            raise IndexError("Queue underflow")
        v = self.arr[self.front]
        self.arr[self.front] = None
        self.front += 1
        if self.front > self.rear:                   # empty -> reset indices
            self.front = self.rear = -1
        return v

    def peek(self):
        if self.is_empty():
            raise IndexError("Queue is empty")
        return self.arr[self.front]

    def delete_queue(self) -> None:
        self.arr = [None] * len(self.arr)
        self.front = self.rear = -1

    def __repr__(self) -> str:
        cells = [f"{v:>3}" if v is not None else "  ." for v in self.arr]
        return "[" + "|".join(cells) + f"]  front={self.front} rear={self.rear}"


def bank_scenario() -> None:
    print("\n=== SCENARIO: Bank Token Counter ===")
    tokens = LinearQueue(6)
    for t in range(101, 105):
        tokens.enqueue(t)
    print(f"  Serving token {tokens.dequeue()}")
    print(f"  Serving token {tokens.dequeue()}")
    print(f"  Next in line : {tokens.peek()}")
    tokens.enqueue(105)
    tokens.enqueue(106)
    print("  Two slots are free at the front, but...")
    tokens.enqueue(107)
    print("  => wasted space: use a CIRCULAR queue to reuse freed slots.")


def main() -> None:
    print("=== LINEAR QUEUE OPERATIONS ===")
    q = LinearQueue(4)
    print(f"  is_empty: {q.is_empty()}")
    for v in (10, 20, 30):
        q.enqueue(v)
    print(f"  peek   : {q.peek()}")
    print(f"  dequeue: {q.dequeue()} -> {q}")
    q.enqueue(40)
    print(f"  is_full: {q.is_full()}")
    q.delete_queue()
    print(f"  after delete_queue -> {q} is_empty={q.is_empty()}")
    bank_scenario()


if __name__ == "__main__":
    main()
