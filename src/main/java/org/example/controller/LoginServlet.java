package org.example.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")   // mapped to /login
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Get form parameters
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        response.setContentType("text/plain");

        // Simple check (later replace with database query)
        if ("admin".equals(username) && "secret".equals(password)) {
            response.getWriter().write("Login successful! Welcome " + username);
        } else {
            response.getWriter().write("Invalid username or password.");
        }
    }

    // Optional: allow GET so you can test in the browser directly
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.getWriter().write("Send a POST request to /login to log in.");
    }
}