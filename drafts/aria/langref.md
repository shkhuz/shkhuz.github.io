# Aria Language Reference

## Functions

```aria
// Default function (no args, void return)
func main() {
}

// Function with a return type
func main() u8 {
}

// Function with arguments
func main(argc: u8, argv: **u8) {
}

// Generic function
func parse::T(s: []u8) T {
    return if (@typeof(s) == .int) parse_int(s);
    else if (@typeof(s) == .float) parse_float(s);
}
```

## Variables

```aria
// Immutable (with a mandatory initialisation expression)
imm count = 12;

// Mutable
mut count: u8;

// Mutable with an initialisation expression
mut count = 12;
```

## Pointers

```aria
mut size: u32 = 4;

// Immutable pointer
imm ptr: *imm u32 = &size;

// Mutable pointer (data is mutable, pointer itself is immutable in both cases)
imm ptr: *u32 = &size;

// Const-ness if not specified will depend on the pointee (mutable in this case)
imm ptr = &size;

// Dereferencing a pointer
set_players(ptr.*);
ptr.* = 5;
```

## Arrays

```aria
// Size and type specified
imm colors: [3]u8 = [0x1, 0x2, 0x3];

// Only type specified
imm colors: [_]u8 = [0x1, 0x2, 0x3];

// Only size specified
imm colors = [0x1, 0x2, 0x3];
```

## Slices

```aria
mut colors: [_]u32 = [0x1, 0x2, 0x3];

// Immutable slice
imm slice: []imm u32 = &size;

// Mutable slice (data is mutable, slice itself is immutable in both cases)
imm slice: []u32 = &size;

// Const-ness if not specified will depend on the pointee (mutable in this case)
imm slice = &size;

// Using a slice
show_color(slice[0]);
slice[0] = 0x4;

// Slices have a ptr and a len field
imm len = slice.len;
imm ptr = slice.ptr;
```

## Structs

```aria
// Empty struct
type Info = struct {
};

// Struct with fields
type Info = struct {
    player_count: u32,
    enemy_count: u32,
};

// Struct with generic type
type Vec = struct::T {
    data: []T,
};

// Struct with constrained generic type
type Vec = struct::(T = ToString) {
    data: []T,
};

// Constrained using where clause
type HashMap = struct::(K, V)
where
    K = ToIntegral,
    V = ToString + Serialize,
{
    data: []struct {k: K, v: V},
};

// Instatiating a struct
imm info: Info;

// Instatiating a struct with generic type
mut v: Vec::u32;

// Multiple generic types
mut h: HashMap::(u32, []u8);

// Instantialing a struct with a struct literal
mut v = Vec::(u32) [
    .data = @cast(malloc(64), *u32),
];
```

## Intrinsic functions

```aria
// @import()
type std = @import("std");

// @cast()
imm count: *u8 = @cast(0x0, *u8);
```

## Return

```aria
return true;

// Return void
return;
```

## Yield

```aria
imm count = {
    imm players = Info.players();
    if (players < 32) players = 32;
    yield players;
};
```

## If

```aria
if (allowed) {
    return true;
}

// If with prologue declaration
if with(x = opts()) (x.enabled) {
    return x.flag;
}

// If with variable capture
if (get_global_state()) |s| {
    s.exit();
}
```

## While

```aria
while (is_running()) {
}

// While with variable capture
while (next()) |x| {
}

// While with prologue declaration
while with(n = next()) (n.enabled) {
}
```

## For

```aria
// Looping a slice with variable capture
for (players) |p| {
}

// Optional index
for (players) |p, i| {
}

// C-style for
for (mut i = 0; i < 10; i += 1) {
}
```

## Optionals

```aria
mut val: ?u32 = null;
mut unwrapped: u32 = @unwrap_or(val, 0);
```

```aria
func get_active_player() ?u32 {
    imm ctx = get_current_ctx()?;
    ctx.enabled = true;
    return ctx.player;
}
```

## Errors

```aria
mut val: !u8 = parse_int("38");
if (val) |n| printf("Parsed as {}", n);
else |err| when (err) {
    Err.Invalid => @panic();
    else => {},
}
```

```aria
// Verbose
func read_file(f: []u8) ![]u8 {
    return (fs.open_file(f) catch |err| return err).read() catch |err| return err;
}

// Compact
func read_file(f: []u8) ![]u8 {
    return fs.open_file(f)?.read()?;
}
```

```aria
trait Integral {
    type Ty;
    func value() Ty;
}

type Integer = struct { i: i32 }
impl Integral for Integer {
    type Ty = i32;
    func value() Ty {
        return self.i;
    }
}

type s = struct::(T = Integer) {
    x: T,
    y: T,
};
```

```aria
// test.ar
mod std = @import("std");
use std.Debug;

func main() {
    imm n = 12;
    imm n_str = n.to_string();
}

mod std = @import("std");
use std.{String, Vec, HashMap};

type allocator = std.alloc.TempAllocator;

imm str = String.new(allocator, "Hey!");

impl::T Clone for !?T
where
    T = Integer,
{
}
```

```aria
// vec.ar
pub type Vec = struct::T {
    data: []T,
    len: usize,
    cap: usize,
}

impl::T Vec::T {
    pub func new() Self {
        return Self [
            .data = @cast(malloc(32), []T),
            .len = 0,
            .cap = 0,
        ];
    }
}

impl::T Iterator for Vec::T {
    type Item = T;
    func next(self) Item {

    }
}
```

```aria
// std.ar
pub type Vec = @import("vec.ar").Vec;
```

```aria
// main.ar
mod std = @import("std.ar");
use std.{Vec, io.println};

pub func main() {
    mut v = Vec::u32.new();
    v.push(0);
    v.push(1);
    v.push(2);

    for (v.iter()) |e| {
        println("{}", e);
    }
}
```
