package ast;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SuggestionStringConcatenation extends SuggestionNode {

    private SuggestionNode currentCode;
    private SuggestionNode suggestion;

    public static Map<String, Boolean> stringVariables = new HashMap<>();

    public static void rememberStringVariables(ExpressionStmt expressionStmt) {
        if (expressionStmt.getExpression().isVariableDeclarationExpr()) {
            VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expressionStmt.getExpression();

            for (VariableDeclarator variable : variableDeclarationExpr.getVariables()) {
                if (variable.getTypeAsString().equals("String") && variable.getInitializer().isPresent()) {
                    String identifier = variable.getName().getIdentifier();
                    stringVariables.put(identifier, true);
                }
            }
        }
    }

    private void setCurrentCode(Statement statement) {
        this.currentCode = new SuggestionNode();

        if (statement.getBegin().isPresent()) {
            this.currentCode.setBegin(String.valueOf(statement.getBegin().get().line));
        }
        if (statement.getEnd().isPresent()) {
            this.currentCode.setEnd(String.valueOf(statement.getEnd().get().line));
        }

        this.currentCode.setCode(statement.toString());
    }

    private void setSuggestion(Statement statement, Statement loopStatement, String id, String value) {
        this.suggestion = new SuggestionNode();
        this.suggestion.setBegin(this.currentCode.getBegin());
        this.suggestion.setEnd(String.valueOf(Integer.parseInt(this.currentCode.getEnd()) + 2));
        this.suggestion.setCode(createSuggestionCode(statement, loopStatement, id, value));
    }

    private String createSuggestionCode(Statement statement, Statement loopStatement, String id, String value) {
        return "StringBuilder builder = new StringBuilder("
               + id + ");\n"
               + createLoopCodeString(statement, loopStatement, value) + "\n"
               + id + " = builder.toString()";
    }

    private String createLoopCodeString(Statement statement, Statement loopStatement, String value) {
        if (loopStatement.isForStmt()) {
            ForStmt forStatement = loopStatement.asForStmt();
            var bodyString = createLoopBody(forStatement.getBody(), statement, value);
            var initializationString = forStatement.getInitialization().toString()
                    .replace("[", "")
                    .replace("]", "");
            var compare = forStatement.getCompare();
            var updateString = forStatement.getUpdate().toString()
                    .replace("[", "")
                    .replace("]", "");
            var forLine = "for (" + initializationString + "; "
                        + (compare.isPresent()? compare.get() : "")
                        + "; " + updateString + ") {\n";
            return forLine + bodyString + "}";
        } else if (loopStatement.isForEachStmt()) {
            ForEachStmt forEachStatement = loopStatement.asForEachStmt();
            var bodyString = createLoopBody(forEachStatement.getBody(), statement, value);
            var forEachLine = "for (" + forEachStatement.getVariable()
                            + " : " + forEachStatement.getIterable() + ") {\n";
            return forEachLine + bodyString + "}";
        } else if (loopStatement.isWhileStmt()) {
            WhileStmt whileStatement = loopStatement.asWhileStmt();
            var bodyString = createLoopBody(whileStatement.getBody(), statement, value);
            var whileLine = "while (" + whileStatement.getCondition() + ") {\n";
            return whileLine + bodyString + "}";
        }

        return "{}";
    }

    private String createLoopBody(Statement bodyStatement, Statement statement, String value) {
        StringBuilder builder = new StringBuilder();
        if (bodyStatement.isBlockStmt()) {
            var blockStatement = (BlockStmt) bodyStatement;
            var statements = blockStatement.getStatements();
            var iterator = statements.stream().iterator();
            while (iterator.hasNext()) {
                var stmt = iterator.next();
                if (stmt.equals(statement)) {
                    builder.append("\tbuilder.append(");
                    builder.append(value);
                    builder.append(");");
                } else {
                    builder.append("\t");
                    builder.append(stmt);
                }

                builder.append("\n");
            }

            return builder.toString();
        } else {
            return bodyStatement.toString();
        }
    }

    /**
     * In Java, string objects are immutable, which means once it is created, you cannot change it.
     * So when we concatenate one string with another, a new string is created, and the older one
     * is marked for the garbage collector. The problem with this is if we need to concatenate a lot
     * of strings. To solve this problem, the StringBuilder class is used. It works like a mutable String object.
     * However, Java does this string concatenation using StringBuilder by default for simple cases.
     * If you need to concatenate inside a loop, you need to manually apply StringBuilder.
     */
    public void replaceStringConcatenationWithStringBuilder(Statement loopStatement) {
        if (loopStatement.isForStmt()) {
            ForStmt forStatement = (ForStmt) loopStatement;
            replaceStringConcatenationInBody(forStatement.getBody(), loopStatement);
        } else if (loopStatement.isForEachStmt()) {
            ForEachStmt forEachStatement = (ForEachStmt) loopStatement;
            replaceStringConcatenationInBody(forEachStatement.getBody(), loopStatement);
        } else if (loopStatement.isWhileStmt()) {
            WhileStmt whileStatement = (WhileStmt) loopStatement;
            replaceStringConcatenationInBody(whileStatement.getBody(), loopStatement);
        }
    }

    private void replaceStringConcatenationInBody(Statement statement, Statement loopStatement) {
        if (statement.isBlockStmt()) {
            var blockStatement = (BlockStmt) statement;
            var statements = blockStatement.getStatements();
            if (statements.isNonEmpty()) {
                for (var stmt: statements) {
                    var pair = getIdAndValueIfStringConcatenation(stmt);
                    pair.ifPresent(strings -> createSuggestion(stmt, loopStatement, strings[0], strings[1]));
                }
            }
        } else {
            var pair = getIdAndValueIfStringConcatenation(statement);
            pair.ifPresent(strings -> createSuggestion(statement, loopStatement, strings[0], strings[1]));
        }
    }

    private Optional<String[]> getIdAndValueIfStringConcatenation(Statement statement) {
        if (statement.isExpressionStmt()) {
            var expression = statement.asExpressionStmt().getExpression();
            if (expression.isAssignExpr()) {
                var assignExpression = expression.asAssignExpr();
                if (assignExpression.getTarget().isNameExpr()) {
                    NameExpr nameExpression = (NameExpr) assignExpression.getTarget();
                    String identifier = nameExpression.getName().getIdentifier();
                    if (stringVariables.get(identifier)) {
                        var rightSideExpression = assignExpression.getValue();
                        if (assignExpression.getOperator() == AssignExpr.Operator.PLUS) {
                            var returnPair = new String[2];
                            returnPair[0] = identifier;
                            returnPair[1] = rightSideExpression.toString();
                            return Optional.of(returnPair);
                        } else if (assignExpression.getOperator() == AssignExpr.Operator.ASSIGN
                                   && rightSideExpression.isBinaryExpr()) {
                            var rightSide = rightSideExpression.asBinaryExpr();
                            if (rightSide.getOperator() == BinaryExpr.Operator.PLUS) {
                                if (rightSide.getLeft().isNameExpr()
                                    && rightSide.getLeft().asNameExpr().getName().getIdentifier().equals(identifier)) {
                                    var returnPair = new String[2];
                                    returnPair[0] = identifier;
                                    returnPair[1] = rightSide.getRight().toString();
                                    return Optional.of(returnPair);
                                } else if (rightSide.getRight().isNameExpr()
                                          && rightSide.getRight().asNameExpr().getName().getIdentifier().equals(identifier)) {
                                    var returnPair = new String[2];
                                    returnPair[0] = identifier;
                                    returnPair[1] = rightSide.getLeft().toString();
                                    return Optional.of(returnPair);
                                }
                            }
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private void createSuggestion(Statement statement, Statement loopStatement, String id, String value) {
        setCurrentCode(loopStatement);
        setSuggestion(statement, loopStatement, id,value);
        SuggestionUtil.suggestions.add(new Suggestion(
                this.currentCode,
                this.suggestion,
                SuggestionTypeEnum.STRING_CONCATENATION)
        );
    }
}
