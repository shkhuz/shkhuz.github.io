import sys
import os
import subprocess
import re
from pathlib import Path
from glob import glob

def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)

TOKEN_STRING = 0
TOKEN_WORD = 1
TOKEN_NUMBER = 2
TOKEN_SPACE = 3
TOKEN_NEWLINE = 4
TOKEN_ELSE = 5

PRE_ARIA = 0
PRE_CONSOLE = 1
PRE_NONE = 2
    
class Token:
    lexeme = ""
    kind = 5

    def __init__(self, l, kind):
        self.lexeme = l
        self.kind = kind

    def __str__(self):
        return self.lexeme

    def __repr__(self):
        return self.lexeme

def lex(mdcode):
    tokensall = []
    tokensline = []
    start = 0
    current = 0

    while True:
        start = current
        if mdcode[current].isalpha() or mdcode[current] == '_':
            while mdcode[current].isalnum() or mdcode[current] == '_':
                current += 1
            tokensline.append(Token(mdcode[start:current], TOKEN_WORD))
        elif mdcode[current] == '"':
            current += 1
            while mdcode[current] != '"' and mdcode[current] != '\n':
                current += 1
            if mdcode[current] == '"':
                current += 1
            tokensline.append(Token(mdcode[start:current], TOKEN_STRING))
        elif mdcode[current] == '`' and mdcode[current+1] == '`' and mdcode[current+2] == '`':
            current += 3
            tokensline.append(Token(mdcode[start:current], TOKEN_ELSE))
        elif mdcode[current] == '[' and mdcode[current+1] == '[' and mdcode[current+2] == '[':
            current += 3
            tokensline.append(Token(mdcode[start:current], TOKEN_ELSE))
        elif mdcode[current] == ']' and mdcode[current+1] == ']' and mdcode[current+2] == ']':
            current += 3
            tokensline.append(Token(mdcode[start:current], TOKEN_ELSE))
        elif mdcode[current] == ' ' or mdcode[current] == '\t':
            while mdcode[current] == ' ' or mdcode[current] == '\t':
                current += 1
            tokensline.append(Token(mdcode[start:current], TOKEN_SPACE))
        elif mdcode[current] == '\n':
            current += 1
            tokensline.append(Token(mdcode[start:current], TOKEN_NEWLINE))
            tokensall.append(tokensline)
            tokensline = []
        elif mdcode[current] == '\0':
            if mdcode[current-1] != '\n':
                tokensall.append(tokensline)
            return tokensall
        else:
            current += 1
            tokensline.append(Token(mdcode[start:current], TOKEN_ELSE))
        

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

tokens = lex(contents)

title = ""
for tok in tokens[0][2:]:
    title += tok.lexeme

if mdpath != Path("index.md"):
    tokens[0][2].lexeme = "<a href='/'>huzaifa</a> / " + tokens[0][2].lexeme
    if Path("blog/") in mdpath.parents:
        mdpathspl = str(mdpath).split('/')
        author = "<br><span class='author'>By Huzaifa Shaikh, " + subprocess.check_output(["date", "-d", "{}/{}/{}".format(mdpathspl[-4], mdpathspl[-3], mdpathspl[-2]), "+%b %d, %Y"]).decode("utf-8").strip() + "</span>\n"
        tokens[0][len(tokens[0])-1].lexeme = author + tokens[0][len(tokens[0])-1].lexeme

pre = False
pretype = PRE_NONE

for i, _ in enumerate(tokens):
    for j, _ in enumerate(tokens[i]):
        if tokens[i][j].lexeme == "```":
            pre = not pre
            if pre:
                if tokens[i][j+1].kind != TOKEN_NEWLINE:
                    tokens[i][j].lexeme = "<pre class='"
                    tokens[i][len(tokens[i])-1].lexeme = "'><code>"
                    if tokens[i][j+1].lexeme == "aria":
                        j += 1
                        pretype = PRE_ARIA
            else:
                if tokens[i][j+1].kind != TOKEN_NEWLINE:
                    tokens[i][j].lexeme = "</code></pre>\n<div class='code-snippet-filename'>"
                    tokens[i][len(tokens[i])-1].lexeme = "</div>\n"
                else:
                    tokens[i][j].lexeme = "</code></pre>"

        elif tokens[i][j].lexeme == "[[[":
            tokens[i][j].lexeme = "<aside>\n"
        elif tokens[i][j].lexeme == "]]]":
            tokens[i][j].lexeme = "\n</aside>"

        elif pretype == PRE_ARIA:
            if tokens[i][j].kind == TOKEN_STRING:
                tokens[i][j].lexeme = "<span class='s'>" + tokens[i][j].lexeme + "</span>"

mdcode_modified = ""
for line in tokens:
    for tok in line:
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
