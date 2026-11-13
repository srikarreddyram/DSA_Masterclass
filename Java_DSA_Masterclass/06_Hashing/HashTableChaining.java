/*
 * ============================================================================
 *  MODULE 06 : HASH TABLE with SEPARATE CHAINING (built from scratch)
 * ============================================================================
 *
 *  WHAT IS HASHING?
 *  ----------------
 *  A HASH FUNCTION turns a key (e.g. "srikar") into an integer; we reduce it
 *  to a bucket index with  index = hash(key) mod capacity. If the function
 *  spreads keys evenly, each lookup touches only a tiny bucket -> O(1) avg.
 *
 *  TERMINOLOGY
 *   - Hash function : key -> integer (should be fast, deterministic, uniform)
 *   - Hash value    : the integer it produced
 *   - Bucket/slot   : array cell the key maps to
 *   - Collision     : two different keys map to the same bucket
 *   - Load factor   : alpha = size / capacity
 *
 *  COLLISION RESOLUTION #1 : SEPARATE CHAINING (this file)
 *  -------------------------------------------------------
 *  Every bucket holds a LINKED LIST of entries. Colliding keys simply join
 *  the chain.
 *
 *     bucket 0 : -> null
 *     bucket 1 : -> [apple|5] -> [grape|2] -> null     (collision!)
 *     bucket 2 : -> [mango|7] -> null
 *     bucket 3 : -> null
 *
 *  When alpha exceeds 0.75 we DOUBLE the capacity and REHASH every entry
 *  (indices change because capacity changed). This keeps chains short.
 *  (See HashTableOpenAddressing.java for linear/quadratic probing & double
 *   hashing, the other family of collision strategies.)
 *
 *  PROS / CONS OF CHAINING
 *   + Never "full"; degrades gracefully even when alpha > 1
 *   + Deletion is trivial (unlink a node)
 *   - Extra memory for node references; pointer-chasing hurts the CPU cache
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - java.util.HashMap (chaining; long chains turn into Red-Black trees)
 *   - Login/session stores, caches, symbol tables in compilers
 *   - Counting frequencies, de-duplication, database hash joins
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   | Operation       | Average | Worst (all keys collide) |
 *   |-----------------|---------|--------------------------|
 *   | put/get/remove  | O(1)    | O(n)                     |
 *   | resize/rehash   | O(n), amortised O(1) per put       |
 *   Space: O(n + capacity)
 *
 *  HOW TO RUN
 *  ----------
 *   javac HashTableChaining.java && java HashTableChaining
 * ============================================================================
 */

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class HashTableChaining {

    static class MyHashMap<K, V> {
        /** Chain node. */
        static class Entry<K, V> {
            final K key; V value; Entry<K, V> next;
            Entry(K k, V v, Entry<K, V> n) { key = k; value = v; next = n; }
        }

        private Entry<K, V>[] buckets;
        private int size;
        private final double maxLoad;
        int rehashCount = 0;

        @SuppressWarnings("unchecked")
        MyHashMap(int capacity, double maxLoad) {
            buckets = (Entry<K, V>[]) new Entry[capacity];
            this.maxLoad = maxLoad;
        }

        MyHashMap() { this(8, 0.75); }

        /**
         * Our own polynomial rolling hash for Strings (same idea as
         * String.hashCode: h = 31*h + c). Other key types fall back to
         * hashCode(). We then "spread" the bits so low bits are well mixed,
         * and mask off the sign bit before taking the modulus.
         */
        int indexFor(K key, int capacity) {
            int h;
            if (key instanceof String) {
                h = 0;
                for (char c : ((String) key).toCharArray()) h = 31 * h + c;  // overflow is fine
            } else {
                h = key.hashCode();
            }
            h ^= (h >>> 16);                       // mix high bits into low bits
            return (h & 0x7fffffff) % capacity;    // non-negative index
        }

        double loadFactor() { return (double) size / buckets.length; }
        int size() { return size; }
        int capacity() { return buckets.length; }

        void put(K key, V value) {
            int i = indexFor(key, buckets.length);
            for (Entry<K, V> e = buckets[i]; e != null; e = e.next) {
                if (e.key.equals(key)) { e.value = value; return; }   // update existing
            }
            buckets[i] = new Entry<>(key, value, buckets[i]);         // prepend: O(1)
            size++;
            if (loadFactor() > maxLoad) rehash();
        }

        V get(K key) {
            for (Entry<K, V> e = buckets[indexFor(key, buckets.length)]; e != null; e = e.next)
                if (e.key.equals(key)) return e.value;
            return null;
        }

        boolean containsKey(K key) { return get(key) != null; }

        V remove(K key) {
            int i = indexFor(key, buckets.length);
            Entry<K, V> prev = null;
            for (Entry<K, V> e = buckets[i]; e != null; prev = e, e = e.next) {
                if (e.key.equals(key)) {
                    if (prev == null) buckets[i] = e.next; else prev.next = e.next;   // unlink
                    size--;
                    return e.value;
                }
            }
            return null;
        }

        /** Double capacity and re-insert every entry: its bucket index changes. */
        @SuppressWarnings("unchecked")
        private void rehash() {
            Entry<K, V>[] old = buckets;
            buckets = (Entry<K, V>[]) new Entry[old.length * 2];
            for (Entry<K, V> head : old) {
                for (Entry<K, V> e = head; e != null; ) {
                    Entry<K, V> next = e.next;
                    int i = indexFor(e.key, buckets.length);
                    e.next = buckets[i];           // move node into new table
                    buckets[i] = e;
                    e = next;
                }
            }
            rehashCount++;
        }

        int longestChain() {
            int best = 0;
            for (Entry<K, V> head : buckets) {
                int len = 0;
                for (Entry<K, V> e = head; e != null; e = e.next) len++;
                best = Math.max(best, len);
            }
            return best;
        }

        List<K> keys() {
            List<K> out = new ArrayList<>();
            for (Entry<K, V> head : buckets) for (Entry<K, V> e = head; e != null; e = e.next) out.add(e.key);
            return out;
        }

        void printBuckets() {
            for (int i = 0; i < buckets.length; i++) {
                StringBuilder sb = new StringBuilder(String.format("    [%2d] ", i));
                for (Entry<K, V> e = buckets[i]; e != null; e = e.next)
                    sb.append("-> (").append(e.key).append('=').append(e.value).append(") ");
                System.out.println(sb);
            }
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: User Login System
    //  username -> UserRecord(salt, SHA-256(salt + password), failedAttempts)
    //  Passwords are NEVER stored in plain text. (Production systems use a
    //  deliberately slow KDF such as bcrypt/scrypt/Argon2 instead of SHA-256.)
    // ------------------------------------------------------------------------
    static class UserRecord {
        final String salt, passwordHash; int failedAttempts; boolean locked;
        UserRecord(String salt, String hash) { this.salt = salt; this.passwordHash = hash; }
    }

    static String sha256Hex(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static class AuthService {
        private final MyHashMap<String, UserRecord> users = new MyHashMap<>(4, 0.75);
        private final SecureRandom rnd = new SecureRandom();
        static final int MAX_ATTEMPTS = 3;

        boolean register(String username, String password) {
            String key = username.toLowerCase();
            if (users.containsKey(key)) {
                System.out.println("  register " + username + " -> FAILED (username taken)");
                return false;
            }
            byte[] saltBytes = new byte[8];
            rnd.nextBytes(saltBytes);
            StringBuilder salt = new StringBuilder();
            for (byte b : saltBytes) salt.append(String.format("%02x", b));
            users.put(key, new UserRecord(salt.toString(), sha256Hex(salt + password)));   // O(1) avg
            System.out.println("  register " + username + " -> OK");
            return true;
        }

        void login(String username, String password) {
            UserRecord u = users.get(username.toLowerCase());          // O(1) avg lookup
            if (u == null) { System.out.println("  login " + username + " -> unknown user"); return; }
            if (u.locked)  { System.out.println("  login " + username + " -> ACCOUNT LOCKED"); return; }
            if (sha256Hex(u.salt + password).equals(u.passwordHash)) {
                u.failedAttempts = 0;
                System.out.println("  login " + username + " -> SUCCESS");
            } else {
                u.failedAttempts++;
                if (u.failedAttempts >= MAX_ATTEMPTS) u.locked = true;
                System.out.println("  login " + username + " -> wrong password (" + u.failedAttempts + "/" + MAX_ATTEMPTS
                        + (u.locked ? ", now LOCKED)" : ")"));
            }
        }
    }

    static void loginScenario() {
        System.out.println("\n=== SCENARIO: User Login System (hash table of salted password hashes) ===");
        AuthService auth = new AuthService();
        auth.register("srikar", "Dsa@2026");
        auth.register("himesh", "mininet!");
        auth.register("krrish", "sdn#rocks");
        auth.register("Srikar", "another");                 // case-insensitive duplicate
        auth.login("srikar", "Dsa@2026");
        auth.login("himesh", "wrong1");
        auth.login("himesh", "wrong2");
        auth.login("himesh", "wrong3");
        auth.login("himesh", "mininet!");                    // locked now
        auth.login("ghost", "x");
        UserRecord r = auth.users.get("srikar");
        System.out.println("  stored for srikar -> salt=" + r.salt + " hash=" + r.passwordHash.substring(0, 16) + "...");
        System.out.printf("  table: %d users, capacity %d, load %.2f, rehashes %d%n",
                auth.users.size(), auth.users.capacity(), auth.users.loadFactor(), auth.users.rehashCount);
    }

    public static void main(String[] args) {
        System.out.println("=== HASH TABLE (SEPARATE CHAINING) ===");
        MyHashMap<String, Integer> fruit = new MyHashMap<>(5, 10.0);   // tiny + no resize: force collisions
        String[] names = {"apple", "mango", "grape", "kiwi", "banana", "cherry", "papaya", "guava"};
        for (int i = 0; i < names.length; i++) fruit.put(names[i], (i + 1) * 10);
        System.out.println("  capacity 5, 8 keys -> collisions are guaranteed:");
        fruit.printBuckets();
        System.out.println("  get(grape)=" + fruit.get("grape") + " get(lychee)=" + fruit.get("lychee"));
        fruit.put("grape", 999);
        System.out.println("  put(grape, 999) updates in place -> " + fruit.get("grape"));
        System.out.println("  remove(mango)=" + fruit.remove("mango") + " size=" + fruit.size());
        System.out.printf("  load factor %.2f, longest chain %d%n", fruit.loadFactor(), fruit.longestChain());

        System.out.println("\n=== AUTOMATIC REHASHING (max load 0.75) ===");
        MyHashMap<Integer, String> ids = new MyHashMap<>();
        for (int i = 1; i <= 50; i++) {
            int cap = ids.capacity();
            ids.put(i * 7919, "user" + i);
            if (ids.capacity() != cap)
                System.out.printf("  size %2d -> resized %d -> %d (rehash #%d)%n", ids.size(), cap, ids.capacity(), ids.rehashCount);
        }
        System.out.printf("  final: size=%d capacity=%d load=%.2f longest chain=%d%n",
                ids.size(), ids.capacity(), ids.loadFactor(), ids.longestChain());

        loginScenario();
    }

}
