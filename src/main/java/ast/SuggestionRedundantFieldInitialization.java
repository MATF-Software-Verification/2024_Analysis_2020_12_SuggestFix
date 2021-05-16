package ast;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.util.ArrayList;

public class SuggestionRedundantFieldInitialization {

    private SuggestionNode currentCode;
    private SuggestionNode suggestion;

    private void setCurrentCode(FieldDeclaration declaration) {
        this.currentCode = new SuggestionNode();

        if (declaration.getBegin().isPresent()) {
            this.currentCode.setBegin(String.valueOf(declaration.getBegin().get().line));
        }
        if (declaration.getEnd().isPresent()) {
            this.currentCode.setEnd(String.valueOf(declaration.getEnd().get().line));
        }

        this.currentCode.setCode(declaration.toString());
    }

    private void setSuggestion(String suggestedCode) {
        this.suggestion = new SuggestionNode();
        this.suggestion.setCode(suggestedCode);
    }

    private String createSuggestedCodeString(FieldDeclaration declaration) {
        var modifiersList = declaration.getModifiers();
        StringBuilder result = new StringBuilder();
        for (var modifier: modifiersList) {
            result.append(modifier.toString());
        }

        var variableDeclarations = declaration.getVariables();
        var variableNamesArray = new ArrayList<String>();
        for (var variableDeclaration: variableDeclarations) {
            variableNamesArray.add(variableDeclaration.getName().toString());
        }

        return result
                .append(declaration.getElementType())
                .append(" ")
                .append(String.join(", ", variableNamesArray))
                .append(";")
                .toString();
    }

    /**
     * It’s very unnecessary to initialize member variables to the following values: 0, false and null,
     * because these values are the default initialization values of member variables in Java.
     * Therefore, if you know the default initialization values of member variables,
     * you will avoid unnecessary explicit initialization.
     */
    public void removeRedundantFieldInitialization(FieldDeclaration declaration) {
        // Find last field in declaration to get init value
        var lastVariableDeclaration = declaration.getVariables().getLast();
        if (lastVariableDeclaration.isPresent()) {
            var lastVariable = (VariableDeclarator) lastVariableDeclaration.get();

            var initializer = lastVariable.getInitializer();
            if (initializer.isPresent()) {
                var initValue = initializer.get().toString();

                switch (initValue) {
                    case "0":
                    case "false":
                    case "null":
                        this.setCurrentCode(declaration);
                        this.setSuggestion(this.createSuggestedCodeString(declaration));
                        SuggestionUtil.suggestions.add(new Suggestion(
                                this.currentCode,
                                this.suggestion,
                                SuggestionTypeEnum.REDUNDANT_INITIALIZATION)
                        );
                }
            }
        }
    }
}
