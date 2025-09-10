package org.example.controller;

import org.example.model.user.UserDAO;
import org.example.model.user.UserModel;
import org.example.util.PasswordUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.regex.Pattern;


@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;

    // Constants
    private static final String CONTENT_TYPE_PLAIN = "text/plain";
    private static final String REDIRECT_PAGE = "login.html";
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_PASSWORD_LENGTH = 128;

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // Common weak passwords
    private static final String[] COMMON_PASSWORDS = {
            "password", "123456", "123456789", "qwerty", "abc123",
            "password123", "admin", "letmein", "welcome", "monkey"
    };

    // Error messages
    private static final String ERROR_EMAIL_REQUIRED = "Email is required";
    private static final String ERROR_PASSWORD_REQUIRED = "Password is required";
    private static final String ERROR_CONFIRM_PASSWORD_REQUIRED = "Password confirmation is required";
    private static final String ERROR_EMAIL_TOO_LONG = "Email address is too long";
    private static final String ERROR_PASSWORD_TOO_LONG = "Password is too long";
    private static final String ERROR_INVALID_EMAIL = "Invalid email format";
    private static final String ERROR_WEAK_PASSWORD = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)";
    private static final String ERROR_PASSWORDS_MISMATCH = "Passwords do not match";
    private static final String ERROR_COMMON_PASSWORD = "Password is too common. Please choose a more secure password.";
    private static final String ERROR_PERSONAL_INFO = "Password cannot contain your email address or username.";
    private static final String ERROR_CONTACT_ADMIN = "You cannot register. Please contact admin.";
    private static final String ERROR_ALREADY_REGISTERED = "You are already registered. Please log in.";
    private static final String ERROR_REGISTRATION_FAILED = "Registration failed. Try again.";

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType(CONTENT_TYPE_PLAIN);
        PrintWriter out = response.getWriter();

        try {
            RegistrationRequest registrationRequest = extractRegistrationData(request);

            // Debug logging
            System.out.println("Registration attempt:");
            System.out.println("Username: '" + registrationRequest.username + "'");
            System.out.println("Password length: " + (registrationRequest.password != null ? registrationRequest.password.length() : "null"));
            System.out.println("Confirm password length: " + (registrationRequest.confirmPassword != null ? registrationRequest.confirmPassword.length() : "null"));

            String validationError = validateRegistrationRequest(registrationRequest);
            if (validationError != null) {
                System.out.println("Validation error: " + validationError);
                out.print(validationError);
                return;
            }

            UserModel existingUser = userDAO.findUserByEmail(registrationRequest.username);

            String businessLogicError = validateBusinessLogic(existingUser);
            if (businessLogicError != null) {
                System.out.println("Business logic error: " + businessLogicError);
                out.print(businessLogicError);
                return;
            }

            String hashedPassword = PasswordUtils.hashPassword(registrationRequest.password);
            existingUser.setPasswordHash(hashedPassword);
            existingUser.setRegister(true);            // Mark as registered

            boolean success = userDAO.registerUser(existingUser);
            handleRegistrationResult(success, response, out);

        } catch (Exception e) {
            handleException(e, out);
        } finally {
            out.close();
        }
    }

    private RegistrationRequest extractRegistrationData(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Trim whitespace from parameters
        username = username != null ? username.trim() : null;

        return new RegistrationRequest(username, password, confirmPassword);
    }

    private String validateRegistrationRequest(RegistrationRequest request) {
        String basicValidationError = validateBasicInput(request);
        if (basicValidationError != null) {
            return basicValidationError;
        }

        if (!isValidEmail(request.username)) {
            return ERROR_INVALID_EMAIL;
        }

        if (!isValidPassword(request.password)) {
            return ERROR_WEAK_PASSWORD;
        }

        if (!request.password.equals(request.confirmPassword)) {
            return ERROR_PASSWORDS_MISMATCH;
        }

        if (isPasswordTooCommon(request.password)) {
            return ERROR_COMMON_PASSWORD;
        }

        if (containsPersonalInfo(request.password, request.username)) {
            return ERROR_PERSONAL_INFO;
        }

        return null;
    }

    private String validateBasicInput(RegistrationRequest request) {
        if (isNullOrEmpty(request.username)) {
            return ERROR_EMAIL_REQUIRED;
        }

        if (isNullOrEmpty(request.password)) {
            return ERROR_PASSWORD_REQUIRED;
        }

        if (isNullOrEmpty(request.confirmPassword)) {
            return ERROR_CONFIRM_PASSWORD_REQUIRED;
        }

        if (request.username.length() > MAX_EMAIL_LENGTH) {
            return ERROR_EMAIL_TOO_LONG;
        }

        if (request.password.length() > MAX_PASSWORD_LENGTH) {
            return ERROR_PASSWORD_TOO_LONG;
        }

        return null;
    }

    private String validateBusinessLogic(UserModel existingUser) {

        if (existingUser == null  || !existingUser.isActive()) {
            return ERROR_CONTACT_ADMIN;
        }


        if (existingUser.isIs_registered()) {
            return ERROR_ALREADY_REGISTERED;
        }



        return null;
    }

    private void handleRegistrationResult(boolean success, HttpServletResponse response, PrintWriter out)
            throws IOException {
        if (success) {
            System.out.println("Registration successful, redirecting to login page");
            response.sendRedirect(REDIRECT_PAGE);
        } else {
            System.out.println("Registration failed");
            out.print(ERROR_REGISTRATION_FAILED);
        }
    }

    private void handleException(Exception e, PrintWriter out) {
        System.out.println("Exception occurred: " + e.getMessage());
        e.printStackTrace();
        out.print("Error: " + e.getMessage());
    }

    private boolean isNullOrEmpty(String str) {
        boolean result = str == null || str.trim().isEmpty();
        System.out.println("isNullOrEmpty check for '" + str + "': " + result);
        return result;
    }

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        boolean result = EMAIL_PATTERN.matcher(email.trim()).matches();
        System.out.println("Email validation for '" + email + "': " + result);
        return result;
    }

    private boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        boolean result = PASSWORD_PATTERN.matcher(password).matches();
        System.out.println("Password validation result: " + result);
        return result;
    }

    private boolean isPasswordTooCommon(String password) {
        if (password == null) {
            return false;
        }

        String lowerPassword = password.toLowerCase();
        for (String common : COMMON_PASSWORDS) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPersonalInfo(String password, String email) {
        if (password == null || email == null) {
            return false;
        }

        String lowerPassword = password.toLowerCase();
        String lowerEmail = email.toLowerCase();

        if (lowerPassword.contains(lowerEmail)) {
            return true;
        }

        String username = email.substring(0, email.indexOf('@'));
        return username.length() >= 3 && lowerPassword.contains(username.toLowerCase());
    }

    private static class RegistrationRequest {
        final String username;
        final String password;
        final String confirmPassword;

        RegistrationRequest(String username, String password, String confirmPassword) {
            this.username = username;
            this.password = password;
            this.confirmPassword = confirmPassword;
        }
    }
}
