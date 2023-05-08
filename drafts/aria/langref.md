# Aria Language Reference

```aria
import "std";
import "GameObject.ar";

use std.{
    Vec,
    HashMap,
    io.stdin,
    math.Vector2,
};

struct Player<T: Integer> {
    pos: Vector2,
    vel: Vector2,
    id: T,
}

fn print<T>(fmt: []u8, args: T) void
where T: Tuple
{
}

fn main() void {
    print("hello, {}", ["df"]);
    imm v = Vec::<u32>.new();
    v.push(1);
    v.push(2);
    imm sum = v
        .sum()
        .collect::<u32>();
    imm pairs: [_][usize, []u8] = [[0, "zero"], [1, "one"]];
    imm size = @sizeof(u32);
    mut player = Player {
        .pos = Vector2.from_double(1.0, 2.0),
        .vel = Vector2.zero,
        .id = {
            imm ptr = get_default_id_ref();
            ptr.* += 1;
            yield ptr.* - 1;
        },
    };
    game.register_player(player);
}

error FileError {
    invalid_path: []u8,
    cannot_open: []u8,
}

fn read_file(path: []u8) FileError![]u8 {
}
```main.ar

```aria
fn malloc<T>(n: usize) ![]T {
    imm page = current_page()?;
    imm ptr = page.ptr?;
    page.ptr += n;
    return []T [.ptr = ptr, .len = n];
}

fn realloc<T>(old: []T, newsize: usize) ![]T {
    imm new = malloc(newsize);
    memcpy(new, old);
    free(old);
    return new;
}

struct Vec<T> {
    data: []T,
    len: usize,
    cap: usize,

    imm default_len = 8;
    imm multiplier = 1.5;
}

impl<T> Vec<T> {
    fn new() !Self {
        return Self [
            .data = malloc(
                @sizeof(T) * Self.default_len)?,
            .len = 0,
            .cap = Self.default_len,
        ];
    }

    fn push(self: *Self, elem: T) !void {
        grow()?;
        self.data[self.len] = elem;
        self.len += 1;
    }

    fn grow(self: *Self) !void {
        if (self.len == self.cap) {
            imm newcap = (self.cap * 1.5) as usize;
            self.data = realloc(self.data, newcap)?;
            self.cap = newcap;
        }
    }
}

trait ToString {
    fn to_string(self: *Self) String;
}

impl<T: ToString> ToString for Vec<T> {
    fn to_string(self: *Self) String {
        mut s = String.new();
        for (self.data) |e, i| {
            s.append(e.to_string());
            if (i != self.data.len-1) s.append(", ");
        }
    }
}
```std.ar

```aria
import "std";
use std.{malloc, Vec, ToString};

fn main() void {
    mut v = Vec::<i32>.new();
    v.push(1);
    v.push(2);

    std.println("{}", v.to_string]);[
}
```main.ar

```aria
error AllocError {
    mem_not_available: i8,
}

fn malloc<T>(n: usize) ?![]T {
    imm page = get_current_page()?;
}

fn get_current_page() !Page {
    return error.std.FileError.not_found: 10;
}

union(enum) U {
    real: f32,
    int: i32,
}

union(enum) Expr {
    binary: [?*Expr, ?*Expr],
    unary: ?*Expr,
}

fn main() {
    imm expr: Expr = Expr.binary: [null, null];
    return error.not_found: ["main.ar", "File not found"];
}
```
