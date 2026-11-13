/*
 * ============================================================================
 *  MODULE 02 : CIRCULAR DOUBLY LINKED LIST (CDLL)
 * ============================================================================
 *
 *  WHAT IS IT?
 *  -----------
 *  The most flexible linked list: every node has prev AND next references,
 *  and the ends are joined so the structure is a ring in both directions.
 *
 *       +-----------------------------------------------+
 *       |  +-----------------------------------------+  |
 *       v  |                                         |  |
 *     [p|A|n] <--> [p|B|n] <--> [p|C|n] <--> [p|D|n]  |
 *      head  ^                                tail |  |
 *            +-------------------------------------+  |
 *            (head.prev == tail, tail.next == head)   |
 *
 *  Invariants:  head.prev == tail   AND   tail.next == head
 *
 *  REAL-WORLD USE CASES
 *  --------------------
 *   - Music playlist with "repeat all" + next / previous buttons
 *   - Image carousels, alt-tab window switching
 *   - Fibonacci heap root lists, OS ready queues that move both ways
 *
 *  TIME COMPLEXITY
 *  ---------------
 *   | Operation                    | Time |
 *   |------------------------------|------|
 *   | Insert/delete at head        | O(1) |
 *   | Insert/delete at tail        | O(1) |  (tail.prev gives predecessor)
 *   | Insert/delete at position k  | O(n) |
 *   | Delete given node reference  | O(1) |
 *   | Search / traverse (both ways)| O(n) |
 *   Space: O(n)
 *
 *  HOW TO RUN
 *  ----------
 *   javac CircularDoublyLinkedList.java && java CircularDoublyLinkedList
 * ============================================================================
 */

import java.util.Random;

public class CircularDoublyLinkedList {

    static class Node<T> {
        T value;
        Node<T> prev, next;
        Node(T v) { value = v; }
    }

    static class CDLL<T> {
        Node<T> head, tail;
        int size;

        /** Re-establish the two circular invariants after any head/tail change. */
        private void closeRing() {
            head.prev = tail;
            tail.next = head;
        }

        void insert(T v, int location) {
            Node<T> n = new Node<>(v);
            if (head == null) {
                head = tail = n;
                n.next = n.prev = n;            // a ring of one
            } else if (location <= 0) {
                n.next = head;
                head.prev = n;
                head = n;
                closeRing();
            } else if (location >= size) {
                n.prev = tail;
                tail.next = n;
                tail = n;
                closeRing();
            } else {
                Node<T> before = head;
                for (int i = 0; i < location - 1; i++) before = before.next;
                Node<T> after = before.next;
                n.prev = before; n.next = after;
                before.next = n; after.prev = n;
            }
            size++;
        }

        void addLast(T v) { insert(v, size); }

        /** O(1) removal of a known node - used heavily by the playlist. */
        T unlink(Node<T> n) {
            if (size == 1) {
                n.next = n.prev = null;
                head = tail = null;
            } else {
                n.prev.next = n.next;           // neighbours skip over n
                n.next.prev = n.prev;
                if (n == head) head = n.next;
                if (n == tail) tail = n.prev;
                n.next = n.prev = null;
            }
            size--;
            return n.value;
        }

        T delete(int location) {
            if (head == null) return null;
            if (location <= 0) return unlink(head);
            if (location >= size - 1) return unlink(tail);
            Node<T> cur = head;
            for (int i = 0; i < location; i++) cur = cur.next;
            return unlink(cur);
        }

        int search(T v) {
            if (head == null) return -1;
            Node<T> cur = head;
            int i = 0;
            do {
                if (cur.value.equals(v)) return i;
                cur = cur.next; i++;
            } while (cur != head);
            return -1;
        }

        void traverse() {
            if (head == null) { System.out.println("  (empty)"); return; }
            StringBuilder sb = new StringBuilder("  ");
            Node<T> c = head;
            do { sb.append(c.value).append(" <-> "); c = c.next; } while (c != head);
            System.out.println(sb.append("(").append(head.value).append(")"));
        }

        void reverseTraverse() {
            if (tail == null) { System.out.println("  (empty)"); return; }
            StringBuilder sb = new StringBuilder("  reverse: ");
            Node<T> c = tail;
            do { sb.append(c.value).append(" <- "); c = c.prev; } while (c != tail);
            System.out.println(sb.append("(").append(tail.value).append(")"));
        }

        /** Break the ring in both directions, then drop references. */
        void deleteAll() {
            if (head == null) return;
            Node<T> c = head;
            do { c.prev = null; c = c.next; } while (c != null && c != head);
            tail.next = null;
            head = tail = null;
            size = 0;
        }
    }

