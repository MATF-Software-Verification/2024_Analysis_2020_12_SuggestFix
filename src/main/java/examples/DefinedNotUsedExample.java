package examples;

public class DefinedNotUsedExample {

    private int localExample() {
        int a=2, b=5, c;
        return b;
    }

    protected int parameterExample(int a, int b) {
        return b;
    }
}

class DefinedNotUsedSubclassExample extends DefinedNotUsedExample {

    private final int i = 1;

    @Override
    protected int parameterExample(int a, int b) {
        return b;
    }
}
