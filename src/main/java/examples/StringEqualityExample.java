package examples;

public class StringEqualityExample {

    public static void main(String[] args) {
        String name1 = "John";
        String name2 = "John";
        if (name1 == name2) {
            System.out.println("Equal");
        }

        String input = getUserInput();
        if (input == "admin") {
            System.out.println("Admin");
        }

        if (input == null) {
            System.out.println("Null");
        }
    }

    private static String getUserInput() {
        return "admin";
    }
}

