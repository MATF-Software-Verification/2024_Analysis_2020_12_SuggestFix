package examples;

import java.util.ArrayList;
import java.util.List;

public class ForLoopToForEachExample {

    public static void main(String[] args) {
        List<String> students = new ArrayList<>();
        students.add("Alice");
        students.add("Bob");
        students.add("Charlie");
        students.add("Diana");

        System.out.println("Students:");
        for (int i = 0; i < students.size(); i++) {
            System.out.println(students.get(i));
        }

        String[] courses = {"Math", "Physics", "Chemistry", "Biology"};
        System.out.println("\nCourses:");
        for (int i = 0; i < courses.length; i++) {
            System.out.println(courses[i]);
        }

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            numbers.add(i * 10);
        }

        int sum = 0;
        for (int i = 0; i < numbers.size(); i++) {
            sum += numbers.get(i);
        }
        System.out.println("\nSum: " + sum);

        System.out.println("\nNumbers greater than 20:");
        for (int i = 0; i < numbers.size(); i++) {
            if (numbers.get(i) > 20) {
                System.out.println(numbers.get(i));
            }
        }
    }
}

