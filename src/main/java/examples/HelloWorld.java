package examples;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HelloWorld {

    private static String i = null;
    private boolean t = false;

    public static void main(String[] args) {
        int z = 2;
        int y;
        int a, c;
        if (z == 2) {
            y = 2;
            z = 3 + 4;
            int b;
            b = 3;
        }
        if (z == 2) {
            y = 2;
            z = 3 + 4;
        }
        int x; x = z + 3;
        z = x + 2;
        y = 5;
        proba(1, 21, 3.2);

        // SplitException - EXAMPLE
        try {
            URL url = new URL("www.google.rs");
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        String dateStr = "2011-11-19";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dateFormat.parse(dateStr);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (x > y) {
            if (y > z) {
                System.out.println("Hello");
            }
        }

        List<String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Bob");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i));
        }

    }

    public static float proba(int p, float q, double r) {
        float s = 10;
        double qp = 12.2;
        int pst;
        return p + q * s;
    }

    public void p2(int znj) {

    }

    public int getGreater(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

    public LocalDate getBirthDate() {
        LocalDate birthDate = LocalDate.of(5, 5, 5);
        return birthDate;
    }

    private int a;
    private List<Integer> articles;
    public List<Integer> getArticles() {
        List<Integer> as = new ArrayList<>(3), bs = new ArrayList<>(2);
        as = null;
        return as;
    }

    public Optional<List<Integer>> getAs() {
        return Optional.empty(); // return null
    }

    private int retA() {
        return a;
    }
}

class HiWorld extends HelloWorld {

    private final int i = 1;

    @Override
    public void p2(int znj) {
        int i = 0;
        while (i < 10) {
            i++;
        }
    }

    public static void main(String[] args) {
        String example = "Dots: ";
        while (example.length() < 20) {
            example = example + ".";
        }
    }
}
