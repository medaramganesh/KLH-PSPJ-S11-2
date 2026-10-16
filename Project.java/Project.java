import java.io.*;
import java.util.*;
import java.util.stream.*;


interface Reportable {
    String report();
}

class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}

abstract class Person {
    protected final String id;
    protected final String name;

    Person(String id, String name) {
        this.id = id.trim().toUpperCase();
        this.name = name.trim();
    }

    String getId() { return id; }
    String getName() { return name; }

    abstract String role();
}


class Student extends Person implements Reportable {
    static final String[] SUBJECTS = {"Java", "Maths", "Physics", "English"};
    static final int COMPONENTS = 2;          
    static final int MAX_PER_COMPONENT = 50;  

    private final int[][] marks = new int[SUBJECTS.length][COMPONENTS]; 
    private final int[] attended = new int[SUBJECTS.length];            
    private final int[] held = new int[SUBJECTS.length];                

    Student(String id, String name) {
        super(id, name);
    }

    @Override
    String role() { return "Student"; }

    void setMarks(int subject, int internal, int external) throws InvalidInputException {
        checkSubject(subject);
        if (internal < 0 || internal > MAX_PER_COMPONENT
                || external < 0 || external > MAX_PER_COMPONENT) {
            throw new InvalidInputException(
                    "Marks must be between 0 and " + MAX_PER_COMPONENT + " for each component.");
        }
        marks[subject][0] = internal;
        marks[subject][1] = external;
    }

    void recordAttendance(int subject, boolean present) throws InvalidInputException {
        checkSubject(subject);
        held[subject]++;
        if (present) attended[subject]++;
    }

    void setAttendance(int subject, int attendedCount, int heldCount) throws InvalidInputException {
        checkSubject(subject);
        if (attendedCount < 0 || heldCount < 0 || attendedCount > heldCount) {
            throw new InvalidInputException("Attended classes cannot exceed classes held.");
        }
        attended[subject] = attendedCount;
        held[subject] = heldCount;
    }

    private void checkSubject(int subject) throws InvalidInputException {
        if (subject < 0 || subject >= SUBJECTS.length) {
            throw new InvalidInputException("Invalid subject number.");
        }
    }

    int subjectTotal(int subject) {
        int sum = 0;
        for (int c = 0; c < COMPONENTS; c++) {
            sum += marks[subject][c];
        }
        return sum;
    }

    
    int totalMarks() {
        return sumFrom(0);
    }

    private int sumFrom(int subject) {
        if (subject == SUBJECTS.length) return 0;           
        return subjectTotal(subject) + sumFrom(subject + 1); 
    }

    double percentage() {
        return totalMarks() * 100.0 / (SUBJECTS.length * 100);
    }

    double attendancePercent() {
        int a = 0, h = 0;
        for (int i = 0; i < SUBJECTS.length; i++) {
            a += attended[i];
            h += held[i];
        }
        return h == 0 ? 0.0 : a * 100.0 / h;
    }

    
    static String gradeFor(double pct) {
        if (pct >= 90) return "O";
        else if (pct >= 80) return "A+";
        else if (pct >= 70) return "A";
        else if (pct >= 60) return "B";
        else if (pct >= 50) return "C";
        else if (pct >= 40) return "D";
        else return "F";
    }

    String grade() { return gradeFor(percentage()); }

    static double gradePoint(String grade) {
        switch (grade) {
            case "O":  return 10;
            case "A+": return 9;
            case "A":  return 8;
            case "B":  return 7;
            case "C":  return 6;
            case "D":  return 5;
            default:   return 0;
        }
    }

