import java.util.*;

public class YamlParser {
    private final List<String> lines;
    private int i = 0;

    YamlParser(String yaml) {
        String[] arr = yaml.split("\\r?\\n");
        lines = Arrays.asList(arr);
    }

    public Object parse() {
        return parseBlock(0);
    }

    Object parseBlock(int baseIndent) {
        if (i >= lines.size()) return "";

        String line = lines.get(i);
        int indent = countIndent(line);

        String trimmed = trim(line);

        if (trimmed.startsWith("-"))
            return parseList(baseIndent);

        return parseMap(baseIndent);
    }

    Map<String, Object> parseMap(int baseIndent) {
        Map<String, Object> map = new LinkedHashMap<>();

        while (i < lines.size()) {
            String line = lines.get(i);
            int indent = countIndent(line);
            String trimmed = trim(line);
            if (trimmed.isEmpty()) {
                i++;
                continue;
            }

            if (indent < baseIndent) break;
            if (trimmed.startsWith("-")) break;

            int colon = trimmed.indexOf(':');
            if (colon == -1) {
                i++;
                continue;
            }

            String key = trimmed.substring(0, colon).trim();
            String after = trimmed.substring(colon + 1).trim();

            i++;

            Object value;
            if (after.equals(">")) {
                value = parseFoldedBlock(baseIndent + 2);
            } else if (!after.isEmpty()) {
                value = after;
            } else {
                value = parseBlock(baseIndent + 2);
            }

            map.put(key, value);
        }

        return map;
    }

    List<Object> parseList(int baseIndent) {
        List<Object> list = new ArrayList<>();

        while (i < lines.size()) {
            String line = lines.get(i);
            int indent = countIndent(line);
            String trimmed = trim(line);
            if (trimmed.isEmpty()) {
                i++;
                continue;
            }

            if (indent < baseIndent) break;
            if (!trimmed.startsWith("-")) break;

            String afterDash = trimmed.substring(1).trim();
            i++;

            if (afterDash.contains(":")) {
                int colon = afterDash.indexOf(':');
                String key = afterDash.substring(0, colon).trim();
                String valuePart = afterDash.substring(colon + 1).trim();

                Map<String, Object> obj = new LinkedHashMap<>();

                if (!valuePart.isEmpty()) {
                    obj.put(key, valuePart);
                } else {
                    Object nested = parseBlock(baseIndent + 2);
                    obj.put(key, nested);
                }

                while (i < lines.size()) {
                    String next = lines.get(i);
                    int nextIndent = countIndent(next);
                    if (nextIndent < baseIndent + 2) break;
                    String nextTrim = trim(next);

                    if (!nextTrim.contains(":")) {
                        break;
                    }

                    Map<String,Object> nestedMap = parseMap(baseIndent + 2);
                    obj.putAll(nestedMap);
                }

                list.add(obj);
                continue;
            }

            if (!afterDash.isEmpty()) {
                list.add(afterDash);
                continue;
            }

            list.add(parseBlock(baseIndent + 2));
        }

        return list;
    }

    String parseFoldedBlock(int baseIndent) {
        StringBuilder sb = new StringBuilder();

        while (i < lines.size()) {
            String line = lines.get(i);
            int indent = countIndent(line);
            String trimmed = trim(line);
            if (trimmed.isEmpty()) {
                i++;
                sb.append("\n");
                continue;
            }
            if (indent < baseIndent) break;

            sb.append(trimmed).append(" ");
            i++;
        }

        return sb.toString().trim();
    }

    private static int countIndent(String s) {
        int n = 0;
        int len = s.length();
        for (int j = 0; j < len; j++) {
            if (s.charAt(j) == ' ') n++;
            else break;
        }
        return n;
    }

    private static String trim(String s) {
        return s.replaceFirst("^ +", "");
    }
}

