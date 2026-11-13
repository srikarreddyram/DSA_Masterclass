"""
============================================================================
 MODULE 07 : SORTING ALGORITHMS
 Bubble, Selection, Insertion, Bucket, Merge, Quick, Heap
============================================================================

 TERMINOLOGY
  In-place : O(1) (or O(log n) stack) extra memory
  Stable   : equal keys keep their original order (matters for multi-key sorts)
  Adaptive : faster on nearly-sorted input

 COMPARISON TABLE
  | Algorithm | Best       | Average    | Worst      | Space    | Stable | In-place |
  |-----------|------------|------------|------------|----------|--------|----------|
  | Bubble    | O(n)       | O(n^2)     | O(n^2)     | O(1)     | yes    | yes      |
  | Selection | O(n^2)     | O(n^2)     | O(n^2)     | O(1)     | NO     | yes      |
  | Insertion | O(n)       | O(n^2)     | O(n^2)     | O(1)     | yes    | yes      |
  | Bucket    | O(n+k)     | O(n+k)     | O(n^2)     | O(n+k)   | yes*   | no       |
  | Merge     | O(n log n) | O(n log n) | O(n log n) | O(n)     | yes    | no       |
  | Quick     | O(n log n) | O(n log n) | O(n^2)     | O(log n) | NO     | yes      |
  | Heap      | O(n log n) | O(n log n) | O(n log n) | O(1)     | NO     | yes      |
  * if buckets are sorted stably

 IN PRACTICE: Python's sorted()/list.sort() use TimSort (merge + insertion,
 stable, adaptive). Learn these to understand the trade-offs and to answer
 interview questions.

 Every function sorts IN PLACE and accepts key= and reverse= just like
 list.sort(), so the same code sorts numbers or product records.

 HOW TO RUN:  python sorting_algorithms.py
============================================================================
"""
import math
import random
import time


def _cmp(key, reverse):
    """Return a 'greater than' test that honours key/reverse."""
    if reverse:
        return lambda a, b: key(a) < key(b)
    return lambda a, b: key(a) > key(b)


def bubble_sort(a, key=lambda x: x, reverse=False):
    gt = _cmp(key, reverse)
    n = len(a)
    for p in range(n - 1):
        swapped = False
        for j in range(n - 1 - p):              # last p items already in place
            if gt(a[j], a[j + 1]):
                a[j], a[j + 1] = a[j + 1], a[j]
                swapped = True
        if not swapped:                          # early exit -> O(n) best case
            break


def selection_sort(a, key=lambda x: x, reverse=False):
    gt = _cmp(key, reverse)
    for i in range(len(a) - 1):
        m = i
        for j in range(i + 1, len(a)):
            if gt(a[m], a[j]):                   # a[j] should come before a[m]
                m = j
        if m != i:
            a[i], a[m] = a[m], a[i]              # long-distance swap = unstable


def insertion_sort(a, key=lambda x: x, reverse=False):
    gt = _cmp(key, reverse)
    for i in range(1, len(a)):
        cur, j = a[i], i - 1
        while j >= 0 and gt(a[j], cur):          # shift larger items right
            a[j + 1] = a[j]
            j -= 1
        a[j + 1] = cur


