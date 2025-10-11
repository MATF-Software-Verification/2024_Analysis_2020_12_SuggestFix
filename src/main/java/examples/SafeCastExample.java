package examples;

public class SafeCastExample {

    public static void main(String[] args) {
        Object obj1 = "Hello World";
        Object obj2 = 42;

        String str = (String) obj1;
        System.out.println(str);

        Integer num = (Integer) obj2;
        System.out.println(num);

        processString((String) obj1);
    }

    public static void processString(String s) {
        System.out.println("Processing: " + s);
    }
}
