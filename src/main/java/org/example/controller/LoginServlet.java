package org.example.controller;

import org.example.model.user.UserDAO;
import org.example.model.user.UserModel;
import org.example.model.Order.OrderDAO; // Add this import
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;

public class LoginServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    // 10 months in seconds
    private static final int TEN_MONTHS_IN_SECONDS = 10 * 30 * 24 * 60 * 60;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print("Username or password missing");
                out.close();
                return;
            }

            // First get user to check if they exist and are active
            UserModel user = userDAO.getUserByUsername(username);

            if (user == null) {
                System.out.println("[DEBUG][LoginServlet] User not found: " + username);
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print("Username not found or wrong password");
                out.close();
                return;
            }

            // Check if user is active BEFORE validating password
            if (!user.isActive()) {
                System.out.println("[DEBUG][LoginServlet] User account inactive: " + username);
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print("You cannot login, please contact the admin");
                out.close();
                return;
            }

            // Now validate password
            String accessLevel = userDAO.validateUser(username, password);

            if (accessLevel != null && !accessLevel.isEmpty()) {
                HttpSession session = request.getSession(true);

                // 🔹 Regenerate session ID for security
                request.changeSessionId();

                // Set session attributes
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("accessLevel", user.getAccessLevel());
                session.setAttribute("isLoggedIn", true);

                // Set session timeout to 10 months (in seconds)
                session.setMaxInactiveInterval(TEN_MONTHS_IN_SECONDS);

                // 🍪 Create persistent cookies for 10 months
                createUserCookies(response, user.getUserId(), user.getUsername(), user.getAccessLevel());

                // 🧹 CLEANUP: If user is a chef, trigger cleanup of old completed orders
                if ("chef".equalsIgnoreCase(user.getAccessLevel())) {
                    try {
                        int cleanedCount = OrderDAO.cleanupOldCompletedOrders();
                        System.out.println("[DEBUG][LoginServlet] Chef login triggered cleanup of " + cleanedCount + " old completed orders for user: " + username);
                    } catch (Exception cleanupError) {
                        System.err.println("[ERROR][LoginServlet] Failed to cleanup old orders on chef login: " + cleanupError.getMessage());
                        cleanupError.printStackTrace();
                        // Don't fail the login process if cleanup fails
                    }
                }

                System.out.println("[DEBUG][LoginServlet] Session created for user: " + username +
                        ", Role: " + user.getAccessLevel() +
                        ", SessionId: " + session.getId() +
                        ", Cookies created for 10 months");

                if ("chef".equalsIgnoreCase(user.getAccessLevel())) {
                    response.sendRedirect("chefWelcomePage.html");
                } else {
                    response.sendRedirect("EmployeeMenu.html");
                }
            } else {
                System.out.println("[DEBUG][LoginServlet] Invalid password for username: " + username);
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print("Incorrect password");
                out.close();
            }

        } catch (Exception e) {
            System.out.println("[DEBUG][LoginServlet] Login error for username: " + username + ", Error: " + e.getMessage());
            e.printStackTrace();
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("Error: " + e.getMessage());
            out.close();
        }
    }

    /**
     * Create persistent cookies when user logs in successfully
     * These cookies will last for 10 months and allow session restoration
     */
    private void createUserCookies(HttpServletResponse response, int userId, String username, String accessLevel) {
        try {
            // Create cookies for user data
            Cookie userIdCookie = new Cookie("userId", String.valueOf(userId));
            Cookie usernameCookie = new Cookie("username", username);
            Cookie accessLevelCookie = new Cookie("accessLevel", accessLevel);
            Cookie loginStatusCookie = new Cookie("isLoggedIn", "true");

            // Set cookie properties - must match SessionInfoServlet settings
            Cookie[] cookies = {userIdCookie, usernameCookie, accessLevelCookie, loginStatusCookie};

            for (Cookie cookie : cookies) {
                cookie.setMaxAge(TEN_MONTHS_IN_SECONDS); // 10 months persistence
                cookie.setPath("/"); // Available for entire application
                cookie.setHttpOnly(true); // Security: prevent XSS attacks
                cookie.setSecure(false); // Set to true in production with HTTPS
                response.addCookie(cookie);
            }

            System.out.println("[DEBUG][LoginServlet] Created persistent cookies for user: " + username + " (10 months)");

        } catch (Exception e) {
            System.err.println("[ERROR][LoginServlet] Failed to create cookies: " + e.getMessage());
            e.printStackTrace();
        }
    }
}