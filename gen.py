import sys
import os
import subprocess
import re
from pathlib import Path
from glob import glob
from enum import Enum

def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)

class TokenKind(Enum):
    STRING, \
    CHAR, \
    WORD, \
    NUMBER, \
    SPACE, \
    NEWLINE, \
    TRIPLE_LBRACK, \
    TRIPLE_RBRACK, \
    TRIPLE_BACKTICK, \
    TRIPLE_EQUAL, \
    TRIPLE_TILDE, \
    COMMENT, \
    FAT_ARROW, \
    LANGBR, \
    RANGBR, \
    ELSE = range(16)

PRE_ARIA = 0
PRE_CONSOLE = 1
PRE_NONE = 2

synhlt = {
    "imm": "k",
    "mut": "k",
    "pub": "k",
    "fn": "k",
    "type": "k",
    "struct": "k",
    "union": "k",
    "enum": "k",
    "error": "k",
    "extern": "k",
    "if": "k",
    "else": "k",
    "for": "k",
    "while": "k",
    "return": "k",
    "break": "k",
    "continue": "k",
    "yield": "k",
    "use": "k",
    "import": "k",
    "where": "k",
    "for": "k",
    "as": "k",
    "and": "k",
    "or": "k",
    "=": "k",
    "|": "k",

    "u8": "t",
    "u16": "t",
    "u32": "t",
    "u64": "t",
    "usize": "t",
    "i8": "t",
    "i16": "t",
    "i32": "t",
    "i64": "t",
    "isize": "t",
    "f32": "t",
    "f64": "t",
    "bool": "t",
    "void": "t",
    "noreturn": "t",
    "Self": "t",

    "true": "c",
    "false": "c",
    "null": "c",
}

class Token:
    lexeme = ""
    kind = TokenKind.ELSE
    line = 0 # zero indexed

    def __init__(self, lexeme, kind, line):
        self.lexeme = lexeme
        self.kind = kind
        self.line = line

    def __str__(self):
        return self.lexeme

    def __repr__(self):
        return self.lexeme

def lex(mdcode):
    tokens = []
    newline_pos = {}
    start = 0
    current = 0
    line = 0

    while True:
        start = current
        if mdcode[current].isalpha() or mdcode[current] == '_':
            while mdcode[current].isalnum() or mdcode[current] == '_':
                current += 1
            tokens.append(Token(mdcode[start:current], TokenKind.WORD, line))
        elif mdcode[current].isdigit():
            while mdcode[current].isalnum() or mdcode[current] == '.':
                current += 1
            tokens.append(Token(mdcode[start:current], TokenKind.NUMBER, line))
        elif mdcode[current] == '"':
            current += 1
            while mdcode[current] != '"' and mdcode[current] != '\n':
                current += 1
            if mdcode[current] == '"':
                current += 1
            tokens.append(Token(mdcode[start:current], TokenKind.STRING, line))
        elif mdcode[current] == "'":
            current += 1
            while mdcode[current] != "'" and mdcode[current] != '\n':
                current += 1
            if mdcode[current] == "'":
                current += 1
            tokens.append(Token(mdcode[start:current], TokenKind.CHAR, line))
        elif mdcode[current] == '`' and mdcode[current+1] == '`' and mdcode[current+2] == '`':
            current += 3
            tokens.append(Token(mdcode[start:current], TokenKind.TRIPLE_BACKTICK, line))
        elif mdcode[current] == '[' and mdcode[current+1] == '[' and mdcode[current+2] == '[':
            current += 3
            tokens.append(Token(mdcode[start:current], TokenKind.TRIPLE_LBRACK, line))
        elif mdcode[current] == ']' and mdcode[current+1] == ']' and mdcode[current+2] == ']':
            current += 3
            tokens.append(Token(mdcode[start:current], TokenKind.TRIPLE_RBRACK, line))
        elif mdcode[current] == '=' and mdcode[current+1] == '=' and mdcode[current+2] == '=':
            current += 3
            tokens.append(Token(mdcode[start:current], TokenKind.TRIPLE_EQUAL, line))
        elif mdcode[current] == '~' and mdcode[current+1] == '~' and mdcode[current+2] == '~':
            current += 3
            tokens.append(Token(mdcode[start:current], TokenKind.TRIPLE_TILDE, line))
        elif mdcode[current] == '=' and mdcode[current+1] == '>':
            current += 2
            tokens.append(Token(mdcode[start:current], TokenKind.FAT_ARROW, line))
        elif mdcode[current] == '<':
            current += 1
            tokens.append(Token(mdcode[start:current], TokenKind.LANGBR, line))
        elif mdcode[current] == '>':
            current += 1
            tokens.append(Token(mdcode[start:current], TokenKind.RANGBR, line))
        elif mdcode[current] == '/' and mdcode[current+1] == '/':
            current += 2
            while mdcode[current] != '\n' and mdcode[current] != '\0':
                current += 1
            tokens.append(Token(mdcode[start:current], TokenKind.COMMENT, line))
        elif mdcode[current] == ' ' or mdcode[current] == '\t':
            while mdcode[current] == ' ' or mdcode[current] == '\t':
                current += 1
            tokens.append(Token(mdcode[start:current], TokenKind.SPACE, line))
        elif mdcode[current] == '\n':
            current += 1
            newline_pos[line] = len(tokens)
            tokens.append(Token(mdcode[start:current], TokenKind.NEWLINE, line))
            line += 1
        elif mdcode[current] == '\0':
            return tokens, newline_pos
        else:
            current += 1
            tokens.append(Token(mdcode[start:current], TokenKind.ELSE, line))

