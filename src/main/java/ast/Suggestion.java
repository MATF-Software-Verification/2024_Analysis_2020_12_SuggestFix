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

    private SuggestionTypeEnum type;

    public Suggestion() {
    }

    public Suggestion(SuggestionNode current, SuggestionNode suggested, SuggestionTypeEnum type) {
        this.current = current;
        this.suggested = suggested;
        this.type = type;
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

    public SuggestionTypeEnum getType() {
        return type;
    }

    public void setType(SuggestionTypeEnum type) {
        this.type = type;
    }
}
