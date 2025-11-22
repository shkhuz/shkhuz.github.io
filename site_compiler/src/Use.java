import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Use {
    public static void main(String[] args) throws IOException {
        Map<Path, Map<String, Object>> posts = new LinkedHashMap<>();
        Path blogDir = Paths.get(args[0]);
        Files.walk(blogDir)
            .filter(p -> p.toString().endsWith(".md"))
            .forEach(p -> {
                try {
                    Map<String, Object> fm = FrontMatterParser.parse(p);
                    posts.put(p, fm);
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

        for (Path p: posts.keySet()) {
            // if (Files.isSameFile(Paths.get("./index.md"), p)) {
            //     continue;
            // }
            System.out.println("===== " + p.toString() + " =====");
            System.out.println(posts.get(p));
            Main m = new Main();
            m.convertAndOutput(p, posts);
        }
    }
}
