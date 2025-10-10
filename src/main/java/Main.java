import ast.ASTUtil;
import ast.SuggestionTypeEnum;
import ast.SuggestionUtil;

import java.io.File;

import static ast.SuggestionTypeEnum.*;
import static java.lang.System.exit;

public class Main {


    public static void main(String[] args) {

        var exampleFolder = String.join(File.separator, new String[]{".", "src", "main", "java", "examples"});
        var examplePath = String.join(File.separator, new String[]{exampleFolder, "HelloWorld.java"});

        String file, wantedSuggestions;
        SuggestionTypeEnum[] wantedSuggestionsArray = new SuggestionTypeEnum[0];

        if (args.length == 4) {
            if (args[0].equals("-f") && args[2].equals("-s")) {
                file = args[1] + ".java";
                examplePath = String.join(File.separator, new String[]{exampleFolder, file});

                wantedSuggestions = args[3];
                wantedSuggestionsArray = splitSuggestions(wantedSuggestions);
            }
            else {
                printUsageAndExit();
            }
        }

        if (args.length == 2) {
            if (args[0].equals("-f")) {
                file = args[1] + ".java";
                examplePath = String.join(File.separator, new String[]{exampleFolder, file});
            }
            else if (args[0].equals("-s")) {
                wantedSuggestions = args[1];
                wantedSuggestionsArray = splitSuggestions(wantedSuggestions);
            }
            else {
                printUsageAndExit();
            }
        }

        // Specific suggestions are not given, we are using all suggestions
        if (wantedSuggestionsArray.length == 0) {
            wantedSuggestionsArray = new SuggestionTypeEnum[values().length];
            wantedSuggestionsArray[0] = IDENTIFIER_ASSIGNMENT;
            wantedSuggestionsArray[1] = VARIABLE_DEFINED_NOT_USED;
            wantedSuggestionsArray[2] = PARAMETER_NOT_USED;
            wantedSuggestionsArray[3] = REDUNDANT_INITIALIZATION;
            wantedSuggestionsArray[4] = WHILE_TO_FOR;
            wantedSuggestionsArray[5] = VARIABLE_CAN_BE_NULL;
            wantedSuggestionsArray[6] = EXCEPTION_SPLIT;
            wantedSuggestionsArray[7] = STRING_CONCATENATION;
            wantedSuggestionsArray[8] = NESTED_IF_TO_SINGLE_IF;
            wantedSuggestionsArray[9] = IF_ELSE_TO_TERNARY;
            wantedSuggestionsArray[10] = FOR_LOOP_TO_FOR_EACH;
        }

        File fileToReadFrom = new File(examplePath);
        if (fileToReadFrom.exists()) {
            var compilationUnit = ASTUtil.getCompilationUnit(fileToReadFrom);
            if (compilationUnit != null) {
                ASTUtil.traverseTree(compilationUnit, wantedSuggestionsArray);
                System.out.println(SuggestionUtil.printSuggestions());
            } else {
                System.out.println("An error occurred while traversing the tree");
                printUsageAndExit();
            }
        }
        else {
            System.out.println("File with given name does not exist");
            printUsageAndExit();
        }
    }

    private static void printUsageAndExit() {
        System.out.println("Arguments usage: fileName [-f fileNameToAnalyze(has to be in 'examples' folder)] " +
                "[-s wantedSuggestions (\ni - IDENTIFIER_ASSIGNMENT," +
                "\nv - VARIABLE_DEFINED_NOT_USED, \np - PARAMETER_NOT_USED, \nr - REDUNDANT_INITIALIZATION, " +
                "\nw - WHILE_TO_FOR, \nn - VARIABLE_CAN_BE_NULL, \ne - EXCEPTION_SPLIT " +
                "\nc - STRING_CONCATENATION\n" + "f - NESTED_IF_TO_SINGLE_IF\n" + "t - IF_ELSE_TO_TERNARY\n" +
                "l - FOR_LOOP_TO_FOR_EACH\n)]");
        exit(0);
    }

    private static SuggestionTypeEnum[] splitSuggestions(String suggestions) {
        SuggestionTypeEnum[] suggestionsArray = new SuggestionTypeEnum[suggestions.length()];

        int i = 0;
        for (char suggestion: suggestions.toCharArray()) {
            switch (suggestion) {
                case 'i':
                    suggestionsArray[i] = IDENTIFIER_ASSIGNMENT;
                    break;
                case 'v':
                    suggestionsArray[i] = VARIABLE_DEFINED_NOT_USED;
                    break;
                case 'p':
                    suggestionsArray[i] = PARAMETER_NOT_USED;
                    break;
                case 'r':
                    suggestionsArray[i] = REDUNDANT_INITIALIZATION;
                    break;
                case 'w':
                    suggestionsArray[i] = WHILE_TO_FOR;
                    break;
                case 'n':
                    suggestionsArray[i] = VARIABLE_CAN_BE_NULL;
                    break;
                case 'e':
                    suggestionsArray[i] = EXCEPTION_SPLIT;
                    break;
                case 'c':
                    suggestionsArray[i] = STRING_CONCATENATION;
                    break;
                case 'f':
                    suggestionsArray[i] = NESTED_IF_TO_SINGLE_IF;
                    break;
                case 't':
                    suggestionsArray[i] = IF_ELSE_TO_TERNARY;
                    break;
                case 'l':
                    suggestionsArray[i] = FOR_LOOP_TO_FOR_EACH;
                    break;
                default:
                    printUsageAndExit();
            }
            i++;
        }

        return suggestionsArray;
    }
}
