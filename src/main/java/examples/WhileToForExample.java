package examples;

public class WhileToForExample {
    public static void main(String[] args) {
        int i = 0;
        int x = 3;

        if (i < x) {
            System.out.println("Hi :)");
        }

        i = 4;

        while (10 < i) {
            System.out.println("Hi :)");
            i++;
        }

        x = 7;

        while (x < 10) {
            System.out.println("Infinite loop");
        }

        while (3 < x) {
            --x;
        }
    }
}