if len(sys.argv) < 2:
    eprint("error: no input files");
    sys.exit(1)

mdpath = Path(sys.argv[1])
print("Compiling ", mdpath, "...", sep='', end='')
contents = ""
with mdpath.open('rb') as handle:
    contents = handle.read().decode("utf-8")
    contents += '\0'
    handle.close()

tokens, newline_pos = lex(contents)

title = ""
change_title = False
if mdpath != Path("index.md"):
    change_title = True
    #if Path("blog/") in mdpath.parents:
    #    mdpathspl = str(mdpath).split('/')
    #    author = "<br><span class='author'>By Huzaifa Shaikh, " + subprocess.check_output(["date", "-d", "{}/{}/{}".format(mdpathspl[-4], mdpathspl[-3], mdpathspl[-2]), "+%b %d, %Y"]).decode("utf-8").strip() + "</span>\n"
    #    tokens[newline_pos[0]].lexeme = author + tokens[newline_pos[0]].lexeme

nav = False
pre = False
pretype = PRE_NONE
toc = False
toc_add_idx = 0

for i, _ in enumerate(tokens):
    if tokens[i].kind == TokenKind.TRIPLE_BACKTICK:
        pre = not pre
        if pre:
            if tokens[i+1].kind != TokenKind.NEWLINE:
                tokens[i].lexeme = "<pre class='"
                tokens[newline_pos[tokens[i].line]].lexeme = "'><code>"
                if tokens[i+1].lexeme == "aria":
                    i += 1
                    pretype = PRE_ARIA
        else:
            if tokens[i+1].kind != TokenKind.NEWLINE:
                tokens[i].lexeme = "</code></pre>\n<div class='code-snippet-filename'>"
                tokens[newline_pos[tokens[i].line]].lexeme = "</div>\n"
            else:
                tokens[i].lexeme = "</code></pre>"
            pretype = PRE_NONE

    elif tokens[i].kind == TokenKind.TRIPLE_EQUAL:
        nav = not nav
        if nav:
            tokens[i].lexeme = "<nav id='main-nav'>"
        else:
            tokens[i].lexeme = "</nav>"

    elif tokens[i].kind == TokenKind.TRIPLE_LBRACK:
        tokens[i].lexeme = "<aside>\n"
    elif tokens[i].kind == TokenKind.TRIPLE_RBRACK:
        tokens[i].lexeme = "\n</aside>"

    elif tokens[i].kind == TokenKind.TRIPLE_TILDE:
        toc = True
        toc_add_idx = i

    elif tokens[i].kind == TokenKind.LANGBR and pre:
        tokens[i].lexeme = "&lt;"
    elif tokens[i].kind == TokenKind.RANGBR and pre:
        tokens[i].lexeme = "&gt;"

    elif tokens[i].lexeme == '#' and tokens[i-1].kind == TokenKind.NEWLINE and nav and not toc:
        for tok in tokens[i+1:newline_pos[tokens[i].line]]:
            title += tok.lexeme
        if change_title:
            tokens[i+1].lexeme = "<a href='/'>huzaifa</a> / " + tokens[i+1].lexeme
        tokens[i].lexeme = '# ';

    elif pretype == PRE_ARIA:
        if tokens[i].kind == TokenKind.STRING:
            tokens[i].lexeme = "<span class='s'>" + tokens[i].lexeme + "</span>"
        elif tokens[i].kind == TokenKind.CHAR:
            tokens[i].lexeme = "<span class='ch'>" + tokens[i].lexeme + "</span>"
        elif tokens[i].kind == TokenKind.NUMBER:
            tokens[i].lexeme = "<span class='n'>" + tokens[i].lexeme + "</span>"
        elif tokens[i].kind == TokenKind.COMMENT:
            tokens[i].lexeme = "<span class='com'>" + tokens[i].lexeme + "</span>"
        elif tokens[i].lexeme in synhlt:
            tokens[i].lexeme = "<span class='" + synhlt[tokens[i].lexeme] + "'>" + tokens[i].lexeme + "</span>"
        elif tokens[i].lexeme == '@' and tokens[i+1].kind == TokenKind.WORD and tokens[i+2].lexeme == '(':
            tokens[i].lexeme = "<span class='i'>@"
            i += 1
            tokens[i].lexeme += "</span>"
            i += 1

