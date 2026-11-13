"""
============================================================================
 MODULE 08 : SEARCHING ALGORITHMS - Linear Search & Binary Search
============================================================================

 LINEAR SEARCH: check every element. Works on unsorted data, linked lists,
 streams.                               Best O(1) | Avg/Worst O(n) | Space O(1)

 BINARY SEARCH (SORTED data only): compare with the MIDDLE element and
 discard the half that cannot contain the target.
     n -> n/2 -> n/4 -> ... -> 1    => at most floor(log2 n) + 1 steps
 1 million items -> ~20 comparisons; 1 billion -> ~30.
                                       Best O(1) | Avg/Worst O(log n)
                                       Space O(1) iterative, O(log n) recursive

     lo                mid                hi
     [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]     target 23
      23 > 16 -> lo = mid + 1  ... found at index 5

 PITFALLS: off-by-one (lo <= hi for inclusive hi), duplicates (use
 lower/upper bound), and in fixed-width languages mid = lo + (hi-lo)//2 to
 avoid overflow (Python ints don't overflow, but the habit is good).
 Python's `bisect` module provides bisect_left / bisect_right.

 USE CASES: sorted indexes (ISBN, roll numbers, timestamps), git bisect,
 "binary search on the answer".

 HOW TO RUN:  python searching_algorithms.py
============================================================================
"""
import bisect
import math
import random

comparisons = 0


def linear_search(a, target) -> int:
    global comparisons
    for i, v in enumerate(a):
        comparisons += 1
        if v == target:
            return i
    return -1


def binary_search(a, target, key=lambda x: x) -> int:
    global comparisons
    lo, hi = 0, len(a) - 1
    while lo <= hi:
        mid = lo + (hi - lo) // 2
        comparisons += 1
        k = key(a[mid])
        if k == target:
            return mid
        if k < target:
            lo = mid + 1                         # discard left half
        else:
            hi = mid - 1                         # discard right half
    return -1


def binary_search_recursive(a, target, lo, hi) -> int:
    if lo > hi:
        return -1                                # empty range
    mid = (lo + hi) // 2
    if a[mid] == target:
        return mid
    if a[mid] < target:
        return binary_search_recursive(a, target, mid + 1, hi)
    return binary_search_recursive(a, target, lo, mid - 1)


def lower_bound(a, target) -> int:
    """First index with a[i] >= target (same as bisect.bisect_left)."""
    lo, hi = 0, len(a)                           # half-open [lo, hi)
    while lo < hi:
        mid = (lo + hi) // 2
        if a[mid] < target: lo = mid + 1
        else: hi = mid
    return lo


def upper_bound(a, target) -> int:
    """First index with a[i] > target (same as bisect.bisect_right)."""
    lo, hi = 0, len(a)
    while lo < hi:
        mid = (lo + hi) // 2
        if a[mid] <= target: lo = mid + 1
        else: hi = mid
    return lo


def library_scenario() -> None:
    global comparisons
    print("\n=== SCENARIO: Library ISBN Lookup (sorted database of 200,000 books) ===")
    rnd = random.Random(2026)
    isbns = set()
    while len(isbns) < 199_996:
        isbns.add(9_780_000_000_000 + rnd.randrange(1_999_999_999))
    db = [{"isbn": i, "title": f"Generated Title #{n}", "author": "Various", "copies": 1 + rnd.randrange(5)}
          for n, i in enumerate(isbns)]
    db += [
        {"isbn": 9780262046305, "title": "Introduction to Algorithms (4th ed.)", "author": "Cormen et al.", "copies": 3},
        {"isbn": 9780984782857, "title": "Cracking the Coding Interview", "author": "G. L. McDowell", "copies": 5},
        {"isbn": 9780134685991, "title": "Effective Java (3rd ed.)", "author": "Joshua Bloch", "copies": 2},
        {"isbn": 9781492051367, "title": "Fluent Python (2nd ed.)", "author": "Luciano Ramalho", "copies": 1},
    ]
    db.sort(key=lambda b: b["isbn"])             # sort the index ONCE
    isbn_list = [b["isbn"] for b in db]

    for q in (9780262046305, 9781492051367, 9780000000001):
        comparisons = 0
        li = linear_search(isbn_list, q)
        linear = comparisons
        comparisons = 0
        bi = binary_search(db, q, key=lambda b: b["isbn"])
        binary = comparisons
        if bi >= 0:
            b = db[bi]
            result = f"\"{b['title']}\" by {b['author']}, copies {b['copies']}"
        else:
            result = "NOT IN CATALOGUE"
        print(f"  ISBN {q} -> {result}")
        print(f"      linear search: {linear:7,d} comparisons | binary search: {binary} comparisons "
              f"(same answer: {li == bi})")
    print(f"  log2({len(db):,}) = {math.log2(len(db)):.1f}  -> binary search never needs more than "
          f"{int(math.log2(len(db))) + 1} probes")


def main() -> None:
    print("=== LINEAR vs BINARY SEARCH ===")
    unsorted = [38, 5, 91, 12, 72, 2, 23, 56, 16, 8]
    print(f"  unsorted {unsorted}")
    print(f"  linear_search(23) = {linear_search(unsorted, 23)}  (binary search would be WRONG here)")
    s = [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]
    print(f"  sorted   {s}")
    print(f"  binary_search(23)            = {binary_search(s, 23)}")
    print(f"  binary_search_recursive(72)  = {binary_search_recursive(s, 72, 0, len(s) - 1)}")
    print(f"  binary_search(40)            = {binary_search(s, 40)}  (absent)")
    print(f"  insertion point for 40       = {lower_bound(s, 40)} (bisect agrees: {bisect.bisect_left(s, 40)})")

    dup = [1, 3, 3, 3, 3, 7, 9, 9, 12]
    print("\n=== DUPLICATES: lower / upper bound ===")
    print(f"  array {dup}")
    first, last = lower_bound(dup, 3), upper_bound(dup, 3) - 1
    print(f"  first 3 at {first}, last 3 at {last}, count = {last - first + 1}")
    print(f"  count of 9 = {upper_bound(dup, 9) - lower_bound(dup, 9)}, "
          f"count of 5 = {upper_bound(dup, 5) - lower_bound(dup, 5)}")
    library_scenario()


if __name__ == "__main__":
    main()
