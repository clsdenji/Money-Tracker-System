import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Registration extends User {
    public Registration(String username, String password) {
        super(username, password);
    }

    public static void registerUser(Scanner scanner) {
        System.out.println("Enter your Name: ");
        String name = scanner.nextLine();
        String username;
        do {
            System.out.println("Enter username: ");
            username = scanner.nextLine();

            if (userExists(username))
                System.out.println("Username already exists. Please create another.");
        } while (userExists(username));

        System.out.println("Enter password: ");
        String password = scanner.nextLine();

        String confirmPassword;
        do {
            System.out.println("Confirm your password: ");
            confirmPassword = scanner.nextLine();

            if (!password.equals(confirmPassword)) {
                System.out.println("Password does not match. Try again.");
            } else {
                System.out.println("Password matched.");
            }
        } while (!password.equals(confirmPassword));

        String filePath = "C:\\EXPMNG\\user.txt";
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(name + "," + username + "," + password + "\n");
            System.out.println("Credentials have been recorded. You can now try logging in.");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    private static boolean userExists(String username) {
        String filePath = "C:\\EXPMNG\\user.txt";
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[1].equals(username)) {
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error reading from file: " + ex.getMessage());
        }
        return false;
    }
}
