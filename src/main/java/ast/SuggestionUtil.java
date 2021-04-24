package ast;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuggestionUtil {

    public static List<Suggestion> suggestions = new ArrayList<>();
    public static Map<String, SuggestionIdentifiersAndAssignments> identifiersAndAssignments = new HashMap<>();

    private static String toString(List<Suggestion> suggestions) {
        StringBuilder sb = new StringBuilder();
        for (var suggestion : suggestions) {
            switch (suggestion.getType()) {
                case IDENTIFIER_ASSIGNMENT:
                    sb.append(identifierAssignmentToString(suggestion));
                    break;
            }

        }
        return sb.toString();
    }

    private static String identifierAssignmentToString(Suggestion suggestion) {
        return new StringBuilder().append("Begin [")
                .append(suggestion.getCurrent().getBegin())
                .append("]\n")
                .append(suggestion.getCurrent().getCode())
                .append("\nEnd [")
                .append(suggestion.getCurrent().getEnd())
                .append("]\n")
                .append("Can be replaced with:\n")
                .append("Begin [")
                .append(suggestion.getSuggested().getBegin())
                .append("]\n")
                .append(suggestion.getSuggested().getCode())
                .append("\nEnd [")
                .append(suggestion.getSuggested().getEnd())
                .append("]\n").toString();
    }

    public static void mergeInitializationAndAssignment(Statement statement, ExpressionStmt expressionStmt) {

        SuggestionIdentifiersAndAssignments suggestion;
        SuggestionIdentifiersAndAssignments current;
        if (expressionStmt.getExpression().isVariableDeclarationExpr()) {
            VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expressionStmt.getExpression();
            for (VariableDeclarator variable : variableDeclarationExpr.getVariables()) {
                String identifier = variable.getName().getIdentifier();
                if (!identifiersAndAssignments.containsKey(identifier)) {
                    current = new SuggestionIdentifiersAndAssignments();
                    if (variable.getBegin().isPresent()) {
                        current.setBegin(String.valueOf(variable.getBegin().get().line));
                    }
                    if (variable.getEnd().isPresent()) {
                        current.setEnd(String.valueOf(variable.getBegin().get().line));
                    }
                    if (variable.getInitializer().isEmpty()) {
                        current.setCode(statement.toString());
                        current.setInitialization(variable.getTypeAsString() + " " + variable.getNameAsString());
                    }
                    identifiersAndAssignments.put(identifier, current);
                }
            }
        }
        if (expressionStmt.getExpression().isAssignExpr()) {
            AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
            if (assignExpr.getTarget().isNameExpr()) {
                NameExpr nameExpression = (NameExpr) assignExpr.getTarget();
                String identifier = nameExpression.getName().getIdentifier();
                if (identifiersAndAssignments.containsKey(identifier)) {
                    current = identifiersAndAssignments.get(identifier);
                    if (!current.isUsedInOtherScope()) {
                        suggestion = new SuggestionIdentifiersAndAssignments();
                        if (assignExpr.getBegin().isPresent()) {
                            suggestion.setBegin(String.valueOf(assignExpr.getBegin().get().line));
                        }
                        if (assignExpr.getEnd().isPresent()) {
                            suggestion.setEnd(String.valueOf(assignExpr.getBegin().get().line));
                        }
                        suggestion.setCode(current.getInitialization() + " = " + assignExpr.getValue().toString() + ";");
                        suggestions.add(new Suggestion(
                                current,
                                suggestion,
                                SuggestionTypeEnum.IDENTIFIER_ASSIGNMENT)
                        );
                    }
                } else {
                    current = new SuggestionIdentifiersAndAssignments();
                    current.setUsedInOtherScope(true);
                    identifiersAndAssignments.put(identifier, current);
                }
            }
        }
    }

    public static String printSuggestions() {
        return toString(suggestions);
    }
}