    boolean isShortOnAttendance() {
        return attendancePercent() < 75.0;
    }

   
    @Override
    public String report() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=========================================\n");
        sb.append(String.format("%s : %s (%s)%n", role(), name, id));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-10s %8s %8s %8s %10s%n",
                "Subject", "Internal", "External", "Total", "Attend%"));
        for (int i = 0; i < SUBJECTS.length; i++) {
            double ap = held[i] == 0 ? 0 : attended[i] * 100.0 / held[i];
            sb.append(String.format("%-10s %8d %8d %8d %9.1f%%%n",
                    SUBJECTS[i], marks[i][0], marks[i][1], subjectTotal(i), ap));
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("Total: %d/%d  Percentage: %.2f%%  Grade: %s%n",
                totalMarks(), SUBJECTS.length * 100, percentage(), grade()));
        sb.append(String.format("Overall attendance: %.2f%% %s%n", attendancePercent(),
                isShortOnAttendance() ? "(SHORTAGE - below 75%)" : "(OK)"));
        sb.append("=========================================");
        return sb.toString();
    }

    
    String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(',').append(name);
        for (int i = 0; i < SUBJECTS.length; i++) {
            for (int c = 0; c < COMPONENTS; c++) {
                sb.append(',').append(marks[i][c]);
            }
        }
        for (int i = 0; i < SUBJECTS.length; i++) sb.append(',').append(attended[i]);
        for (int i = 0; i < SUBJECTS.length; i++) sb.append(',').append(held[i]);
        return sb.toString();
    }

    static Student fromCsv(String line) throws InvalidInputException {
        String[] p = line.split(",");
        int expected = 2 + SUBJECTS.length * COMPONENTS + 2 * SUBJECTS.length;
        if (p.length != expected) {
            throw new InvalidInputException("Bad record (expected " + expected + " fields): " + line);
        }
        try {
            Student s = new Student(p[0], p[1]);
            int idx = 2;
            for (int i = 0; i < SUBJECTS.length; i++) {
                int in = Integer.parseInt(p[idx++].trim());
                int ex = Integer.parseInt(p[idx++].trim());
                s.setMarks(i, in, ex);
            }
            int[] att = new int[SUBJECTS.length];
            for (int i = 0; i < SUBJECTS.length; i++) att[i] = Integer.parseInt(p[idx++].trim());
            for (int i = 0; i < SUBJECTS.length; i++) {
                s.setAttendance(i, att[i], Integer.parseInt(p[idx++].trim()));
            }
            return s;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Non-numeric value in record: " + line);
        }
    }
}


class Tracker {
    private final List<Student> students = new ArrayList<>();         
    private final Map<String, Student> byId = new HashMap<>();        

    void add(Student s) throws InvalidInputException {
        if (byId.containsKey(s.getId())) {
            throw new InvalidInputException("Student ID already exists: " + s.getId());
        }
        students.add(s);
        byId.put(s.getId(), s);
    }

    Student find(String id) throws InvalidInputException {
        Student s = byId.get(id.trim().toUpperCase());
        if (s == null) throw new InvalidInputException("No student with ID " + id);
        return s;
    }

    boolean isEmpty() { return students.isEmpty(); }

    void listAll() {
        System.out.printf("%-10s %-20s %8s %6s %9s%n", "ID", "Name", "Percent", "Grade", "Attend%");
        for (Student s : students) {
            System.out.printf("%-10s %-20s %8.2f %6s %9.2f%n",
                    s.getId(), s.getName(), s.percentage(), s.grade(), s.attendancePercent());
        }
    }

    
    void analytics() {
        if (students.isEmpty()) {
            System.out.println("No students yet.");
            return;
        }

        double avg = students.stream().mapToDouble(Student::percentage).average().orElse(0);
        System.out.printf("Class average: %.2f%%%n", avg);

        System.out.println("\nTop 3 students:");
        students.stream()
                .sorted(Comparator.comparingDouble(Student::percentage).reversed())
                .limit(3)
                .forEach(s -> System.out.printf("  %s - %.2f%% (%s)%n",
                        s.getName(), s.percentage(), s.grade()));

        System.out.println("\nStudents per grade:");
        Map<String, Long> counts = students.stream()
                .collect(Collectors.groupingBy(Student::grade, TreeMap::new, Collectors.counting()));
        counts.forEach((g, n) -> System.out.println("  " + g + " : " + n));

        long passed = students.stream().filter(s -> !s.grade().equals("F")).count();
        System.out.printf("%nPass rate: %.1f%%%n", passed * 100.0 / students.size());

        System.out.println("\nSubject-wise class average (out of 100):");
        for (int i = 0; i < Student.SUBJECTS.length; i++) {
            final int sub = i;
            double subAvg = students.stream().mapToInt(s -> s.subjectTotal(sub)).average().orElse(0);
            System.out.printf("  %-10s %.2f%n", Student.SUBJECTS[i], subAvg);
        }
    }

