# Aria Programming Language

Aria is an experimental general-purpose programming language built to improve
on C. 

<a class="button extern-link" target="_blank" href="https://github.com/shkhuz/aria">GitHub</a>

_Note: Aria is in heavy development right now. Please refrain from using the 
compiler apart from testing until it matures enough._

Obligatory hello world code snippet:

```
const std = @import("std");

fn main() {
    std.write("Hello, World!");
}
```

```shell
$ ariac main.ar
$ ./a.out
Hello, World!
```

Aria is heavily inspired by C, Zig, Rust, Go, and others. Some neat features:

- No runtime or GC.
- Compile-time support.
- Types as first class citizens.
- No classes/inheritance garbage.
- Minimal features, no bloat (yes this is a feature).

Aria's primary aim is to be _simple_. By simple, we mean:

- No unnecessary features,
- Unambiguous, readable code,
- Very close to bare-metal.

Please see the [documentation](doc.html) to learn more about the language.

We hope you like what we have made ;)
