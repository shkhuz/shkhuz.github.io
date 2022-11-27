# Aria language reference

## Table of contents

- [Variables](#variables)
- [Basic Types](#basic-types)
  - [Primitives](#primitives)
  - [Pointers](#pointers)
  - [Slices](#slices)
  - [Arrays](#arrays)
  - [Tuples](#tuples)
- [Functions](#functions)

## Variables

```aria
// Variables are defined using `mut`
mut index = get_index();

// If the compiler cannot unambiguously infer 
// the type, then the type has to be specified
mut index: u8 = 24;

// If only type is specified, then the variable is 
// zero-initialized
mut index: u8;
```

```aria
// If the value must not change, then `imm` 
// should be used
imm max_length = get_max_length();

// Even if the exact type cannot be inferred here,
// the compiler knows the value at compile-time 
// to ensure all the operations are safe
imm max_length = 5;
```

## Basic types

### Primitives

|              |        |        |       |       |      |       |       |       |
|--------------|--------|--------|-------|-------|------|-------|-------|-------|
| __Integers__ | `u8`   | `u16`  | `u32` | `u64` | `i8` | `i16` | `i32` | `i64` |
| __Floats__   | `f32`  | `f64`  |       |       |      |       |       |       |
| __Other__    | `bool` | `void` |       |       |      |       |       |       |

### Pointers

```aria
// Pointers are declared using `*`
mut entity: *Entity;

// Pointers are mutable by default.
// To declare an immutable pointer, use `imm`
mut emtity: *imm Entity;
```

### Slices

```aria
// Slices are declared using `[]`
imm slice: []u32;

// Similar to pointers, slices are
// mutable by default.
// To declare an immutable slice, use `imm`
imm slice: []imm u32;
```

### Arrays

```aria
// Arrays are declared using `[]` infixed
// with a number
imm array: [3]bool;

// The number may be replaced with an `_`
// to have the compiler infer it
```

### Tuples

```aria
// Tuples are declared using `[]`
imm tuple: [u32, bool, *u8];

// Tuple may be empty
imm tuple: [];
```

## Functions

```aria
// Functions are defined using the `fn` keyword
fn main() {}

// Functions can accept parameters, and return values
fn get_position(player: Player) Coord {}
```
