package org.example.util;

import org.mindrot.jbcrypt.BCrypt;
import java.util.Scanner;

public class PasswordUtils {

    // Hash a password using BCrypt
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    // Verify a password against a hashed password
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String username = "testUser";
        String password = "testPassword";

        // Hash the password
        String hashedPassword = hashPassword(password);
        System.out.println("Stored hashed password: " + hashedPassword);

        System.out.print("Enter password for " + username + ": ");
        String inputPassword = scanner.nextLine();

        // Verify the password
        if (verifyPassword(inputPassword, hashedPassword)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }

        scanner.close();
    }
}
