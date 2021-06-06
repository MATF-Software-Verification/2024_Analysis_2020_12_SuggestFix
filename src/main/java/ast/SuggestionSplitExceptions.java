package ast;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.ArrayList;
import java.util.List;

public class SuggestionSplitExceptions {

    private SuggestionNode currentCode;
    private SuggestionNode suggestion;

    private void setCurrentCode(TryStmt statement) {
        this.currentCode = new SuggestionNode();

        if(statement.getBegin().isPresent()) {
            this.currentCode.setBegin(String.valueOf(statement.getBegin().get().line));
        }
        if(statement.getEnd().isPresent()) {
            this.currentCode.setEnd(String.valueOf(statement.getEnd().get().line));
        }

        this.currentCode.setCode(statement.toString());
    }

    private void setSuggestion(String suggestedCode) {
        this.suggestion = new SuggestionNode();
        this.suggestion.setCode(suggestedCode);
    }

    private String createSuggestedCodeString(List<String> exceptions) {
        StringBuilder result = new StringBuilder();
        for (String exception: exceptions) {
            result.append(exception);
            result.append(System.getProperty("line.separator"));
        }
        return result.toString();
    }

    public void splitExceptions(TryStmt tryStatement) {
        // Get all statements in try block
        BlockStmt statements = tryStatement.getTryBlock();

        // If try block is empty, return
        if(statements.getStatements().isEmpty()) {
            return;
        }

        // All exceptions that should be split
        List<String> exceptions = new ArrayList<>();

        // For each statement, check if its expression (if&for cannot make exceptions)
        for (var statement: statements.getStatements()) {
            if(statement.isExpressionStmt()) {
                var expr = ((ExpressionStmt)statement).getExpression();

                // Exceptions made by constructor calls using new
                if(expr.isObjectCreationExpr()) {
                    ClassOrInterfaceType className = ((ObjectCreationExpr)expr).getType();
                    switch(className.getNameAsString()) {
                        case "File":
                            exceptions.add("FileNotFoundException");
                            break;
                        case "URL":
                            exceptions.add("MalformedUrlException");
                            break;
                    }
                }

                // Exceptions made by method calls
                if(expr.isMethodCallExpr()) {
                    var methodCall = (MethodCallExpr)expr;
                    switch (methodCall.getNameAsString()) {
                        case "sleep":
                            exceptions.add("InterruptedException");
                            break;
                        case "parse":
                            exceptions.add("ParseException");
                            break;
                        case "insert":
                            exceptions.add("StringIndexOutOfBoundsException");
                            break;
                    }
                }
            }

            // Set code and suggestion, add it to list
            this.setCurrentCode(tryStatement);
            this.setSuggestion(this.createSuggestedCodeString(exceptions));
            SuggestionUtil.suggestions.add(
                    new Suggestion(
                            this.currentCode,
                            this.suggestion,
                            SuggestionTypeEnum.EXCEPTION_SPLIT
                    )
            );
        }

    }
}
