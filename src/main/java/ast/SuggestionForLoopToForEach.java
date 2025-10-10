package ast;

import java.util.Optional;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;

public class SuggestionForLoopToForEach {

    private SuggestionNode currentCode;
    private SuggestionNode suggestion;

    private void setCurrentCode(ForStmt forStatement) {
        this.currentCode = new SuggestionNode();

        if (forStatement.getBegin().isPresent()) {
            this.currentCode.setBegin(String.valueOf(forStatement.getBegin().get().line));
        }
        if (forStatement.getEnd().isPresent()) {
            this.currentCode.setEnd(String.valueOf(forStatement.getEnd().get().line));
        }

        this.currentCode.setCode(forStatement.toString());
    }

    private void setSuggestion(String suggestedCode) {
        this.suggestion = new SuggestionNode();

        if (this.currentCode != null) {
            this.suggestion.setBegin(this.currentCode.getBegin());
            this.suggestion.setEnd(this.currentCode.getEnd());
        }

        this.suggestion.setCode(suggestedCode);
    }

    public void changeForLoopToForEach(ForStmt forStatement) {
        if (!forStatement.getInitialization().isNonEmpty() ||
                forStatement.getCompare().isEmpty() ||
            !forStatement.getUpdate().isNonEmpty()) {
            return;
        }

        Expression initExpr = forStatement.getInitialization().get(0);
        if (!initExpr.isVariableDeclarationExpr()) {
            return;
        }

        VariableDeclarationExpr varDecl = initExpr.asVariableDeclarationExpr();
        if (varDecl.getVariables().size() != 1) {
            return;
        }

        String loopVar = varDecl.getVariables().get(0).getNameAsString();
        Optional<Expression> initValue = varDecl.getVariables().get(0).getInitializer();

        if (initValue.isEmpty() || !initValue.get().toString().equals("0")) {
            return;
        }

        Expression condition = forStatement.getCompare().get();
        CollectionInfo collectionInfo = analyzeCondition(condition, loopVar);
        if (collectionInfo == null) {
            return;
        }

        Expression updateExpr = forStatement.getUpdate().get(0);
        if (!isSimpleIncrement(updateExpr, loopVar)) {
            return;
        }

        Statement body = forStatement.getBody();
        AccessPattern accessPattern = analyzeBodyForAccess(body, loopVar, collectionInfo.collectionName);
        if (accessPattern == null || accessPattern.modifiesCollection || accessPattern.modifiesLoopVar) {
            return;
        }

        String itemVarName = generateItemVarName(collectionInfo.collectionName);
        String newBody = replaceAccessWithVar(body, loopVar, collectionInfo.collectionName, 
                                               itemVarName, collectionInfo.isArray);
        
        String suggestedCode = "for (" + collectionInfo.elementType + " " + itemVarName + 
                               " : " + collectionInfo.collectionName + ") " + newBody;

        setCurrentCode(forStatement);
        setSuggestion(suggestedCode);
        SuggestionUtil.suggestions.add(new Suggestion(
                this.currentCode,
                this.suggestion,
                SuggestionTypeEnum.FOR_LOOP_TO_FOR_EACH)
        );
    }

    private CollectionInfo analyzeCondition(Expression condition, String loopVar) {
        if (!condition.isBinaryExpr()) {
            return null;
        }

        BinaryExpr binaryExpr = condition.asBinaryExpr();
        BinaryExpr.Operator operator = binaryExpr.getOperator();

        if (operator != BinaryExpr.Operator.LESS && operator != BinaryExpr.Operator.LESS_EQUALS) {
            return null;
        }

        if (!binaryExpr.getLeft().isNameExpr() ||
            !binaryExpr.getLeft().asNameExpr().getNameAsString().equals(loopVar)) {
            return null;
        }

        Expression rightSide = binaryExpr.getRight();
        
        if (rightSide.isFieldAccessExpr()) {
            FieldAccessExpr fieldAccess = rightSide.asFieldAccessExpr();
            if (fieldAccess.getNameAsString().equals("length")) {
                String arrayName = fieldAccess.getScope().toString();
                return new CollectionInfo(arrayName, true, "Object");
            }
        }
        
        if (rightSide.isMethodCallExpr()) {
            MethodCallExpr methodCall = rightSide.asMethodCallExpr();
            if (methodCall.getNameAsString().equals("size") && methodCall.getArguments().isEmpty()) {
                if (methodCall.getScope().isPresent()) {
                    String collectionName = methodCall.getScope().get().toString();
                    return new CollectionInfo(collectionName, false, "Object");
                }
            }
        }

        return null;
    }