    // ------------------------------------------------------------------------
    //  SCENARIO: Music Player Playlist (repeat-all, next, prev, remove, shuffle)
    // ------------------------------------------------------------------------
    static class Song {
        final String title, artist; final int seconds;
        Song(String t, String a, int s) { title = t; artist = a; seconds = s; }
        @Override public boolean equals(Object o) { return o instanceof Song && ((Song) o).title.equals(title); }
        @Override public int hashCode() { return title.hashCode(); }
        @Override public String toString() { return title; }
    }

    static class MusicPlayer {
        final CDLL<Song> playlist = new CDLL<>();
        Node<Song> nowPlaying;

        void add(Song s) {
            playlist.addLast(s);
            if (nowPlaying == null) nowPlaying = playlist.head;
        }

        void show() {
            System.out.printf("  >> NOW PLAYING: %s - %s (%d:%02d)%n",
                    nowPlaying.value.title, nowPlaying.value.artist,
                    nowPlaying.value.seconds / 60, nowPlaying.value.seconds % 60);
        }

        void next()     { nowPlaying = nowPlaying.next; show(); }  // tail.next wraps to head
        void previous() { nowPlaying = nowPlaying.prev; show(); }  // head.prev wraps to tail

        /** Remove the current song and continue with the next one - O(1). */
        void removeCurrent() {
            Node<Song> victim = nowPlaying;
            Node<Song> following = playlist.size > 1 ? victim.next : null;
            System.out.println("  removed: " + playlist.unlink(victim));
            nowPlaying = following;
            if (nowPlaying != null) show();
        }

        /**
         * "Play next": insert a song right after the current one - O(1),
         * because we already hold a reference to the current node.
         */
        void playNext(Song s) {
            Node<Song> n = new Node<>(s);
            Node<Song> after = nowPlaying.next;
            n.prev = nowPlaying; n.next = after;
            nowPlaying.next = n; after.prev = n;
            if (nowPlaying == playlist.tail) playlist.tail = n;
            playlist.size++;
            System.out.println("  queued to play next: " + s);
        }

        /** Fisher-Yates shuffle of the VALUES (node structure stays intact). */
        void shuffle(long seed) {
            Object[] songs = new Object[playlist.size];
            Node<Song> c = playlist.head;
            for (int i = 0; i < songs.length; i++, c = c.next) songs[i] = c.value;
            Random rnd = new Random(seed);
            for (int i = songs.length - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                Object t = songs[i]; songs[i] = songs[j]; songs[j] = t;
            }
            c = playlist.head;
            for (Object s : songs) { c.value = (Song) s; c = c.next; }
            nowPlaying = playlist.head;
            System.out.print("  shuffled:");
            playlist.traverse();
        }

        int totalDurationSeconds() {
            int total = 0;
            Node<Song> c = playlist.head;
            for (int i = 0; i < playlist.size; i++, c = c.next) total += c.value.seconds;
            return total;
        }
    }

    static void musicPlayerScenario() {
        System.out.println("\n=== SCENARIO: Music Player Playlist (Repeat-All) ===");
        MusicPlayer p = new MusicPlayer();
        p.add(new Song("Blinding Lights", "The Weeknd", 200));
        p.add(new Song("Levitating", "Dua Lipa", 203));
        p.add(new Song("Kesariya", "Arijit Singh", 268));
        p.add(new Song("Believer", "Imagine Dragons", 204));
        System.out.print("  playlist:");
        p.playlist.traverse();
        p.show();
        p.next();
        p.next();
        p.next();
        p.next();          // wraps from last song back to first (repeat-all)
        p.previous();      // wraps from first back to last
        p.playNext(new Song("Naatu Naatu", "Rahul Sipligunj", 211));
        p.next();
        p.removeCurrent();
        System.out.print("  playlist:");
        p.playlist.traverse();
        int t = p.totalDurationSeconds();
        System.out.printf("  total duration: %d songs, %d:%02d%n", p.playlist.size, t / 60, t % 60);
        p.shuffle(42);
        p.show();
    }

    public static void main(String[] args) {
        System.out.println("=== CIRCULAR DOUBLY LINKED LIST OPERATIONS ===");
        CDLL<Character> list = new CDLL<>();
        for (char c = 'A'; c <= 'E'; c++) list.addLast(c);
        list.insert('Z', 2);
        list.traverse();
        list.reverseTraverse();
        System.out.println("  head.prev == tail ? " + (list.head.prev == list.tail)
                + " | tail.next == head ? " + (list.tail.next == list.head));
        System.out.println("  search('D') = " + list.search('D'));
        System.out.println("  delete head = " + list.delete(0));
        System.out.println("  delete tail = " + list.delete(list.size - 1));
        System.out.println("  delete(1)   = " + list.delete(1));
        list.traverse();
        list.deleteAll();
        list.traverse();

        musicPlayerScenario();
    }
}
