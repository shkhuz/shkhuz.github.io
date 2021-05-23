---
title: The Aria project
subtitle: An experimental systems programming language.
---

## Sample Code 

```
module math {
	struct Vector2 {
		x, y: f32,

		proc add(self, other: Vector2) {
			Self {
				x: self.x + other.x,
				y: self.y + other.y,
			}
		}
	}
}
```

### Generics

```
proc max<T>(a: T, b: T) T {
	if a > b { a }
	else { b }
}
```

### Errors

```
@import("std");

proc allocate_memory(n: usize) ![]u8 {
	let mem = std::gp_allocator_mem(n)?;
	std::slice::from_raw(mem, n)
}
```

### Optionals

```
@import("std");

proc main() {
	std::printf(if open_file("test.txt") {
		"file successfully opened"
	} else {
		"file cannot be opened"
	});
}

proc open_file(fpath: string) ?std::File {
	if std::os::openf(fpath, std::io::rb) with file {
		file
	} else {
		none
	}
}
```

### Read User Input

```
@import("std");

proc main() !void {
	let input = std::io::read_to_string()?;
	defer free(input);
	redef input = input as []const u8;
}
```

### Conditional Compilation

```
@import("std");

proc main() !void {
	std::writeln(static match std::os::host_os {
		std::os::OsType::UNIX => "we are on *NIX",
		std::os::OsType::Windows => "windows.",
		else => "something else. hmm...",
	});
}
```

## Motivation

A lot of the language features were inspired or taken over from Rust & Zig.
However, both of them fill a niche of people that I am not a part of.

Rust is over-engineered, and feels lile a feature-bloat now (some C++
developers might not agree). Zig came close, but I had _some_ problems. After
mailing Andrew about these nitpicks and never getting a reply back, I decided
to implement a compiler — just for fun.

One gripe I had about C was its header system: it was a mess. I decided to keep
the basic feature set of C and add a tiny module system. 

As macros were a pain for debugging, I reiterated over the Zig's `comptime`
feature. In Zig, functions can accept types as arguments, but I didn't like
that idea because of `extern` functions and FFI stuff.

I decided to implement a `macro` statement, which can accept an AST node as an
argument. How it differs from a traditional macro is that it does not modify
the AST — it modifies the generated machine code. This small change makes it
possible to namespace macros and package it into a module.

Of course, compile-time asserts and conditionals are possible in regular
functions too. 

One thing Zig implemented perfectly is the error-handling mechanism. A small
addition to the return type of a function makes it possible to handle errors in
a clean and runtime-safe way. 

I decided to do the same in Aria, but felt the need of naming errors sets to be
redundant.

Rust had another syntactical element which hoped to increase developer
productivity. As naming variables is hard, variables in Rust can be redeclared
in the _same_ scope—and with a different type too. 

As the compiler does not warn about this redeclaration (as far as I know), I
worried about name collisions going unnoticed and creating hard-to-trace bugs.
For this, I decided to introduce a keyword `redef` that redeclares something in
the same scope, when the programmer consciously wants to do it. It might seem
like a naive design choice, but if it reduces bugs, I'm on it.

## Building

As the project is not fully mature yet, I have decided to build the compiler in
private — once the compiler is able to bootstrap itself, I will release the
repository online.
