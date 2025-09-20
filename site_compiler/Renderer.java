import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;

public class Renderer {
    StringBuilder out = new StringBuilder();
    Node root;

    public Renderer(Node root) {
        this.root = root;
    }

    public boolean isInlineNode(Node node) {
        if (node instanceof TextNode 
            || node instanceof EmNode 
            || node instanceof PrespanNode) {
            return true;
        }
        return false;
    }

    private void nlIndentIfNotDone(Node node, Node prev, int indent) {
        if (prev != null && !isInlineNode(prev) && isInlineNode(node)) {
            String pad = "  ".repeat(indent);
            out.append("\n" + pad);
        }
    }

    private void renderNode(Node node, Node prev, int indent) {
        String pad = "  ".repeat(indent);

        if (node instanceof RootNode) {
            RootNode r = (RootNode) node;
            out.append("<article>");
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
        else if (node instanceof NavbarNode) {
            NavbarNode n = (NavbarNode) node;
            out.append("\n" + pad + "<nav id='main-nav'>");
            renderNode(n.title, n, indent + 1);
            if (n.navlist != null) renderNode(n.navlist, n.title, indent + 1);
            out.append("\n" + pad + "</nav>");
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
            String classes = "";
            if (t.lang != null) classes += "lang-" + t.lang;
            if (t.wrap) classes += " pre-wrap";
            classes = classes.trim();
            out.append("\n" + pad + "<pre");
            if (classes != "") out.append(" class='" + classes + "'");
            out.append("><code>");
            out.append(Lexer.escapeHtml(t.code.trim()));
            out.append("</code></pre>");

            if (t.filepath != null) {
                out.append("\n" + pad + "<div class='code-snippet-filename'>" + t.filepath + "</div>");
            }
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
    }

    public String render() {
        renderNode(root, null, 0);
        return out.toString();
    }
}
