package ast;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuggestionUtil {

    public static List<Suggestion> suggestions = new ArrayList<>();
    public static Map<String, SuggestionIdentifiersAndAssignments> identifiersAndAssignments = new HashMap<>();
    public static Map<String, BlockStmt> blockStmtMap = new HashMap<>();

    private static String toString(List<Suggestion> suggestions) {
        StringBuilder sb = new StringBuilder();
        for (var suggestion : suggestions) {
            switch (suggestion.getType()) {
                case IDENTIFIER_ASSIGNMENT:
                    sb.append(identifierAssignmentToString(suggestion));
                    break;
                case VARIABLE_DEFINED_NOT_USED:
                    sb.append(variableDefinedNotUsed(suggestion));
                    break;
                case PARAMETER_NOT_USED:
                    sb.append(parameterNotUsed(suggestion));
                    break;
                case REDUNDANT_INITIALIZATION:
                    sb.append(redundantInitializationToString(suggestion));
                    break;
            }

            sb.append("\n");
        }
        return sb.toString();
    }

    private static String identifierAssignmentToString(Suggestion suggestion) {
        return new StringBuilder().append("Begin [")
                .append(suggestion.getCurrent().getBegin())
                .append("]\n")
                .append(suggestion.getCurrent().getCode())
                .append("\nEnd [")
                .append(suggestion.getCurrent().getEnd())
                .append("]\n")
                .append("Can be replaced with:\n")
                .append("Begin [")
                .append(suggestion.getSuggested().getBegin())
                .append("]\n")
                .append(suggestion.getSuggested().getCode())
                .append("\nEnd [")
                .append(suggestion.getSuggested().getEnd())
                .append("]\n").toString();
    }

    private static String variableDefinedNotUsed(Suggestion suggestion) {
        return "Variable '" + suggestion.getVariableName() +
               "' declared on line: " + suggestion.getVariablePosition().line + " not used " +
                "-> can be removed\n";
    }

    private static String parameterNotUsed(Suggestion suggestion) {
        return "Parameter '" + suggestion.getVariableName() +
                "' declared in method: '" + suggestion.getMethodName() +
                "' on line: " + suggestion.getVariablePosition().line + " not used " +
                "-> can be removed\n";
    }

    private static String redundantInitializationToString(Suggestion suggestion) {
        return new StringBuilder("Redundant initialization:\n")
                .append("[")
                .append(suggestion.getCurrent().getBegin())
                .append("]\t")
                .append(suggestion.getCurrent().getCode())
                .append("\n")
                .append("Can be removed and replaced with:\n")
                .append("[")
                .append(suggestion.getCurrent().getBegin())
                .append("]\t")
                .append(suggestion.getSuggested().getCode())
                .append("\n")
                .toString();
    }

    public static String printSuggestions() {
        return toString(suggestions);
    }

    public static String getKey(BlockStmt n) {
        String key = "";
        if (n.getBegin().isPresent()) {
           key = createKey(n.getBegin().get().line, n.getBegin().get().column);
        }
        else {
            addBlockToMap(n, new Pair<>(key, false));
        }
        if (blockStmtMap.containsKey(key)) {
            addBlockToMap(n, new Pair<>(key, true));
        }
        else {
            addBlockToMap(n, new Pair<>(key, false));
        }
        return key;
    }
    public static String createKey(int line, int column) {
        return "l" + line + "c" + column;
    }

    public static void addBlockToMap(BlockStmt n, Pair<String, Boolean> key) {
        if (key.b) {
            SuggestionUtil.blockStmtMap.get(key.a);
        }
        else {
            if (!key.a.isEmpty()) {
                SuggestionUtil.blockStmtMap.put(key.a, n);
            }
        }
    }
}
