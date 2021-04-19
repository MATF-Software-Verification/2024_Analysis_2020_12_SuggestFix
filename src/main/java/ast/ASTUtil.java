package ast;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.utils.VisitorList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.UUID;

public class ASTUtil {

    public static CompilationUnit getCompilationUnit(File file) {
        try {
            return StaticJavaParser.parse(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ASTVisitor traverseTree(CompilationUnit cu){

        ASTVisitor visitor = new ASTVisitor();
        cu.accept(visitor, null);
        return visitor;
    }

}
