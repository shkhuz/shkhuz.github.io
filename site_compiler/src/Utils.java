import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Utils {

    public static String changeExt(String path, String newExt) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex != -1) {
            path = path.substring(0, dotIndex);
        }
        path += newExt;
        return path;
    }

    public static String readFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public static String getValueStr(Map<String, Object> map, String key) {
        Object o = map.get(key);
        if (o != null) return o.toString();
        else return "";
    }
}
