package examples;

public class StringConcatenationExample {
    public static void main(String[] args) {
        String example = "Numbers: " + " ";

        for (int i = 0; i < 10; i++) {
            example += i;
        }

        while (example.length() < 20) {
            example = example + "!";
        }

        String newExample = example + ";";
        System.out.println(newExample);

        String[] strings = {"a", "b", "c"};
        for (var str: strings) {
            newExample += str;
        }
    }
}
