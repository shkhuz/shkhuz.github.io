import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.StringBuffer;
import java.util.Map;
import java.util.Set;

public class Hlt {
    static Pattern patternCType;
    static Pattern patternPython;
    static Pattern patternConsole;

    static final Map<String, Set<String>> KEYWORDS = Map.of(
        "c", Set.of(
            "int","char","return","if","else","while","for","void",
            "float","double","struct"
        ),
        "cpp", Set.of(
            "int","char","return","if","else","while","for","void","float",
            "double","struct","class","namespace","template","auto"
        ),
        "aria", Set.of(
            "fn","imm","mut","return","if","else","while","for","void","f32",
            "f64","struct","union","import"
        ),
        "java", Set.of(
            "class","public","private","protected","static","void","int",
            "new","return","if","else","for","while","import","package"
        ),
        "javascript", Set.of(
            "let","var","const","function","return","if","else","for","while",
            "class","new","import","export"
        ),
        "python", Set.of(
            "def","class","return","if","else","elif","for","while",
            "import","from","as"
        ),
        "sh", Set.of(
            "if","then","fi","elif","else","for","while","do","done","function"
        )
    );

    static final Map<String, Set<String>> CONSTS = Map.of(
        "c", Set.of("true","false","NULL"),
        "cpp", Set.of("true","false","nullptr"),
        "aria", Set.of("true","false","null","undefined"),
        "java", Set.of("true","false","null"),
        "javascript", Set.of("true","false","null"),
        "python", Set.of("True","False","None")
    );

    static final Map<String, Set<String>> INTRINSICS = Map.of(
        "c", Set.of("#include","#define"),
        "cpp", Set.of("#include","#define","#pragma"),
        "aria", Set.of("@import","@to")
    );

    public static void init() {
        patternCType = buildPattern("c");
        patternPython = buildPattern("python");
        patternConsole = Pattern.compile("(?m)^(\\$ )(.*)$");
    }

    private static Pattern buildPattern(String lang) {
        // String str = "(['\"])(?:\\\\.|(?!\\1).)*\\1";     // group 1
        // String str = "(?:(['\"])(?:\\\\.|(?!\\1).)*\\1)"; // group 1
        String str = "((?:\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'))";
        String num = "([+-]?[0-9]+(?:\\.[0-9]+)?)";       // group 2
        // String ident = "([A-Za-z_][A-Za-z0-9_]*)";        // group 3
        String ident = "([#@]?[A-Za-z_][A-Za-z0-9_]*)";  // group 3
        String comment;
        switch (lang) {
            case "c": 
            case "cpp":
            case "java":
            case "aria":
                comment = "(//.*)";
                break;
            case "python":
                comment = "(#.*)";
                break;
            default:
                comment = "()";
                break;
        }
        String other = "([\\s\\S])";
        String master = String.join("|", str, num, ident, comment, other);
        return Pattern.compile(master);
    }

    private static String calloutHighlight(String code) {
        StringBuilder sb = new StringBuilder(code.length());
        boolean inCallout = false;
        int start = 0;
        int len = code.length();

        sb.append("<pre class='before'>");
        for (int i = 0; i < len;) {
            int lineEnd = code.indexOf('\n', i);
            if (lineEnd == -1) lineEnd = len;
            String line = code.substring(i, lineEnd);

            if (line.contains("hlt-start")) 
                sb.append("</pre>\n<pre class='callout'>");
            else if (line.contains("hlt-end")) 
                sb.append("</pre>\n<pre class='after'>");
            else {
                sb.append(line);
                sb.append('\n');
            }

            i = lineEnd + 1;
        }
        sb.append("</pre>");

        return sb.toString();
    }

