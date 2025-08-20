package org.example.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        response.setContentType("text/plain");

        // For now, just echo back (later: insert into database)
        if (username != null && password != null && email != null) {
            response.getWriter().write(
                    "Registration successful!\nUsername: " + username +
                            "\nEmail: " + email
            );
        } else {
            response.getWriter().write("Please fill in all fields.");
        }
    }

    // Allow GET so you can test in browser
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.getWriter().write("Send a POST request to /register to register.");
    }
}