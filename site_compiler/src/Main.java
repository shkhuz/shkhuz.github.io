import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Main {
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
            Hlt.init();
            Path f = Paths.get(filePath).normalize();
            String markdown = readFile(filePath);
            if (f.compareTo(Paths.get("index.md")) == 0) {
                markdown += buildBlogIndex();
            }
            Lexer l = new Lexer(markdown);
            List<Token> tokens = l.lex();
            Parser p = new Parser(markdown, tokens, l.indentsList, l.newlineList);
            Node root = p.parse();
            Renderer r = new Renderer(f, root);
            String html = r.render();
            System.out.println(html);

            try (FileWriter writer = new FileWriter(changeExt(filePath, ".html"))) {
                writer.write(blob1);
                writer.write(extractTitle(f));
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

    private String buildBlogIndex() throws IOException {
        Path blogDir = Paths.get("blog");
        List<Path> posts = new ArrayList<>();

        Files.walk(blogDir)
            .filter(p -> {
                Path abs = p.toAbsolutePath().normalize();
                return !abs.startsWith(blogDir.resolve("diary").toAbsolutePath().normalize());
            })
            .filter(p -> p.toString().endsWith(".md"))
            .forEach(posts::add);

        Collections.sort(posts, new Comparator<Path>() {
            public int compare(Path a, Path b) {
                String da = extractDateString(blogDir, a, false);
                String db = extractDateString(blogDir, b, false);
                return db.compareTo(da); // reverse order
            }
        });

        StringBuilder md = new StringBuilder();
        md.append("\n## Blog\n\n<ul class='blog-posts'>\n");
        for (Path p : posts) {
            md.append(buildListItem(blogDir, p));
        }
        md.append("</ul>");
        return md.toString();
    }

    private String buildListItem(Path blogDir, Path file) throws IOException {
        String date = extractDateString(blogDir, file, true);
        String title = extractTitle(file);
        String link = file
            .toString()
            .replace(File.separatorChar, '/')
            .replace(".md", ".html");
        
        return String.format("  <li>\n    <span class='blog-entry-date'>%s</span> <a href='%s'>%s</a>\n  </li>%n", date, link, title);
    }

    private String extractDateString(Path blogDir, Path file, boolean longFormat) {
        Path relative = blogDir.relativize(file);
        String[] parts = relative.toString().split(Pattern.quote(File.separator));
        String dateDotted = parts.length >= 4 
            ? String.format("%s.%s.%s", parts[0], parts[1], parts[2])
            : "0000.00.00";

        if (longFormat) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd");
            Date date;
            try {
                date = inputFormat.parse(dateDotted);
            } catch (Exception e) {
                System.err.println(e);
                return "";
            }
            SimpleDateFormat dayFormat = new SimpleDateFormat("d");
            int day = Integer.parseInt(dayFormat.format(date));
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM, yyyy", Locale.ENGLISH);
            String monthYear = monthYearFormat.format(date);
            return getDayWithSuffix(day) + " " + monthYear;
        }
        else return dateDotted;
    }

    private static String getDayWithSuffix(int day) {
        if (day >= 11 && day <= 13) return day + "th";
        switch (day % 10) {
            case 1:  return day + "st";
            case 2:  return day + "nd";
            case 3:  return day + "rd";
            default: return day + "th";
        }
    }

    private String extractTitle(Path file) throws IOException {
        BufferedReader reader = Files.newBufferedReader(file);
        try {
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.startsWith("#")) {
                return firstLine.replaceFirst("^#+\\s*", "").trim();
            }
        } finally {
            reader.close();
        }
        String name = file.getFileName().toString();
        return name.replaceFirst("\\.md$", "");
    }

    private void appendln(String text) {
        out.append(text).append("\n");
    }

    private void append(String text) {
        out.append(text);
    }
}

