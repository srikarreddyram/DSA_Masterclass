"""
============================================================================
 MODULE 06 : HASH TABLE with OPEN ADDRESSING (Linear / Quadratic / Double)
============================================================================

 All entries live directly in the array. On a collision, PROBE a sequence of
 slots until an empty one is found:

   Linear    : idx = (h + i)       mod m
   Quadratic : idx = (h + i*i)     mod m
   Double    : idx = (h + i*h2(k)) mod m     (h2(k) never 0)

 PRIMARY CLUSTERING: linear probing creates long runs of occupied slots that
 keep growing. Quadratic jumps out of runs; double hashing gives every key
 its own step size.

 DELETION NEEDS TOMBSTONES: emptying a slot would break the probe chain for
 keys inserted after it, so we write a DELETED marker instead. Searches skip
 it; inserts may reuse it.

 TABLE FULL? Open addressing must resize well before alpha reaches 1
 (we resize at 0.5). Chaining never becomes full but slows down.

 CHAINING vs OPEN ADDRESSING
  | Memory    | node pointers       | one compact array    |
  | Cache     | poor                | great (linear)       |
  | Load      | may exceed 1        | must stay < ~0.7     |
  | Deletion  | easy                | tombstones           |
 CPython's dict is open addressing with a perturbed probe sequence.

 TIME: put/get/remove O(1) average, O(n) worst.  Space O(m)

 HOW TO RUN:  python hash_table_open_addressing.py
============================================================================
"""
import random

DELETED = object()          # tombstone sentinel (identity-compared)


def java_string_hash(s: str) -> int:
    """Same as Java's String.hashCode, so 'Aa' and 'BB' collide here too."""
    h = 0
    for ch in s:
        h = (31 * h + ord(ch)) & 0xFFFFFFFF
    return h


class OpenHashTable:
    def __init__(self, capacity: int, mode: str = "linear", max_load: float = 0.5):
        self.keys = [None] * capacity
        self.values = [None] * capacity
        self.mode = mode
        self.max_load = max_load
        self.size = 0               # live keys
        self.used = 0               # live keys + tombstones
        self.total_probes = 0

    @property
    def capacity(self) -> int:
        return len(self.keys)

    def _h1(self, k, m): return java_string_hash(k) % m

    def _h2(self, k, m):
        h = (java_string_hash(k) * 0x9E3779B1) & 0x7FFFFFFF    # different mixing
        return 1 + h % (m - 1)

    def _probe(self, k, i, m) -> int:
        h = self._h1(k, m)
        if self.mode == "linear":    return (h + i) % m
        if self.mode == "quadratic": return (h + i * i) % m
        return (h + i * self._h2(k, m)) % m

    def put(self, key, value) -> None:
        if (self.used + 1) / self.capacity > self.max_load:
            self._resize(2 * self.capacity)
        first_tomb = None
        for i in range(self.capacity):
            idx = self._probe(key, i, self.capacity)
            self.total_probes += 1
            slot = self.keys[idx]
            if slot is None:                                  # key absent
                target = idx if first_tomb is None else first_tomb
                if target == idx:
                    self.used += 1
                self.keys[target], self.values[target] = key, value
                self.size += 1
                return
            if slot is DELETED:
                if first_tomb is None:
                    first_tomb = idx
                continue
            if slot == key:
                self.values[idx] = value                       # update
                return
        if first_tomb is not None:
            self.keys[first_tomb], self.values[first_tomb] = key, value
            self.size += 1
            return
        self._resize(2 * self.capacity)                        # quadratic may skip slots
        self.put(key, value)

    def get(self, key):
        for i in range(self.capacity):
            idx = self._probe(key, i, self.capacity)
            slot = self.keys[idx]
            if slot is None:
                return None                                    # true hole ends search
            if slot is not DELETED and slot == key:
                return self.values[idx]
        return None

    def remove(self, key) -> bool:
        for i in range(self.capacity):
            idx = self._probe(key, i, self.capacity)
            slot = self.keys[idx]
            if slot is None:
                return False
            if slot is not DELETED and slot == key:
                self.keys[idx], self.values[idx] = DELETED, None   # tombstone, NOT None
                self.size -= 1
                return True
        return False

    def _resize(self, new_cap: int) -> None:
        old = [(k, v) for k, v in zip(self.keys, self.values) if k is not None and k is not DELETED]
        self.keys, self.values = [None] * new_cap, [None] * new_cap
        self.size = self.used = 0
        for k, v in old:                                       # tombstones are dropped
            self.put(k, v)

    def layout(self) -> str:
        rows = []
        for i, k in enumerate(self.keys):
            label = "." if k is None else "XX (tombstone)" if k is DELETED else k
            rows.append(f"    [{i:2d}] {label}")
        return "\n".join(rows)


def duplicate_payment_scenario() -> None:
    print("\n=== SCENARIO: Duplicate Payment Detector ===")
    seen = OpenHashTable(16, "double")
    blocked = 0
    for t in ["TXN-1001", "TXN-1002", "TXN-1003", "TXN-1002", "TXN-1004", "TXN-1001", "TXN-1005"]:
        if seen.get(t) is not None:
            blocked += 1
            print(f"  {t} -> DUPLICATE, payment blocked")
        else:
            seen.put(t, True)
            print(f"  {t} -> processed")
    print(f"  blocked {blocked} duplicate charge(s)")

    print("\n  Probe cost at load factor 0.9 (10,000 random IDs, table 11,113 slots):")
    rnd = random.Random(7)
    ids = [f"TXN-{rnd.getrandbits(64):x}" for _ in range(10_000)]
    for mode in ("linear", "quadratic", "double"):
        t = OpenHashTable(11_113, mode, max_load=0.95)        # prime size, no resize
        for k in ids:
            t.put(k, 1)
        print(f"    {mode.upper():<9} avg probes per insert = {t.total_probes / len(ids):.2f}")
    print("  -> linear probing suffers from primary clustering at high load.")


def main() -> None:
    print("=== LINEAR PROBING WITH TOMBSTONES ===")
    t = OpenHashTable(11, "linear", 0.8)
    for w in ["Aa", "BB", "C#", "java", "dsa", "heap"]:      # 'Aa' and 'BB' share a hash
        t.put(w, len(w))
    print(f"  hash('Aa')={java_string_hash('Aa')} == hash('BB')={java_string_hash('BB')} -> guaranteed collision")
    print(t.layout())
    print(f"  get(BB)={t.get('BB')} (found by probing past Aa)")
    t.remove("Aa")
    print("  remove(Aa) leaves a tombstone:")
    print(t.layout())
    print(f"  get(BB) after delete = {t.get('BB')}  <- still found thanks to the tombstone")
    t.put("Aa", 42)
    print(f"  put(Aa) again reuses the tombstone slot, get(Aa)={t.get('Aa')}")

    print("\n=== QUADRATIC & DOUBLE HASHING ===")
    for mode in ("quadratic", "double"):
        q = OpenHashTable(8, mode, 0.5)
        for i in range(20):
            q.put(f"key{i}", i)
        print(f"  {mode.upper():<9} 20 keys -> size={q.size} capacity={q.capacity} (auto-resized) get(key13)={q.get('key13')}")
    duplicate_payment_scenario()


if __name__ == "__main__":
    main()
