"""
============================================================================
 MODULE 06 : HASH TABLE with SEPARATE CHAINING (built from scratch)
============================================================================

 HASHING: a hash function maps a key to an integer; index = hash mod capacity.
 With a good (uniform) function, each lookup touches a tiny bucket -> O(1) avg.

 TERMINOLOGY: hash function, hash value, bucket, COLLISION (two keys -> same
 bucket), LOAD FACTOR alpha = size / capacity.

 SEPARATE CHAINING: every bucket holds a linked list of entries.

    bucket 0 : -> None
    bucket 1 : -> [apple|5] -> [grape|2] -> None     (collision!)
    bucket 2 : -> [mango|7] -> None

 When alpha > 0.75 we double the capacity and REHASH every entry (indices
 change because capacity changed).

 PROS: never "full", easy deletion.  CONS: pointer overhead, poor locality.
 (Open addressing - linear/quadratic probing, double hashing - is in
  hash_table_open_addressing.py. Python's own dict uses open addressing.)

 TIME COMPLEXITY
  | put/get/remove | O(1) average | O(n) worst (everything collides) |
  | rehash         | O(n), amortised O(1) per put                      |
  Space: O(n + capacity)

 HOW TO RUN:  python hash_table_chaining.py
============================================================================
"""
import hashlib
import secrets


class _Entry:
    __slots__ = ("key", "value", "next")

    def __init__(self, key, value, nxt):
        self.key, self.value, self.next = key, value, nxt


class MyHashMap:
    def __init__(self, capacity: int = 8, max_load: float = 0.75):
        self.buckets = [None] * capacity
        self.size = 0
        self.max_load = max_load
        self.rehash_count = 0

    @staticmethod
    def _hash(key) -> int:
        """Polynomial rolling hash for strings (h = 31*h + c, kept to 32 bits);
        other types use Python's hash(). Then mix high bits into low bits."""
        if isinstance(key, str):
            h = 0
            for ch in key:
                h = (31 * h + ord(ch)) & 0xFFFFFFFF
        else:
            h = hash(key) & 0xFFFFFFFF
        return h ^ (h >> 16)

    def _index(self, key, capacity=None) -> int:
        return self._hash(key) % (capacity or len(self.buckets))

    @property
    def capacity(self) -> int:
        return len(self.buckets)

    def load_factor(self) -> float:
        return self.size / len(self.buckets)

    def put(self, key, value) -> None:
        i = self._index(key)
        e = self.buckets[i]
        while e:
            if e.key == key:
                e.value = value                   # update existing key
                return
            e = e.next
        self.buckets[i] = _Entry(key, value, self.buckets[i])   # prepend O(1)
        self.size += 1
        if self.load_factor() > self.max_load:
            self._rehash()

    def get(self, key, default=None):
        e = self.buckets[self._index(key)]
        while e:
            if e.key == key:
                return e.value
            e = e.next
        return default

    def __contains__(self, key) -> bool:
        return self.get(key, _MISSING) is not _MISSING

    def remove(self, key):
        i = self._index(key)
        prev, e = None, self.buckets[i]
        while e:
            if e.key == key:
                if prev: prev.next = e.next       # unlink
                else: self.buckets[i] = e.next
                self.size -= 1
                return e.value
            prev, e = e, e.next
        return None

    def _rehash(self) -> None:
        """Double capacity and move every node to its NEW bucket."""
        old = self.buckets
        self.buckets = [None] * (2 * len(old))
        for head in old:
            e = head
            while e:
                nxt = e.next
                i = self._index(e.key)
                e.next = self.buckets[i]
                self.buckets[i] = e
                e = nxt
        self.rehash_count += 1

    def longest_chain(self) -> int:
        best = 0
        for head in self.buckets:
            n, e = 0, head
            while e:
                n, e = n + 1, e.next
            best = max(best, n)
        return best

    def print_buckets(self) -> None:
        for i, head in enumerate(self.buckets):
            parts, e = [], head
            while e:
                parts.append(f"-> ({e.key}={e.value})"); e = e.next
            print(f"    [{i:2d}] " + " ".join(parts))


