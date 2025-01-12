package ast;

import com.github.javaparser.Position;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;

public class Suggestion {

    private SuggestionNode current;
    private SuggestionNode suggested;
    private SuggestionTypeEnum type;
    private SimpleName variableName;
    private Position variablePosition;
    private String methodName;
    private Type oldType;
    private String newType;
    private int methodDeclarationLine;
    private ReturnStmt rtrnStmt;
    private int returnStmtLine;
    private String newReturnString;

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

    public Suggestion(String methodName, Type oldType, String newType, int methodDeclarationLine, ReturnStmt rtrnStmt,
                      int returnStmtLine, String newReturnString, SuggestionTypeEnum type) {
        this.methodName = methodName;
        this.oldType = oldType;
        this.newType = newType;
        this.methodDeclarationLine = methodDeclarationLine;
        this.rtrnStmt = rtrnStmt;
        this.returnStmtLine = returnStmtLine;
        this.newReturnString = newReturnString;
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

    public Type getOldType() { return oldType; }

    public String getNewType() { return newType; }

    public int getMethodDeclarationLine() { return methodDeclarationLine; }

    public ReturnStmt getRtrnStmt() { return rtrnStmt; }

    public int getReturnStmtLine() { return returnStmtLine; }

    public String getNewReturnString() { return newReturnString; }
}