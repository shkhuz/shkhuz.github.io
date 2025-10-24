import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;

public class Parser {
    int current = 0;
    int prev = 0;
    String srcfile;
    List<Token> tokens;
    List<Integer> indentsList;
    List<Integer> newlineList;

    public Parser(
            String srcfile, 
            List<Token> tokens, 
            List<Integer> indentsList, 
            List<Integer> newlineList
    ) {
        this.srcfile = srcfile;
        this.tokens = tokens;
        this.indentsList = indentsList;
        this.newlineList = newlineList;
    }

    private Token at(int idx) {
        return idx >= 0 && idx < tokens.size() ? tokens.get(idx) : null;
    }

    private Token at() {
        return at(current);
    }

    private Token advance() {
        return current < tokens.size() ? tokens.get(current++) : null;
    }

    private boolean is(TKind kind) {
        if (at() != null && at().kind == kind) {
            return true;
        }
        return false;
    }

    private boolean match(TKind kind) {
        if (is(kind)) {
            advance();
            return true;
        }
        return false;
    }

    private int getTokenLineIndent(Token t) {
        return indentsList.get(t.line-1);
    }

    private int findInlineEnd(int startAt, int minIndent) {
        int i = startAt;
        while (i < tokens.size()-1) {
            Token t = at(i);
            if (t.kind == TKind.eof) return i;
            if (t.kind == TKind.newline) {
                Token nt = at(i+1);
                if (nt == null) return i;
                if (nt.kind == TKind.newline) return i;
                if (nt.kind.isInlineTerminator()) return i;
                //if (nt.indent < minIndent) return i;
            }
            i++;
        }
        return tokens.size()-1;
    }

    static class Delim {
        TKind kind;
        int count;
        int nodeIdx;
        boolean canOpen;
        boolean canClose;

        public Delim(TKind kind, int count, int nodeIdx, boolean canOpen, boolean canClose) {
            this.kind = kind;
            this.count = count;
            this.nodeIdx = nodeIdx;
            this.canOpen = canOpen;
            this.canClose = canClose;
        }
    }

    private void parseInline(List<Node> out, Token refIndent) {
        Deque<Delim> stack = new ArrayDeque<>(); 
        int till = findInlineEnd(current, refIndent != null
                    ? getTokenLineIndent(refIndent) + refIndent.lexeme.length() 
                    : 0);
        while (current < till) {
            Token t = advance();
            switch (t.kind) {
                case star:
                case underscore: {
                    if (!stack.isEmpty() && stack.peek().kind == t.kind && stack.peek().count == t.count) {
                        Delim opener = stack.pop();
                        List<Node> inner = new ArrayList<>();
                        for (int i = opener.nodeIdx+1; i < out.size(); i++) {
                            inner.add(out.get(i));
                        }
                        while (out.size() > opener.nodeIdx) {
                            out.remove(out.size()-1);
                        }
                        Node em = new EmNode(t.count == 1 ? false : true, inner);
                        out.add(em);
                    } else {
                        Delim d = new Delim(
                                t.kind, 
                                t.count, 
                                out.size(), 
                                t.extra.charAt(0) == '1' ? true : false,
                                t.extra.charAt(1) == '1' ? true : false);
                        stack.push(d);
                        out.add(new TextNode(t.lexeme));
                    }
                } break;

                case anchor: {
                    out.add(new AnchorNode(
                                t.count == -1 ? true : false,
                                t.lexeme,
                                t.extra));
                } break;

                case prespan: {
                    out.add(new PrespanNode(t.lexeme));
                } break;

                case newline: {
                    out.add(new TextNode(" "));
                } break;

                case eof: break;

                default: {
                    out.add(new TextNode(t.lexeme));
                } break;
            }
        }

        while (match(TKind.newline)) {}
    }