def bucket_sort(a):
    """Non-negative integers. bucket index = value * k // (max + 1)."""
    if not a:
        return
    k = math.ceil(math.sqrt(len(a)))
    mx = max(a)
    buckets = [[] for _ in range(k)]
    for v in a:
        buckets[v * k // (mx + 1)].append(v)
    i = 0
    for b in buckets:
        insertion_sort(b)                        # small buckets
        for v in b:
            a[i] = v
            i += 1


def merge_sort(a, key=lambda x: x, reverse=False):
    gt = _cmp(key, reverse)

    def sort(lo, hi):
        if hi - lo <= 1:
            return
        mid = (lo + hi) // 2
        sort(lo, mid)
        sort(mid, hi)
        if not gt(a[mid - 1], a[mid]):           # halves already ordered
            return
        left, right = a[lo:mid], a[mid:hi]
        i = j = 0
        k = lo
        while i < len(left) and j < len(right):
            if not gt(left[i], right[j]):        # take LEFT on ties -> stable
                a[k] = left[i]; i += 1
            else:
                a[k] = right[j]; j += 1
            k += 1
        a[k:hi] = left[i:] + right[j:]           # leftovers

    sort(0, len(a))


_rng = random.Random(42)


def quick_sort(a, key=lambda x: x, reverse=False):
    gt = _cmp(key, reverse)

    def partition(lo, hi):                       # Lomuto, random pivot
        p = _rng.randint(lo, hi)
        a[p], a[hi] = a[hi], a[p]
        pivot, boundary = a[hi], lo
        for j in range(lo, hi):
            if gt(pivot, a[j]):                  # a[j] belongs left of pivot
                a[boundary], a[j] = a[j], a[boundary]
                boundary += 1
        a[boundary], a[hi] = a[hi], a[boundary]
        return boundary

    def sort(lo, hi):
        while lo < hi:
            p = partition(lo, hi)
            if p - lo < hi - p:                  # recurse on smaller side: O(log n) stack
                sort(lo, p - 1); lo = p + 1
            else:
                sort(p + 1, hi); hi = p - 1

    sort(0, len(a) - 1)


def heap_sort(a, key=lambda x: x, reverse=False):
    gt = _cmp(key, reverse)

    def sift_down(i, n):
        while True:
            l, r, big = 2 * i + 1, 2 * i + 2, i
            if l < n and gt(a[l], a[big]): big = l
            if r < n and gt(a[r], a[big]): big = r
            if big == i:
                return
            a[i], a[big] = a[big], a[i]
            i = big

    n = len(a)
    for i in range(n // 2 - 1, -1, -1):          # build max-heap O(n)
        sift_down(i, n)
    for end in range(n - 1, 0, -1):
        a[0], a[end] = a[end], a[0]              # max to its final slot
        sift_down(0, end)


# --------------------------------------------------------------------------
#  SCENARIO: E-commerce product catalogue
# --------------------------------------------------------------------------
def catalogue():
    return [
        {"name": "Boat Rockerz 450", "price": 1499, "rating": 4.1, "reviews": 120_000},
        {"name": "Sony WH-1000XM5", "price": 29990, "rating": 4.6, "reviews": 8_200},
        {"name": "JBL Tune 760NC", "price": 5999, "rating": 4.3, "reviews": 21_000},
        {"name": "Noise Buds VS104", "price": 999, "rating": 3.9, "reviews": 64_000},
        {"name": "Sennheiser HD 450BT", "price": 9990, "rating": 4.3, "reviews": 5_400},
        {"name": "OnePlus Buds 3", "price": 5499, "rating": 4.3, "reviews": 30_500},
        {"name": "Realme Buds Air 5", "price": 3699, "rating": 4.1, "reviews": 41_000},
        {"name": "Apple AirPods Pro 2", "price": 24900, "rating": 4.6, "reviews": 15_000},
    ]


def fmt(p):
    return f"{p['name']:<22} Rs {p['price']:6d}  {p['rating']:.1f}*  ({p['reviews']} reviews)"


def catalogue_scenario() -> None:
    print("\n=== SCENARIO: Sorting an E-commerce Product Catalogue ===")
    items = catalogue()
    quick_sort(items, key=lambda p: p["price"])
    print("  Price: Low to High (quick sort)")
    for p in items:
        print("    " + fmt(p))

    merge_sort(items, key=lambda p: (p["rating"], p["reviews"]), reverse=True)
    print("  Customer Rating (merge sort, rating desc then #reviews desc)")
    for p in items:
        print("    " + fmt(p))

    stable, unstable = catalogue(), catalogue()
    insertion_sort(stable, key=lambda p: p["price"])
    insertion_sort(stable, key=lambda p: p["rating"], reverse=True)
    selection_sort(unstable, key=lambda p: p["price"])
    selection_sort(unstable, key=lambda p: p["rating"], reverse=True)

    def names(arr):
        return ", ".join(f"{p['name']}({p['price']})" for p in arr if p["rating"] == 4.1)

    print("  Two-pass sort (price, then rating) - look at the 4.1* products:")
    print(f"    stable (insertion) : {names(stable)}")
    print(f"    unstable(selection): {names(unstable)}  <- price order lost")


def benchmark() -> None:
    print("\n=== BENCHMARK (n = 3,000 random ints; times are indicative only) ===")
    rnd = random.Random(1)
    base = [rnd.randrange(1_000_000) for _ in range(3_000)]
    expected = sorted(base)
    for name, fn in [("Bubble", bubble_sort), ("Selection", selection_sort), ("Insertion", insertion_sort),
                     ("Bucket", bucket_sort), ("Merge", merge_sort), ("Quick", quick_sort),
                     ("Heap", heap_sort), ("TimSort*", list.sort)]:
        a = base[:]
        t0 = time.perf_counter()
        fn(a)
        ms = (time.perf_counter() - t0) * 1000
        print(f"  {name:<10} {ms:8.1f} ms   correct={a == expected}")
    print("  * built-in list.sort(), implemented in C")


def main() -> None:
    data = [64, 25, 12, 22, 11, 90, 25, 3]
    print("=== SORTING ALGORITHMS ===")
    print(f"  input     : {data}")
    for name, fn in [("bubble", bubble_sort), ("selection", selection_sort), ("insertion", insertion_sort),
                     ("bucket", bucket_sort), ("merge", merge_sort), ("quick", quick_sort), ("heap", heap_sort)]:
        a = data[:]
        fn(a)
        print(f"  {name:<10}: {a}")
    a = data[:]
    merge_sort(a, reverse=True)
    print(f"  merge desc: {a}")
    catalogue_scenario()
    benchmark()


if __name__ == "__main__":
    main()
