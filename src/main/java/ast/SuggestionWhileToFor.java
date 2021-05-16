package ast;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;

import java.util.HashMap;
import java.util.Map;

public class SuggestionWhileToFor {

    private SuggestionNode currentCode;
    private SuggestionNode suggestion;

    private static final Map<String, String> variableAssign = new HashMap<>();
    private static final Map<String, String> assignBegin = new HashMap<>();

    private void setCurrentCode(String iteratorId, WhileStmt whileStatement) {
        this.currentCode = new SuggestionNode();

        if (assignBegin != null) {
            this.currentCode.setBegin(assignBegin.get(iteratorId));
        }
        if (whileStatement.getEnd().isPresent()) {
            this.currentCode.setEnd(String.valueOf(whileStatement.getEnd().get().line));
        }

        String currentCode = new StringBuilder(variableAssign.get(iteratorId))
                .append("\n...\n")
                .append(whileStatement.toString())
                .toString();
        this.currentCode.setCode(currentCode);
    }

    private void setSuggestion(String suggestedCode) {
        this.suggestion = new SuggestionNode();

        if (this.currentCode != null) {
            this.suggestion.setBegin(this.currentCode.getBegin());
            this.suggestion.setEnd(this.currentCode.getEnd());
        }

        this.suggestion.setCode(suggestedCode);
    }

    private static String createVarDeclarationString(String type, String id, String initValue) {
        return type + " " + createVariableInitString(id, initValue);
    }

    private static String createVariableInitString(String id, String value) {
        return id + " = " + value + ";";
    }

    public static void setVariableValue(ExpressionStmt expressionStmt) {
        if (expressionStmt.getExpression().isVariableDeclarationExpr()) {
            VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expressionStmt.getExpression();
            for (VariableDeclarator variable: variableDeclarationExpr.getVariables()) {
                if (variable.getInitializer().isPresent()) {
                    if (variable.getInitializer().isPresent()) {
                        String type = variable.getType().toString();
                        String identifier = variable.getName().getIdentifier();
                        String initValue = variable.getInitializer().get().toString();

                        variableAssign.put(identifier, createVarDeclarationString(type, identifier, initValue));
                        if (variable.getBegin().isPresent()) {
                            assignBegin.put(identifier, String.valueOf(variable.getBegin().get().line));
                        }
                    }
                }
            }
        }

        if (expressionStmt.getExpression().isAssignExpr()) {
            AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
            if (assignExpr.getTarget().isNameExpr()) {
                NameExpr nameExpression = (NameExpr) assignExpr.getTarget();
                String identifier = nameExpression.getName().getIdentifier();
                String value = assignExpr.getValue().toString();
                variableAssign.put(identifier, createVariableInitString(identifier, value));
                if (assignExpr.getBegin().isPresent()) {
                    assignBegin.put(identifier, String.valueOf(assignExpr.getBegin().get().line));
                }
            }
        }
    }

    private static String getIteratorAssignCode(String iteratorId) {
        return variableAssign.get(iteratorId);
    }

    private String findIteratorId(Expression expression) {
        String id = null;
        if (expression.isBinaryExpr()) {
            var binaryExpression = (BinaryExpr) expression;

            if (binaryExpression.getLeft().isNameExpr()) {
                id = binaryExpression.getLeft().asNameExpr().getNameAsString();
            } else if (binaryExpression.getRight().isNameExpr()) {
                id = binaryExpression.getRight().asNameExpr().getNameAsString();
            }
        }

        return id;
    }

    private Statement checkStatementIfChangeExpr(Statement statement, String iteratorId) {
        if (statement.isExpressionStmt()) {
            ExpressionStmt expressionStmt = (ExpressionStmt) statement;
            Expression expression = expressionStmt.getExpression();

            if (expression.isUnaryExpr()) {
                var unaryExpression = (UnaryExpr) expression;
                if (unaryExpression.getExpression().isNameExpr()) {
                    var variable = unaryExpression.getExpression().asNameExpr().getName().toString();
                    if (iteratorId.equals(variable)) {
                        return expressionStmt;
                    }
                }
            } else if (expression.isAssignExpr()) {
                if (expression.isNameExpr()) {
                    var variable = expression.asNameExpr().getName().toString();
                    if (iteratorId.equals(variable)) {
                        return expressionStmt;
                    }
                }
            }
        }

        return null;
    }

    private Statement findIteratorChangeExpr(Statement statement, String iteratorId) {
        if (statement.isBlockStmt()) {
            var blockStatement = (BlockStmt) statement;
            var statements = blockStatement.getStatements();
            if (statements.isNonEmpty()) {
                for (var stmt : statements) {
                    return this.checkStatementIfChangeExpr(stmt, iteratorId);
                }
            }
        } else {
            return this.checkStatementIfChangeExpr(statement, iteratorId);
        }

        return null;
    }

    private String createSuggestedCodeString(String assign, String cond, String update, String body) {
        return "for (" + assign + " " + cond + "; " + update + ") " + body;
    }

    private String getLoopBodyUpdated(Statement body, Statement updateStatement) {
        if (body.isBlockStmt()) {
            var blockStatement = (BlockStmt) body;
            var statements = blockStatement.getStatements();
            var statementsCopy = new NodeList<Statement>(statements);
            statementsCopy.removeIf(statement -> statement == updateStatement);
            return "{" + (statementsCopy.size() > 0? statementsCopy.toString() : "") + "\n}";
        } else {
            return "{}";
        }
    }

    public void changeWhileToForLoop(WhileStmt whileStatement) {
        String iteratorId = findIteratorId(whileStatement.getCondition());
        if (iteratorId != null) {
            String iteratorAssignCode = getIteratorAssignCode(iteratorId);
            String condition = whileStatement.getCondition().toString();
            Statement iteratorUpdateCode = findIteratorChangeExpr(whileStatement.getBody(), iteratorId);
            String bodyCode = this.getLoopBodyUpdated(whileStatement.getBody(), iteratorUpdateCode);

            this.setCurrentCode(iteratorId, whileStatement);
            this.setSuggestion(this.createSuggestedCodeString(
                    iteratorAssignCode,
                    condition,
                    iteratorUpdateCode.asExpressionStmt().getExpression().toString(),
                    bodyCode
            ));
            SuggestionUtil.suggestions.add(new Suggestion(
                    this.currentCode,
                    this.suggestion,
                    SuggestionTypeEnum.WHILE_TO_FOR)
            );
        }
    }
}
