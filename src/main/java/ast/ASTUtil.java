package ast;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileNotFoundException;

public class ASTUtil {

    public static CompilationUnit getCompilationUnit(File file) {
        try {
            return StaticJavaParser.parse(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void traverseTree(CompilationUnit cu) {
        ASTVisitor visitor = new ASTVisitor();
        cu.accept(visitor, null);
    }

}
