# Aria programming language and toolchain

===
- [Source Code](https://github.com/shkhuz/aria)
- [Documentation](doc/)
===

Aria is an experimental systems programming language built to improve on C.

```aria*(hello_world.ar)
import std;

imm newMod = false;

// hlt-start
fn main() {
    std.print("Hello World!");
// hlt-end
}
```

```console
$ aria hello_world.ar
Hello World!
```
