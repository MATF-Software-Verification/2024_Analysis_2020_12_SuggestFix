package ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;

public class SuggestionSafeCast extends SuggestionNode {

    private SuggestionNode currentCode;
    private SuggestionNode suggestion;

    public void checkForUnsafeCast(CastExpr castExpr) {
        Expression expressionBeingCast = castExpr.getExpression();

        Type targetType = castExpr.getType();

        if (isPrimitiveType(targetType)) {
            return;
        }

        Statement containingStatement = findContainingStatement(castExpr);

        if (containingStatement == null) {
            return;
        }

        if (!isSimpleStatement(containingStatement)) {
            return;
        }

        if (isProblematicAssignment(containingStatement)) {
            return;
        }

        createSuggestion(containingStatement, expressionBeingCast, targetType);
    }

    private Statement findContainingStatement(CastExpr castExpr) {
        Node current = castExpr.getParentNode().orElse(null);

        while (current != null) {
            if (current instanceof Statement statement) {
                return statement;
            }
            current = current.getParentNode().orElse(null);
        }
        
        return null;
    }

    private boolean isSimpleStatement(Statement stmt) {
        return stmt.isExpressionStmt();
    }

    private boolean isPrimitiveType(Type type) {
        String typeStr = type.toString();
        return typeStr.equals("int") || typeStr.equals("long") || 
               typeStr.equals("double") || typeStr.equals("float") ||
               typeStr.equals("boolean") || typeStr.equals("char") ||
               typeStr.equals("byte") || typeStr.equals("short");
    }

    private boolean isProblematicAssignment(Statement statement) {
        if (!statement.isExpressionStmt()) {
            return false;
        }

        Expression expr = statement.asExpressionStmt().getExpression();

        if (expr.isAssignExpr()) {
            AssignExpr assignExpr = expr.asAssignExpr();
            Expression target = assignExpr.getTarget();

            if (target.isFieldAccessExpr()) {
                return true;
            }

            if (target.isNameExpr()) {
                String varName = target.asNameExpr().getNameAsString();
                if (isClassField(target, varName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isClassField(Expression expr, String varName) {
        Node current = expr;
        while (current != null) {
            if (current instanceof ClassOrInterfaceDeclaration classDecl) {
                for (Node member : classDecl.getMembers()) {
                    if (member instanceof FieldDeclaration fieldDecl) {
                        for (var variable : fieldDecl.getVariables()) {
                            if (variable.getNameAsString().equals(varName)) {
                                return true;
                            }
                        }
                    }
                }
                break;
            }
            current = current.getParentNode().orElse(null);
        }
        return false;
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

    private void setSuggestion(Statement statement, Expression expressionBeingCast, Type targetType) {
        this.suggestion = new SuggestionNode();
        this.suggestion.setBegin(this.currentCode.getBegin());
        this.suggestion.setEnd(this.currentCode.getEnd());

        String varName = extractVariableName(statement);

        if (varName != null) {
            String patternMatching = expressionBeingCast.toString() + " instanceof " + 
                                    targetType.toString() + " " + varName;
            String wrappedCode = "if (" + patternMatching + ") {\n    // TODO: Move all needed code here\n}";
            this.suggestion.setCode(wrappedCode);
        } else {
            String instanceofCheck = expressionBeingCast.toString() + " instanceof " + targetType.toString();
            String wrappedCode = "if (" + instanceofCheck + ") {\n    " + statement.toString() + "\n}";
            this.suggestion.setCode(wrappedCode);
        }
    }

    private String extractVariableName(Statement statement) {
        if (!statement.isExpressionStmt()) {
            return null;
        }

        Expression expr = statement.asExpressionStmt().getExpression();

        if (expr.isVariableDeclarationExpr()) {
            return extractVariableNameFromDeclaration(expr.asVariableDeclarationExpr());
        }

        if (expr.isAssignExpr()) {
            AssignExpr assignExpr = expr.asAssignExpr();
            if (assignExpr.getTarget().isNameExpr()) {
                return assignExpr.getTarget().asNameExpr().getNameAsString();
            }
        }

        return null;
    }

    private String extractVariableNameFromDeclaration(VariableDeclarationExpr varDecl) {
        if (varDecl.getVariables().size() == 1) {
            return varDecl.getVariables().get(0).getNameAsString();
        }
        return null;
    }

    private void createSuggestion(Statement statement, Expression expressionBeingCast, Type targetType) {
        setCurrentCode(statement);
        setSuggestion(statement, expressionBeingCast, targetType);

        SuggestionUtil.suggestions.add(new Suggestion(
                this.currentCode,
                this.suggestion,
                SuggestionTypeEnum.SAFE_CAST)
        );
        
        System.out.println("Suggested change: Add instanceof check before cast (prevents ClassCastException)");
    }
}
