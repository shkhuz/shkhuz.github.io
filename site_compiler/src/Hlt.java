import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.StringBuffer;


public class Hlt {

    public static String hlt(String code, String lang) {
        StringBuffer s = new StringBuffer(code);
        if (lang.equals("c") || lang.equals("cpp")) {
            hltCpp(s);
        }
        else if (lang.equals("aria")) {
            hltAria(s);
        }
        return s.toString();
    }

    private static void hltCpp(StringBuffer s) {
        Pattern p = Pattern.compile("int");
        Matcher m = p.matcher(s);
        while (m.find()) m.appendReplacement(s, "<span class='pretype-aria k'>int</span>");
    }

    private static void hltAria(StringBuffer s) {}
}
