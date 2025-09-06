import java.io.*;

public class Use {
    public static void main(String[] args) throws IOException {
        StreamingMarkdownParser p = new StreamingMarkdownParser();
        p.parseAndOutput(args[0]);
    }
}
