# C Macros as a Poor Man’s std::vector

If you’ve spent time in Java or C++, you get used to the convenience of standard dynamic containers. `ArrayList` and `std::vector` are taken for granted and are treated as being always there for you when you need it. When you move to C, that comfort disappears. The language gives you raw pointers, `malloc`, `realloc`, and little else. There’s no standard dynamic array type, no generic container, and no unified pattern for managing capacity. You quickly realize that something as basic as “append an element” requires a bit of manual work. That absence becomes noticeable, even annoying.

Earlier C developers handled this by hardcoding the buffer length: want to enter your name? Oops, can't go above 64 characters. Receiving a data packet? Sorry, no more than 128Kb chunks at a time please. But there was still the need to store growable data. What if you didn't want to limit the amount of nodes your compiler can process, or the amount of particles that can be shown on the screen at a given time?

Some people solve this by using ad-hoc structs with data/size/capacity fields, some wrap `realloc` behind helper functions, and some avoid the problem entirely by over-allocating.

I think C is a beautiful language to write code in, and more people would probably use it today if it had a cleaner way to express generic code. But all is not lost. 

[[[
The stretchy buffer implementation I'm going to show you is primarily based on Sean Barrett's excellent header-only [stb](https://github.com/nothings/stb) library. In the real world you'd use that directly, but for the sake of learning we'll implement it ourselves.
]]]

## Using the buffer

Firstly, let's define how we want the user to use our buffer implementation. I always start with this part because this gives it a structure and makes the implementation easier. We want something like this:

```c
int* list = NULL;
```

`list` must have a non-opaque type so that the compiler can typecheck our code at compile-time. Otherwise I've seen many implementations that use `void*` and leave it to the user to perform operations correctly. In our case, by giving it a type `int*` we are telling it to make a list of `int`s. Do note that all buffers must be initialized to `NULL`. I'll go into this later. 

```c*
int* list = NULL;
// hlt-start
bufpush(list, 1);
bufpush(list, 3);
bufpush(list, 9);
// hlt-end
```

We push `1`, `3` and `9` into the buffer. A point to note is that even though we did not initialize the `list` pointer to any memory region or `malloc`ed any storage, we still are able to push elements to our buffer. 

```c*
int* list = NULL;
bufpush(list, 1);
bufpush(list, 3);
bufpush(list, 9);
// hlt-start
bufinsert(list, 1, 2);
// hlt-end
```

This inserts `2` at index `1`. We can loop through the buffer by using `bufloop`:

```c*
int* list = NULL;
bufpush(list, 1);
bufpush(list, 3);
bufpush(list, 9);
bufinsert(list, 1, 2);

// hlt-start
bufloop(list, i) {
    printf("%d\n", list[i]);
}
// hlt-end
```

Running this we get:

```console
1
2
3
9
```

[[[
One caution: because these are macros, arguments may be evaluated multiple times, so avoid passing expressions with side effects into `bufpush`, `bufinsert`, etc.
]]]

Another advantage is that array operators also work for our buffer:

```c*
int* list = NULL;
bufpush(list, 1);
bufpush(list, 3);
bufpush(list, 9);
bufinsert(list, 1, 2);

// hlt-start
// Prints `9`
printf("%d\n", list[3]);
// hlt-end
```

Other operations include `buflen`, `bufcap`, `bufpop`, `bufclear`, etc. Now that we know how the usage of our implementation will look like, let's see how this all is implemented.

## Implementing the buffer

Most buffer implementations will generally store a header before the data that has the buffer length, capacity, etc. 

![](header-layout.svg)

The pointer the user is given does not point to the header, rather it points to data. That's why it can be used with the array operator to get any item at index.

We first define the layout of the buffer:

```c
typedef struct {
    usize cap;
    usize len;
    char data[];
} bufhdr;
```

The data is stored as a `char` array in `bufhdr`. We define a handy macro to get the `bufhdr` struct from a `data` pointer:

```c*
    char data[];
} bufhdr;

// hlt-start
#define _bufhdr(b) ((bufhdr*)((char*)(b) - offsetof(bufhdr, data)))
// hlt-end
```

`offsetof(bufhdr, data)` will give the byte offset of the `data` field inside `bufhdr`. We subtract this from `data` pointer (which is `b` here) to get the first element of the header (which is the header itself). We also define some user-facing functions to get the length and capacity:

```c*
#define _bufhdr(b) ((bufhdr*)((char*)(b) - offsetof(bufhdr, data)))

// hlt-start
usize buflen(const void* buf) {
    return buf ? _bufhdr(buf)->len : 0;
}

usize bufcap(const void* buf) {
    return buf ? _bufhdr(buf)->cap : 0;
}
// hlt-end
```

Let's define the `bufpush` we saw earlier. 

```c*
#define _bufhdr(b) ((bufhdr*)((char*)(b) - offsetof(bufhdr, data)))

// hlt-start
#define bufpush(b, ...) (buffit((b), 1 + buflen((b))), \
    ((b)[_bufhdr((b))->len++] = __VA_ARGS__))
// hlt-end

usize buflen(const void* buf) {
```

It first checks if the buffer is initialized/of sufficient length (`buffit`) and then adds the element to the end of the length. `buffit` is defined as:

```c*
#define _bufhdr(b) ((bufhdr*)((char*)(b) - offsetof(bufhdr, data)))

// hlt-start
#define buffit(b, n) (bufcap(b) >= n ? 0 : \
    ((b) = _bufgrow((b), (n), sizeof(*(b)))))
// hlt-end

#define bufpush(b, ...) (buffit((b), 1 + buflen((b))), \
```

This checks if the capacity is greater than the amount requested. If it isn't, then it grows the buffer using `_bufgrow`. Now comes the most important part:

```c*
usize bufcap(const void* buf) {
    return buf ? _bufhdr(buf)->cap : 0;
}

// hlt-start
void* _bufgrow(const void* buf, usize new_len, usize elem_size) {
    usize new_cap = CLAMP_MIN(2 * bufcap(buf), MAX(new_len, 16));
    assert(new_len <= new_cap);

    usize mem_to_alloc = new_cap * elem_size + offsetof(bufhdr, data);
    bufhdr* new_hdr;
// hlt-end
```

`_bufgrow` first computes the new capacity needed using the arguments passed. Remember when I said we needed to initialize the buffer to `NULL`? That is used here, to check if the buffer is previously initialized or not:

```c*
    bufhdr* new_hdr;
    // hlt-start
    if (buf) {
        new_hdr = (bufhdr*)realloc(_bufhdr(buf), mem_to_alloc);
    }
    else {
        new_hdr = (bufhdr*)malloc(mem_to_alloc);
        new_hdr->len = 0;
    }
    // hlt-end

    new_hdr->cap = new_cap;
    return new_hdr->data;
}
```

If the buffer is not initialized (`NULL`), it allocates a new memory region. Otherwise it `realloc`s to expand to new size. Note that this function returns the pointer to the **`data`**, not header. This is how the `data` pointer passes on to the user. 

Before returning, we copy the new capacity into our new header. Why did we create a new header? Because when we first `malloc`, we start `malloc` from the header. So even to reallocate, we must reallocate the header part too, not just the data. Next up is `bufinsert`:

```c*
#define bufpush(b, ...) (buffit((b), 1 + buflen((b))), \
    ((b)[_bufhdr((b))->len++] = __VA_ARGS__))

// hlt-start
#define bufinsert(b, i, ...) (buffit((b), 1 + buflen((b))), \
    memmove((b+i+1), (b+i), (_bufhdr((b))->len-i) * sizeof(*b)), \
// hlt-end
```

We check if we can accommodate one more element using our handy `buffit`, then we `memmove` elements having index `>= i` forward one element. Then at the now-empty space, we copy our element to insert:

```c*
    memmove((b+i+1), (b+i), (_bufhdr((b))->len-i) * sizeof(*b)), \
    // hlt-start
    ((b)[i] = __VA_ARGS__), \
    _bufhdr((b))->len++)
    // hlt-end

usize buflen(const void* buf) {
```

Implementation of `bufloop` is just a `for` loop:

```c*
    ((b)[_bufhdr((b))->len++] = __VA_ARGS__))

// hlt-start
#define bufloop(b, c) for (usize c = 0; c < buflen(b); c++)
#define bufrevloop(b, c) for (usize c = buflen(b); c-- > 0 ;)
// hlt-end

#define bufinsert(b, i, ...) (buffit((b), 1 + buflen((b))), \
```

We also define a reverse `for` loop in the same way. We used the unofficial `-->` operator for reversing the loop! If you didn't already know, reversing a loop with an `unsigned` variable does not work with the usual syntax. 

Finally, we implement macros for clearing and freeing the buffer.

```c*
    ((b)[_bufhdr((b))->len++] = __VA_ARGS__))

// hlt-start
#define buffree(b) ((b) ? (free(_bufhdr(b)), b=NULL) : 0)
#define bufclear(b) ((b) ? _bufhdr((b))->len = 0 : 0)
// hlt-end

#define bufloop(b, c) for (usize c = 0; c < buflen(b); c++)
```

Freeing just calls `free` on the header, and clearing simply sets the length to 0.

And with that, the buffer implementation is ready to be used! As an exercise, I suggest you to implement `bufremove` which removes an item at a specific index. Here is the whole implementation as a single file:

```c
#include <stdint.h>
#include <stdlib.h>

#define MIN(a, b) (a < b ? a : b)
#define MAX(a, b) (a > b ? a : b)
#define CLAMP_MIN(x, min) (MAX(x, min))
#define CLAMP_MAX(x, max) (MIN(x, max))

typedef size_t usize;
typedef ssize_t isize;

typedef struct {
    usize cap;
    usize len;
    char data[];
} bufhdr;

#define _bufhdr(b) ((bufhdr*)((char*)(b) - offsetof(bufhdr, data)))

#define buffit(b, n) (bufcap(b) >= n ? 0 : \
    ((b) = _bufgrow((b), (n), sizeof(*(b)))))

#define bufpush(b, ...) (buffit((b), 1 + buflen((b))), \
    ((b)[_bufhdr((b))->len++] = __VA_ARGS__))

#define buffree(b) ((b) ? (free(_bufhdr(b)), b=NULL) : 0)
#define bufclear(b) ((b) ? _bufhdr((b))->len = 0 : 0)

#define bufloop(b, c) for (usize c = 0; c < buflen(b); c++)
#define bufrevloop(b, c) for (usize c = buflen(b); c-- > 0 ;)

#define bufinsert(b, i, ...) (buffit((b), 1 + buflen((b))), \
    memmove((b+i+1), (b+i), (_bufhdr((b))->len-i) * sizeof(*b)), \
    ((b)[i] = __VA_ARGS__), \
    _bufhdr((b))->len++)

usize buflen(const void* buf) {
    return buf ? _bufhdr(buf)->len : 0;
}

usize bufcap(const void* buf) {
    return buf ? _bufhdr(buf)->cap : 0;
}

void* _bufgrow(const void* buf, usize new_len, usize elem_size) {
    usize new_cap = CLAMP_MIN(2 * bufcap(buf), MAX(new_len, 16));
    assert(new_len <= new_cap);

    usize mem_to_alloc = new_cap * elem_size + offsetof(bufhdr, data);
    bufhdr* new_hdr;
    if (buf) {
        new_hdr = (bufhdr*)realloc(_bufhdr(buf), mem_to_alloc);
    }
    else {
        new_hdr = (bufhdr*)malloc(mem_to_alloc);
        new_hdr->len = 0;
    }

    new_hdr->cap = new_cap;
    return new_hdr->data;
}
```
