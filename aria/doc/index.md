# Aria language reference

## Hello world

    import std;
    
    fn main() {
        std.print("Hello world!\n");
    }

## Variables

    imm a = 1;
    mut b = 2;
    imm c: u32 = 2;

## Functions

    fn b() {}
    fn b(a: u32) {}
    fn c() u8 {}

## Arrays

    imm a = .{1, 2, 3};
    mut c = b[0];
    mut sz = b.len;

## Pointers

    mut a: usize = 1;
    mut p: *usize = &a;
    mut immp: *imm usize = &a;
    mut b = p.*;

## Slices

    imm a: [3]u8 = .{1, 2, 3};
    mut b: []u8 = &a;
    mut c = b[0];
    mut sz = b.len;

## Expression blocks

    imm a = {
        imm b = 1;
        yield b;
    };

## Loops

    for (mut i = 0u8; i < 10; i += 1) {}    // normal loop
    for (b; e) {}                           // iterating over slice
    for (b; *e) {}                          // iterating over slice with ptr
    for (b; *e, i) {}
    while (true) {}         
    while (a; n) {}                         // iterating while optional is 
                                            // not null

## Continue & Break

    for (a; n, i) {
        if (i == 10) break;
        else if (i == 15) continue;
    }

## If 

    if (a) 1 else 0;
    if (a) {} else {}
    if (a) {} else if (b) {} else {}
    if (a; n) {}                            // unwrapping optional inside if

## Casts

    mut a: u8 = 1;
    mut b: u16 = 2;
    mut c: u16 = a + b;                     // implicit cast
    mut d: u8 = a + to(u8, b);              // explicit cast ??
    mut d: u8 = a + u8(b);                  // explicit cast ??

## Optionals

    mut a: ?u8 = 1;
    mut b: ?u8 = null;
    mut c: u8 = a orelse 0;                 // default value
    mut d: u8 = a catch return 0;           // do something on null

## Enums

    imm A = enum {
        a, b, c,
    };
    
    mut a = A.a;
    mut b = .b;

## Switch

    switch (a) {
        a, b, c => {},
        else => {},
    }
    
    switch (a) {
        a, b, c (n) => {},
        else (n) => {},
    }

## Structs

    imm a = struct {
        a: u8,
        b: u16,
        c: u32,
        d: struct {},
    
        mut e: u8;
        fn f() {}
    };

## Unions

    imm a = union {
        a: u8, 
        b: u16,
    };

## Tagged Unions

    imm a = union(enum) {
        a: u8,
        b: u16,
    }

## Imports

    import basil;                           // imports 'basil.ar' file in cwd
    import std;                             // if file not found in cwd, it checks
                                            // inside the lib directory

## Type

    imm A = u32;
    imm B = u64;
    
    fn get(a: A, b: type) {}

## Function Pointers

    fn a() {}
    imm p: *imm fn() = &a;
    p();

## Built-in functions

    imm img = %embedFile("img.bmp");

## Compile-time code evaluation

    fn add(a: u32, b: u32) u32 {
        return a + b;
    }
    
    mut a = 2u32;
    mut b = 3u32;
    // 'c' is known at compile-time
    mut c = eval add(a, b);