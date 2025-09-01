package org.example.controller;

import org.example.model.user.UserDAO;
import org.example.model.user.UserModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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

                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("accessLevel", user.getAccessLevel());
                session.setAttribute("isLoggedIn", true);
                session.setMaxInactiveInterval(1800);

                System.out.println("[DEBUG][LoginServlet] Session created for user: " + username +
                        ", Role: " + user.getAccessLevel() +
                        ", SessionId: " + session.getId());

                if ("chef".equalsIgnoreCase(user.getAccessLevel())) {
                    response.sendRedirect("chefWelcomePage.html");
                } else {
                    response.sendRedirect("employeeHomePage.html");
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
}
