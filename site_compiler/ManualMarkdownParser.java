import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;

public class ManualMarkdownParser {
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
"<body>\n";

    private String blob3 =
"\n</body>\n" +
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

    public void convertAndOutput(String filePath) throws IOException {
        try {
            String markdown = readFile(filePath);
            Lexer l = new Lexer(markdown);
            List<Token> tokens = l.lex();
            Parser p = new Parser(tokens, l.indentsList);
            Node root = p.parse();
            Renderer r = new Renderer(root);
            String html = r.render();
            // String html = "";
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
}

