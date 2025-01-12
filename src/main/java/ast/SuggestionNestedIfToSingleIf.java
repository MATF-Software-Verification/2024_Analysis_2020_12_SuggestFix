package ast;

import com.github.javaparser.Position;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

public class SuggestionNestedIfToSingleIf extends SuggestionNode {

    public SuggestionNestedIfToSingleIf() {
        super();
    }

    public static void checkAndSuggestNestedIfToSingleIf(NodeList<Statement> statements) {
        for (var statement : statements) {
            if (statement.isIfStmt()) {
                IfStmt outerIfStmt = (IfStmt) statement;
                Statement thenStmt = outerIfStmt.getThenStmt();
                if (thenStmt.isBlockStmt()) {
                    NodeList<Statement> thenStatements = thenStmt.asBlockStmt().getStatements();
                    if (thenStatements.size() == 1 && thenStatements.get(0).isIfStmt()) {
                        IfStmt innerIfStmt = (IfStmt) thenStatements.get(0);
                        if (outerIfStmt.getElseStmt().isEmpty() && innerIfStmt.getElseStmt().isEmpty()) {
                            if (outerIfStmt.getBegin().isPresent() && innerIfStmt.getBegin().isPresent()) {
                                Position outerIfPosition = outerIfStmt.getBegin().get();

                                SuggestionNode currentCode = new SuggestionNode();
                                currentCode.setCode(outerIfStmt.toString());
                                currentCode.setBegin(String.valueOf(outerIfPosition.line));
                                currentCode.setEnd(String.valueOf(outerIfStmt.getEnd().get().line));

                                SuggestionNode suggestedCode = getSuggestionNode(outerIfStmt, innerIfStmt, outerIfPosition);

                                SuggestionUtil.suggestions.add(new Suggestion(
                                        currentCode,
                                        suggestedCode,
                                        SuggestionTypeEnum.NESTED_IF_TO_SINGLE_IF
                                ));
                            }
                        }
                    }
                }
            }
        }
    }

    private static SuggestionNode getSuggestionNode(IfStmt outerIfStmt, IfStmt innerIfStmt, Position outerIfPosition) {
        String suggestedCodeString = "if ((" + outerIfStmt.getCondition() + ") && (" + innerIfStmt.getCondition() + ")) " + innerIfStmt.getThenStmt().toString();
        SuggestionNode suggestedCode = new SuggestionNode();
        suggestedCode.setCode(suggestedCodeString);
        suggestedCode.setBegin(String.valueOf(outerIfPosition.line));
        suggestedCode.setEnd(String.valueOf(innerIfStmt.getEnd().get().line - 1));
        return suggestedCode;
    }
}