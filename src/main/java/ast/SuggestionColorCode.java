package ast;

import java.util.HashMap;
import java.util.Map;

public class SuggestionColorCode {

    private static SuggestionColorCode singleInstance;
    private final Map<SuggestionTypeEnum, String> suggestionColor = new HashMap<>();

    private SuggestionColorCode() {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_YELLOW = "\u001B[33m";
        final String ANSI_BLUE = "\u001B[34m";
        final String ANSI_PURPLE = "\u001B[35m";
        final String ANSI_CYAN = "\u001B[36m";
        final String ANSI_WHITE = "\u001B[37m";
        final String ANSI_BLACK = "\u001B[30m";

        suggestionColor.put(SuggestionTypeEnum.IDENTIFIER_ASSIGNMENT, ANSI_RED);
        suggestionColor.put(SuggestionTypeEnum.VARIABLE_DEFINED_NOT_USED, ANSI_GREEN);
        suggestionColor.put(SuggestionTypeEnum.PARAMETER_NOT_USED, ANSI_YELLOW);
        suggestionColor.put(SuggestionTypeEnum.REDUNDANT_INITIALIZATION, ANSI_BLUE);
        suggestionColor.put(SuggestionTypeEnum.WHILE_TO_FOR, ANSI_PURPLE);
        suggestionColor.put(SuggestionTypeEnum.VARIABLE_CAN_BE_NULL, ANSI_CYAN);
    }

    public static SuggestionColorCode getInstance() {
        if (SuggestionColorCode.singleInstance == null) {
            SuggestionColorCode.singleInstance = new SuggestionColorCode();
        }

        return SuggestionColorCode.singleInstance;
    }

    public String getSuggestionColor(SuggestionTypeEnum type) {
        return this.suggestionColor.get(type);
    }

    public String resetColorCode() {
        final String ANSI_RESET = "\u001B[0m";
        return ANSI_RESET;
    }
}
