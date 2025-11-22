import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.Locale;
import java.util.stream.Collectors;

public class Main {
    private StringBuilder out = new StringBuilder();
    public String title;
    public boolean isIndex = false;
    private Path blogDir = Paths.get("./blog");

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
"<link rel='icon' type='image/png' sizes='16x16' href='/assets/favicon-16x16.png'>\n" +
"<link rel='manifest' href='/assets/site.webmanifest'>\n" +
"<link rel='stylesheet' href='/style.css'>\n" +
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
"<body>\n";

    private String blob3 =
"\n</body>\n" +
"</html>\n";

    public void convertAndOutput(Path f, Map<Path, Map<String, Object>> fms) 
        throws IOException 
    {
        try {
            Hlt.init();
            String filePath = f.toString();
            String markdown = Utils.readFile(filePath);

            if (Files.isSameFile(Paths.get("./index.md"), f)) {
                isIndex = true;
            }

            if (isIndex) {
                Map<Path, Map<String, Object>> filtered = fms.entrySet()
                    .stream()
                    .filter(e -> e.getKey().startsWith(blogDir))
                    .sorted(Comparator.comparing(
                        (Map.Entry<Path, Map<String,Object>> e) ->
                            Utils.getValueStr(e.getValue(), "date")
                    ).reversed())
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a,b) -> a,
                        LinkedHashMap::new
                    ));

                markdown += "<h2>Blog</h2>\n";
                markdown += "<div class='blog-posts'>\n";
                for (Path p: filtered.keySet()) {
                    Map<String, Object> meta = filtered.get(p);
                    String htmlPath = Utils.changeExt(p.toString(), ".html");
                    markdown += "<div class='blog-post'>\n";
                    markdown += "  <a href='" + 
                                htmlPath + 
                                "'>" + 
                                Utils.getValueStr(meta, "title") + 
                                "</a>\n";
                    markdown += "  <small><span class='date'>" + 
                                Utils.formatIsoDate(Utils.getValueStr(meta, "date")) + 
                                "</span>&nbsp;&nbsp;" + 
                                Utils.getValueStr(meta, "synopsis") + 
                                " <a class='read-more' href='" + 
                                htmlPath + 
                                "'>Read more</a></small>\n";
                    markdown += "</div>\n";
                }
                markdown += "</div>\n";
            }

            Map<String, Object> meta = fms.get(f);
            Lexer l = new Lexer(markdown);
            List<Token> tokens = l.lex();
            Parser p = new Parser(markdown, tokens, l.indentsList, l.newlineList);
            Node root = p.parse();
            Renderer r = new Renderer(f, root, meta, isIndex);
            String html = r.render();

            try (FileWriter writer = new FileWriter(Utils.changeExt(filePath, ".html"))) {
                writer.write(blob1);
                writer.write(Utils.getValueStr(meta, "title"));
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
}

