---
title: New
date: 2026-07-17
synopsis: >
  ...
---

Sometime ago I wrote an article on creating generic arraylists using C macros. Because they are "the" building block of other data structures, it is impossible to overstate their value. But there is one non-trivial problem that normal arraylists can't solve -- let me show you what I mean.

Suppose we have a memory buffer like so:

![](assets/007-memory-layout01.svg)

And assume that our memory buffer is managed by the C library `malloc`. Let's initialize our arraylist:

![](assets/007-memory-layout02.svg)

Our memory allocator, in this case `malloc`, reserves a chunk of memory for `bufhdr` and the trailing data where our arraylist can store items into. I've color-coded this so dark grey represents allocated but unused, because we haven't pushed any items into it yet. 

Now let's suppose that some other code requests `malloc` for a chunk of memory immediately after initializing our arraylist. The memory now looks like so: 

![](assets/007-memory-layout03.svg)

So far so good. Now we push items into the arraylist until we hit the maximum capacity:

![](assets/007-memory-layout04.svg)

Uh oh! We've reached the end of our allocated space, i.e. the arraylist is at capacity. Let's walkthrough what would happen if we try to push one more element:

- Our code checks to see if `bufcap(b) >= 1 + buflen(b)`. This returns false, so we call `_bufgrow`.
- `_bufgrow` calculates the new capacity and `realloc`s the previously allocated space to a new location (it cannot be expanded in place due to other `malloc` calls).

The memory layout finally looks like this:

![](assets/007-memory-layout05.svg)

Notice a "hole" in the memory at the beginning. Multiply this operation a thousand times, and you get a fragmented memory space, where free and used blocks are interleaved. Even though there is enough total memory available, the memory is discontiguous enough that any sufficiently large request of memory will inevitably fail.

A better way to manage memory allocations is by grouping them by their lifetimes. This is often called an Arena. For example, all the enemies in a particular level of a game can share an arena, and at the end of the level, that arena can be instantly deallocated without individually freeing all the enemy objects. This would free the whole arena memory, which could be reused for some other level.

But arenas do not solve our problem entirely. Sometimes we cannot predict the amount of space we'd require ahead of time resulting in frequent reallocation of data. A good example is a tokenizer. We can't really predict the length of the `Token` array in advance, we could only have a calculated guess based on the length of the source code etc. 

Another problem arises when we increase the number of arraylists in our program. If many of them use the same arena for their allocations, then the chances of collision when a list needs to expand increases. Or the arenas themselves can collide with each other if a sufficient gap between them is not kept. 

But we're getting ahead of ourselves. Let's first see how a simple bump allocator is implemented.

```c
typedef struct {
    const char* name;
    char* base;
    usize capacity;
    usize pos;
} Arena;
```

We define a simple 4-field type for an `Arena` where `name` is the debug name for an arena, `base` is the starting address, `capacity` is the length in bytes, and `pos` is the current offset into the arena.

```c
Arena arena_create(const char* name, u64 reserve) {
    Arena arena = (Arena){};
    arena.name = name;
    void* ptr = mmap(NULL, reserve, PROT_READ | PROT_WRITE,
        MAP_PRIVATE | MAP_ANONYMOUS | MAP_NORESERVE, -1, 0);
    if (ptr != MAP_FAILED) {
        arena.base = ptr;
        arena.capacity = reserve;
    }
    return arena;
}
```

This creates an arena, taking in a name and a default size. Now we get to the interesting part. The `mmap()` call reserves a virtual address block (but not physical memory) of the specified size. Due to demand paging, even if the requested memory is orders of magnitude higher than the physical memory available, the kernel will not reserve any pages until a memory space is accessed. We also pass the `MAP_ANONYMOUS` flag because we do not need a backing storage `mmap`ed.

```c
void* arena_push(Arena* arena, u64 size) {
    u64 aligned_size = (size + 7) & ~7;
    assert(arena->pos + aligned_size <= arena->capacity && "Arena out of memory!");
    void* ptr = arena->base + arena->pos;
    arena->pos += aligned_size;
    return ptr;
}
```

This function is equivalent to `malloc()` in use, as it accepts a size to allocate as an argument and returns a pointer to the allocated space, as opposed to copying the object onto the arena itself.

```c
void arena_clear(Arena* arena) {
    arena->pos = 0;
}

void arena_destroy(Arena* arena) {
    if (arena->base) {
        munmap(arena->base, arena->capacity);
    }
}
```

These trivial functions deal with clearing and deinitializing arenas. It clears/destroys the whole arena at once, which is primarily what an arena is used for. 

