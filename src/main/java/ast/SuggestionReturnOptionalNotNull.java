package ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;

import java.util.List;

public class SuggestionReturnOptionalNotNull extends SuggestionNode {

    public SuggestionReturnOptionalNotNull() {
        super();
    }

    public static void checkIfMethodCanReturnNull(NodeList<Statement> statements, NodeList<Parameter> parameters, MethodDeclaration md, Node cu) {
        for (var statement : statements) {
            if (statement.isReturnStmt()) {
                ReturnStmt rtrnStmt = (ReturnStmt) statement;
                if (rtrnStmt.getExpression().isPresent()) {
                    Expression ex = rtrnStmt.getExpression().get();
                    //Return statement is method calling expression e.g. Optional.empty()
                    if (ex.isMethodCallExpr()) {
                        return;
                    }
                    List<VariableDeclarator> vd = ex.findAll(VariableDeclarator.class);
                    List<SimpleName> names = ex.findAll(SimpleName.class);
                    for (var name : names) {
                        boolean check = checkIfCanBeNull(name, statements, parameters, cu);
                        if (check) {
                            suggestionOptionalNotNull(md, rtrnStmt);
                        }
                    }
                }
            }
        }
    }

    private static void suggestionOptionalNotNull(MethodDeclaration md, ReturnStmt rtrnStmt) {
        md.isDefault();
        if (md.getRange().isPresent() && rtrnStmt.getRange().isPresent() && rtrnStmt.getExpression().isPresent()) {
            int methodDeclarationLine = md.getRange().get().begin.line;
            String methodName = md.getName().toString();
            int returnStmtLine = rtrnStmt.getRange().get().begin.line;
            Type oldType = md.getType();
            String newType = "Optional<" + oldType.asString() + ">";
            String newReturnString = "return Optional.ofNullable(" +  rtrnStmt.getExpression().get() + ")";
            SuggestionUtil.suggestions.add(new Suggestion(methodName, oldType, newType, methodDeclarationLine, rtrnStmt,
                    returnStmtLine, newReturnString, SuggestionTypeEnum.VARIABLE_CAN_BE_NULL));
        }
    }

    private static boolean checkIfCanBeNull(SimpleName name, NodeList<Statement> stmts, NodeList<Parameter> parameters, Node cu) {
        //Checking local variables
        Expression lastLocalVariableExpression = new NullLiteralExpr();
         for (var stmt : stmts) {
            if (stmt.isExpressionStmt()) {
                ExpressionStmt expStmt = stmt.asExpressionStmt();
                Expression exp = expStmt.getExpression();
                if (exp.isVariableDeclarationExpr()) {
                    VariableDeclarationExpr vde = expStmt.getExpression().asVariableDeclarationExpr();
                    NodeList<VariableDeclarator> vds = vde.getVariables();
                    for (var vd : vds) {
                        Type type = vd.getType();
                        if (type.isPrimitiveType() && vd.getName().toString().compareTo(name.toString()) == 0) {
                            //Variable is primitive type, cannot be null
                            return false;
                        }
                    }
                }
                if (exp.isVariableDeclarationExpr() || exp.isAssignExpr()) {
                    if (exp.isVariableDeclarationExpr() && exp.asVariableDeclarationExpr().findAll(SimpleName.class).contains(name)) {
                        VariableDeclarationExpr varDec = exp.asVariableDeclarationExpr();
                        lastLocalVariableExpression = varDec.getVariable(0).getInitializer().orElse(new NullLiteralExpr());
                    }
                    else if (exp.isAssignExpr() && exp.asAssignExpr().findAll(SimpleName.class).contains(name)) {
                        AssignExpr assignExp = exp.asAssignExpr();
                        lastLocalVariableExpression = assignExp.asAssignExpr().getValue();
                    }
                }
            }

        }
         //Checking if local variable is last defined as not null
        if (!(lastLocalVariableExpression.isNullLiteralExpr())) {
            return false;
        }
        //Checking parameters
        for (var parameter : parameters) {
            Type parameterType = parameter.getType();
            if (parameterType.isPrimitiveType() && parameter.getName().toString().compareTo(name.toString()) == 0) {
                //Parameter is primitive type, cannot be null
                return false;
            }
        }
        //Checking fields
        List<FieldDeclaration> fds = cu.findAll(FieldDeclaration.class);
        for (var fd : fds) {
            NodeList<VariableDeclarator> vds = fd.getVariables();
            for (var vd : vds) {
                Type type = vd.getType();
                if (type.isPrimitiveType() && vd.getName().toString().compareTo(name.toString()) == 0) {
                    //Field is primitive type, cannot be null
                    return false;
                }
            }
        }
        return true;
    }

}
