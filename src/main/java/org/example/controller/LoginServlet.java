package org.example.controller;

import org.example.model.user.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                // Send error message as text response
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print("Username or password missing");
                out.close();
            } else {
                // Get access level (returns null if user invalid)
                String accessLevel = userDAO.validateUser(username, password);

                if (accessLevel != null) {
                    // Successful login - redirect based on access level
                    // Don't set content type when redirecting
                    if ("chef".equalsIgnoreCase(accessLevel)) {
                        response.sendRedirect("chefWelcomePage.html");
                    } else {
                        // User is an employee
                        response.sendRedirect("employeeHomePage.html");
                    }
                } else {
                    // Send error message as text response
                    response.setContentType("text/plain;charset=UTF-8");
                    PrintWriter out = response.getWriter();
                    out.print("Access denied");
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Send error message as text response
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("Error: " + e.getMessage());
            out.close();
        }
    }
}