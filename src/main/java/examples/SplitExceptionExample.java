package examples;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SplitExceptionExample {
    public static void main(String[] args) {
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

    }
}
