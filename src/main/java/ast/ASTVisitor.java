package ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

public class ASTVisitor extends VoidVisitorAdapter<Void> {

    public List<SimpleName> simpleNames = new ArrayList<>();

    @Override
    public void visit(ExpressionStmt n, Void arg) {
        super.visit(n, arg);
        Expression expression = n.getExpression();
        if(expression.isVariableDeclarationExpr()){
            VariableDeclarationExpr variableDeclarationExpr = expression.asVariableDeclarationExpr();
            NodeList<VariableDeclarator> variables = variableDeclarationExpr.getVariables();
            for(VariableDeclarator variable : variables) {
                super.visit(variable, arg);
            }
        }
        else if(expression.isAssignExpr()) {
            AssignExpr assignExpr = expression.asAssignExpr();
            simpleNames.add(assignExpr.getTarget().asNameExpr().getName());
        }
    }

    @Override
    public void visit(VariableDeclarator n, Void arg) {
        super.visit(n, arg);
        simpleNames.add(n.getName());
    }

    @Override
    public void visit(SimpleName n, Void arg) {
        super.visit(n, arg);
    }
}
