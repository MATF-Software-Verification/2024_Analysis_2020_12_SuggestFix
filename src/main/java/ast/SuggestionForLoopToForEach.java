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
        if (hasCollectionAccess(stmt, collectionName, loopVar)) {
            pattern.hasValidAccess = true;
        }

        if (modifiesCollection(stmt, collectionName, loopVar)) {
            pattern.modifiesCollection = true;
            return false;
        }

        if (usesLoopVarForOtherPurpose(stmt, collectionName, loopVar)) {
            pattern.modifiesLoopVar = true;
            return false;
        }

        return true;
    }

    private boolean hasCollectionAccess(Statement stmt, String collectionName, String loopVar) {
        String stmtStr = stmt.toString();
        return stmtStr.contains(collectionName + ".get(" + loopVar + ")") ||
               stmtStr.contains(collectionName + "[" + loopVar + "]");
    }

    private boolean modifiesCollection(Statement stmt, String collectionName, String loopVar) {
        String stmtStr = stmt.toString();

        if (stmtStr.contains(collectionName + ".add(") ||
            stmtStr.contains(collectionName + ".remove(") ||
            stmtStr.contains(collectionName + ".set(") ||
            stmtStr.contains(collectionName + ".clear(") ||
            stmtStr.contains(collectionName + ".put(")) {
            return true;
        }

        if (isCollectionPassedAsParameter(stmtStr, collectionName)) {
            return true;
        }

        if (isArrayElementAssignment(stmtStr, collectionName, loopVar)) {
            return true;
        }

        return isArrayElementUnaryOp(stmtStr, collectionName, loopVar);
    }

    private boolean isCollectionPassedAsParameter(String stmtStr, String collectionName) {
        int index = 0;
        while ((index = stmtStr.indexOf(collectionName, index)) != -1) {
            int nextCharIndex = index + collectionName.length();

            if (nextCharIndex < stmtStr.length()) {
                char nextChar = stmtStr.charAt(nextCharIndex);

                if (nextChar == '[' || nextChar == '.') {
                    index = nextCharIndex;
                    continue;
                }

                int openParenIndex = stmtStr.lastIndexOf('(', index);
                if (openParenIndex != -1) {
                    String between = stmtStr.substring(openParenIndex + 1, index).trim();
                    if (between.isEmpty() || between.endsWith(",")) {
                        return true;
                    }
                }
            }

            index = nextCharIndex;
        }

        return false;
    }

    private boolean isArrayElementAssignment(String stmtStr, String collectionName, String loopVar) {
        return stmtStr.contains(collectionName + "[" + loopVar + "]") &&
               stmtStr.matches(".*" + collectionName + "\\s*\\[\\s*" + loopVar + "\\s*\\]\\s*=.*");
    }

    private boolean isArrayElementUnaryOp(String stmtStr, String collectionName, String loopVar) {
        String arrayAccess = collectionName + "[" + loopVar + "]";
        if (!stmtStr.contains(arrayAccess)) {
            return false;
        }

        return stmtStr.contains(arrayAccess + "++") ||
               stmtStr.contains(arrayAccess + "--") ||
               stmtStr.contains("++" + arrayAccess) ||
               stmtStr.contains("--" + arrayAccess);
    }

    private boolean usesLoopVarForOtherPurpose(Statement stmt, String collectionName, String loopVar) {
        String stmtStr = stmt.toString();

        String withoutAccess = stmtStr
            .replace(collectionName + ".get(" + loopVar + ")", "")
            .replace(collectionName + "[" + loopVar + "]", "");

        return withoutAccess.matches(".*\\b" + loopVar + "\\b.*");
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

