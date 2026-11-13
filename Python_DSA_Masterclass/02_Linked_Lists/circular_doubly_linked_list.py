"""
============================================================================
 MODULE 02 : CIRCULAR DOUBLY LINKED LIST (CDLL)
============================================================================

 prev AND next references, with both ends joined into a ring:

      +-----------------------------------------------+
      |  +-----------------------------------------+  |
      v  |                                         |  |
    [p|A|n] <--> [p|B|n] <--> [p|C|n] <--> [p|D|n]  |
     head  ^                                tail |  |
           +-------------------------------------+  |

 Invariants:  head.prev is tail   AND   tail.next is head

 REAL-WORLD USE CASES: music playlist with repeat-all + next/previous,
 image carousels, alt-tab switching, Fibonacci-heap root lists.

 TIME COMPLEXITY
  | Insert/delete at head          | O(1) |
  | Insert/delete at tail          | O(1) |
  | Insert/delete at position k    | O(n) |
  | Unlink a held node             | O(1) |
  | Search / traverse (both ways)  | O(n) |
  Space: O(n)

 HOW TO RUN:  python circular_doubly_linked_list.py
============================================================================
"""

import random
from dataclasses import dataclass


class Node:
    __slots__ = ("value", "prev", "next")

    def __init__(self, value):
        self.value, self.prev, self.next = value, None, None


class CircularDoublyLinkedList:
    def __init__(self):
        self.head = self.tail = None
        self.size = 0

    def _close_ring(self) -> None:
        """Re-establish both circular invariants after head/tail changes."""
        self.head.prev = self.tail
        self.tail.next = self.head

    def insert(self, v, location: int) -> None:
        n = Node(v)
        if self.head is None:
            self.head = self.tail = n
            n.next = n.prev = n                     # ring of one
        elif location <= 0:
            n.next, self.head.prev = self.head, n
            self.head = n
            self._close_ring()
        elif location >= self.size:
            n.prev, self.tail.next = self.tail, n
            self.tail = n
            self._close_ring()
        else:
            before = self.head
            for _ in range(location - 1):
                before = before.next
            after = before.next
            n.prev, n.next = before, after
            before.next = after.prev = n
        self.size += 1

    def add_last(self, v) -> None:
        self.insert(v, self.size)

    def insert_after(self, node: Node, v) -> Node:
        """O(1) insert right after a node we already hold."""
        n = Node(v)
        after = node.next
        n.prev, n.next = node, after
        node.next = after.prev = n
        if node is self.tail:
            self.tail = n
        self.size += 1
        return n

    def unlink(self, n: Node):
        if self.size == 1:
            self.head = self.tail = None
        else:
            n.prev.next = n.next                    # neighbours skip n
            n.next.prev = n.prev
            if n is self.head: self.head = n.next
            if n is self.tail: self.tail = n.prev
        n.prev = n.next = None
        self.size -= 1
        return n.value

    def delete(self, location: int):
        if self.head is None:
            return None
        if location <= 0:
            return self.unlink(self.head)
        if location >= self.size - 1:
            return self.unlink(self.tail)
        cur = self.head
        for _ in range(location):
            cur = cur.next
        return self.unlink(cur)

    def nodes(self):
        if self.head is None:
            return
        cur = self.head
        for _ in range(self.size):
            yield cur
            cur = cur.next

    def __iter__(self):
        return (n.value for n in self.nodes())

    def search(self, v) -> int:
        for i, x in enumerate(self):
            if x == v:
                return i
        return -1

    def traverse(self) -> str:
        if self.head is None:
            return "  (empty)"
        return "  " + " <-> ".join(map(str, self)) + f" <-> ({self.head.value})"

    def reverse_traverse(self) -> str:
        if self.tail is None:
            return "  (empty)"
        vals, cur = [], self.tail
        for _ in range(self.size):
            vals.append(str(cur.value)); cur = cur.prev
        return "  reverse: " + " <- ".join(vals) + f" <- ({self.tail.value})"

    def delete_all(self) -> None:
        for n in list(self.nodes()):                # break ring both ways
            n.prev = n.next = None
        self.head = self.tail = None
        self.size = 0


