package org.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


public class AuthorizationFilter implements Filter {

    /**
     * Get the appropriate redirect URL based on user role
     */
    private String getRedirectUrlForRole(String userRole, String contextPath) {
        if ("chef".equalsIgnoreCase(userRole)) {
            return contextPath + "/chefWelcomePage.html";
        } else if ("employee".equalsIgnoreCase(userRole)) {
            return contextPath + "/EmployeeMenu.html";
        } else {
            // Default redirect for unknown roles
            return contextPath + "/login.html";
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[DEBUG] AuthorizationFilter: Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        System.out.println("[DEBUG] AuthorizationFilter: Checking role-based access to: " + path);

        // Skip authorization for public resources (these are handled by AuthenticationFilter)
        if (path.equals("/login.html") ||
                path.equals("/register.html") ||
                path.startsWith("/login") ||
                path.startsWith("/register") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.equals("/") ||
                path.startsWith("/logout")) {

            System.out.println("[DEBUG] AuthorizationFilter: Skipping authorization for public resource: " + path);
            chain.doFilter(request, response);
            return;
        }

        // Get user session and role
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            System.out.println("[DEBUG] AuthorizationFilter: No session found, this should have been caught by AuthenticationFilter");
            httpResponse.sendRedirect(contextPath + "/login.html");
            return;
        }

        String userRole = (String) session.getAttribute("accessLevel");
        String username = (String) session.getAttribute("username");

        System.out.println("[DEBUG] AuthorizationFilter: User: " + username + ", Role: " + userRole + ", Accessing: " + path);

        // Define role-based access rules
        boolean hasAccess = checkRoleAccess(path, userRole);

        if (!hasAccess) {
            System.out.println("[DEBUG] AuthorizationFilter: Access DENIED for user: " + username +
                    " (role: " + userRole + ") to path: " + path);

            // Redirect to appropriate page based on user role
            String redirectUrl = getRedirectUrlForRole(userRole, contextPath);
            System.out.println("[DEBUG] AuthorizationFilter: Redirecting to: " + redirectUrl);
            httpResponse.sendRedirect(redirectUrl);
            return;
        }

        System.out.println("[DEBUG] AuthorizationFilter: Access GRANTED for user: " + username +
                " (role: " + userRole + ") to path: " + path);
        chain.doFilter(request, response);
    }

    /**
     * Check if a user with given role can access the specified path
     */
    private boolean checkRoleAccess(String path, String userRole) {
        System.out.println("[DEBUG] AuthorizationFilter: Checking role access - Path: " + path + ", Role: " + userRole);

        if (userRole == null) {
            System.out.println("[DEBUG] AuthorizationFilter: User role is null, denying access");
            return false;
        }

        // Chef-only pages
        if (path.equals("/chefWelcomePage.html") ||
                path.startsWith("/chefManageItemsPage") ||
                path.equals("/chefManageItemsPage.html") ||
                path.startsWith("/accountManagement") ||
                path.equals("/accountManagement.html")) {

            boolean isChef = "chef".equalsIgnoreCase(userRole);
            System.out.println("[DEBUG] AuthorizationFilter: Chef-only page detected. User is chef: " + isChef);
            return isChef;
        }

        // Employee-only pages
        if (path.equals("/EmployeeMenu.html")) {
            boolean isEmployee = "employee".equalsIgnoreCase(userRole);
            System.out.println("[DEBUG] AuthorizationFilter: Employee-only page detected. User is employee: " + isEmployee);
            return isEmployee;
        }

        // Pages accessible to both roles (add more as needed)
        if (path.startsWith("/common") ||
                path.startsWith("/shared") ||
                path.equals("/profile.html") ||
                path.equals("/settings.html")) {

            System.out.println("[DEBUG] AuthorizationFilter: Common page detected, allowing access");
            return true;
        }

        // For any other pages, allow access by default but log it
        System.out.println("[DEBUG] AuthorizationFilter: Unclassified page, allowing access by default: " + path);
        return true;
    }

    @Override
    public void destroy() {
        System.out.println("[DEBUG] AuthorizationFilter: Filter destroyed");
    }
}