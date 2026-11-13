"""
============================================================================
 MODULE 01 : STANDARD ARRAYS (1D and 2D)
============================================================================

 WHAT IS AN ARRAY?
 -----------------
 A FIXED-SIZE, CONTIGUOUS block of memory holding elements of the SAME type.
 Because every slot has the same width, the address of element i is:

       address(arr[i]) = base_address + i * element_size

 That arithmetic gives O(1) random access.

 PYTHON NOTE
 -----------
 Python's built-in `list` is a DYNAMIC array of object references (see
 dynamic_array_list.py). A true fixed-size, typed, contiguous array lives in
 the `array` module (array.array('i', ...)) or in NumPy. In this file we use
 `array.array` for 1D to stay faithful to the classic definition, and a
 pre-sized list of lists for 2D (what most interview code does).

 REAL-WORLD USE CASES
 --------------------
  - Lookup tables, sensor buffers, audio samples
  - Images / game boards / spreadsheets (2D)
  - Backing store for heaps, hash tables, dynamic arrays

 TIME COMPLEXITY (1D, size n)
 ----------------------------
  | Operation                | Time | Notes                       |
  |--------------------------|------|-----------------------------|
  | Create                   | O(n) | every slot initialised      |
  | Insert into empty slot   | O(1) |                             |
  | Insert with shifting     | O(n) | elements after i move right |
  | Access arr[i]            | O(1) |                             |
  | Traverse / linear search | O(n) |                             |
  | Delete (mark empty)      | O(1) |                             |
  | Delete with shifting     | O(n) |                             |
  Space: O(n)

 TIME COMPLEXITY (2D, m x n): Create O(mn) | Insert/Access/Delete O(1)
                              Traverse/Search O(mn) | Space O(mn)

 HOW TO RUN:  python standard_arrays.py
============================================================================
"""

from array import array

EMPTY = -(2 ** 31)          # sentinel for "empty cell" (like Integer.MIN_VALUE)


class SingleDimensionArray:
    """Fixed-size typed int array with explicit empty-slot tracking."""

    def __init__(self, size: int):
        # array('i') = contiguous C ints, exactly like int[] in Java/C
        self.arr = array("i", [EMPTY] * size)     # O(n)

    def insert(self, index: int, value: int) -> bool:
        """Insert into an EMPTY slot - O(1)."""
        if not 0 <= index < len(self.arr):
            print(f"  [insert] index {index} out of bounds")
            return False
        if self.arr[index] != EMPTY:
            print(f"  [insert] cell {index} already occupied")
            return False
        self.arr[index] = value
        return True

    def insert_with_shift(self, index: int, value: int) -> None:
        """O(n): shift right (walking BACKWARDS so nothing is overwritten)."""
        for i in range(len(self.arr) - 1, index, -1):
            self.arr[i] = self.arr[i - 1]
        self.arr[index] = value

    def get(self, index: int):
        if 0 <= index < len(self.arr) and self.arr[index] != EMPTY:
            return self.arr[index]            # O(1)
        return None

    def traverse(self) -> None:
        print("  [" + ", ".join("_" if v == EMPTY else str(v) for v in self.arr) + "]")

    def linear_search(self, target: int) -> int:
        for i, v in enumerate(self.arr):      # O(n)
            if v == target:
                return i
        return -1

    def delete(self, index: int) -> None:
        if 0 <= index < len(self.arr):
            self.arr[index] = EMPTY           # O(1)

    def delete_with_shift(self, index: int) -> None:
        for i in range(index, len(self.arr) - 1):   # O(n) shift left
            self.arr[i] = self.arr[i + 1]
        self.arr[-1] = EMPTY


