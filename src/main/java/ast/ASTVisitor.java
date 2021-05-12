package ast;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Optional;

public class ASTVisitor extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(BlockStmt n, Void arg) {
        super.visit(n, arg);
        String key = SuggestionUtil.getKey(n);

        if (n.getStatements().isNonEmpty()) {
            NodeList<Statement> statements = n.getStatements();
            //Suggestion identifiers and assignments
            for (var statement : statements) {
                if (statement.isExpressionStmt()) {
                    ExpressionStmt expressionStmt = (ExpressionStmt) statement;
                    SuggestionIdentifiersAndAssignments.mergeInitializationAndAssignment(statement, expressionStmt, key);
                }
            }
            //Suggestion defined not used (for variables)
            SuggestionDefinedNotUsed.checkIfVariableIsDeclaredButNotUsed(statements);
        }
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        super.visit(n, arg);

        //If method is overriding another method we won't check if all parameters are used,
        //because it can happened that some parameter is used in super class method
        if (n.getAnnotationByName("Override").isPresent()) {
            return;
        }
        String methodName = n.getName().asString();
        //We won't check out main method, because it has to have args as parameter
        if (methodName.equals("main")) {
            return;
        }

        //Suggestion defined not used (for method parameters)
        NodeList<Parameter> parameters = n.getParameters();
        Optional<BlockStmt> body = n.getBody();
        BlockStmt blockStmt = body.orElse(new BlockStmt());
        NodeList<Statement> statements = blockStmt.getStatements();
        SuggestionDefinedNotUsed.checkIfMethodParameterIsNotUsed(parameters, statements, methodName);
    }
}
