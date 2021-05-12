package examples;

public class HelloWorld {
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
    }
    public static void proba(int p, float q, double r) {
        float s = p + q * 2;
    }

    public void p2(int znj) {

    }
}

class HiWorld extends HelloWorld {
    @Override
    public void p2(int znj) {

    }
}
