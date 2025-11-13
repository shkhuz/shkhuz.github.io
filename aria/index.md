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
    std.print('c');  // character!
    std.print(192);  // character!
// hlt-end
}
```

```diff
+added this
-removed this
nothing
```

```console
$ aria hello_world.ar \
  nice
Hello World!
```
