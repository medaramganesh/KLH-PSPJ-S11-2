
import java.util.Scanner;

class Student {

    private int rollNo;
    private String name;
    private int marks;
    private int totalClasses;
    private int attendedClasses;

    
    Student(int rollNo, String name, int marks,
            int totalClasses, int attendedClasses) {

        this.rollNo = rollNo;
        this.name = name;
        this.marks = marks;
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
    }

    
    public String calculateGrade() {

        if (marks >= 90) {
            return "A+";
        } else if (marks >= 80) {
            return "A";
        } else if (marks >= 70) {
            return "B";
        } else if (marks >= 60) {
            return "C";
        } else if (marks >= 50) {
            return "D";
        } else {
            return "Fail";
        }
    }

    
    public double calculateAttendance() {

        if (totalClasses == 0) {
            return 0;
        }

        return (attendedClasses * 100.0) / totalClasses;
    }

    
    public void displayDetails() {

        System.out.println("\n----- STUDENT REPORT -----");

        System.out.println("Roll Number: " + rollNo);
        System.out.println("Student Name: " + name);
        System.out.println("Marks: " + marks);
        System.out.println("Grade: " + calculateGrade());

        System.out.printf("Attendance: %.2f%%\n",
                          calculateAttendance());

        if (calculateAttendance() >= 75) {
            System.out.println("Attendance Status: Eligible");
        } else {
            System.out.println("Attendance Status: Shortage");
        }
    }
}

public class Project {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("================================");
        System.out.println(" UNIVERSITY GRADE AND ATTENDANCE TRACKER ");
        System.out.println("================================");

        System.out.print("Enter Roll Number: ");
        int rollNo = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Student Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Marks (0-100): ");
        int marks = sc.nextInt();

        System.out.print("Enter Total Classes: ");
        int totalClasses = sc.nextInt();

        System.out.print("Enter Attended Classes: ");
        int attendedClasses = sc.nextInt();

        if (marks < 0 || marks > 100 ||
            totalClasses < 0 ||
            attendedClasses < 0 ||
            attendedClasses > totalClasses) {

            System.out.println("Invalid input. Please check your values.");

        } else {

            Student student = new Student(
                rollNo,
                name,
                marks,
                totalClasses,
                attendedClasses
            );

            student.displayDetails();
        }

        sc.close();
    }
}

