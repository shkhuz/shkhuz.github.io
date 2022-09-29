# Aria Programming Language

Aria is an experimental low-level programming language built to improve on C. 

<a class="button extern-link" href="https://github.com/shkhuz/aria">GitHub</a>

[[[
Note: Aria is in heavy development right now. Please refrain from using the 
compiler apart from testing until it matures enough.

To report a bug or suggest a feature, open an issue <a class="extern-link" href="https://github.com/shkhuz/aria/issues">here</a>.
]]]

Please keep in mind that Aria is a personal project—I solely develop and 
maintain the project, to see what a simple, fast, efficient, no-legacy-baggage 
language would look like.

Obligatory hello world code snippet:

{{{aria
const std = @load("std.ar");

def main() {
    std.write("Hello, World!");
}
}}}/hello_world.ar

{{{console
$ ariac hello_world.ar
$ ./a.out
Hello, World!
}}}

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

Please see the [documentation](doc) to learn more about the language.

Aria toolchain is GPLv3 licensed. See the <a class="extern-link" href="https://github.com/shkhuz/aria/blob/master/COPYING">COPYING</a> file for more details.