if toc:
    pandoctocproc = subprocess.Popen(["pandoc", "--toc", "--template=toc-only.html5"], stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    pandoctocproc.stdin.write(''.join(contents).encode("utf-8"))
    pandoctocout, pandoctocerr = pandoctocproc.communicate()
    pandoctocproc.wait()

    pandoctocoutstr = pandoctocout.decode("utf-8")
    tokens[toc_add_idx].lexeme = "<details><summary><span>Table of contents</span></summary>" + pandoctocoutstr + "</details>"

mdcode_modified = ""
for tok in tokens:
    mdcode_modified += tok.lexeme

htmlpath = mdpath.with_suffix(".html")
handle = htmlpath.open('w')

handle.write("""<!DOCTYPE html>
<html lang=\"en\">
<head>
<meta charset=\"utf-8\">
<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">

<title>""")
handle.write(title)
handle.write(
"""</title>
<link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"/assets/apple-touch-icon.png\">
<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"/assets/favicon-32x32.png\">
<link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"/assets/favicon-16x16.png\">
<link rel=\"manifest\" href=\"/assets/site.webmanifest\">
<link rel=stylesheet href=\"/style.css\">

<!-- Google tag (gtag.js) -->
<script async src=\"https://www.googletagmanager.com/gtag/js?id=G-QQS3D5BETB\"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', 'G-QQS3D5BETB');
</script>

<script data-host=\"https://microanalytics.io\" data-dnt=\"false\" src=\"https://microanalytics.io/js/script.js\" id="ZwSg9rf6GA" async defer></script>

<script id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js\"></script>

<script>
(function() {
  var h, a, f;
  a = document.getElementsByTagName('link');
  for (h = 0; h < a.length; h++) {
    f = a[h];
    if (f.rel.toLowerCase().match(/stylesheet/) && f.href) {
      var g = f.href.replace(/(&|\?)rnd=\d+/, '');
      f.href = g + (g.match(/\?/) ? '&' : '?');
      f.href += 'rnd=' + (new Date().valueOf());
    }
  } // for
})()
</script>

</head>

<body>
<article>
""")

pandocproc = subprocess.Popen(["pandoc", "--from=markdown+tex_math_single_backslash+tex_math_dollars", "--to=html5", "--mathjax"], stdout=subprocess.PIPE, stdin=subprocess.PIPE)
pandocproc.stdin.write(''.join(mdcode_modified).encode("utf-8"))
pandocout, pandocerr = pandocproc.communicate()
pandocproc.wait()

pandocoutstr = pandocout.decode("utf-8")
pandocoutstr = re.sub("(<table.*>)", "<div class='table-wrapper'>\n\\1", pandocoutstr)
pandocoutstr = re.sub("</table>", "</table>\n</div>", pandocoutstr)
pandocoutstr = re.sub("<summary>\n", "<summary>", pandocoutstr)
pandocoutstr = re.sub("\n</summary>", "</summary>", pandocoutstr)

handle.write(pandocoutstr)

if mdpath == Path("index.md"):
    handle.write("<h2>Blog</h2>\n")
    if os.path.isdir("blog"):
        handle.write("<ul>\n")
        for year in sorted(glob("blog/*")):
            for month in sorted(glob(year + "/*")):
                for day in sorted(glob(month + "/*")):
                    for post in sorted(glob(day + "/*")):
                        if post.endswith(".md"):
                            posttitle = ""
                            with open(post) as postfile:
                                posttitle = postfile.readline()[2:].strip()
                            handle.write("<li>{}/{}/{}: <a href='{}.html'>{}</a></li>\n".format(
                                os.path.basename(year),
                                os.path.basename(month),
                                os.path.basename(day),
                                os.path.splitext(post)[0],
                                posttitle))
        handle.write("</ul>\n")

handle.write(
"""</article>
</body>
</html>""")
handle.close()
print(" ok")

