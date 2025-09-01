package org.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }

    // ← ADD POST support for AJAX calls
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            String username = (String) session.getAttribute("username");
            String role = (String) session.getAttribute("accessLevel");

            System.out.println("[DEBUG][LogoutServlet] Logging out user: " + username + " (Role: " + role + ")");

            // 🔑 Destroy session completely
            session.invalidate();
        } else {
            System.out.println("[DEBUG][LogoutServlet] No active session found at logout");
        }

        // 🔑 Clear JSESSIONID cookie to prevent session reuse
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setMaxAge(0);
                    cookie.setPath(request.getContextPath());
                    cookie.setSecure(request.isSecure()); // Set secure if HTTPS
                    response.addCookie(cookie);
                    System.out.println("[DEBUG][LogoutServlet] JSESSIONID cookie cleared");
                    break;
                }
            }
        }

        // 🔑 Enhanced security headers
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");

        System.out.println("[DEBUG][LogoutServlet] Logout completed, redirecting to login");

        // For AJAX requests, return JSON response instead of redirect
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith) ||
                request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json")) {

            response.setContentType("application/json");
            response.getWriter().write("{\"success\": true, \"redirect\": \"login.html\"}");
        } else {
            // Regular redirect for direct servlet access
            response.sendRedirect(request.getContextPath() + "/login.html");
        }
    }
}
