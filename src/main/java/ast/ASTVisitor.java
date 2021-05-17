package ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.*;
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

                if (statement.isWhileStmt()) {
                    WhileStmt whileStatement = (WhileStmt) statement;
                    new SuggestionWhileToFor().changeWhileToForLoop(whileStatement);
                } else if (statement.isExpressionStmt()) {
                    ExpressionStmt expressionStmt = (ExpressionStmt) statement;
                    SuggestionWhileToFor.setVariableValue(expressionStmt);
                }
            }
            //Suggestion defined not used (for variables)
            SuggestionDefinedNotUsed.checkIfVariableIsDeclaredButNotUsed(statements);
        }
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        super.visit(n, arg);

        String methodName = n.getName().asString();
        //We won't check out main method, because it has to have args as parameter
        if (methodName.equals("main")) {
            return;
        }

        //If method is overriding another method we won't check if all parameters are used,
        //because it can happened that some parameter is used in super class method
        if (n.getAnnotationByName("Override").isEmpty()) {
            //Suggestion defined not used (for method parameters)
            NodeList<Parameter> parameters = n.getParameters();
            Optional<BlockStmt> body = n.getBody();
            BlockStmt blockStmt = body.orElse(new BlockStmt());
            NodeList<Statement> statements = blockStmt.getStatements();
            SuggestionDefinedNotUsed.checkIfMethodParameterIsNotUsed(parameters, statements, methodName);
        }

        //Suggestion return optional not null
        Optional<BlockStmt> body = n.getBody();
        BlockStmt blockStmt = body.orElse(new BlockStmt());
        NodeList<Statement> statements = blockStmt.getStatements();
        NodeList<Parameter> parameters = n.getParameters();
        Node cu = n.getParentNode().orElse(new EmptyStmt());
        SuggestionReturnOptionalNotNull.checkIfMethodCanReturnNull(statements, parameters, n, cu);
    }

    @Override
    public void visit(FieldDeclaration fieldDeclaration, Void arg) {
        super.visit(fieldDeclaration, arg);

        new SuggestionRedundantFieldInitialization().removeRedundantFieldInitialization(fieldDeclaration);
    }
}
