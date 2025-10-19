import java.io.*;
import java.nio.file.*;

public class StreamingMarkdownParser {

    public enum PreType {
        C_CPP,
        CONSOLE,
        DEFAULT,
    }

    private boolean inCodeBlock = false;
    private boolean inNav = false;
    private boolean inAside = false;
    private boolean inList = false;
    private boolean inParagraph = false;
    private boolean inBlockquote = false;
    private PreType preType;

    private StringBuilder out = new StringBuilder();
    public String title;

    private String blob1 =
"<!DOCTYPE html>\n" + 
"<html lang='en'>\n" + 
"<head>\n" + 
"<meta charset='utf-8'>\n" + 
"<meta name='viewport' content='width=device-width, initial-scale=1'>\n" + 
"\n" +
"<title>";

    private String blob2 =
"</title>\n" +
"<link rel='apple-touch-icon' sizes='180x180' href='/assets/apple-touch-icon.png'>\n" +
"<link rel='icon' type='image/png' sizes='32x32' href='/assets/favicon-32x32.png'>\n" +
"<link rel='iconj type='image/png' sizes='16x16' href='/assets/favicon-16x16.png'>\n" +
"<link rel='manifest' href='/assets/site.webmanifest'>\n" +
"<link rel=stylesheet href='/style.css'>\n" +
"\n" + 
"<!-- Google tag (gtag.js) -->\n" +
"<script async src='https://www.googletagmanager.com/gtag/js?id=G-QQS3D5BETB'></script>\n" +
"<script>\n" +
"  window.dataLayer = window.dataLayer || [];\n" +
"  function gtag(){dataLayer.push(arguments);}\n" +
"  gtag('js', new Date());\n" +
"\n" +
"  gtag('config', 'G-QQS3D5BETB');\n" +
"</script>\n" +
"\n" +
"<script data-host='https://microanalytics.io' data-dnt='false' src='https://microanalytics.io/js/script.js' id='ZwSg9rf6GA' async defer></script>\n" +
"\n" +
"<script id='MathJax-script' async src='https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js'></script>\n" +
"\n" +
"<script>\n" +
"(function() {\n" +
"  var h, a, f;\n" +
"  a = document.getElementsByTagName('link');\n" +
"  for (h = 0; h < a.length; h++) {\n" +
"    f = a[h];\n" +
"    if (f.rel.toLowerCase().match(/stylesheet/) && f.href) {\n" +
"      var g = f.href.replace(/(&|\\?)rnd=\\d+/, '');\n" +
"      f.href = g + (g.match(/\\?/) ? '&' : '?');\n" +
"      f.href += 'rnd=' + (new Date().valueOf());\n" +
"    }\n" +
"  } // for\n" +
"})()\n" +
"</script>\n" +
"\n" +
"</head>\n" +
"\n" +
"<body>\n" +
"<article>\n";

    private String blob3 =
"</article>\n" +
"</body>\n" +
"</html>\n";

