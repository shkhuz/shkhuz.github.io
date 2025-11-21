import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FrontMatterParser {
    public static Object parse(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        boolean inFrontMatter = false;
        int start = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.equals("---")) {
                if (!inFrontMatter) {
                    inFrontMatter = true;
                    start = i+1;
                } 
                else {
                    String yaml = String.join("\n", lines.subList(start, i));
                    YamlParser y = new YamlParser(yaml);
                    return y.parse();
                }
            }
        }
        return Collections.emptyMap();
    }
}
