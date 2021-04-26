package ast;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ASTVisitor extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(BlockStmt n, Void arg) {
        super.visit(n, arg);
        String key = SuggestionUtil.getKey(n);

        if (n.getStatements().isNonEmpty()) {
            NodeList<Statement> statements = n.getStatements();
            for (var statement : statements) {
                if (statement.isExpressionStmt()) {
                    ExpressionStmt expressionStmt = (ExpressionStmt) statement;
                    SuggestionIdentifiersAndAssignments.mergeInitializationAndAssignment(statement, expressionStmt, key);
                }
            }
        }
    }
}
