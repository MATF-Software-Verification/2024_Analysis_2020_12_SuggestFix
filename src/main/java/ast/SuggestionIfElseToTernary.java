package ast;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.Statement;

public class SuggestionIfElseToTernary extends SuggestionNode {

    public SuggestionIfElseToTernary() {
        super();
    }

    public static void checkAndSuggestIfElseToTernary(NodeList<Statement> statements) {
        for (var statement : statements) {
            if (statement.isIfStmt()) {
                IfStmt ifStmt = (IfStmt) statement;

                // Check if there is an else statement
                if (ifStmt.getElseStmt().isPresent()) {
                    // Check if the then block is a BlockStmt and contains a ReturnStmt
                    if (ifStmt.getThenStmt() instanceof BlockStmt thenBlock) {
                        if (thenBlock.getStatements().size() == 1 && thenBlock.getStatement(0) instanceof ReturnStmt thenReturnStmt) {

                            // Check if the else block is a BlockStmt and contains a ReturnStmt
                            if (ifStmt.getElseStmt().get() instanceof BlockStmt elseBlock) {
                                if (elseBlock.getStatements().size() == 1 && elseBlock.getStatement(0) instanceof ReturnStmt elseReturnStmt) {

                                    // Create suggestions
                                    SuggestionNode currentCode = new SuggestionNode();
                                    currentCode.setCode(ifStmt.toString());
                                    currentCode.setBegin(String.valueOf(ifStmt.getBegin().get().line));
                                    currentCode.setEnd(String.valueOf(ifStmt.getEnd().get().line));

                                    String ternaryExpression = "return " + ifStmt.getCondition() + " ? " +
                                            thenReturnStmt.getExpression().get() + " : " +
                                            elseReturnStmt.getExpression().get() + ";";
                                    SuggestionNode suggestedCode = new SuggestionNode();
                                    suggestedCode.setCode(ternaryExpression);
                                    suggestedCode.setBegin(currentCode.getBegin());
                                    suggestedCode.setEnd(currentCode.getBegin());

                                    SuggestionUtil.suggestions.add(new Suggestion(currentCode, suggestedCode, SuggestionTypeEnum.IF_ELSE_TO_TERNARY));
                                    System.out.println("Suggested change: If-Else to Ternary");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}