"""
============================================================================
 MODULE 03 : STACK (Linked-List / Node-based)
============================================================================

 The TOP of the stack is the HEAD of a singly linked list:
   push -> insert at head (O(1))     pop -> remove head (O(1))
 No capacity limit, so there is no is_full().

       top
        |
      [30|*] -> [20|*] -> [10|None]

 ARRAY vs LINKED STACK
  | Aspect         | Array            | Linked list          |
  |----------------|------------------|----------------------|
  | Capacity       | fixed            | unbounded            |
  | Memory/element | 1 slot           | 1 slot + 1 reference |
  | Cache locality | excellent        | poor                 |
  | push/pop       | O(1)             | O(1)                 |

 TIME COMPLEXITY: create/push/pop/peek/is_empty/delete all O(1). Space O(n).

 HOW TO RUN:  python stack_linked_list.py
============================================================================
"""


class Node:
    __slots__ = ("value", "next")

    def __init__(self, value, next=None):
        self.value, self.next = value, next


class LinkedStack:
    def __init__(self):
        self._top = None
        self._size = 0

    def is_empty(self) -> bool: return self._top is None
    def __len__(self) -> int: return self._size

    def push(self, v) -> None:
        self._top = Node(v, self._top)        # new node points at old top
        self._size += 1

    def pop(self):
        if self.is_empty():
            raise IndexError("Stack underflow")
        v = self._top.value
        self._top = self._top.next
        self._size -= 1
        return v

    def peek(self):
        if self.is_empty():
            raise IndexError("Stack is empty")
        return self._top.value

    def delete_stack(self) -> None:
        self._top, self._size = None, 0

    def __repr__(self) -> str:
        vals, cur = [], self._top
        while cur:
            vals.append(str(cur.value)); cur = cur.next
        return "[top] " + " ".join(vals) + " [bottom]"


# --------------------------------------------------------------------------
#  SCENARIO 1: Editor UNDO / REDO with two stacks
# --------------------------------------------------------------------------
class Editor:
    def __init__(self):
        self.text = ""
        self.undo_stack, self.redo_stack = LinkedStack(), LinkedStack()

    def type(self, s: str) -> None:
        self.undo_stack.push(self.text)       # snapshot BEFORE change
        self.redo_stack.delete_stack()        # new edit kills redo history
        self.text += s
        print(f"  type   {repr(s):<10} -> \"{self.text}\"")

    def undo(self) -> None:
        if self.undo_stack.is_empty():
            print("  undo   (nothing to undo)"); return
        self.redo_stack.push(self.text)
        self.text = self.undo_stack.pop()
        print(f"  undo              -> \"{self.text}\"")

    def redo(self) -> None:
        if self.redo_stack.is_empty():
            print("  redo   (nothing to redo)"); return
        self.undo_stack.push(self.text)
        self.text = self.redo_stack.pop()
        print(f"  redo              -> \"{self.text}\"")


# --------------------------------------------------------------------------
#  SCENARIO 2: Next greater stock price (monotonic stack) - O(n)
# --------------------------------------------------------------------------
def next_greater(prices):
    ans = [-1] * len(prices)
    st = LinkedStack()                        # indices, values strictly decreasing
    for i in range(len(prices) - 1, -1, -1):
        while not st.is_empty() and prices[st.peek()] <= prices[i]:
            st.pop()                          # can never be anyone's answer now
        ans[i] = -1 if st.is_empty() else prices[st.peek()]
        st.push(i)
    return ans


def main() -> None:
    print("=== LINKED-LIST STACK OPERATIONS ===")
    s = LinkedStack()
    for x in "ABCD":
        s.push(x)
    print(f"  after pushes : {s} size={len(s)}")
    print(f"  peek         : {s.peek()}")
    print(f"  pop          : {s.pop()} -> {s}")
    s.delete_stack()
    print(f"  after delete : is_empty={s.is_empty()}")

    print("\n=== SCENARIO 1: Editor Undo / Redo ===")
    e = Editor()
    e.type("Hello"); e.type(" DSA"); e.type(" world")
    e.undo(); e.undo(); e.redo()
    e.type("!"); e.redo()

    print("\n=== SCENARIO 2: Next Greater Stock Price (monotonic stack) ===")
    prices = [100, 80, 60, 70, 60, 75, 85]
    for i, (p, g) in enumerate(zip(prices, next_greater(prices))):
        print(f"  day {i} price {p:3d} -> next higher price: {'none' if g == -1 else g}")


if __name__ == "__main__":
    main()
