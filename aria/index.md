---
title: Aria programming language and toolchain
nav: 
  - label: Source Code
    url: https://github.com/shkhuz/aria
  - label: Documentation
    url: doc/
---

Aria is everything I wanted C to be. I think Zig/Rust/Odin come close to being a better C but there were some things they lacked or their vision wasn't totally aligned on how I did things. Partly the fault lies in me: I just wanted something that I fully understand and have total control over. Think of this a fork of Zig. This is my contribution for it.

Some things I took inspiration from:

- Zig: `||` clause was pretty neat. It's usage for optionals, slices, switches without a lexical keyword really made it a good fit for that syntax. `comptime` is another one. Compile-time evaluation without macros is a godsend once you use it. This multi-stage programming model fares much better that any AST-substitution macro technique I know.
- Go: `defer` but instead of deferring on function-scope, it defers on block-scope.
- Rust: clean syntax.
- Jai: Anonymous literals `.{}`.
- ...

```aria(hello_world.ar)
imm std = struct("std");

fn main() void {
    std.print("Hello World!");
}

std :: struct("std");
main :: fn() {
    std.print("Hello world!");
}
```

```console
$ aria hello_world.ar
Hello World!
```
