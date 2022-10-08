import os
import sys
import re
import subprocess
from glob import glob

CT_NONE = 0
CT_ARIA = 1
CT_CONSOLE = 2

PS_NONE = 0
PS_STRING = 1
PS_CHAR = 2
PS_NUMBER = 3
PS_WORD = 4
PS_INPUT = 5

CLOSE_SPAN_AFTER_WORD = False

synhlt = {
    "let": "k",
    "mut": "k",
    "fn": "k",
    "struct": "k",
    "extern": "k",
    "if": "k",
    "else": "k",
    "return": "k",
    "for": "k",
    "in": "k",
    "as": "k",
    "with": "k",
    "and": "k",
    "or": "k",
    "not": "k",

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
    "bool": "t",
    "void": "t",
    "Self": "t",

    "true": "c",
    "false": "c",
    "null": "c",
    "self": "c",
}

mdfiles = [y for x in os.walk('.') for y in glob(os.path.join(x[0], '*.md'))]
for mdpath in mdfiles:
    print("Compiling ", mdpath, "...", sep='')
    contents = ""
    with open(mdpath, 'r') as handle:
        contents = handle.readlines()
        handle.close()

    title = contents[0][2:].strip()
    htmlpath = mdpath.replace("md", "html")

    handle = open(htmlpath, 'w')
    handle.write(
"""<!DOCTYPE html>
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
"""
    )

    if mdpath != "./index.md":
        contents[0] = "# <a href='/'>huzaifa</a> / " + title
        if "/blog/" not in mdpath:
            contents[0] += '\n'

    if "/blog/" in mdpath:
        mdpathspl = mdpath.split('/')
        author = "<br><span class='author'>By Huzaifa Shaikh, " + subprocess.check_output(["date", "-d", "{}/{}/{}".format(mdpathspl[-4], mdpathspl[-3], mdpathspl[-2]), "+%b %d, %Y"]).decode("utf-8").strip() + "</span>\n"
        contents[0] = contents[0] + author

    codetype = CT_NONE
    word = ""
    for i, line in enumerate(contents):
        newline = ""
        parsestate = PS_NONE
        if line.strip().startswith("{{{"):
            ctstr = line.strip()[3:]
            contents[i] = "<pre class='" + ctstr + "'><code>"

            if ctstr == "aria": 
                codetype = CT_ARIA
            elif ctstr == "console":
                codetype = CT_CONSOLE
        
        elif line.strip() == "[[[":
            contents[i] = "<aside>\n\n"

        elif line.strip().startswith("}}}"):
            snippetpath = line.strip()[4:]
            if snippetpath != "":
                contents[i] = "</code></pre>\n<div class='code-snippet-filename'>" + snippetpath + "</div>\n"
            else:
                contents[i] = "</code></pre>\n"
            codetype = CT_NONE

        elif line.strip() == "]]]":
            contents[i] = "\n</aside>\n"

        elif codetype == CT_ARIA:
            for j, ch in enumerate(line):
                if ch == '"':
                    if parsestate != PS_STRING:
                        parsestate = PS_STRING
                        newline += "<span class='s'>\""
                    elif parsestate == PS_STRING:
                        parsestate = PS_NONE
                        newline += "\"</span>"
                
                elif parsestate == PS_STRING:
                    newline += ch

                elif ch == "'":
                    if parsestate != PS_CHAR:
                        parsestate = PS_CHAR
                        newline += "<span class='ch'>'"
                    elif parsestate == PS_CHAR:
                        parsestate = PS_NONE
                        newline += "'</span>"
                elif parsestate == PS_CHAR:
                    newline += ch

                elif ch == '@':
                    newline += "<span class='i'>@"
                    CLOSE_SPAN_AFTER_WORD = True

                elif parsestate != PS_WORD and (ch.isalpha() or ch == '_'):
                    parsestate = PS_WORD
                    word += ch

                elif parsestate == PS_WORD and (ch.isalpha() or ch.isdigit() or ch == '_'):
                    word += ch

                elif parsestate != PS_NUMBER and ch >= '0' and ch <= '9':
                    parsestate = PS_NUMBER
                    newline += "<span class='n'>"
                    newline += ch

                elif parsestate == PS_NUMBER and ((ch >= '0' and ch <= '9') or ch == '.'):
                    newline += ch

                else:
                    if parsestate == PS_NUMBER:
                        parsestate = PS_NONE
                        newline += "</span>"
                        newline += ch
                    elif parsestate == PS_WORD:
                        parsestate = PS_NONE
                        if word in synhlt or CLOSE_SPAN_AFTER_WORD:
                            if word in synhlt:
                                newline += "<span class='" + synhlt[word] + "'>"
                                newline += word
                                newline += "</span>"
                                newline += ch
                        
                            if CLOSE_SPAN_AFTER_WORD:
                                CLOSE_SPAN_AFTER_WORD = False
                                newline += word
                                newline += "</span>"
                                newline += ch
                        
                        else:
                            newline += word
                            newline += ch
                        word = ""
                    else:
                        newline += ch
            newline = re.sub("([a-zA-Z_][a-zA-Z0-9_.]*::)", "<span class='strp'>\\1</span>", newline)
            contents[i] = newline
        elif codetype == CT_CONSOLE:
            for j, ch in enumerate(line):
                if j == 0 and ch == '$':
                    parsestate = PS_INPUT
                    newline += ch
                    newline += "<span class='i'>"
                else:
                    newline += ch
            if parsestate == PS_INPUT:
                parsestate = PS_NONE
                newline += "</span>"
            contents[i] = newline


    pandocproc = subprocess.Popen(["pandoc", "--from=markdown+tex_math_single_backslash+tex_math_dollars", "--to=html5", "--mathjax"], stdout=subprocess.PIPE, stdin=subprocess.PIPE)
    pandocproc.stdin.write(''.join(contents).encode("utf-8"))
    pandocout, pandocerr = pandocproc.communicate()
    pandocproc.wait()

    pandocoutstr = pandocout.decode("utf-8")
    pandocoutstr = re.sub("(<table.*>)", "<div class='table-wrapper'>\n\\1", pandocoutstr)
    pandocoutstr = re.sub("</table>", "</table>\n</div>", pandocoutstr)

    handle.write(pandocoutstr)
    
    if mdpath == "./index.md":
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
</html>"""
    )
    handle.close()

if len(sys.argv) > 1 and sys.argv[1] == '-r':
    httpserverproc = subprocess.Popen(['http-server', '-p', '8080', '.'])
    httpserverproc.wait()
