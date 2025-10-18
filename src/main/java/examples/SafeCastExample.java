package examples;

public class SafeCastExample {

    public static void main(String[] args) {
        Object obj1 = "Hello World";

        String str = (String) obj1;
        System.out.println(str);

        processString((String) obj1);

        String result;
        result = (String) obj1;
        System.out.println(result);

        TestClass test = new TestClass(obj1);
    }

    static class TestClass {
        final String data;
        
        TestClass(Object obj) {
            this.data = (String) obj;
        }
    }

    public static void processString(String s) {
        System.out.println("Processing: " + s);
    }
}
