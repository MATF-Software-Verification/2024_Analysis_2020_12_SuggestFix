package examples;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HelloWorld {

    private static String i = null;
    private boolean t = false;

    public static void main(String[] args) {
        int x;
        int z = 2;
        int y;
        int a, c;
        if (z == 2) {
            int b;
            y = 2;
            x = 3;
            z = 3 + 4;
        }
        if (z == 2) {
            y = 2;
            x = 3;
            z = 3 + 4;
        }
        x = z + 3;
        proba(1, 21, 3.2);

        try {
            URL url = new URL("www.google.rs");
            File file = new File("./examples/HelloWorld.java");
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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
}
