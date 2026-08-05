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

Another problem arises when we increase the number of arraylists in our program. If many of them use the same arena for their allocations, then the chances of collision when a list needs to expand is increased. Or the arenas themselves can collide with each other if a sufficient gap between them is not kept. 

But we're getting ahead of ourselves.
