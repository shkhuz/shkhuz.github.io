import java.io.*;

public class Use {
    public static void main(String[] args) throws IOException {
        ManualMarkdownParser p = new ManualMarkdownParser();
        p.convertAndOutput(args[0]);
    }
}
