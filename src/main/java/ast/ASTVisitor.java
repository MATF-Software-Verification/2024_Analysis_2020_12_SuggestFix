package ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

public class ASTVisitor extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(BlockStmt n, Void arg) {
        super.visit(n, arg);
        if (n.getStatements().isNonEmpty()) {
            NodeList<Statement> statements = n.getStatements();
            for (var statement : statements) {
                if (statement.isExpressionStmt()) {
                    ExpressionStmt expressionStmt = (ExpressionStmt) statement;
                    SuggestionUtil.mergeInitializationAndAssignment(statement, expressionStmt);
                }
            }
        }
    }
}
