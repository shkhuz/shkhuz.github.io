import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;

public class Lexer {
    enum State {
        text, 
        preblock,
        mathblock,
        prespan,
        other,
    }

    State state = State.other;
    int start = 0;
    int current = 0;
    int line = 1;
    String preblock_extra;

    // Indentation of every line.
    public List<Integer> indentsList = new ArrayList<>();
    // Offset in srcfile for every newline.
    public List<Integer> newlineList = new ArrayList<>();
    int indent = 0;
    boolean indentAvail = false;

    String srcfile;
    List<Token> tokens = new ArrayList<Token>();

    public Lexer(String srcfile) {
        this.srcfile = srcfile;
        // First line ALWAYS is at indentation 0.
        indentsList.add(0);
        // 0th index has no newline. Lines start from 1.
        newlineList.add(-1);
    }

    private boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }

    private boolean isPunctuation(char c) {
        return "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".indexOf(c) != -1;
    }

    private boolean isAlphanumeric(char c) {
        return Character.isLetterOrDigit(c);
    }

    private char at(int idx) {
        if (idx >= 0 && idx < srcfile.length()) {
            return srcfile.charAt(idx);
        }
        throw new Error("Out of bounds");
    }

    private boolean is(int idx, char c) {
        if (idx >= 0 && idx < srcfile.length() && srcfile.charAt(idx) == c) {
            return true;
        }
        return false;
    }

    private boolean is(char c) {
        return is(current, c);
    }

    private boolean match(char c) {
        if (is(c)) {
            current++;
            return true;
        }
        return false;
    }

    private boolean expect(char c) {
        if (!match(c)) {
            System.err.println("Expected '" + c + "'");
            return false;
        }
        return true;
    }

    private Token lastTok() {
        return tokens.size() > 0 ? tokens.get(tokens.size()-1) : null;
    }

    private void appendTokWithSubstrTill(TKind kind, int count, int till) {
        this.tokens.add(new Token(kind, count, srcfile.substring(start, till), line));
        start = current;
        state = State.other;
        if (indentAvail) {
            lastTok().indent = indent;
            indentAvail = false;
        }
    }

    private void appendTok(TKind kind, int count) {
        appendTokWithSubstrTill(kind, count, current);
    }

    public static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static String escape(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                case '\"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                default:
                    if (Character.isISOControl(c)) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private void fin() {
        if (state == State.text) {
            appendTok(TKind.text, 1);
        }
    }

    private void defaultCharacter() {
        // Before moving on to text, we check if it is
        // an ordered list marker.
        if (Character.isDigit(at(current))) {
            int idx = current;
            while (Character.isDigit(at(idx))) idx++;
            if (is(idx, '.') || is(idx, ')')) {
                idx++;
                if (is(idx, ' ')) {
                    fin();
                    idx++;
                    current = idx;
                    appendTok(TKind.orderedMarker, 1);
                    return;
                }
            }
        }
        else if (is('!') && is(current+1, '[')) {
            // skip `!` cuz lexAnchor() expects to start
            // from `[`.
            current++;
            lexAnchor(true);
            return;
        } 
        else if (is('$') && is(current+1, '$')) {
            fin();
            current += 2;
            if (state == State.mathblock) {
                appendTok(TKind.mathblock, 1);
            } else {
                state = State.mathblock;
            }
            return;
        }

        switch (state) {
            case text: {
            } break;

            case other: {
                state = State.text;
            } break;
        }
        current++;
    }

    private void consecTokSpace(char c, TKind kind) {
        int idx = current;
        while (is(idx, c)) idx++;
        if (is(idx, ' ')) {
            fin();
            int count = idx - current;
            idx++; // space
            current = idx;
            appendTok(kind, count);
        } else {
            defaultCharacter();
        }
    }

    private void consecTok(char c, TKind kind) {
        int count = 0;
        while (match(c)) {
            count++;
        }
        appendTok(kind, count);
    }

    private void appendIf3ElseCont(char c, TKind kind) {
        int idx = current;
        while (is(idx, c)) {
            idx++;
        }
        if (idx-current == 3) {
            fin();
            current = idx;
            appendTok(kind, 1);
        } else {
            defaultCharacter();
        }
    }

    private void computeCanOpenCanClose(
            Token prevToken, 
            char marker, 
            Token currentToken, 
            char nextChar) {
        char prevChar = prevToken != null && prevToken.lexeme.length() > 0
                        ? prevToken.lexeme.charAt(prevToken.lexeme.length() - 1) 
                        : 0; 
        boolean prevIsWhitespace = prevChar == 0 || isWhitespace(prevChar);
        boolean nextIsWhitespace = nextChar == 0 || isWhitespace(nextChar);
        boolean prevIsPunct = prevChar != 0 && isPunctuation(prevChar);
        boolean nextIsPunct = nextChar != 0 && isPunctuation(nextChar);
        boolean leftFlanking = !nextIsWhitespace && 
            (!nextIsPunct || prevIsWhitespace || prevIsPunct);
        boolean rightFlanking = !prevIsWhitespace && 
            (!prevIsPunct || nextIsWhitespace || nextIsPunct);

        if (marker == '_') {
            boolean prevAlnum = prevChar != 0 && isAlphanumeric(prevChar);
            boolean nextAlnum = nextChar != 0 && isAlphanumeric(nextChar);
            if (prevAlnum && nextAlnum) {
                leftFlanking = false;
                rightFlanking = false;
            }
        }

        currentToken.extra = String.format("%d%d", leftFlanking ? 1 : 0, rightFlanking ? 1 : 0);
    }

    private void lexAnchor(boolean image) {
        fin();
        match('[');
        start = current;
        while (!match(']')) {
            if (is('\n') || is('\0')) {
                System.err.println("Unterminated '['");
                return;
            }
            current++;
        }
        appendTokWithSubstrTill(TKind.anchor, image ? -1 : 1, current-1);

        expect('(');
        start = current;
        while (!match(')')) {
            if (is('\n') || is('\0')) {
                System.err.println("Unterminated '('");
                return;
            }
            current++;
        }

        String link = srcfile.substring(start, current-1);
        lastTok().extra = link;
        start = current;
    }

    public List<Token> lex() {
        for (;;) {
            if (current == srcfile.length()) {
                // appendTok(TKind.eof, 1); 
                for (int i = 0; i < tokens.size(); i++) {
                    Token t = tokens.get(i);
                    System.out.println(
                            i 
                            + " "
                            + (t.indent != 0 ? "|" + t.indent + " " : "")
                            + t.kind 
                            + "(" 
                            + t.count 
                            + (t.extra != null ? ", " + t.extra : "") 
                            + ") " 
                            + escape(t.lexeme)
                    );
                }
                return tokens;
            }

            if (state == State.prespan && !is('`')) {
                current++;
                continue;
            } else if (state == State.preblock && !(is('`') && is(current+1, '`') && is(current+2, '`'))) {
                current++;
                continue;
            } else if (state == State.mathblock && !(is('$') && is(current+1, '$'))) {
                current++;
                continue;
            }

            switch (this.srcfile.charAt(current)) {
                case '`': {
                    fin();
                    int count = 0;
                    while (match('`')) {
                        count++;
                    }

                    if (count == 1) {
                        if (state == State.preblock) continue;
                        else if (state == State.prespan) {
                            appendTokWithSubstrTill(TKind.prespan, 1, current-count);
                        } else {
                            state = State.prespan;
                            start += count;
                        }
                    } else if (count == 3) {
                        if (state == State.prespan) continue;
                        else if (state == State.preblock) {
                            appendTokWithSubstrTill(TKind.preblock, 1, current-count);
                            // if (!is('\n') && !is('\0')) {
                            //     int beg = current;
                            //     while (!is('\n') && !is('\0')) current++;
                            //     preblock_extra += "|";
                            //     preblock_extra += srcfile.substring(beg, current); 
                            // }
                            lastTok().extra = preblock_extra;
                        } else {
                            state = State.preblock;
                            start += count;
                            while (!is('\n') && !is('\0')) current++;
                            preblock_extra = srcfile.substring(start, current);
                            current++; // eat the newline 
                            start = current;
                        }
                    } else {
                        throw new Error("invalid '`' count");
                    }
                } break;

                // We don't fin() here as we don't want to make two separate
                // text tokens if the count doesn't equal 3.
                // We first check if the count == 3 and if it is, we 
                // fin() and append.
                case '[': {
                    if (!is(current + 1, '[')) {
                        lexAnchor(false);
                    }
                    else appendIf3ElseCont('[', TKind.lbrack3); 
                } break;
                case ']':  appendIf3ElseCont(']', TKind.rbrack3); break;
                case '=':  appendIf3ElseCont('=', TKind.equal3); break;

                case '*': {
                    fin(); 
                    Token prevTok = lastTok();
                    consecTok('*', TKind.star); 
                    computeCanOpenCanClose(
                            prevTok, 
                            '*', 
                            lastTok(), 
                            current<srcfile.length()-1 ? at(current+1) : 0);
                } break;

                case '_': {
                    fin(); 
                    Token prevTok = lastTok();
                    consecTok('_', TKind.underscore); 
                    computeCanOpenCanClose(
                            prevTok, 
                            '_', 
                            lastTok(), 
                            current<srcfile.length()-1 ? at(current+1) : 0);
                } break;

                // We don't fin() here because we need to check if 
                // the token is followed by a space. Only then we 
                // add it as a token. Else it's text.
                case '#':  consecTokSpace('#', TKind.pound); break;
                case '-':  consecTokSpace('-', TKind.minus); break;
                case '+':  consecTokSpace('+', TKind.plus); break;

                case '\n': {
                    fin(); 
                    newlineList.add(current);
                    current++; 
                    appendTok(TKind.newline, 1); 
                    line++;

                    indent = 0;
                    indentAvail = true;
                    while (match(' ')) indent++;
                    indentsList.add(indent);
                    start = current;
                } break;

                default: defaultCharacter(); break;
            }
        }
    }
}
