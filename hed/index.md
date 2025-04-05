===
# Hed -- a highly efficient text editor

- [Source Code](https://github.com/shkhuz/hed)
- [Download](https://github.com/shkhuz/hed)
===

Hed is a highly efficient modal text editor which uses keyboard-only
actions to edit text.

**Why another text editor?** Hed has much going for it:

- Lightweight: ~230KB on x64 linux
- Mark editing: operate on regions defined by a mark
- Modal editing: like Vim you know and love
- Intuitive & sane default keybinds: based on finger positions and not on
"first-letter-of-word", etc.
- Just a single executable--no runtimes or external dependencies

**Why you shouldn't consider this editor:** I made this editor
to be as simple and lightweight as possible: if you need:

- An IDE-like editing experience,
- LSP-support,
- tree-sitter,
- Emacs-like customizability,
- GUI, etc

then this editor is not for you.

### Keybindings (Normal mode)

Key | Action                                  | Key | Action
--- | ---------                               | --- | ---------
`i` | switch to insert mode                   | `,` | open line below cursor
`w` | delete character under cursor           | `d` | set mark
`<backtick>` | exit editor                    | `f` | cut marked region
`U` | scroll page up                          | `c` | paste from clipboard
`M` | scroll page down                        | `b` | repeat search forward
`a` | goto beginning of line                  | `B` | repeat search backward
`;` | goto end of line                        | `<Alt-m>` | switch to command mode
`h` | left                                    | `<Alt-s>` | save file
`j` | down                                    | `/` | switch to search mode
`k` | up                                      | `gg` | goto first row
`l` | right                                   | `G` | goto last row
`o` | forward word                            | `e` | undo
`n` | backward word                           | `E` | redo

Maybe:

< -> open line above cursor
s -> undo / redo

### Keybindings (Insert mode)

Key | Action                                  | Key | Action
--- | ------------                            | --- | ------------
`<bksp>` | delete character to the left of cursor | `<tab>` | indent
`<left>` | left                                 | `<right>` | right
`<up>` | up                                     | `<down>` | down
`esc` | switch to normal mode                 | |

