"""
============================================================================
 MODULE 01 : DYNAMIC ARRAY (custom ArrayList built from scratch)
============================================================================

 WHAT IS A DYNAMIC ARRAY?
 ------------------------
 A fixed-capacity array wrapped in logic that GROWS it automatically. When
 full, allocate a bigger array (2x here; CPython's list grows ~1.125x) and
 copy every element across.

     size     = number of elements stored
     capacity = length of the hidden backing array

 To be honest to the "from scratch" idea we do NOT append to a Python list;
 we allocate raw fixed-size blocks with ctypes (like `new Object[n]` in Java)
 and manage size/capacity ourselves.

 AMORTISED ANALYSIS
 ------------------
 Doubling means copies of size 1, 2, 4, ... n -> total < 2n work spread over
 n appends => O(1) amortised per append. We shrink to half at 1/4 occupancy
 (not 1/2) to avoid "thrashing" at the boundary.

 TIME COMPLEXITY
 ---------------
  | get / set          | O(1)           |
  | append             | O(1) amortised |
  | insert(index, v)   | O(n)           |
  | remove(index)      | O(n)           |
  | pop() (last)       | O(1) amortised |
  | index_of/contains  | O(n)           |
  Space: O(n)

 REAL-WORLD USE CASES: Python list, Java ArrayList, C++ vector, shopping
 carts, chat histories - any list that grows.

 HOW TO RUN:  python dynamic_array_list.py
============================================================================
"""

import ctypes


class MyArrayList:
    DEFAULT_CAPACITY = 4

    def __init__(self, capacity: int = DEFAULT_CAPACITY):
        if capacity <= 0:
            raise ValueError("capacity must be > 0")
        self._size = 0
        self._capacity = capacity
        self._data = self._make_block(capacity)
        self.resize_count = 0

    @staticmethod
    def _make_block(n: int):
        """Allocate a raw, fixed-size array of n Python object references."""
        return (n * ctypes.py_object)()

    # ---------------- helpers ----------------
    def __len__(self) -> int:
        return self._size

    @property
    def capacity(self) -> int:
        return self._capacity

    def is_empty(self) -> bool:
        return self._size == 0

    def _check(self, index: int) -> None:
        if not 0 <= index < self._size:
            raise IndexError(f"Index {index}, size {self._size}")

    def _resize(self, new_capacity: int) -> None:
        """The only O(n) step of append: copy into a bigger (or smaller) block."""
        block = self._make_block(new_capacity)
        for i in range(self._size):
            block[i] = self._data[i]
        self._data, self._capacity = block, new_capacity
        self.resize_count += 1

    # ---------------- add ----------------
    def append(self, value) -> None:
        if self._size == self._capacity:
            self._resize(2 * self._capacity)          # grow 2x when full
        self._data[self._size] = value
        self._size += 1

    def insert(self, index: int, value) -> None:
        if not 0 <= index <= self._size:
            raise IndexError(f"Index {index}")
        if self._size == self._capacity:
            self._resize(2 * self._capacity)
        for i in range(self._size, index, -1):       # shift right
            self._data[i] = self._data[i - 1]
        self._data[index] = value
        self._size += 1

    # ---------------- read / write ----------------
    def __getitem__(self, index: int):
        self._check(index)
        return self._data[index]                      # O(1)

    def __setitem__(self, index: int, value) -> None:
        self._check(index)
        self._data[index] = value

    def index_of(self, value) -> int:
        for i in range(self._size):
            if self._data[i] == value:
                return i
        return -1

    def __contains__(self, value) -> bool:
        return self.index_of(value) != -1

    # ---------------- remove ----------------
    def remove_at(self, index: int):
        self._check(index)
        removed = self._data[index]
        for i in range(index, self._size - 1):        # shift left
            self._data[i] = self._data[i + 1]
        self._size -= 1
        self._data[self._size] = None                 # drop reference for GC
        # shrink at 1/4 occupancy to avoid thrashing
        if 0 < self._size == self._capacity // 4 and self._capacity // 2 >= self.DEFAULT_CAPACITY:
            self._resize(self._capacity // 2)
        return removed

    def remove(self, value) -> bool:
        i = self.index_of(value)
        if i == -1:
            return False
        self.remove_at(i)
        return True

    def pop(self):
        if self.is_empty():
            raise IndexError("pop from empty list")
        return self.remove_at(self._size - 1)

    def clear(self) -> None:
        self._data = self._make_block(self.DEFAULT_CAPACITY)
        self._capacity, self._size = self.DEFAULT_CAPACITY, 0

    # ---------------- iteration ----------------
    def __iter__(self):
        for i in range(self._size):
            yield self._data[i]

    def __repr__(self) -> str:
        return "[" + ", ".join(repr(x) for x in self) + "]"


# --------------------------------------------------------------------------
#  SCENARIO: E-commerce shopping cart that grows and shrinks
# --------------------------------------------------------------------------
class CartItem:
    def __init__(self, name: str, price: float, qty: int = 1):
        self.name, self.price, self.qty = name, price, qty

    def total(self) -> float:
        return self.price * self.qty

    def __eq__(self, other) -> bool:
        return isinstance(other, CartItem) and other.name == self.name

    def __repr__(self) -> str:
        return f"{self.name} x{self.qty}"


def shopping_cart_scenario() -> None:
    print("\n=== SCENARIO: Shopping Cart (dynamic array) ===")
    cart = MyArrayList(2)
    catalogue = [("Keyboard", 1499), ("Mouse", 699), ("Monitor", 10999),
                 ("USB-C Hub", 1899), ("Webcam", 2499), ("Headphones", 3999)]
    for name, price in catalogue:
        before = cart.capacity
        cart.append(CartItem(name, price))
        note = f"  <-- RESIZED {before} -> {cart.capacity}" if cart.capacity != before else ""
        print(f"  added {name:<11} size={len(cart)} cap={cart.capacity}{note}")

    cart[1].qty = 3                                # O(1) access
    cart.remove(CartItem("Webcam", 0))            # O(n) find + shift
    print(f"  cart now : {cart}")
    print(f"  subtotal : Rs {sum(item.total() for item in cart):.2f}")   # uses __iter__

    print("  removing items until the cart shrinks...")
    while len(cart) > 1:
        removed = cart.pop()
        print(f"  removed {removed.name:<11} size={len(cart)} cap={cart.capacity}")
    print(f"  total resizes performed: {cart.resize_count}")


def main() -> None:
    print("=== CUSTOM ArrayList OPERATIONS ===")
    lst = MyArrayList()
    for i in range(1, 10):
        lst.append(i * 10)
        print(f"  append({i * 10}) -> size={len(lst)} capacity={lst.capacity}")
    print(f"  list            : {lst}")
    lst.insert(0, 5)
    print(f"  insert(0, 5)    : {lst}")
    print(f"  lst[3]          : {lst[3]}")
    old = lst[3]
    lst[3] = 333
    print(f"  lst[3] = 333    : old value {old}")
    print(f"  index_of(70)    : {lst.index_of(70)}")
    print(f"  999 in lst      : {999 in lst}")
    print(f"  remove_at(0)    : {lst.remove_at(0)} -> {lst}")
    try:
        lst[100]
    except IndexError as e:
        print(f"  lst[100] raised : {e}")

    shopping_cart_scenario()


if __name__ == "__main__":
    main()
