package ast;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.Objects;

public class SuggestionIdentifiersAndAssignments extends SuggestionNode {

    private String initialization;

    private String assignment;

    private String blockId;

    public SuggestionIdentifiersAndAssignments() {
        super();
    }

    public String getInitialization() {
        return initialization;
    }

    public void setInitialization(String initialization) {
        this.initialization = initialization;
    }

    public String getAssignment() {
        return assignment;
    }

    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public static void mergeInitializationAndAssignment(Statement statement, ExpressionStmt expressionStmt, String key) {
        SuggestionIdentifiersAndAssignments suggestion;
        SuggestionIdentifiersAndAssignments current;
        if (expressionStmt.getExpression().isVariableDeclarationExpr()) {
            VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expressionStmt.getExpression();
            for (VariableDeclarator variable : variableDeclarationExpr.getVariables()) {
                if (variable.getInitializer().isEmpty()) {
                    String identifier = variable.getName().getIdentifier();
                    if (!SuggestionUtil.identifiersAndAssignments.containsKey(key + identifier)) {
                        current = new SuggestionIdentifiersAndAssignments();
                        if (variable.getBegin().isPresent()) {
                            current.setBegin(String.valueOf(variable.getBegin().get().line));
                        }
                        if (variable.getEnd().isPresent()) {
                            current.setEnd(String.valueOf(variable.getBegin().get().line));
                        }

                        current.setCode(statement.toString());
                        current.setInitialization(variable.getTypeAsString() + " " + variable.getNameAsString());

                        SuggestionUtil.identifiersAndAssignments.put(key + identifier, current);
                    }
                }
            }
        }
        if (expressionStmt.getExpression().isAssignExpr()) {
            AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
            if (assignExpr.getTarget().isNameExpr()) {
                NameExpr nameExpression = (NameExpr) assignExpr.getTarget();
                String identifier = nameExpression.getName().getIdentifier();
                if (SuggestionUtil.identifiersAndAssignments.containsKey(key + identifier)) {
                    current = SuggestionUtil.identifiersAndAssignments.get(key + identifier);
                    if (Objects.nonNull(current)) {
                        suggestion = new SuggestionIdentifiersAndAssignments();
                        if (assignExpr.getBegin().isPresent()) {
                            suggestion.setBegin(String.valueOf(assignExpr.getBegin().get().line));
                        }
                        if (assignExpr.getEnd().isPresent()) {
                            suggestion.setEnd(String.valueOf(assignExpr.getBegin().get().line));
                        }
                        suggestion.setCode(current.getInitialization() + " = " + assignExpr.getValue().toString() + ";");
                        SuggestionUtil.suggestions.add(new Suggestion(
                                current,
                                suggestion,
                                SuggestionTypeEnum.IDENTIFIER_ASSIGNMENT)
                        );
                    }
                }
            }
        }
    }
}