    private String changeExt(String path, String newExt) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex != -1) {
            path = path.substring(0, dotIndex);
        }
        path += newExt;
        return path;
    }

    private String readFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public void parseAndOutput(String filePath) throws IOException {
        try {
            String markdown = readFile(filePath);
            String html = parse(markdown); 
            System.out.println(html);

            try (FileWriter writer = new FileWriter(changeExt(filePath, ".html"))) {
                writer.write(blob1);
                if (title != null) 
                    writer.write(title);
                writer.write(blob2);
                writer.write(html);
                writer.write(blob3);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }
    }
    private void appendln(String text) {
        out.append(text).append("\n");
    }

    private void append(String text) {
        out.append(text);
    }

    public String parse(String markdown) {
        String[] lines = markdown.split("\n");
        for (int lineidx = 0; lineidx < lines.length; lineidx++) {
            String untrimmedLine = lines[lineidx];
            untrimmedLine = untrimmedLine.replace("\t", "    "); 
            String line = untrimmedLine.trim();

            // Fenced code block
            if (line.startsWith("```")) {
                if (inCodeBlock) {
                    appendln("</code></pre>");
                    if (line.length() != 3) {
                        String fileName = line.substring(3);
                        appendln("<div class='code-snippet-filename'>" + fileName + "</div>");
                    }
                    inCodeBlock = false;
                } else {
                    boolean nowrap = false;
                    int i = 3;
                    if (line.charAt(i) == '[') {
                        int start = i+1;
                        while (line.charAt(i) != ']') {
                            i++;
                        }
                        String options = line.substring(start, i);
                        i++;
                        if (options.equals("nowrap")) nowrap = true;
                    }

                    if (i != line.length()) {
                        String preTypeStr = line.substring(i);
                        if (preTypeStr.equalsIgnoreCase("console")) preType = PreType.CONSOLE;
                        else if (preTypeStr.equalsIgnoreCase("c") || 
                                  preTypeStr.equalsIgnoreCase("cpp")) preType = PreType.C_CPP;
                        else preType = PreType.DEFAULT;
                    }

                    closeStructures();
                    if (nowrap || preType != PreType.DEFAULT) {
                        append("<pre class='");
                        if (nowrap) append("pre-nowrap");
                        switch (preType) {
                            case CONSOLE: append(" pretype-console"); break;
                            case C_CPP:   append(" pretype-c-cpp"); break;
                            case DEFAULT: break;
                        }
                        append("'><code>");
                    } else {
                        append("<pre><code>");
                    }
                    inCodeBlock = true;
                }
                continue;
            } else if (line.startsWith("===")) {
                if (inNav) {
                    closeStructures();
                    appendln("</nav>");
                    inNav = false;
                } else {
                    closeStructures();
                    appendln("<nav id='main-nav'>");
                    inNav = true;
                }
                continue;
            } else if (line.startsWith("[[[")) {
                closeStructures();
                appendln("<aside>");
                inAside = true;
                continue;
            } else if (line.startsWith("]]]")) {
                closeStructures();
                appendln("</aside>");
                inAside = false;
                continue;
            }

            if (inCodeBlock) {
                if (preType == PreType.CONSOLE && untrimmedLine.startsWith("$ ")) {
                    append("<span class='prompt'>$ </span><span class='sh'>");
                    append(escapeHtml(untrimmedLine.substring(2)));
                    appendln("</span>");
                } else {
                    appendln(escapeHtml(untrimmedLine));
                }
                continue;
            }

            // Blank line closes paragraphs and lists (but not blockquotes)
            if (line.isEmpty()) {
                closeParaList();
                continue;
            }

            // Blockquotes
            if (line.startsWith(">")) {
                if (!inBlockquote) {
                    // close paragraph or list first
                    closeParaList();
                    appendln("<blockquote>");
                    inBlockquote = true;
                }
                String inner = line.substring(1).trim();
                if (!inner.isEmpty()) {
                    if (!inParagraph) { append("<p>"); inParagraph = true; }
                    else append(" ");
                    append(parseInline(inner));
                }
                continue;
            } else {
                if (inBlockquote) {
                    closeStructures();
                }
            }

            // Headings
            if (line.startsWith("### ")) {
                closeParaList();
                appendln("<h3>" + parseInline(line.substring(4).trim()) + "</h3>");
                continue;
            } else if (line.startsWith("## ")) {
                closeParaList();
                appendln("<h2>" + parseInline(line.substring(3).trim()) + "</h2>");
                continue;
            } else if (line.startsWith("# ")) {
                closeParaList();
                if (inNav) {
                    title = parseInline(line.substring(2).trim());
                    if (title.startsWith("/")) {
                        appendln("<h1>" + title + "</h1>");
                    } else {
                        appendln("<h1><a href='/'>~</a> / " + title + "</h1>");
                    }
                } else {
                    appendln("<h1>" + parseInline(line.substring(2).trim()) + "</h1>");
                }
                continue;
            }

            // Lists
            if (line.matches("^[-*] .*")) {
                if (!inList) {
                    closePara();
                    appendln("<ul>");
                    inList = true;
                }
                appendln("<li>" + parseInline(line.substring(2).trim()) + "</li>");
                continue;
            } else if (inList && line.isEmpty()) {
                closeList();
            }

            // Normal paragraph text
            if (!inParagraph) {
                append("<p>");
                inParagraph = true;
            } else {
                append(" ");
            }
            append(parseInline(line));
        }

        // Close anything still open
        closeParaList();
        if (inBlockquote) {
            if (inParagraph) appendln("</p>");
            appendln("</blockquote>");
        }
        if (inCodeBlock) appendln("</code></pre>");

        return out.toString();
    }

    private void closeStructures() {
        closeParaList();
        if (inBlockquote) { appendln("</blockquote>"); inBlockquote = false; }
    }

    private void closeParaList() {
        closePara();
        closeList();
    }

    private void closePara() {
        if (inParagraph) { appendln("</p>"); inParagraph = false; }
    }

    private void closeList() {
        if (inList) { appendln("</ul>"); inList = false; }
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String parseInline(String text) {
        // inline replacements: bold, italic, code, links, images
        text = text.replaceAll("`([^`]+)`", "<code>$1</code>");
        text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>");
        text = text.replaceAll("\\*([^*]+)\\*", "<em>$1</em>");
        text = text.replaceAll(
            "!\\[([^\\]]*)\\]\\s*\\(([^\\s)]+)\\)",
            "<img src=\"$2\" alt=\"$1\" />"
        );
        text = text.replaceAll(
            "\\[([^\\]]+)\\]\\(([^)]+)\\)",
            "<a href=\"$2\" rel=\"noopener noreferrer\">$1</a>"
        );

        return text;
    }
}

