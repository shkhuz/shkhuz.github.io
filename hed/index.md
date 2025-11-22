---
title: Hed -- a highly efficient text editor
nav:
  - label: Source Code
    url: https://github.com/shkhuz/hed
  - label: Download
    url: https://github.com/shkhuz/hed
---

Hed is a highly efficient modal text editor which uses keyboard-only
actions to edit text.

**Why another text editor?** Hed has much going for it:

- Lightweight: ~230KB on x64 linux
- Mark editing: operate on regions defined by a mark
- Modal editing: like Vim you know and love
- Intuitive & sane default keybinds: based on finger positions and not on
"first-letter-of-word", etc.
- Just a single executable--no runtimes or external dependencies.

**Why you shouldn't consider this editor:** I made this editor
to be as simple and lightweight as possible: if you need:

- An IDE-like editing experience,
- LSP-support,
- tree-sitter,
- Emacs-like customizability,
- GUI, etc

then this editor is not for you.

### Keybindings (Normal mode)

<table><thead>
  <tr>
    <th>Key</th>
    <th>Action</th>
    <th>Key</th>
    <th>Action</th>
  </tr></thead>
<tbody>
  <tr>
    <td>`i`</td>
    <td>switch to insert mode</td>
    <td>`,`</td>
    <td>open line below cursor</td>
  </tr>
  <tr>
    <td>`w`</td>
    <td>delete char under cursor</td>
    <td>`d`</td>
    <td>set mark</td>
  </tr>
  <tr>
    <td>`&lt;backtick&gt;`</td>
    <td>exit editor</td>
    <td>`f`</td>
    <td>cut marked region</td>
  </tr>
  <tr>
    <td>`&lt;C-k&gt;`</td>
    <td>scroll page up</td>
    <td>`c`</td>
    <td>paste from clipboard</td>
  </tr>
  <tr>
    <td>`&lt;C-j&gt;`</td>
    <td>scroll page down</td>
    <td>`b`</td>
    <td>repeat search forward</td>
  </tr>
  <tr>
    <td>`a`</td>
    <td>goto beginning of line</td>
    <td>`B`</td>
    <td>repeat search backward</td>
  </tr>
  <tr>
    <td>`;`</td>
    <td>goto end of line</td>
    <td>`&lt;A-m&gt;`</td>
    <td>switch to command mode</td>
  </tr>
  <tr>
    <td>`h`</td>
    <td>left</td>
    <td>`&lt;A-s&gt;`</td>
    <td>save file</td>
  </tr>
  <tr>
    <td>`j`</td>
    <td>down</td>
    <td>`/`</td>
    <td>switch to search mode</td>
  </tr>
  <tr>
    <td>`k`</td>
    <td>up</td>
    <td>`gg`</td>
    <td>goto first line</td>
  </tr>
  <tr>
    <td>`l`</td>
    <td>right</td>
    <td>`G`</td>
    <td>goto last line</td>
  </tr>
  <tr>
    <td>`o`</td>
    <td>forward word</td>
    <td>`e`</td>
    <td>undo</td>
  </tr>
  <tr>
    <td>`n`</td>
    <td>backward word</td>
    <td>`E`</td>
    <td>redo</td>
  </tr>
  <tr>
    <td>`J`</td>
    <td>next paragraph</td>
    <td>`K`</td>
    <td>prev paragraph</td>
  </tr>
</tbody></table>

<!--
< -> open line above cursor
s -> undo / redo
-->

### Keybindings (Insert mode)

<table><thead>
  <tr>
    <th>Key</th>
    <th>Action</th>
    <th>Key</th>
    <th>Action</th>
  </tr></thead>
<tbody>
  <tr>
    <td>`&lt;bksp&gt;`</td>
    <td>delete char to left</td>
    <td>`&lt;tab&gt;`</td>
    <td>indent</td>
  </tr>
  <tr>
    <td>`&lt;left&gt;`</td>
    <td>left</td>
    <td>`&lt;right&gt;`</td>
    <td>right</td>
  </tr>
  <tr>
    <td>`&lt;up&gt;`</td>
    <td>up</td>
    <td>`&lt;down&gt;`</td>
    <td>down</td>
  </tr>
  <tr>
    <td>`&lt;esc&gt;`</td>
    <td>switch to normal mode</td>
    <td></td>
    <td></td>
  </tr>
</tbody>
</table>
