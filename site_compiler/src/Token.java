public class Token {
    TKind kind;

    // for anchors: count is -1 for images
    // 1 for links
    int count;
    int indent;
    int line;
    String lexeme;
    String extra = null;

    public Token(TKind kind, int count, String lexeme, int line) {
        this.kind = kind;
        this.indent = -1;
        this.count = count;
        this.lexeme = lexeme;
        this.line = line;
    }
}

