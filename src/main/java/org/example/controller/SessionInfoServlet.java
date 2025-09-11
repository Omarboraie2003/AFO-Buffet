package org.example.controller;

import com.google.gson.JsonObject;
import org.example.model.user.UserDAO;
import org.example.model.user.UserModel;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/sessionInfo")
public class SessionInfoServlet extends HttpServlet {

    private static final int TEN_MONTHS_IN_SECONDS = 10 * 30 * 24 * 60 * 60; // 10 months in seconds
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            HttpSession session = request.getSession(false); // don't create new session
            JsonObject json = new JsonObject();

            // First check session
            if (session != null && Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
                // Session exists and is valid
                json.addProperty("isLoggedIn", true);
                json.addProperty("userId", (Integer) session.getAttribute("userId"));
                json.addProperty("username", (String) session.getAttribute("username"));
                json.addProperty("accessLevel", (String) session.getAttribute("accessLevel"));
                json.addProperty("sessionId", session.getId());

                // Get user details from database to include names
                addUserNamesToJson(json, (Integer) session.getAttribute("userId"));

                // Update cookies to extend their life
                updateCookies(response, session);

            } else {
                // No valid session, check cookies
                UserInfo userInfo = getUserInfoFromCookies(request);

                if (userInfo != null) {
                    // Valid cookies found, restore session
                    session = request.getSession(true); // create new session
                    session.setAttribute("isLoggedIn", true);
                    session.setAttribute("userId", userInfo.userId);
                    session.setAttribute("username", userInfo.username);
                    session.setAttribute("accessLevel", userInfo.accessLevel);

                    // Set session timeout to 10 months (in seconds)
                    session.setMaxInactiveInterval(TEN_MONTHS_IN_SECONDS);

                    json.addProperty("isLoggedIn", true);
                    json.addProperty("userId", userInfo.userId);
                    json.addProperty("username", userInfo.username);
                    json.addProperty("accessLevel", userInfo.accessLevel);
                    json.addProperty("sessionId", session.getId());

                    // Get user details from database to include names
                    addUserNamesToJson(json, userInfo.userId);

                    System.out.println("[DEBUG][SessionInfoServlet] Session restored from cookies for user: " + userInfo.username);
                } else {
                    // No valid session or cookies
                    json.addProperty("isLoggedIn", false);
                }
            }

            out.print(json.toString());
            System.out.println("[DEBUG][SessionInfoServlet] Sent session info: " + json);

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            out.print(error.toString());
        } finally {
            out.close();
        }
    }

    /**
     * Add user's first name and last name to JSON response
     */
    private void addUserNamesToJson(JsonObject json, int userId) {
        try {
            UserModel user = userDAO.getUserById(userId);
            if (user != null) {
                String firstName = user.getFirst_name();
                String lastName = user.getLast_name();

                if (firstName != null) {
                    json.addProperty("firstName", firstName);
                }
                if (lastName != null) {
                    json.addProperty("lastName", lastName);
                }

                System.out.println("[DEBUG][SessionInfoServlet] Added names - First: " + firstName + ", Last: " + lastName);
            } else {
                System.out.println("[DEBUG][SessionInfoServlet] User not found for ID: " + userId);
            }
        } catch (Exception e) {
            System.err.println("[ERROR][SessionInfoServlet] Failed to get user names: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Store user session data in cookies for 10 months
     */
    private void updateCookies(HttpServletResponse response, HttpSession session) {
        try {
            // Create cookies for user data
            Cookie userIdCookie = new Cookie("userId", session.getAttribute("userId").toString());
            Cookie usernameCookie = new Cookie("username", (String) session.getAttribute("username"));
            Cookie accessLevelCookie = new Cookie("accessLevel", (String) session.getAttribute("accessLevel"));
            Cookie loginStatusCookie = new Cookie("isLoggedIn", "true");

            // Set cookie properties
            Cookie[] cookies = {userIdCookie, usernameCookie, accessLevelCookie, loginStatusCookie};

            for (Cookie cookie : cookies) {
                cookie.setMaxAge(TEN_MONTHS_IN_SECONDS); // 10 months
                cookie.setPath("/"); // Available for entire application
                cookie.setHttpOnly(true); // Security: prevent XSS attacks
                cookie.setSecure(false); // Set to true in production with HTTPS
                response.addCookie(cookie);
            }

            System.out.println("[DEBUG][SessionInfoServlet] Cookies updated for 10 months");

        } catch (Exception e) {
            System.err.println("[ERROR][SessionInfoServlet] Failed to update cookies: " + e.getMessage());
        }
    }

    /**
     * Retrieve user information from cookies
     */
    private UserInfo getUserInfoFromCookies(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) return null;

            String userId = null;
            String username = null;
            String accessLevel = null;
            String isLoggedIn = null;

            // Extract values from cookies
            for (Cookie cookie : cookies) {
                switch (cookie.getName()) {
                    case "userId":
                        userId = cookie.getValue();
                        break;
                    case "username":
                        username = cookie.getValue();
                        break;
                    case "accessLevel":
                        accessLevel = cookie.getValue();
                        break;
                    case "isLoggedIn":
                        isLoggedIn = cookie.getValue();
                        break;
                }
            }

            // Validate that all required cookies exist
            if ("true".equals(isLoggedIn) && userId != null && username != null && accessLevel != null) {
                return new UserInfo(Integer.parseInt(userId), username, accessLevel);
            }

            return null;

        } catch (Exception e) {
            System.err.println("[ERROR][SessionInfoServlet] Failed to read cookies: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper class to hold user information
     */
    private static class UserInfo {
        final int userId;
        final String username;
        final String accessLevel;

        UserInfo(int userId, String username, String accessLevel) {
            this.userId = userId;
            this.username = username;
            this.accessLevel = accessLevel;
        }
    }
}