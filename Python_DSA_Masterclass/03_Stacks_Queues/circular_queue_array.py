"""
============================================================================
 MODULE 03 : CIRCULAR QUEUE (Array-based ring buffer)
============================================================================

 Treat the array as a RING - indices wrap with modulo arithmetic:

       next = (i + 1) % capacity

        index:  0     1     2     3     4
              [ 60 ][ 70 ][    ][ 40 ][ 50 ]
                      ^rear        ^front
        Dequeue order: 40, 50, 60, 70  (front wraps 4 -> 0)

 We track `size` explicitly, which removes the classic front == rear
 ambiguity (empty or full?). Alternative trick: keep one slot unused.

 REAL-WORLD USE CASES: keyboard/network/audio buffers, producer-consumer,
 sliding windows over streams ("last N readings"), collections.deque(maxlen=N).

 TIME COMPLEXITY: enqueue O(1) | dequeue O(1) | peek O(1) | is_empty O(1)
                  is_full O(1) | delete O(1)  | Space O(n)

 HOW TO RUN:  python circular_queue_array.py
============================================================================
"""


class CircularQueue:
    def __init__(self, capacity: int):
        self.arr = [None] * capacity
        self.front = 0          # index of oldest element
        self.rear = -1          # index of newest element
        self.size = 0

    def is_empty(self) -> bool: return self.size == 0
    def is_full(self) -> bool: return self.size == len(self.arr)

    def enqueue(self, v) -> bool:
        if self.is_full():
            return False
        self.rear = (self.rear + 1) % len(self.arr)     # wrap around
        self.arr[self.rear] = v
        self.size += 1
        return True

    def dequeue(self):
        if self.is_empty():
            raise IndexError("Queue underflow")
        v = self.arr[self.front]
        self.arr[self.front] = None
        self.front = (self.front + 1) % len(self.arr)   # wrap around
        self.size -= 1
        return v

    def peek(self):
        if self.is_empty():
            raise IndexError("Queue is empty")
        return self.arr[self.front]

    def delete_queue(self) -> None:
        self.arr = [None] * len(self.arr)
        self.front, self.rear, self.size = 0, -1, 0

    def __iter__(self):
        """Logical FIFO order: i-th element lives at (front + i) % capacity."""
        for i in range(self.size):
            yield self.arr[(self.front + i) % len(self.arr)]

    def raw_layout(self) -> str:
        cells = [f"{str(v):>3}" if v is not None else "  ." for v in self.arr]
        return "raw[" + "|".join(cells) + f"] front={self.front} rear={self.rear}"


def sensor_scenario() -> None:
    print("\n=== SCENARIO 1: Sensor Moving Average (window = 4) ===")
    window = CircularQueue(4)
    running = 0.0
    for r in [22.1, 22.4, 23.0, 25.8, 24.9, 23.3, 22.7]:
        if window.is_full():
            running -= window.dequeue()         # evict oldest - O(1)
        window.enqueue(r)
        running += r
        print(f"  reading {r:.1f} -> avg of last {window.size} = {running / window.size:.2f}")


def printer_scenario() -> None:
    print("\n=== SCENARIO 2: Printer Spooler (capacity 3) ===")
    spool = CircularQueue(3)
    events = ["+Resume.pdf", "+Report.docx", "+Ticket.png", "+Notes.txt", "print",
              "+Notes.txt", "print", "print", "print", "print"]
    for e in events:
        if e.startswith("+"):
            job = e[1:]
            if spool.enqueue(job):
                print(f"  queued   {job:<12} {spool.raw_layout()}")
            else:
                print(f"  REJECTED {job} (spooler full)")
        elif spool.is_empty():
            print("  printer idle - no jobs")
        else:
            job = spool.dequeue()
            print(f"  printing {job:<12} {spool.raw_layout()}")


def main() -> None:
    print("=== CIRCULAR QUEUE OPERATIONS ===")
    q = CircularQueue(5)
    for i in range(1, 6):
        q.enqueue(i * 10)
    print(f"  full?  {q.is_full()}  {q.raw_layout()}")
    print(f"  dequeue {q.dequeue()}, {q.dequeue()}")
    q.enqueue(60)
    q.enqueue(70)                               # these wrap to index 0 and 1
    print(f"  after wrap: {q.raw_layout()}")
    print("  FIFO order: " + " ".join(map(str, q)))
    print(f"  peek = {q.peek()}")
    q.delete_queue()
    print(f"  after delete_queue: is_empty={q.is_empty()}")
    sensor_scenario()
    printer_scenario()


if __name__ == "__main__":
    main()
