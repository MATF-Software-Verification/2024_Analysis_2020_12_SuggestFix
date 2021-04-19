package ast;

import com.github.javaparser.ast.Node;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Suggestion {

    List<Node> nodes = new ArrayList<>();

    public Suggestion() {
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
