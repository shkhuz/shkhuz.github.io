---
title: Aria language reference
---

This is an informal dump of my design of the language.

```aria
// Hello World
imm std = struct("std");

pub fn main() {
    std.print("Hello world!");
}

// Variables
imm a = 1;
mut b = 2;
imm c: u32 = 2;

// Functions
fn b() {}
fn b(a: u32) {}
fn b(a: u32 = 1) {}                     // optional parameter value
fn c() u8 {}

// Arrays
imm a = .{1, 2, 3};
mut c = b[0];
mut sz = b.len;

// Pointers
mut a: usize = 1;
mut p: *usize = &a;
mut immp: *imm usize = &a;
mut b = p.*;

// Slices
imm a: [3]u8 = .{1, 2, 3};
mut b: []u8 = &a;
mut c = b[0];
mut ptr = b.ptr;
mut len = b.len;

// Expression blocks
imm a = {
    imm b = 1;
    yield b;
};

// Loops
for (mut i = 0u8; i < 10; i += 1) {}    // normal loop
for e (b) {}                            // iterating over slice
for *e (b) {}                           // iterating over slice with ptr
for *e, i (b) {}
while (true) {}         
while n (a) {}                          // iterating while optional is not null

// Continue & Break
for e, i (b) {
    if (i == 10) break;
    else if (i == 15) continue;
}

// If 
if (a) 1 else 0;
if (a) {} else {}
if (a) {} else if (b) {} else {}
if n (a) {}                             // unwrapping optional inside if

// Casts
mut a: u8 = 1;
mut b: u16 = 2;
mut c: u16 = a + b;                     // implicit cast
mut d: u8 = @cast(3.00f);               // explicit cast
mut d = @cast(u8, 3.00f);               // explicit cast

// Enums
imm A = enum {
    a, b, c,
};

mut a = A.a;
mut b = .b;

// Switch
switch (a) {
    a, b, c => {},
    else => {},
}

// Structs
imm a = struct {
    a: u8,
    b: u16,
    c: u32,
    d: struct {},

    mut e: u8;
    fn f() {}
};

// Unions
imm a = union {
    a: u8, 
    b: u16,
};

// Imports
imm basil = struct("basil");            // imports 'basil.ar' file in cwd
                                        // if file not found in cwd, it checks
                                        // inside the lib directory

// Type
imm A = u32;
imm B = u64;

fn get(a: A, b: type) {}

// Function Pointers
fn a() {}
imm p: *imm fn() = &a;
p();

// Static invocation at compile-time
fn add(a: u32, b: u32) u32 {
    return a + b;
}

mut a = 2u32;
mut b = 3u32;
// 'c' is known at compile-time
mut c = siv add(a, b);
```