    private Node parseHeading() {
        Token t = advance();
        int level = t.count;
        List<Node> inline = new ArrayList<>();
        parseInline(inline, t);

        Node heading = new HeadingNode(level, inline);
        if (level == 1) {
            Node navlist = null;
            if (match(TKind.equal3)) {
                while (match(TKind.newline)) {}
                navlist = parseList();
                if (!match(TKind.equal3)) {
                    System.err.println("Expected '='"); 
                }
                while (match(TKind.newline)) {}
            }
            return new NavbarNode(heading, navlist);
        } else {
            return heading;
        }
    }

    private Node parseParagraph() {
        List<Node> inline = new ArrayList<>();
        parseInline(inline, null);
        return new ParagraphNode(inline);
    }

    private ListItemNode parseListItem(Token marker) {
        List<Node> children = new ArrayList<>();
        parseInline(children, marker);

        while (at() != null) {
            Token t = at();
            if (t.kind.isListMarker()) {
                // Same level separate list item: we handle this in parseList
                if (t.indent == marker.indent) break;

                // Nested list
                if (t.indent > marker.indent) {
                    children.add(parseList());
                    continue;
                }
            }

            if (t.indent >= marker.indent + marker.count + 1) {
                parseInline(children, marker);
                continue;
            }

            // TODO: Handle blankline here
            
            break;
        }

        return new ListItemNode(children);
    }

    private Node parseList() {
        Token marker = at();
        boolean ordered = marker.kind == TKind.orderedMarker;
        List<ListItemNode> items = new ArrayList<>();
        
        while (at() != null) {
            Token item = at();
            if (!(item.kind.isListMarker() && item.indent == marker.indent))
                break;
            boolean itemOrdered = item.kind == TKind.orderedMarker;
            if (itemOrdered != ordered) break;
            advance();
            items.add(parseListItem(marker));
        }

        return new ListNode(ordered, items);
    }

    private Node parsePreblock() {
        Token t = at();
        advance();
        String lang = null;
        String filepath = null;
        boolean wrap = false;
        if (t.extra != null) {
            String extra = t.extra;
            int i = 0;
            if (i < extra.length() && Character.isLetter(extra.charAt(i))) {
                while (i < extra.length() 
                       && Character.isLetter(extra.charAt(i))) i++;
                lang = extra.substring(0, i); 
            }
            if (i < extra.length() && extra.charAt(i) == '!') {
                wrap = true;
                i++;
            }
            if (i < extra.length() && extra.charAt(i) == '(') {
                i++;
                int beg = i;
                while (i < extra.length()
                       && extra.charAt(i) != ')') {
                    if (extra.charAt(i) == '\n' || extra.charAt(i) == '\0') 
                        System.err.println("Unterminated '('");
                    i++;
                }
                filepath = extra.substring(beg, i);
            }
        }
        while (match(TKind.newline)) {}
        return new PreblockNode(lang, wrap, t.lexeme, filepath);
    }

    private Node parseIndentedPreblock() {
        StringBuilder code = new StringBuilder();
        while (at() != null) {
            Token t = at();
            advance();
            if (t.indent == -1) continue;
            if (t.indent >= 4) {
                int prevNlOffset = t.line == 1 ? 4 : newlineList.get(t.line-1)+4 + 1; 
                int curNlOffset = newlineList.get(t.line)+1;  
                code.append(srcfile.substring(prevNlOffset, curNlOffset));
            }
            else break;
        }
        while (match(TKind.newline)) {}
        return new PreblockNode(null, false, code.toString(), null);
    }

    private Node parseMathblock() {
        Token t = at();
        advance();
        while (match(TKind.newline)) {}
        return new MathblockNode(t.lexeme);
    }

    private Node parseAside() {
        advance();
        while (match(TKind.newline)) {}
        List<Node> children = new ArrayList<>();
        while (!match(TKind.rbrack3)) {
            children.add(parseBlock());  
        }
        while (match(TKind.newline)) {}
        return new AsideNode(children);
    }

