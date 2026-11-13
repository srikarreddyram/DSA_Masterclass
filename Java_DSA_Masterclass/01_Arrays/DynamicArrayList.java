/*
 * ============================================================================
 *  MODULE 01 : DYNAMIC ARRAY (custom ArrayList built from scratch)
 * ============================================================================
 *
 *  WHAT IS A DYNAMIC ARRAY?
 *  ------------------------
 *  A plain array has a fixed capacity. A dynamic array wraps a plain array and
 *  GROWS automatically: when it runs out of room it allocates a bigger array
 *  (usually 2x, java.util.ArrayList uses 1.5x) and copies everything over.
 *
 *      size     = number of elements the user has stored
 *      capacity = length of the hidden backing array
 *
 *        add(40) when size == capacity == 4
 *        [10|20|30|40]           -> full!
 *        [10|20|30|40|  |  |  |  ] -> new array (cap 8), copy 4 items, then add
 *
 *  AMORTIZED ANALYSIS (why append is O(1) "on average")
 *  ----------------------------------------------------
 *  With doubling, copies happen at sizes 1, 2, 4, 8, ..., n. Total copy work
 *  = 1 + 2 + 4 + ... + n < 2n. Spread over n appends, each append costs O(1)
 *  amortized, even though a single unlucky append costs O(n).
 *
 *  We also SHRINK to half when size drops to 1/4 of capacity. Shrinking at
 *  1/2 (not 1/4) would cause "thrashing": add/remove at the boundary would
 *  resize every single time.
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - java.util.ArrayList, Python list, C++ std::vector, JS arrays
 *   - Shopping carts, chat message history, any list that grows
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   | Operation          | Time              |
 *   |--------------------|-------------------|
 *   | get / set          | O(1)              |
 *   | add (append)       | O(1) amortized    |
 *   | add(index, value)  | O(n) (shifting)   |
 *   | remove(index)      | O(n) (shifting)   |
 *   | removeLast         | O(1) amortized    |
 *   | indexOf / contains | O(n)              |
 *   | resize             | O(n)              |
 *   Space: O(n)  (at most ~4x the elements because of the shrink rule)
 *
 *  HOW TO RUN
 *  ----------
 *   javac DynamicArrayList.java && java DynamicArrayList
 * ============================================================================
 */

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DynamicArrayList {

    /**
     * Generic dynamic array. Implements Iterable so it works in for-each loops,
     * exactly like java.util.ArrayList.
     */
    static class MyArrayList<T> implements Iterable<T> {
        private static final int DEFAULT_CAPACITY = 4;
        private Object[] data;       // Java can't create "new T[]" (type erasure)
        private int size;
        private int resizeCount;     // for teaching: how many times we resized

        MyArrayList() { this(DEFAULT_CAPACITY); }

        MyArrayList(int initialCapacity) {
            if (initialCapacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
            data = new Object[initialCapacity];
        }

        int size()      { return size; }
        int capacity()  { return data.length; }
        boolean isEmpty() { return size == 0; }
        int resizeCount() { return resizeCount; }

        // ---------------- core: resizing ----------------
        /**
         * Allocate a new backing array and copy the live elements across.
         * This is the ONLY O(n) step in append - and it happens rarely.
         */
        private void resize(int newCapacity) {
            Object[] bigger = new Object[newCapacity];
            for (int i = 0; i < size; i++) bigger[i] = data[i];   // == System.arraycopy
            data = bigger;
            resizeCount++;
        }

        private void checkIndex(int index) {
            if (index < 0 || index >= size)
                throw new IndexOutOfBoundsException("Index " + index + ", size " + size);
        }

        // ---------------- add ----------------
        /** Append - O(1) amortized. */
        void add(T value) {
            if (size == data.length) resize(data.length * 2);   // grow 2x when full
            data[size++] = value;
        }

        /** Insert at index - O(n) because elements must shift right. */
        void add(int index, T value) {
            if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index " + index);
            if (size == data.length) resize(data.length * 2);
            for (int i = size; i > index; i--) data[i] = data[i - 1];   // shift right
            data[index] = value;
            size++;
        }

        // ---------------- read / write ----------------
        @SuppressWarnings("unchecked")
        T get(int index) {
            checkIndex(index);
            return (T) data[index];          // O(1)
        }

        @SuppressWarnings("unchecked")
        T set(int index, T value) {
            checkIndex(index);
            T old = (T) data[index];
            data[index] = value;
            return old;
        }

        int indexOf(T value) {
            for (int i = 0; i < size; i++) {
                if (value == null ? data[i] == null : value.equals(data[i])) return i;
            }
            return -1;
        }

        boolean contains(T value) { return indexOf(value) != -1; }

        // ---------------- remove ----------------
        /** Remove at index - O(n) (shift left). Shrinks when 1/4 full. */
        @SuppressWarnings("unchecked")
        T remove(int index) {
            checkIndex(index);
            T removed = (T) data[index];
            for (int i = index; i < size - 1; i++) data[i] = data[i + 1];  // shift left
            data[--size] = null;            // help the garbage collector
            // Shrink at 1/4 occupancy -> halves capacity. Avoids thrashing.
            if (size > 0 && size == data.length / 4 && data.length / 2 >= DEFAULT_CAPACITY) {
                resize(data.length / 2);
            }
            return removed;
        }

        boolean removeValue(T value) {
            int idx = indexOf(value);
            if (idx == -1) return false;
            remove(idx);
            return true;
        }

        T removeLast() {
            if (isEmpty()) throw new NoSuchElementException("list is empty");
            return remove(size - 1);
        }

        void clear() {
            data = new Object[DEFAULT_CAPACITY];
            size = 0;
        }

        // ---------------- iteration ----------------
        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int cursor = 0;
                public boolean hasNext() { return cursor < size; }
                @SuppressWarnings("unchecked")
                public T next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    return (T) data[cursor++];
                }
            };
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < size; i++) {
                sb.append(data[i]);
                if (i < size - 1) sb.append(", ");
            }
            return sb.append("]").toString();
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: E-commerce shopping cart that grows and shrinks
    // ------------------------------------------------------------------------
    static class CartItem {
        final String name; final double price; int qty;
        CartItem(String name, double price, int qty) { this.name = name; this.price = price; this.qty = qty; }
        double total() { return price * qty; }
        @Override public boolean equals(Object o) {
            return o instanceof CartItem && ((CartItem) o).name.equals(name);
        }
        @Override public int hashCode() { return name.hashCode(); }
        @Override public String toString() { return name + " x" + qty; }
    }

    static void shoppingCartScenario() {
        System.out.println("\n=== SCENARIO: Shopping Cart (dynamic array) ===");
        MyArrayList<CartItem> cart = new MyArrayList<>(2);
        String[][] catalogue = {
                {"Keyboard", "1499"}, {"Mouse", "699"}, {"Monitor", "10999"},
                {"USB-C Hub", "1899"}, {"Webcam", "2499"}, {"Headphones", "3999"}};

        for (String[] p : catalogue) {
            int before = cart.capacity();
            cart.add(new CartItem(p[0], Double.parseDouble(p[1]), 1));
            String note = cart.capacity() != before
                    ? "  <-- RESIZED " + before + " -> " + cart.capacity() : "";
            System.out.printf("  added %-11s size=%d cap=%d%s%n", p[0], cart.size(), cart.capacity(), note);
        }

        cart.get(1).qty = 3;                                // O(1) update via index
        cart.removeValue(new CartItem("Webcam", 0, 0));     // O(n) find + shift
        System.out.println("  cart now : " + cart);

        double total = 0;
        for (CartItem item : cart) total += item.total();   // uses our Iterator
        System.out.printf("  subtotal : Rs %.2f%n", total);

        System.out.println("  removing items until the cart shrinks...");
        while (cart.size() > 1) {
            CartItem removed = cart.removeLast();
            System.out.printf("  removed %-11s size=%d cap=%d%n", removed.name, cart.size(), cart.capacity());
        }
        System.out.println("  total resizes performed: " + cart.resizeCount());
    }

    public static void main(String[] args) {
        System.out.println("=== CUSTOM ArrayList OPERATIONS ===");
        MyArrayList<Integer> list = new MyArrayList<>();
        for (int i = 1; i <= 9; i++) {
            list.add(i * 10);
            System.out.printf("  add(%d)  -> size=%d capacity=%d%n", i * 10, list.size(), list.capacity());
        }
        System.out.println("  list            : " + list);
        list.add(0, 5);
        System.out.println("  add(0, 5)       : " + list);
        System.out.println("  get(3)          : " + list.get(3));
        System.out.println("  set(3, 333) old : " + list.set(3, 333));
        System.out.println("  indexOf(70)     : " + list.indexOf(70));
        System.out.println("  contains(999)   : " + list.contains(999));
        System.out.println("  remove(0)       : " + list.remove(0) + " -> " + list);

        try {
            list.get(100);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("  get(100) threw  : " + e.getMessage());
        }

        shoppingCartScenario();
    }
}
