package ast;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.Pair;

import java.util.*;

public class SuggestionUtil {

    public static List<Suggestion> suggestions = new ArrayList<>();
    public static Map<String, SuggestionIdentifiersAndAssignments> identifiersAndAssignments = new HashMap<>();
    public static SuggestionColorCode colorCode = SuggestionColorCode.getInstance();

    private static String toString(List<Suggestion> suggestions) {
        suggestions.sort(Comparator.comparing(Suggestion::getType));
        StringBuilder sb = new StringBuilder();
        for (var suggestion : suggestions) {
            sb.append(colorCode.getSuggestionColor(suggestion.getType()));
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
                case WHILE_TO_FOR:
                    sb.append(whileToForLoop(suggestion));
                    break;
                case VARIABLE_CAN_BE_NULL:
                    sb.append(optionalNotNull(suggestion));
                    break;
                case EXCEPTION_SPLIT:
                    sb.append(splitExceptionsToString(suggestion));
                    break;
                case STRING_CONCATENATION:
                    sb.append(stringConcatenationToString(suggestion));
                    break;
                case NESTED_IF_TO_SINGLE_IF:
                    sb.append(nestedIfToSingleIfToString(suggestion));
                    break;
            }
            sb.append(colorCode.getSuggestionColor(suggestion.getType()));

            sb.append("\n");
        }

        sb.append(colorCode.resetColorCode());
        return sb.toString();
    }

    private static String identifierAssignmentToString(Suggestion suggestion) {
        return new StringBuilder().append("Merge declaration and assignment:\n")
                .append("Begin [")
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

    private static String splitExceptionsToString(Suggestion suggestion) {
        return new StringBuilder("Split exceptions:\n")
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

    private static String whileToForLoop(Suggestion suggestion) {
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
                .append("]\n")
                .toString();
    }

    private static String optionalNotNull(Suggestion suggestion) {
        return "Change type of method: '" + suggestion.getMethodName() + "' on line: " +
                suggestion.getMethodDeclarationLine() +
                " from: '" + suggestion.getOldType().toString() + "' to: '" + suggestion.getNewType() + "'\n" +
                "Change return statement '" + suggestion.getRtrnStmt() + "' on line: " + suggestion.getReturnStmtLine() +
                " to: '" + suggestion.getNewReturnString() + ";'\n";
    }

    private static String stringConcatenationToString(Suggestion suggestion) {
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
                .append("]\n")
                .toString();
    }

    private static String nestedIfToSingleIfToString(Suggestion suggestion) {
        return new StringBuilder().append("Combine nested if statements:\n")
                .append("Begin [")
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
                .append("]\n")
                .toString();
}

    public static String printSuggestions() {
        return toString(suggestions);
    }
}
