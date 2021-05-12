import ast.ASTUtil;
import ast.ASTVisitor;
import ast.SuggestionUtil;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;

public class Main {


    public static void main(String[] args) {

        var cu = ASTUtil.getCompilationUnit(new File("D:\\BATAVELJKO\\FAKS\\9. SEMESTAR\\VS\\Projekat\\2020_12_SuggestFix\\src\\main\\java\\examples\\HelloWorld.java"));
        ASTUtil.traverseTree(cu);
        System.out.println(SuggestionUtil.printSuggestions());

    }
}