Our arena implementation is complete, but useless for us in this state. Why? Let me reiterate our problem statement. What we want is a collision-free way of allocating objects, where an arraylist can grow infinitely without colliding with another allocation. Some of you might think that this can be easily solved by `mmap`ing a huge 1TB block of virtual memory and segregating that block into granular address spaces for each arraylist. Thus the kernel won't allocate physical pages if the memory space is not written to. But this presents us with some problems:

- Some embedded systems do not support demand paging, thus any allocation will commit physical RAM.
- Even on desktop Linux (and other OSes) the overcommit behaviour depends on kernel flags like `vm.overcommit_memory`, where the kernel will not let you reserve virtual address spaces exceeding `swap + (ram * overcommit_ratio)` if the proper value is not set.
- Modern kernels use 4 or 5-level page table hierarchies, where each page table consumes 4KB. If you write a single byte to an address far into the memory space, the kernel will have to allocate intermediate page table entries (PUD, PMD, and PTE) to map the physical page.
- It puts some hard limit (depending on the granularity of division of 1TB space) on the size of arraylists. Maybe you need to store 10GB worth of stuff in one arraylist, but you won't be able to if the block size is only 1GB.

So what can we do? Let's think about it for a moment. Let's assume we allocate a default size for every arraylist, say 128 elements. Then as the arraylist fills up, the length slowly increases to eventually equal 128, wherein no further elements can be stored. Now, we make a safe assumption that we cannot extend the capacity in place (collision with other allocations), but what if we set the next free pointer to the end of the arena? 

![](assets/007-memory-layout06.svg)

Our arraylist now becomes discontinuous i.e. the elements are allocated in chunks with unrelated allocations sitting between them. This makes pushing elements to the list trivial, but what about accessing elements from the list? Currently we have no way of knowing where the elements 128 and above are stored. They might as well be garbage memory for all the arraylist cares. Now what?

Some of you may see the solution already. What if we store the individual pointers to chunks in the header? From our previous example, when an arraylist with length 128 is pushed to again, it would store the next free arena address in its header so it'd look something like:

        Chunk #0 => 0xfffdffe0c24deb70 (the "default" chunk)
    --> Chunk #1 => 0xfffdffe0c24ded30 (arena's next free spot)

The arraylist would tell the arena to reserve 128 elements from its current position, so the next 128 elements of the arraylist could be stored here. Also the pointers to these chunks could be stored in the header so we could access any element just by indexing into it's chunk pointer.

This brings us to a caveat of this data structure: any time you'd want to access a particular element (random access), you'd first need to find the associated chunk pointer of that element (using some bitwise math) in the table stored in the header. Then another read would be required to load the element from the chunk. This indirect memory access would cause cache locality and access times to take a hit, but would be much better at appending/deleting items from/to the list. 

<table><thead>
  <tr>
    <th>Operation</th>
    <th>Standard ArrayList</th>
    <th>LinkedList</th>
    <th>Chunked ArrayList</th>
  </tr></thead>
<tbody>
  <tr>
    <td>Random Access (get / set)</td>
    <td>O(1) (fast pointer math)</td>
    <td>O(n) (pointer traversal)</td>
    <td>O(1) but slower compared to Standard ArrayList</td>
  </tr>
  <tr>
    <td>Append</td>
    <td>Amortized O(1) (expensive resize if full)</td>
    <td>O(1) (fast)</td>
    <td>O(1) (as fast as LinkedList; spawns a new chunk if full without copying over data)</td>
  </tr>
  <tr>
    <td>Insert / Delete</td>
    <td>O(n)</td>
    <td>O(1) if pointer to element is known else O(n)</td>
    <td>O(c) to O(n) for insertion, O(c) for deletion</td>
  </tr>
  <tr>
    <td>Memory Overhead</td>
    <td>Low</td>
    <td>High (prev/next pointer for every element)</td>
    <td>Little higher than Standard ArrayList (overhead per chunk not per element)</td>
  </tr>
  <tr>
    <td>Cache Locality</td>
    <td>Excellent (continuous)</td>
    <td>Poor (elements sparsely spaced)</td>
    <td>Good to Excellent (continuous chunks separated in memory)</td>
  </tr>
</tbody></table>

A chunked arraylist approach combines benefits of both a standard arraylist and a linked list. This novel structure is a great fit for long-running service daemons, which require minimal memory fragmentation to minimize allocation failures. By now you must be somewhat inclined to see how this works in action. Let's get to the implementation.