    private boolean isSimpleIncrement(Expression updateExpr, String loopVar) {
        if (updateExpr.isUnaryExpr()) {
            UnaryExpr unaryExpr = updateExpr.asUnaryExpr();
            if ((unaryExpr.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT ||
                 unaryExpr.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT) &&
                unaryExpr.getExpression().isNameExpr() &&
                unaryExpr.getExpression().asNameExpr().getNameAsString().equals(loopVar)) {
                return true;
            }
        }
        return false;
    }

    private AccessPattern analyzeBodyForAccess(Statement body, String loopVar, String collectionName) {
        AccessPattern pattern = new AccessPattern();
        
        if (body.isBlockStmt()) {
            BlockStmt blockStmt = body.asBlockStmt();
            for (Statement stmt : blockStmt.getStatements()) {
                if (!analyzeStatementForAccess(stmt, loopVar, collectionName, pattern)) {
                    return null;
                }
            }
        } else {
            if (!analyzeStatementForAccess(body, loopVar, collectionName, pattern)) {
                return null;
            }
        }

        return pattern.hasValidAccess ? pattern : null;
    }

    private boolean analyzeStatementForAccess(Statement stmt, String loopVar, 
                                               String collectionName, AccessPattern pattern) {
        String stmtStr = stmt.toString();
        
        boolean hasGetAccess = stmtStr.contains(collectionName + ".get(" + loopVar + ")");
        boolean hasArrayAccess = stmtStr.contains(collectionName + "[" + loopVar + "]");
        
        if (hasGetAccess || hasArrayAccess) {
            pattern.hasValidAccess = true;
        }

        if (stmtStr.contains(collectionName + ".add(") ||
            stmtStr.contains(collectionName + ".remove(") ||
            stmtStr.contains(collectionName + ".set(") ||
            stmtStr.contains(collectionName + ".clear(") ||
            stmtStr.contains(collectionName + ".put(")) {
            pattern.modifiesCollection = true;
            return false;
        }

        if (stmtStr.matches(".*\\b" + collectionName + "\\s*\\[\\s*" + loopVar + "\\s*\\]\\s*=.*")) {
            pattern.modifiesCollection = true;
            return false;
        }

        String withoutCollectionAccess = stmtStr;
        if (hasGetAccess) {
            withoutCollectionAccess = withoutCollectionAccess.replace(collectionName + ".get(" + loopVar + ")", "");
        }
        if (hasArrayAccess) {
            withoutCollectionAccess = withoutCollectionAccess.replace(collectionName + "[" + loopVar + "]", "");
        }

        if (withoutCollectionAccess.matches(".*\\b" + loopVar + "\\b.*")) {
            pattern.modifiesLoopVar = true;
            return false;
        }

        return true;
    }

    private String generateItemVarName(String collectionName) {
        if (collectionName.endsWith("s") && collectionName.length() > 1) {
            return collectionName.substring(0, collectionName.length() - 1);
        }
        String lowerName = collectionName.toLowerCase();
        if (lowerName.contains("list")) {
            return "item";
        }
        if (lowerName.contains("array")) {
            return "element";
        }
        return collectionName + "Item";
    }

    private String replaceAccessWithVar(Statement body, String loopVar, String collectionName, 
                                         String itemVarName, boolean isArray) {
        String bodyStr = body.toString();
        
        if (isArray) {
            bodyStr = bodyStr.replaceAll(collectionName + "\\[" + loopVar + "\\]", itemVarName);
        } else {
            bodyStr = bodyStr.replaceAll(collectionName + "\\.get\\(" + loopVar + "\\)", itemVarName);
        }
        
        return bodyStr;
    }

    private static class CollectionInfo {
        String collectionName;
        boolean isArray;
        String elementType;

        CollectionInfo(String collectionName, boolean isArray, String elementType) {
            this.collectionName = collectionName;
            this.isArray = isArray;
            this.elementType = elementType;
        }
    }

    private static class AccessPattern {
        boolean hasValidAccess = false;
        boolean modifiesCollection = false;
        boolean modifiesLoopVar = false;
    }
}

