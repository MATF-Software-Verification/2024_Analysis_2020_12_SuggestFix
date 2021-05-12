package ast;

import com.github.javaparser.Position;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.List;

public class SuggestionDefinedNotUsed extends SuggestionNode {

    public SuggestionDefinedNotUsed() {
        super();
    }

    public static void checkIfVariableIsDeclaredButNotUsed(NodeList<Statement> statements) {
        for (var statement : statements) {
            if (statement.isExpressionStmt()) {
                ExpressionStmt expressionStmt = (ExpressionStmt) statement;
                if (expressionStmt.getExpression().isVariableDeclarationExpr()) {
                    VariableDeclarationExpr vde = (VariableDeclarationExpr) expressionStmt.getExpression();
                    NodeList<VariableDeclarator> variables = vde.getVariables();
                    for (var variable : variables) {
                        SimpleName variableName = variable.getName();
                        boolean check = checkIfVariableIsUsed(variableName, statements, statement);
                        if (!check && variable.getBegin().isPresent()) {
                            Position variablePosition = variable.getBegin().get();
                            SuggestionUtil.suggestions.add(new Suggestion(variableName, variablePosition, "", SuggestionTypeEnum.VARIABLE_DEFINED_NOT_USED));
//                          System.out.println("Variable '" + variableName + "' declared on line: " + variablePosition.line + " but not used");
                        }
                    }
                }
            }
        }
    }

    private static boolean checkIfVariableIsUsed(SimpleName name, NodeList<Statement> statements, Statement statement) {
        for (var stmt : statements) {
            if (stmt == statement) {
                continue;
            }
            List<SimpleName> names = stmt.findAll(SimpleName.class);
            if (names.contains(name)) {
                return true;
            }
        }
        return false;
    }

    public static void checkIfMethodParameterIsNotUsed(NodeList<Parameter> parameters, NodeList<Statement> statements, String methodName) {
        for (var parameter : parameters) {
            SimpleName parameterName = parameter.getName();
            boolean check = checkIfParameterIsUsed(parameterName, statements);
            if (!check && parameter.getBegin().isPresent()) {
                Position parameterPosition = parameter.getBegin().get();
                SuggestionUtil.suggestions.add(new Suggestion(parameterName, parameterPosition, methodName, SuggestionTypeEnum.PARAMETER_NOT_USED));
//                System.out.println("Parameter '" + parameterName + "' declared in method: '" + methodName + "' on line: " + parameterPosition.line + " but not used");
            }
        }
    }

    private static boolean checkIfParameterIsUsed(SimpleName name, NodeList<Statement> statements) {
        for (var statement : statements) {
            List<SimpleName> names = statement.findAll(SimpleName.class);
            if (names.contains(name)) {
                return true;
            }
        }
        return false;
    }
}
