# Aria Language Reference

```aria
import "std";
import "GameObject.ar";

use std::{Vec, HashMap, io::stdin, math::Vector2};

struct Player<T: Integer> {
    pos: Vector2,
    vel: Vector2,
    id: T,
}

fn main() {
    imm v = Vec::<u32>::new();
    v.push(1);
    v.push(2);
    imm size = @sizeof(u32);
    mut player = Player {
        .pos = Vector2::from_double(1.0, 2.0),
        .vel = Vector2::zero,
        .id = {
            imm ptr = get_default_id_ref();
            ptr.* += 1;
            yeild ptr.* - 1;
        },
    };
    game.register_player(player);
}
```
