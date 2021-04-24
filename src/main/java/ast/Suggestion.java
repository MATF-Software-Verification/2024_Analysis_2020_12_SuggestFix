package ast;

import com.github.javaparser.ast.Node;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Suggestion {

    private SuggestionNode current;

    private SuggestionNode suggested;

    public Suggestion() {
    }

    public SuggestionNode getCurrent() {
        return current;
    }

    public void setCurrent(SuggestionNode current) {
        this.current = current;
    }

    public SuggestionNode getSuggested() {
        return suggested;
    }

    public void setSuggested(SuggestionNode suggested) {
        this.suggested = suggested;
    }
}
