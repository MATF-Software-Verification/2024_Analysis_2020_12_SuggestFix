import ast.ASTUtil;
import ast.SuggestionUtil;

import java.io.File;

public class Main {


    public static void main(String[] args) {

        var examplePath = "./src/main/java/examples/HelloWorld.java";
        var compilationUnit = ASTUtil.getCompilationUnit(new File(examplePath));
        ASTUtil.traverseTree(compilationUnit);
        System.out.println(SuggestionUtil.printSuggestions());

    }
}