class TwoDimensionArray:
    def __init__(self, rows: int, cols: int):
        # IMPORTANT: [[EMPTY]*cols]*rows would create `rows` references to the
        # SAME inner list - a classic Python bug. Use a comprehension instead.
        self.grid = [[EMPTY] * cols for _ in range(rows)]
        self.rows, self.cols = rows, cols

    def insert(self, r: int, c: int, value: int) -> bool:
        if not (0 <= r < self.rows and 0 <= c < self.cols) or self.grid[r][c] != EMPTY:
            return False
        self.grid[r][c] = value
        return True

    def get(self, r: int, c: int):
        if 0 <= r < self.rows and 0 <= c < self.cols and self.grid[r][c] != EMPTY:
            return self.grid[r][c]
        return None

    def traverse(self) -> None:          # row-major order - O(mn)
        for row in self.grid:
            print("  " + "".join(f"{('_' if v == EMPTY else v):>4}" for v in row))

    def search(self, target: int):
        for r in range(self.rows):
            for c in range(self.cols):
                if self.grid[r][c] == target:
                    return (r, c)
        return None

    def delete(self, r: int, c: int) -> None:
        self.grid[r][c] = EMPTY


# --------------------------------------------------------------------------
#  SCENARIO 1: Weekly temperature analysis (1D)
# --------------------------------------------------------------------------
def temperature_scenario() -> None:
    print("\n=== SCENARIO 1: Weekly Temperature Analysis (1D array) ===")
    days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
    temps = array("d", [31.5, 33.0, 29.8, 35.2, 34.1, 30.0, 32.7])
    total, hottest = 0.0, 0
    for i, t in enumerate(temps):             # one O(n) pass
        total += t
        if t > temps[hottest]:
            hottest = i
    avg = total / len(temps)
    print(f"  Average temperature : {avg:.2f} C")
    print(f"  Hottest day         : {days[hottest]} ({temps[hottest]:.1f} C)")
    print("  Days above average  : " + " ".join(d for d, t in zip(days, temps) if t > avg))


# --------------------------------------------------------------------------
#  SCENARIO 2: Cinema seat booking (2D)
# --------------------------------------------------------------------------
def cinema_scenario() -> None:
    print("\n=== SCENARIO 2: Cinema Seat Booking (2D array) ===")
    rows, seats = 4, 6
    hall = [["O"] * seats for _ in range(rows)]
    for r, c in [(0, 2), (0, 3), (2, 0), (2, 1), (3, 5), (0, 2)]:
        label = f"{chr(65 + r)}{c + 1}"
        if hall[r][c] == "X":
            print(f"  Seat {label} is ALREADY booked")
        else:
            hall[r][c] = "X"                  # O(1)
            print(f"  Booked seat {label}")
    print("  Screen this way --> [==============]")
    for r in range(rows):
        print(f"  Row {chr(65 + r)}: " + " ".join(hall[r]))
    for r in range(rows):                     # first pair of adjacent free seats
        for c in range(seats - 1):
            if hall[r][c] == "O" and hall[r][c + 1] == "O":
                print(f"  First pair of adjacent seats: {chr(65 + r)}{c + 1}-{chr(65 + r)}{c + 2}")
                return


def main() -> None:
    print("=== 1D ARRAY OPERATIONS ===")
    sda = SingleDimensionArray(8)
    for i, v in [(0, 10), (1, 20), (2, 30), (4, 50), (1, 99), (12, 5)]:
        sda.insert(i, v)
    sda.traverse()
    print(f"  get(2)              = {sda.get(2)}")
    print(f"  linear_search(50)   = {sda.linear_search(50)}")
    print(f"  linear_search(77)   = {sda.linear_search(77)}")
    sda.insert_with_shift(1, 15)
    print("  after insert_with_shift(1, 15):")
    sda.traverse()
    sda.delete(5)
    print("  after delete(5):")
    sda.traverse()
    sda.delete_with_shift(0)
    print("  after delete_with_shift(0):")
    sda.traverse()

    print("\n=== 2D ARRAY OPERATIONS ===")
    tda = TwoDimensionArray(3, 4)
    v = 1
    for r in range(3):
        for c in range(4):
            if (r + c) % 2 == 0:
                tda.insert(r, c, v * 10)
                v += 1
    tda.traverse()
    print(f"  get(1,1)   = {tda.get(1, 1)}")
    print(f"  search(50) = {tda.search(50)}")
    tda.delete(0, 0)
    print("  after delete(0,0):")
    tda.traverse()

    temperature_scenario()
    cinema_scenario()


if __name__ == "__main__":
    main()
