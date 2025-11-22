import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Renderer {
    StringBuilder out = new StringBuilder();
    Node root;
    Map<String, Object> meta;
    boolean isIndex = false;
    Path filePath;

    public Renderer(Path filePath, Node root, Map<String, Object> meta, boolean isIndex) {
        this.filePath = filePath;
        this.root = root;
        this.meta = meta;
        this.isIndex = isIndex;
    }

    public boolean isInlineNode(Node node) {
        if (node instanceof TextNode 
            || node instanceof EmNode 
            || node instanceof AnchorNode 
            || node instanceof PrespanNode) {
            return true;
        }
        else if (node instanceof RootNode
            || node instanceof ParagraphNode
            || node instanceof AsideNode
            || node instanceof HeadingNode
            || node instanceof PreblockNode
            || node instanceof MathblockNode
            || node instanceof HtmlBlockNode
            || node instanceof ListNode
            || node instanceof ListItemNode) {
            return false;
        }
        else {
            System.err.println("Add to Renderer.isInlineNode()");
            return false;
        }
    }

    private void nlIndentIfNotDone(Node node, Node prev, int indent) {
        if (prev != null && !isInlineNode(prev) && isInlineNode(node)) {
            String pad = "  ".repeat(indent);
            out.append("\n" + pad);
        }
    }

    @SuppressWarnings("unchecked")
    private void renderNode(Node node, Node prev, int indent) {
        String pad = "  ".repeat(indent);

        if (node instanceof RootNode) {
            RootNode r = (RootNode) node;
            out.append("<article>");
            out.append("\n  <nav id='main-nav'>");

            if (meta.containsKey("title")) {
                System.out.println("INDEX ? " + isIndex);
                String tilde = !isIndex ? "<a href='/'>~/</a> " : "";
                out.append("\n    <h1>" + tilde + Utils.getValueStr(meta, "title") + "</h1>");
            }

            if (meta.containsKey("nav")) {
                List<Object> navList = (List<Object>) meta.get("nav");
                out.append("\n    <ul>");
                for (Object o: navList) {
                    Map<String, Object> anchor = (Map<String, Object>) o; 
                    out.append("\n      <li>");
                    out.append("\n        <a href='" + 
                               Utils.getValueStr(anchor, "url") + 
                               "'>" + 
                               Utils.getValueStr(anchor, "label") + 
                               "</a>"
                    );
                    out.append("\n      </li>");
                }
                out.append("\n    </ul>");
            }

            out.append("\n  </nav>");
            for (int i = 0; i < r.children.size(); i++) {
                renderNode(r.children.get(i), i == 0 ? node : r.children.get(i-1), indent + 1);
            }
            out.append("\n" + pad + "</article>");
        }
        else if (node instanceof ParagraphNode) {
            ParagraphNode p = (ParagraphNode) node;
            out.append("\n" + pad + "<p>");
            for (int i = 0; i < p.children.size(); i++) {
                renderNode(p.children.get(i), i == 0 ? node : p.children.get(i-1), indent + 1);
            }
            out.append("\n" + pad + "</p>");
        }
        else if (node instanceof AsideNode) {
            AsideNode a = (AsideNode) node;
            out.append("\n" + pad + "<aside>");
            for (int i = 0; i < a.children.size(); i++) {
                renderNode(a.children.get(i), i == 0 ? node : a.children.get(i-1), indent + 1);
            }
            out.append("\n" + pad + "</aside>");
        }
        else if (node instanceof HeadingNode) {
            HeadingNode h = (HeadingNode) node;
            out.append("\n" + pad + "<h" + h.level + ">");
            for (int i = 0; i < h.children.size(); i++) {
                renderNode(h.children.get(i), i == 0 ? node : h.children.get(i-1), indent + 1);
            }
            out.append("\n" + pad + "</h" + h.level + ">");
        }
        else if (node instanceof ListNode) {
            ListNode l = (ListNode) node;
            out.append("\n" + pad + (l.ordered ? "<ol>" : "<ul>"));
            for (int i = 0; i < l.items.size(); i++) {
                renderNode(l.items.get(i), i == 0 ? node : l.items.get(i-1), indent + 1);
            }
            out.append("\n" + pad + (l.ordered ? "</ol>" : "</ul>"));
        }
        else if (node instanceof ListItemNode) {
            ListItemNode li = (ListItemNode) node;
            out.append("\n" + pad + "<li>");
            for (int i = 0; i < li.children.size(); i++) {
                renderNode(li.children.get(i), i == 0 ? node : li.children.get(i-1), indent + 1);
            }
            out.append("\n" + pad + "</li>");
        }
        else if (node instanceof TextNode) {
            TextNode t = (TextNode) node;
            nlIndentIfNotDone(node, prev, indent);
            out.append(t.text);
        }
        else if (node instanceof PrespanNode) {
            PrespanNode t = (PrespanNode) node;
            nlIndentIfNotDone(node, prev, indent);
            out.append("<code>" + t.code + "</code>");
        }
        else if (node instanceof PreblockNode) {
            PreblockNode t = (PreblockNode) node;
            String classes = "code";
            if (t.lang != "") classes += " lang-" + t.lang;
            if (t.wrap) classes += " wrap";
            classes = classes.trim();
            out.append("\n" + pad + "<div");
            out.append(" class='" + classes + "'>");
            String code = Lexer.escapeHtml(t.code);
            code = Hlt.hlt(code, t.lang, t.callout);
            out.append(code);
            out.append("</div>");

            if (t.filepath != null) {
                out.append("\n" + pad + "<div class='code-snippet-filename'>" + t.filepath + "</div>");
            }
        }
        else if (node instanceof MathblockNode) {
            MathblockNode m = (MathblockNode) node;
            out.append("\n" + pad + m.text);
        }
        else if (node instanceof HtmlBlockNode) {
            HtmlBlockNode h = (HtmlBlockNode) node;
            String text = h.raw;
            String textAlt = text.replace("\n", "\n" + pad);
            out.append("\n" + pad + textAlt);
        }
        else if (node instanceof EmNode) {
            EmNode e = (EmNode) node;
            nlIndentIfNotDone(node, prev, indent);
            out.append(e.strong ? "<strong>" : "<em>");
            for (int i = 0; i < e.children.size(); i++) {
                renderNode(e.children.get(i), i == 0 ? node : e.children.get(i-1), indent + 1);
            }
            out.append(e.strong ? "</strong>" : "</em>");
        }
        else if (node instanceof AnchorNode) {
            AnchorNode a = (AnchorNode) node;
            nlIndentIfNotDone(node, prev, indent);
            if (a.image) {
                out.append("<img src='" + a.url + "' alt='" + a.text + "'>");
            } else {
                out.append("<a href='" + a.url + "'>" + a.text + "</a>");
            }
        }
    }

    public String render() {
        renderNode(root, null, 0);
        return out.toString();
    }
}
