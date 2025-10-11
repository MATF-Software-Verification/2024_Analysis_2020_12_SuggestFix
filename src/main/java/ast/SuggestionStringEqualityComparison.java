package ast;

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

public class SuggestionStringEqualityComparison extends SuggestionNode {

    private SuggestionNode currentCode;
    private SuggestionNode suggestion;
    
    public static Map<String, Boolean> stringVariables = new HashMap<>();


    public static void rememberStringVariables(ExpressionStmt expressionStmt) {
        if (expressionStmt.getExpression().isVariableDeclarationExpr()) {
            VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expressionStmt.getExpression();
            
            for (VariableDeclarator variable : variableDeclarationExpr.getVariables()) {
                if (variable.getTypeAsString().equals("String")) {
                    String identifier = variable.getName().getIdentifier();
                    stringVariables.put(identifier, true);
                }
            }
        }
    }

    public static void rememberStringParameters(NodeList<Parameter> parameters) {
        for (Parameter param : parameters) {
            if (param.getTypeAsString().equals("String")) {
                String identifier = param.getName().getIdentifier();
                stringVariables.put(identifier, true);
            }
        }
    }

    public static void rememberStringFields(FieldDeclaration fieldDeclaration) {
        for (VariableDeclarator variable : fieldDeclaration.getVariables()) {
            if (variable.getTypeAsString().equals("String")) {
                String identifier = variable.getName().getIdentifier();
                stringVariables.put(identifier, true);
            }
        }
    }

    public void checkStringEqualityComparison(BinaryExpr binaryExpr) {
        BinaryExpr.Operator operator = binaryExpr.getOperator();
        
        if (operator != BinaryExpr.Operator.EQUALS && operator != BinaryExpr.Operator.NOT_EQUALS) {
            return;
        }

        Expression left = binaryExpr.getLeft();
        Expression right = binaryExpr.getRight();

        if (isNullLiteral(left) || isNullLiteral(right)) {
            return;
        }

        boolean leftIsString = isStringType(left);
        boolean rightIsString = isStringType(right);

        if (leftIsString && rightIsString) {
            createSuggestion(binaryExpr, operator);
        }
    }

    private boolean isNullLiteral(Expression expr) {
        return expr instanceof NullLiteralExpr;
    }

    private boolean isStringType(Expression expr) {
        if (expr.isStringLiteralExpr()) {
            return true;
        }

        if (expr.isNameExpr()) {
            String identifier = expr.asNameExpr().getName().getIdentifier();
            Boolean isString = stringVariables.get(identifier);
            if (isString != null && isString) {
                return true;
            }
        }

        return false;
    }

    private void setCurrentCode(BinaryExpr binaryExpr) {
        this.currentCode = new SuggestionNode();

        if (binaryExpr.getBegin().isPresent()) {
            this.currentCode.setBegin(String.valueOf(binaryExpr.getBegin().get().line));
        }
        if (binaryExpr.getEnd().isPresent()) {
            this.currentCode.setEnd(String.valueOf(binaryExpr.getEnd().get().line));
        }

        this.currentCode.setCode(binaryExpr.toString());
    }

    private void setSuggestion(BinaryExpr binaryExpr, BinaryExpr.Operator operator) {
        this.suggestion = new SuggestionNode();
        this.suggestion.setBegin(this.currentCode.getBegin());
        this.suggestion.setEnd(this.currentCode.getEnd());
        this.suggestion.setCode(createSuggestionCode(binaryExpr, operator));
    }

    private String createSuggestionCode(BinaryExpr binaryExpr, BinaryExpr.Operator operator) {
        Expression left = binaryExpr.getLeft();
        Expression right = binaryExpr.getRight();

        String leftStr = left.toString();
        String rightStr = right.toString();

        boolean leftIsLiteral = left.isStringLiteralExpr();
        boolean rightIsLiteral = right.isStringLiteralExpr();

        String caller;
        String argument;

        if (rightIsLiteral && !leftIsLiteral) {
            caller = rightStr;
            argument = leftStr;
        } else {
            caller = leftStr;
            argument = rightStr;
        }

        if (operator == BinaryExpr.Operator.EQUALS) {
            return caller + ".equals(" + argument + ")";
        } else {
            return "!" + caller + ".equals(" + argument + ")";
        }
    }

    private void createSuggestion(BinaryExpr binaryExpr, BinaryExpr.Operator operator) {
        setCurrentCode(binaryExpr);
        setSuggestion(binaryExpr, operator);
        
        SuggestionUtil.suggestions.add(new Suggestion(
                this.currentCode,
                this.suggestion,
                SuggestionTypeEnum.STRING_EQUALITY_COMPARISON)
        );
        
        System.out.println("Suggested change: String Equality Comparison (use .equals() instead of ==)");
    }
}

