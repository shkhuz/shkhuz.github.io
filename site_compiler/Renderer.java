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

    public String render() {
        return out.toString();
    }
}
