import java.util.List;

interface Node {}

class RootNode implements Node {
    List<Node> children;

    public RootNode(List<Node> children) {
        this.children = children; 
    }
}

class ParagraphNode implements Node {
    List<Node> children;

    public ParagraphNode(List<Node> children) {
        this.children = children; 
    }
}

class HeadingNode implements Node {
    int level;
    List<Node> children;

    public HeadingNode(int level, List<Node> children) {
        this.level = level;
        this.children = children; 
    }
}

class TextNode implements Node {
    String text;

    public TextNode(String text) {
        this.text = text;
    }
}

class EmNode implements Node {
    boolean strong;
    List<Node> children;

    public EmNode(boolean strong, List<Node> children) {
        this.strong = strong;
        this.children = children; 
    }
}

class PreblockNode implements Node {
    String lang;
    boolean nowrap;
    String code;

    public PreblockNode(String lang, boolean nowrap, String code) {
        this.lang = lang;
        this.nowrap = nowrap;
        this.code = code;
    }
}

class PrespanNode implements Node {
    String code;

    public PrespanNode(String code) {
        this.code = code;
    }
}

class ListNode implements Node {
    boolean ordered;
    List<ListItemNode> items;

    public ListNode(boolean ordered, List<ListItemNode> items) {
        this.ordered = ordered;
        this.items = items; 
    }
}

class ListItemNode implements Node {
    List<Node> children;

    public ListItemNode(List<Node> children) {
        this.children = children; 
    }
}
