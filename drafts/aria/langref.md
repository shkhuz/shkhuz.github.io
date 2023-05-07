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
    mut player = Player [
        .pos = Vector2.from_double(1.0, 2.0),
        .vel = Vector2.zero,
        .id = {
            imm ptr = get_default_id_ref();
            ptr.* += 1;
            yield ptr.* - 1;
        },
    ];
    game.register_player(player);
}

error FileError {
    invalid_path: []u8,
    cannot_open: []u8,
}

fn read_file(path: []u8) FileError![]u8 {
}
```main.ar
