package examples;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReturnOptionalNotNullExample {

    int fieldX = 10;
    private int primitiveTypeUsed(int parameterX){
        int x = 5;
        return x + parameterX + fieldX;
    }

    public LocalDate variableInitialized() {
        LocalDate birthDate = LocalDate.of(5, 5, 5);
        return birthDate;
    }

    public List<Integer> localVariableCanBeNull() {
        List<Integer> as = new ArrayList<>(3);
        as = null;
        return as;
    }

}