_MISSING = object()


# --------------------------------------------------------------------------
#  SCENARIO: User Login System
#  username -> {salt, sha256(salt + password), failed_attempts, locked}
#  Production systems use a slow KDF (bcrypt/scrypt/Argon2, or
#  hashlib.pbkdf2_hmac) instead of a single SHA-256.
# --------------------------------------------------------------------------
class AuthService:
    MAX_ATTEMPTS = 3

    def __init__(self):
        self.users = MyHashMap(4)

    @staticmethod
    def _digest(salt: str, password: str) -> str:
        return hashlib.sha256((salt + password).encode()).hexdigest()

    def register(self, username: str, password: str) -> bool:
        key = username.lower()
        if key in self.users:
            print(f"  register {username} -> FAILED (username taken)")
            return False
        salt = secrets.token_hex(8)
        self.users.put(key, {"salt": salt, "hash": self._digest(salt, password),
                             "failed": 0, "locked": False})            # O(1) avg
        print(f"  register {username} -> OK")
        return True

    def login(self, username: str, password: str) -> None:
        u = self.users.get(username.lower())                            # O(1) avg
        if u is None:
            print(f"  login {username} -> unknown user"); return
        if u["locked"]:
            print(f"  login {username} -> ACCOUNT LOCKED"); return
        if secrets.compare_digest(self._digest(u["salt"], password), u["hash"]):   # constant-time compare
            u["failed"] = 0
            print(f"  login {username} -> SUCCESS")
        else:
            u["failed"] += 1
            u["locked"] = u["failed"] >= self.MAX_ATTEMPTS
            extra = ", now LOCKED" if u["locked"] else ""
            print(f"  login {username} -> wrong password ({u['failed']}/{self.MAX_ATTEMPTS}{extra})")


def login_scenario() -> None:
    print("\n=== SCENARIO: User Login System (hash table of salted password hashes) ===")
    auth = AuthService()
    auth.register("srikar", "Dsa@2026")
    auth.register("himesh", "mininet!")
    auth.register("krrish", "sdn#rocks")
    auth.register("Srikar", "another")          # case-insensitive duplicate
    auth.login("srikar", "Dsa@2026")
    for pw in ("wrong1", "wrong2", "wrong3", "mininet!"):
        auth.login("himesh", pw)
    auth.login("ghost", "x")
    r = auth.users.get("srikar")
    print(f"  stored for srikar -> salt={r['salt']} hash={r['hash'][:16]}...")
    print(f"  table: {auth.users.size} users, capacity {auth.users.capacity}, "
          f"load {auth.users.load_factor():.2f}, rehashes {auth.users.rehash_count}")


def main() -> None:
    print("=== HASH TABLE (SEPARATE CHAINING) ===")
    fruit = MyHashMap(5, max_load=10.0)          # tiny + no resize: force collisions
    for i, name in enumerate(["apple", "mango", "grape", "kiwi", "banana", "cherry", "papaya", "guava"]):
        fruit.put(name, (i + 1) * 10)
    print("  capacity 5, 8 keys -> collisions are guaranteed:")
    fruit.print_buckets()
    print(f"  get(grape)={fruit.get('grape')} get(lychee)={fruit.get('lychee')}")
    fruit.put("grape", 999)
    print(f"  put(grape, 999) updates in place -> {fruit.get('grape')}")
    print(f"  remove(mango)={fruit.remove('mango')} size={fruit.size}")
    print(f"  load factor {fruit.load_factor():.2f}, longest chain {fruit.longest_chain()}")

    print("\n=== AUTOMATIC REHASHING (max load 0.75) ===")
    ids = MyHashMap()
    for i in range(1, 51):
        cap = ids.capacity
        ids.put(i * 7919, f"user{i}")
        if ids.capacity != cap:
            print(f"  size {ids.size:2d} -> resized {cap} -> {ids.capacity} (rehash #{ids.rehash_count})")
    print(f"  final: size={ids.size} capacity={ids.capacity} load={ids.load_factor():.2f} "
          f"longest chain={ids.longest_chain()}")
    login_scenario()


if __name__ == "__main__":
    main()