    void lowAttendance() {
        List<String> names = students.stream()
                .filter(Student::isShortOnAttendance)
                .sorted(Comparator.comparingDouble(Student::attendancePercent))
                .map(s -> String.format("%s (%s) - %.1f%%", s.getName(), s.getId(), s.attendancePercent()))
                .collect(Collectors.toList());
        if (names.isEmpty()) {
            System.out.println("Nobody is below 75% attendance.");
        } else {
            System.out.println("Students with attendance shortage:");
            names.forEach(n -> System.out.println("  " + n));
        }
    }

    
    void save(String path) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            for (Student s : students) out.println(s.toCsv());
        }
        System.out.println("Saved " + students.size() + " record(s) to " + path);
    }

    void load(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            System.out.println("File not found: " + path);
            return;
        }
        int loaded = 0, skipped = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    add(Student.fromCsv(line));
                    loaded++;
                } catch (InvalidInputException e) {
                    skipped++;
                    System.out.println("Skipped: " + e.getMessage());
                }
            }
        }
        System.out.println("Loaded " + loaded + " record(s), skipped " + skipped + ".");
    }
}


public class Project {
    private static final Scanner sc = new Scanner(System.in);
    private static final String DATA_FILE = "students.csv";

    public static void main(String[] args) {
        Tracker tracker = new Tracker();
        int choice;

        
        do {
            printMenu();
            choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1: addStudent(tracker); break;
                    case 2: enterMarks(tracker); break;
                    case 3: markAttendance(tracker); break;
                    case 4:
                        System.out.println(tracker.find(readLine("Student ID: ")).report());
                        break;
                    case 5: tracker.listAll(); break;
                    case 6: tracker.analytics(); break;
                    case 7: tracker.lowAttendance(); break;
                    case 8: tracker.save(DATA_FILE); break;
                    case 9: tracker.load(DATA_FILE); break;
                    case 0: System.out.println("Goodbye!"); break;
                    default: System.out.println("Invalid option, try again.");
                }
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("File error: " + e.getMessage());
            } finally {
                if (choice != 0) System.out.println();
            }
        } while (choice != 0);
    }

    static void printMenu() {
        System.out.println("===== University Grade & Attendance Tracker =====");
        System.out.println("1. Add student");
        System.out.println("2. Enter marks");
        System.out.println("3. Mark attendance");
        System.out.println("4. View student report");
        System.out.println("5. List all students");
        System.out.println("6. Class analytics");
        System.out.println("7. Attendance shortage list");
        System.out.println("8. Save to file");
        System.out.println("9. Load from file");
        System.out.println("0. Exit");
    }

    static void addStudent(Tracker t) throws InvalidInputException {
        String id = readLine("Student ID: ");
        String name = readLine("Name: ");
        if (id.isEmpty() || name.isEmpty()) {
            throw new InvalidInputException("ID and name cannot be empty.");
        }
        t.add(new Student(id, name));
        System.out.println("Student added.");
    }

    static void enterMarks(Tracker t) throws InvalidInputException {
        Student s = t.find(readLine("Student ID: "));
        
        for (int i = 0; i < Student.SUBJECTS.length; i++) {
            System.out.println("Subject: " + Student.SUBJECTS[i]);
            int in = readInt("  Internal (0-" + Student.MAX_PER_COMPONENT + "): ");
            int ex = readInt("  External (0-" + Student.MAX_PER_COMPONENT + "): ");
            s.setMarks(i, in, ex);
        }
        System.out.println("Marks saved. Grade: " + s.grade());
    }

    static void markAttendance(Tracker t) throws InvalidInputException {
        Student s = t.find(readLine("Student ID: "));
        for (int i = 0; i < Student.SUBJECTS.length; i++) {
            System.out.println((i + 1) + ". " + Student.SUBJECTS[i]);
        }
        int sub = readInt("Subject number: ") - 1;
        String ans = readLine("Present? (y/n): ").toLowerCase();
        s.recordAttendance(sub, ans.startsWith("y"));
        System.out.printf("Overall attendance now %.2f%%%n", s.attendancePercent());
    }


    static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}