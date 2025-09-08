public class Token {
    TKind kind;
    int count;
    int indent;
    int line;
    String lexeme;
    String extra = null;

    public Token(TKind kind, int count, String lexeme, int line) {
        this.kind = kind;
        this.count = count;
        this.lexeme = lexeme;
        this.line = line;
    }
}

