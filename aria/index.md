---
title: Aria programming language and toolchain
nav: 
  - label: Source Code
    url: https://github.com/shkhuz/aria
  - label: Documentation
    url: doc/
---

```aria(hello_world.ar)
imm std = struct("std");

fn main() void {
    std.print("Hello World!");
}
```

```console
$ aria hello_world.ar
$ ./a.out
Hello World!
```
