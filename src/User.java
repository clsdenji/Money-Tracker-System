import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class User {
    private final String username;
    private final String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static boolean userLogin(String username_login, String password_login) {
        try (Scanner fileScanner = new Scanner(new File("C:\\EXPMNG\\user.txt"))) { //change to your desired path
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[1].equals(username_login) && parts[2].equals(password_login)) {
                    System.out.println("Successfully logged in.");
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error reading from file: " + ex.getMessage());
        }
        System.out.println("Login failed. Please try again.");
        return false;
    }
}
