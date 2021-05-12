package ast;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.SimpleName;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Suggestion {

    private SuggestionNode current;
    private SuggestionNode suggested;
    private SuggestionTypeEnum type;
    private SimpleName variableName;
    private Position variablePosition;
    private String methodName;

    public Suggestion() {
    }

    public Suggestion(SuggestionNode current, SuggestionNode suggested, SuggestionTypeEnum type) {
        this.current = current;
        this.suggested = suggested;
        this.type = type;
    }

    public Suggestion(SimpleName variableName, Position variablePosition, String methodName, SuggestionTypeEnum type) {
        this.variableName = variableName;
        this.variablePosition = variablePosition;
        this.methodName = methodName;
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

    public Position getVariablePosition() { return variablePosition; }

    public void setVariablePosition(Position variablePosition) { this.variablePosition = variablePosition; }

    public SimpleName getVariableName() { return variableName; }

    public void setVariableName(SimpleName variableName) { this.variableName = variableName; }

    public String getMethodName() { return methodName; }

    public void setMethodName(String methodName) { this.methodName = methodName; }
}
