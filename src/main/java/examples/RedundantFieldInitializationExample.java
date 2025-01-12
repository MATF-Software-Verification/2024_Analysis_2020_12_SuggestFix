package examples;

public class RedundantFieldInitializationExample {
    public static int counter = 0;
    private String value = "Init";
    private final boolean flag = false;

    public RedundantFieldInitializationExample(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return flag? "Hello everyone" : "Hello world";
    }
}