    private static String diffHighlight(String code) {
        StringBuilder sb = new StringBuilder(code.length());

        String currentType = null;
        int len = code.length();

        for (int i = 0; i < len;) {
            int lineEnd = code.indexOf('\n', i);
            if (lineEnd == -1) lineEnd = len;
            String line = code.substring(i, lineEnd);

            String newType;
            if (line.startsWith("+")) newType = "add";
            else if (line.startsWith("-")) newType = "remove";
            else newType = "same";

            if (newType != currentType) {
                if (currentType != null) sb.append("</pre>\n");
                sb.append("<pre class='diff-").append(newType).append("'>");
                currentType = newType;
            }

            sb.append(line).append('\n');
            i = lineEnd + 1;
        }

        if (currentType != null) sb.append("</pre>");

        return sb.toString();
    }

    private static String hltCode(String lang, Matcher m) {
        Set<String> keywords = KEYWORDS.getOrDefault(lang, Set.of());
        Set<String> consts = CONSTS.getOrDefault(lang, Set.of());
        Set<String> intrinsics = INTRINSICS.getOrDefault(lang, Set.of());

        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String strV = m.group(1);
            String numV = m.group(2);
            String identV = m.group(3);
            String comment = m.group(4);
            String other = m.group(5);

            if (strV != null) {
                out.append("<span class='string'>").append(strV).append("</span>");
            }
            else if (numV != null) {
                out.append("<span class='const'>").append(numV).append("</span>");
            }
            else if (identV != null) {
                if (keywords.contains(identV)) {
                    out.append("<span class='keyword'>").append(identV).append("</span>");
                } 
                else if (consts.contains(identV)) {
                    out.append("<span class='const'>").append(identV).append("</span>");
                } 
                else if (intrinsics.contains(identV)) {
                    out.append("<span class='intrinsic'>").append(identV).append("</span>");
                }
                else out.append(identV);
            }
            else if (comment != null && !comment.isEmpty()) {
                out.append("<span class='comment'>").append(comment).append("</span>");
            }
            else {
                out.append(other);
            }
        }
        return out.toString();
    }

    public static String hlt(String code, String lang, boolean callout) {
        switch (lang) {
            case "c":
            case "cpp":
            case "java":
            case "aria":
                code = hltCode(lang, patternCType.matcher(code));
                break;
            case "python":
                code = hltCode(lang, patternPython.matcher(code));
                break;

            // case "console": {
            //     StringBuilder out = new StringBuilder();
            //     String[] lines = code.split("\n", -1); // keep trailing blank lines
            //     boolean inInput = false;

            //     for (int i = 0; i < lines.length; i++) {
            //         String line = lines[i];
            //         boolean isPrompt = line.startsWith("$ ");
            //         boolean isContinuation = inInput && (
            //             line.startsWith("> ") ||
            //             (i > 0 && lines[i - 1].endsWith("\\"))
            //         );

            //         if (isPrompt) {
            //             // Close previous input
            //             if (inInput) {
            //                 out.append("</span>");
            //                 inInput = false;
            //             }
            //             out.append("<span class='console-prompt'>$ </span>");
            //             out.append("<span class='console-input'>");
            //             out.append(line.substring(2));
            //             inInput = true;
            //         }
            //         else if (isContinuation) {
            //             out.append(line);
            //         }
            //         else {
            //             if (inInput) {
            //                 out.append("</span>\n");
            //                 inInput = false;
            //             }
            //             out.append(line);
            //         }

            //         // Only append newline *if we’re not already inside continuation*
            //         if (i < lines.length - 1 && !isContinuation)
            //             out.append('\n');
            //     }

            //     if (inInput) out.append("</span>");
            //     code = out.toString();
            //     break;
            // }

            case "console": {
                StringBuilder out = new StringBuilder();
                Matcher m = patternConsole.matcher(code);
                int last = 0;
                while (m.find()) {
                    out.append(code, last, m.start());
                    out.append("<span class='console-prompt'>");
                    out.append(m.group(1)); // "$ "
                    out.append("</span><span class='console-input'>");
                    out.append(m.group(2));
                    out.append("</span>");
                    last = m.end();
                }
                out.append(code.substring(last));
                code = out.toString();
            } break;

            case "diff":
                code = diffHighlight(code);
                break;
        }

        if (callout) code = calloutHighlight(code);
        else if (!lang.equals("diff"))
            code = "<pre>" + code + "</pre>";

        return code;
    }
}
