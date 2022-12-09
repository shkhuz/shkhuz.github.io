# Aria language reference

```aria
where T;

type std = @import("std");

data: []T,
cap: usize,
len: usize,

imm default_cap = 8;

pub fn new() !Self {
    return Self [
        .data = std.malloc(@sizeof(T) * default_cap)!,
        .cap = default_cap,
        .len = 0,
    ];
}

pub fn from_slice(slice: []imm T) !Self {
    return Self [
        .data = {
            mut buf: *T = std.malloc(@sizeof(T) * slice.len)!;
            std.mem.cpy(buf, slice.ptr, slice.len)!;
            yield buf;
        },
        .cap = slice.len,
        .len = slice.len,
    ];
}
```Vec.ar

```aria
type Vec = @import("Vec.ar");

fn main() !void {
    mut v = Vec<usize>.from_slice(&[1, 4, 9])!;
}
```main.ar
