# Aria language reference

This document is a reference for the _Aria language specification_. All features
are in reference to the _master_ branch, unless otherwise noted.

## Functions

Function definitions are done using `fn`. Functions are private by default.

### Function definitions

```aria
// Private function with no parameters
// and no return value
fn main() void {}
```

```aria
// Public function with one parameter
// and a return value
pub fn get_entity(idx: usize) ?*Entity {
    return null;
}
```
