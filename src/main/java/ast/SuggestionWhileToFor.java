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
import java.util.Optional;

public class SuggestionWhileToFor {

    private SuggestionNode currentCode;
    private SuggestionNode suggestion;

    private static final Map<String, String> variableAssign = new HashMap<>();
    private static final Map<String, String> assignBegin = new HashMap<>();

    private void setCurrentCode(String iteratorId, WhileStmt whileStatement) {
        this.currentCode = new SuggestionNode();

        if (!assignBegin.isEmpty()) {
            this.currentCode.setBegin(assignBegin.get(iteratorId));
        }
        if (whileStatement.getEnd().isPresent()) {
            this.currentCode.setEnd(String.valueOf(whileStatement.getEnd().get().line));
        }

        String currentCode = variableAssign.get(iteratorId) + "\n...\n" + whileStatement;
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

    private boolean checkStatementIfChangeExpr(Statement statement, String iteratorId) {
        if (statement.isExpressionStmt()) {
            ExpressionStmt expressionStmt = (ExpressionStmt) statement;
            Expression expression = expressionStmt.getExpression();

            if (expression.isUnaryExpr()) {
                var unaryExpression = (UnaryExpr) expression;
                if (unaryExpression.getExpression().isNameExpr()) {
                    var variable = unaryExpression.getExpression().asNameExpr().getName().toString();
                    return iteratorId.equals(variable);
                }
            } else if (expression.isAssignExpr()) {
                if (expression.isNameExpr()) {
                    var variable = expression.asNameExpr().getName().toString();
                    return iteratorId.equals(variable);
                }
            }
        }

        return false;
    }

    private Optional<Statement> findIteratorChangeExpr(Statement statement, String iteratorId) {
        if (statement.isBlockStmt()) {
            var blockStatement = (BlockStmt) statement;
            var statements = blockStatement.getStatements();
            if (statements.isNonEmpty()) {
                for (var stmt : statements) {
                    if (checkStatementIfChangeExpr(stmt, iteratorId)) {
                        return Optional.of(stmt);
                    }
                }
            }
        } else {
            if (checkStatementIfChangeExpr(statement, iteratorId)) {
                return Optional.of(statement);
            }
        }

        return Optional.empty();
    }

    private String createSuggestedCodeString(String assign, String cond, String update, String body) {
        return "for (" + assign + " " + cond + "; " + update + ") " + body;
    }

    private String getLoopBodyUpdated(Statement body, Statement updateStatement) {
        if (body.isBlockStmt()) {
            var blockStatement = (BlockStmt) body;
            var statements = blockStatement.getStatements();
            var statementsCopy = new NodeList<>(statements);
            statementsCopy.removeIf(statement -> statement == updateStatement);

            StringBuilder blockStringBuilder = new StringBuilder("{");
            if (statementsCopy.size() > 0) {
                blockStringBuilder.append("\n");
            }
            var iterator = statementsCopy.iterator();
            while (iterator.hasNext()) {
                blockStringBuilder.append("\t");
                blockStringBuilder.append(iterator.next().toString());
                if (iterator.hasNext()) {
                    blockStringBuilder.append("\n");
                }
            }
            if (statementsCopy.size() > 0) {
                blockStringBuilder.append("\n");
            }
            blockStringBuilder.append("}");
            return blockStringBuilder.toString();
        } else {
            return "{}";
        }
    }

    /**
     * for (int i = 1; i < n; i++) is a programming idiom, which is the usual way to code a task and
     * therefore is recommended to write a loop in this way if it is possible. One of the reasons
     * behind this is readability and the fact that with for loop you can manage to keep i within
     * the scope of the loop instead of letting it escape to the rest of the code. Also with the
     * use of for loop, you get the same results all wrapped up in just one line.
     */
    public void changeWhileToForLoop(WhileStmt whileStatement) {
        String iteratorId = findIteratorId(whileStatement.getCondition());
        if (iteratorId != null) {
            String iteratorAssignCode = getIteratorAssignCode(iteratorId);
            String condition = whileStatement.getCondition().toString();

            var iteratorChangeExpr= findIteratorChangeExpr(whileStatement.getBody(), iteratorId);
            if (iteratorChangeExpr.isPresent()) {
                Statement iteratorUpdateCode = iteratorChangeExpr.get();
                String bodyCode = getLoopBodyUpdated(whileStatement.getBody(), iteratorUpdateCode);

                setCurrentCode(iteratorId, whileStatement);
                setSuggestion(createSuggestedCodeString(
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
}
