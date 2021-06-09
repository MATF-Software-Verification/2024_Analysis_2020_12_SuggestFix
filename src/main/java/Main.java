import ast.ASTUtil;
import ast.SuggestionTypeEnum;
import ast.SuggestionUtil;

import java.io.File;
import java.io.FileNotFoundException;

import static ast.SuggestionTypeEnum.*;
import static java.lang.System.exit;

public class Main {


    public static void main(String[] args) {

        var examplePath = String.join(File.separator, new String[]{".", "src", "main", "java", "examples", "HelloWorld.java"});
        String file;
        String wantedSuggestions;
        SuggestionTypeEnum[] wantedSuggestionsArray;

        if (args.length == 5) {
            if (args[1].equals("-f") && args[3].equals("-s")) {
                file = args[2] + ".java";
                examplePath = String.join(File.separator, new String[]{".", "src", "main", "java", "examples", file});

                wantedSuggestions = args[4];
                wantedSuggestionsArray = splitSuggestions(wantedSuggestions);
            }
            else {
                printUsage();
            }
        }

        if (args.length == 3) {
            if (args[1].equals("-f")) {
                file = args[2] + ".java";
                examplePath = String.join(File.separator, new String[]{".", "src", "main", "java", "examples", file});
            }
            else if (args[1].equals("-s")) {
                wantedSuggestions = args[2];
                wantedSuggestionsArray = splitSuggestions(wantedSuggestions);
            }
            else {
                printUsage();
            }
        }

        File fileToReadFrom = new File(examplePath);

        if (fileToReadFrom.exists()) {
            var compilationUnit = ASTUtil.getCompilationUnit(fileToReadFrom);
            ASTUtil.traverseTree(compilationUnit);
            System.out.println(SuggestionUtil.printSuggestions());
        }
        else {
            System.out.println("File with given name does not exist");
        }
    }

    private static void printUsage() {
        System.out.println("Arguments usage: fileName [-f fileNameToAnalyze] [-s wantedSuggestions (\ni - IDENTIFIER_ASSIGNMENT," +
                "\nv - VARIABLE_DEFINED_NOT_USED, \np - PARAMETER_NOT_USED, \nr - REDUNDANT_INITIALIZATION, " +
                "\nw - WHILE_TO_FOR, \n - nVARIABLE_CAN_BE_NULL, \ne - EXCEPTION_SPLIT)]");
        exit(0);
    }

    private static SuggestionTypeEnum[] splitSuggestions(String suggestions) {
        SuggestionTypeEnum[] suggestionsArray = new  SuggestionTypeEnum[suggestions.length()];
        int i = 0;

        for (char suggestion: suggestions.toCharArray()) {
            switch (suggestion) {
                case 'i':
                    suggestionsArray[i++] = IDENTIFIER_ASSIGNMENT;
                case 'v':
                    suggestionsArray[i++] = VARIABLE_DEFINED_NOT_USED;
                case 'p':
                    suggestionsArray[i++] = PARAMETER_NOT_USED;
                case 'r':
                    suggestionsArray[i++] = REDUNDANT_INITIALIZATION;
                case 'w':
                    suggestionsArray[i++] = WHILE_TO_FOR;
                case 'n':
                    suggestionsArray[i++] = VARIABLE_CAN_BE_NULL;
                case 'e':
                    suggestionsArray[i++] = EXCEPTION_SPLIT;
                default:
                    printUsage();
            }
        }

        return suggestionsArray;
    }
}