# --------------------------------------------------------------------------
#  SCENARIO: Music Player Playlist (repeat-all, next, prev, play-next, shuffle)
# --------------------------------------------------------------------------
@dataclass
class Song:
    title: str
    artist: str
    seconds: int

    def __str__(self) -> str:
        return self.title


class MusicPlayer:
    def __init__(self):
        self.playlist = CircularDoublyLinkedList()
        self.now_playing = None

    def add(self, song: Song) -> None:
        self.playlist.add_last(song)
        if self.now_playing is None:
            self.now_playing = self.playlist.head

    def show(self) -> None:
        s = self.now_playing.value
        print(f"  >> NOW PLAYING: {s.title} - {s.artist} ({s.seconds // 60}:{s.seconds % 60:02d})")

    def next(self) -> None:
        self.now_playing = self.now_playing.next    # tail.next wraps to head
        self.show()

    def previous(self) -> None:
        self.now_playing = self.now_playing.prev    # head.prev wraps to tail
        self.show()

    def play_next(self, song: Song) -> None:
        self.playlist.insert_after(self.now_playing, song)   # O(1)
        print(f"  queued to play next: {song}")

    def remove_current(self) -> None:
        victim = self.now_playing
        following = victim.next if self.playlist.size > 1 else None
        print(f"  removed: {self.playlist.unlink(victim)}")
        self.now_playing = following
        if following:
            self.show()

    def shuffle(self, seed: int) -> None:
        """Fisher-Yates on the VALUES; the node ring itself is untouched."""
        songs = list(self.playlist)
        rnd = random.Random(seed)
        for i in range(len(songs) - 1, 0, -1):
            j = rnd.randint(0, i)
            songs[i], songs[j] = songs[j], songs[i]
        for node, song in zip(self.playlist.nodes(), songs):
            node.value = song
        self.now_playing = self.playlist.head
        print("  shuffled:" + self.playlist.traverse())

    def total_duration(self) -> int:
        return sum(s.seconds for s in self.playlist)


def music_player_scenario() -> None:
    print("\n=== SCENARIO: Music Player Playlist (Repeat-All) ===")
    p = MusicPlayer()
    for s in [Song("Blinding Lights", "The Weeknd", 200), Song("Levitating", "Dua Lipa", 203),
              Song("Kesariya", "Arijit Singh", 268), Song("Believer", "Imagine Dragons", 204)]:
        p.add(s)
    print("  playlist:" + p.playlist.traverse())
    p.show()
    for _ in range(4):
        p.next()                 # the 4th call wraps last -> first
    p.previous()                 # wraps first -> last
    p.play_next(Song("Naatu Naatu", "Rahul Sipligunj", 211))
    p.next()
    p.remove_current()
    print("  playlist:" + p.playlist.traverse())
    t = p.total_duration()
    print(f"  total duration: {p.playlist.size} songs, {t // 60}:{t % 60:02d}")
    p.shuffle(42)
    p.show()


def main() -> None:
    print("=== CIRCULAR DOUBLY LINKED LIST OPERATIONS ===")
    c = CircularDoublyLinkedList()
    for ch in "ABCDE":
        c.add_last(ch)
    c.insert("Z", 2)
    print(c.traverse())
    print(c.reverse_traverse())
    print(f"  head.prev is tail ? {c.head.prev is c.tail} | tail.next is head ? {c.tail.next is c.head}")
    print(f"  search('D') = {c.search('D')}")
    print(f"  delete head = {c.delete(0)}")
    print(f"  delete tail = {c.delete(c.size - 1)}")
    print(f"  delete(1)   = {c.delete(1)}")
    print(c.traverse())
    c.delete_all()
    print(c.traverse())
    music_player_scenario()


if __name__ == "__main__":
    main()