    private Node parseBlock() {
        // Each block element's parseInline() function handles 
        // skipping newlines at the end. Only in parsePreblock() do we
        // manually skip newlines. If a new block element is added, the
        // code to skip nl should it's responsibility, not parseBlock().
        Token t = at();
        if (t.kind == TKind.pound) return parseHeading();
        else if (t.kind.isListMarker() && t.indent == 0) return parseList();
        else if (t.kind == TKind.preblock) return parsePreblock();
        else if (t.kind == TKind.mathblock) return parseMathblock();
        else if (t.kind == TKind.lbrack3) return parseAside();
        else if (t.indent >= 4) return parseIndentedPreblock();
        else return parseParagraph();
    }

    public static void printNode(Node node, int indent) {
        String pad = "  ".repeat(indent);

        if (node instanceof RootNode) {
            RootNode r = (RootNode) node;
            System.out.println(pad + "Root");
            for (Node child : r.children) {
                printNode(child, indent + 1);
            }
        }
        else if (node instanceof ParagraphNode) {
            ParagraphNode p = (ParagraphNode) node;
            System.out.println(pad + "Paragraph");
            for (Node child : p.children) {
                printNode(child, indent + 1);
            }
        }
        else if (node instanceof NavbarNode) {
            NavbarNode n = (NavbarNode) node;
            System.out.println(pad + "Navbar");
            printNode(n.title, indent + 1);
            if (n.navlist != null) printNode(n.navlist, indent + 1);
        }
        else if (node instanceof AsideNode) {
            AsideNode a = (AsideNode) node;
            System.out.println(pad + "Aside");
            for (Node child : a.children) {
                printNode(child, indent + 1);
            }
        }
        else if (node instanceof HeadingNode) {
            HeadingNode h = (HeadingNode) node;
            System.out.println(pad + "Heading level=" + h.level);
            for (Node child : h.children) {
                printNode(child, indent + 1);
            }
        }
        else if (node instanceof ListNode) {
            ListNode l = (ListNode) node;
            System.out.println(pad + "List (" + (l.ordered ? "ordered" : "unordered") + ")");
            for (ListItemNode item : l.items) {
                printNode(item, indent + 1);
            }
        }
        else if (node instanceof ListItemNode) {
            ListItemNode li = (ListItemNode) node;
            System.out.println(pad + "Item");
            for (Node child : li.children) {
                printNode(child, indent + 1);
            }
        }
        else if (node instanceof TextNode) {
            TextNode t = (TextNode) node;
            System.out.println(pad + "Text: \"" + t.text + "\"");
        }
        else if (node instanceof PrespanNode) {
            PrespanNode t = (PrespanNode) node;
            System.out.println(pad + "Prespan: \"" + t.code + "\"");
        }
        else if (node instanceof PreblockNode) {
            PreblockNode t = (PreblockNode) node;
            System.out.println(pad 
                    + "Preblock lang=" 
                    + (t.lang != null ? t.lang : "[none]") 
                    + ", wrap=" 
                    + (t.wrap ? "true" : "false") 
                    + ", filepath="
                    + (t.filepath != null ? t.filepath : "[none]")
                    + ": \"" 
                    + Lexer.escape(t.code)
                    + "\"");
        }
        else if (node instanceof MathblockNode) {
            MathblockNode t = (MathblockNode) node;
            System.out.println(pad 
                    + "Mathblock: \"" 
                    + t.text
                    + "\"");
        }
        else if (node instanceof EmNode) {
            EmNode e = (EmNode) node;
            System.out.println(pad + "Em " + (e.strong ? "bold" : "italic"));
            for (Node child : e.children) {
                printNode(child, indent + 1);
            }
        }
        else if (node instanceof AnchorNode) {
            AnchorNode a = (AnchorNode) node;
            System.out.println(pad 
                    + (a.image ? "Image" : "Link") 
                    + " text="
                    + a.text
                    + ", url="
                    + a.url);
        }
        else {
            System.out.println(pad + "Unknown node: " + node.getClass().getSimpleName());
        }
    }

    public Node parse() {
        List<Node> blocks = new ArrayList<>();
        while (at() != null) {
            blocks.add(parseBlock());
        }
        RootNode root = new RootNode(blocks);
        printNode(root, 0);
        return root;
    }
}
