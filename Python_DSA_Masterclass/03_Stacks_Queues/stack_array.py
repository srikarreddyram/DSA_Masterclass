"""
============================================================================
 MODULE 03 : STACK (Array-based, fixed capacity)
============================================================================

 LIFO - Last In, First Out (a pile of plates).

       push(30)          pop() -> 30
       | 30 | <- top     |    |
       | 20 |            | 20 | <- top
       | 10 |            | 10 |
       +----+            +----+

 ARRAY IMPLEMENTATION: a pre-sized array + integer `top` (-1 = empty).
   push: arr[top+1] = v; top += 1       pop: v = arr[top]; top -= 1
 Fixed capacity -> is_full() exists. (Python's list.append/pop is the
 everyday stack, but here we build it ourselves to show the mechanics.)

 REAL-WORLD USE CASES: call stack, undo, bracket matching, expression
 evaluation, DFS / backtracking.

 TIME COMPLEXITY
  create O(n)* | push O(1) | pop O(1) | peek O(1) | is_empty O(1)
  is_full O(1) | delete O(1)          Space: O(n)
  * pre-allocating n slots

 HOW TO RUN:  python stack_array.py
============================================================================
"""
import re


class ArrayStack:
    def __init__(self, capacity: int):
        self._arr = [None] * capacity      # pre-allocated fixed block
        self._top = -1

    def is_empty(self) -> bool: return self._top == -1
    def is_full(self) -> bool: return self._top == len(self._arr) - 1
    def __len__(self) -> int: return self._top + 1

    def push(self, v) -> None:
        if self.is_full():
            raise OverflowError(f"Stack overflow (capacity {len(self._arr)})")
        self._top += 1
        self._arr[self._top] = v

    def pop(self):
        if self.is_empty():
            raise IndexError("Stack underflow")
        v = self._arr[self._top]
        self._arr[self._top] = None        # avoid loitering reference
        self._top -= 1
        return v

    def peek(self):
        if self.is_empty():
            raise IndexError("Stack is empty")
        return self._arr[self._top]

    def delete_stack(self) -> None:
        self._arr = [None] * len(self._arr)
        self._top = -1

    def __repr__(self) -> str:
        return "[bottom] " + " ".join(map(str, self._arr[: self._top + 1])) + " [top]"


# --------------------------------------------------------------------------
#  SCENARIO 1: Code editor bracket validator
# --------------------------------------------------------------------------
def validate_brackets(code: str) -> str:
    pairs = {")": "(", "]": "[", "}": "{"}
    st = ArrayStack(len(code) or 1)
    for i, c in enumerate(code):
        if c in "([{":
            st.push((c, i))
        elif c in pairs:
            if st.is_empty():
                return f"unexpected '{c}' at col {i}"
            open_c, j = st.pop()
            if pairs[c] != open_c:
                return f"'{open_c}' at col {j} closed by '{c}' at col {i}"
    if st.is_empty():
        return "VALID"
    c, j = st.peek()
    return f"unclosed '{c}' at col {j}"


# --------------------------------------------------------------------------
#  SCENARIO 2: Postfix (Reverse Polish) calculator
# --------------------------------------------------------------------------
def evaluate_postfix(expr: str) -> float:
    tokens = expr.split()
    st = ArrayStack(len(tokens))
    ops = {"+": lambda a, b: a + b, "-": lambda a, b: a - b,
           "*": lambda a, b: a * b, "/": lambda a, b: a / b}
    for t in tokens:
        if re.fullmatch(r"-?\d+(\.\d+)?", t):
            st.push(float(t))
        else:
            b, a = st.pop(), st.pop()      # NOTE: b is popped first
            st.push(ops[t](a, b))
    return st.pop()


def main() -> None:
    print("=== ARRAY STACK OPERATIONS ===")
    s = ArrayStack(4)
    print(f"  is_empty: {s.is_empty()}")
    for i in range(1, 5):
        s.push(i * 10)
        print(f"  push {i * 10} -> {s}")
    print(f"  is_full : {s.is_full()}")
    try:
        s.push(50)
    except OverflowError as e:
        print(f"  push 50 -> {e}")
    print(f"  peek    : {s.peek()}")
    print(f"  pop     : {s.pop()} -> {s}")
    print(f"  pop     : {s.pop()} -> {s}")
    s.delete_stack()
    print(f"  after delete_stack is_empty={s.is_empty()}")
    try:
        s.pop()
    except IndexError as e:
        print(f"  pop on empty -> {e}")

    print("\n=== SCENARIO 1: Bracket Validator ===")
    for code in ["int f() { return a[(b+c)]; }", "if (x > 0 { y = [1, 2); }", "while (true) {", "a = b)"]:
        print(f"  {code:<32} => {validate_brackets(code)}")

    print("\n=== SCENARIO 2: Postfix Calculator ===")
    for e in ["5 1 2 + 4 * + 3 -", "2 3 4 * +", "100 5 / 3 -"]:
        print(f"  {e:<20} = {evaluate_postfix(e):.2f}")


if __name__ == "__main__":
    main()
