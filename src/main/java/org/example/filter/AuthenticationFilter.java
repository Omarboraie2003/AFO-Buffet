package org.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*") // Apply filter to all requests
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[DEBUG][AuthenticationFilter] Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        System.out.println("[DEBUG][AuthenticationFilter] Checking access to: " + path + " (Method: " + httpRequest.getMethod() + ")");

        // ✅ Allow public resources without session
        if (path.equals("/login.html") ||
                path.equals("/register.html") ||
                path.startsWith("/login") ||
                path.startsWith("/register") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/Images/") || // Add this for your image folder
                path.equals("/")) {

            System.out.println("[DEBUG][AuthenticationFilter] Public resource, allowing: " + path);
            chain.doFilter(request, response);
            return;
        }

        // ✅ Block caching of ALL protected content
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1+
        httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0
        httpResponse.setDateHeader("Expires", 0); // Proxies

        // ✅ Check session
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = false;
        String userRole = null;
        String username = null;

        if (session != null) {
            Boolean sessionLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            userRole = (String) session.getAttribute("accessLevel");
            username = (String) session.getAttribute("username");
            isLoggedIn = sessionLoggedIn != null && sessionLoggedIn;

            System.out.println("[DEBUG][AuthenticationFilter] Session found: User=" + username +
                    ", Role=" + userRole + ", LoggedIn=" + isLoggedIn);
        } else {
            System.out.println("[DEBUG][AuthenticationFilter] No session found");
        }

        // ✅ If not logged in → handle based on request type
        if (!isLoggedIn) {
            System.out.println("[DEBUG][AuthenticationFilter] User not authenticated");

            // For API requests (AJAX/fetch), return JSON error instead of redirect
            if (isApiRequest(httpRequest)) {
                System.out.println("[DEBUG][AuthenticationFilter] API request - returning JSON error");
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"success\": false, \"message\": \"Session expired. Please login again.\"}");
                return;
            } else {
                // For page requests, redirect to login
                System.out.println("[DEBUG][AuthenticationFilter] Page request - redirecting to login");
                httpResponse.sendRedirect(contextPath + "/login.html");
                return;
            }
        }

        // ✅ Role-based access control for pages
        if (path.startsWith("/chef") && !"chef".equalsIgnoreCase(userRole)) {
            System.out.println("[DEBUG][AuthenticationFilter] Access denied: Non-chef trying to access chef page");
            httpResponse.sendRedirect(contextPath + "/unauthorized.html");
            return;
        }

        if (path.startsWith("/employee") && !"employee".equalsIgnoreCase(userRole)) {
            System.out.println("[DEBUG][AuthenticationFilter] Access denied: Non-employee trying to access employee page");
            httpResponse.sendRedirect(contextPath + "/unauthorized.html");
            return;
        }

        // ✅ Role-based access control for API endpoints
        if (path.startsWith("/menu-items") && !"chef".equalsIgnoreCase(userRole) && !"employee".equalsIgnoreCase(userRole)) {
            System.out.println("[DEBUG][AuthenticationFilter] Access denied: User role '" + userRole + "' cannot access menu API");
            if (isApiRequest(httpRequest)) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"success\": false, \"message\": \"Access denied. Insufficient permissions.\"}");
                return;
            } else {
                httpResponse.sendRedirect(contextPath + "/unauthorized.html");
                return;
            }
        }

        System.out.println("[DEBUG][AuthenticationFilter] Access granted for " + username + " to " + path);

        // ✅ Continue filter chain
        chain.doFilter(request, response);
    }

    /**
     * Determines if this is an API request (AJAX/fetch) vs a page request
     */
    private boolean isApiRequest(HttpServletRequest request) {
        // Check for AJAX indicators
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        String contentType = request.getHeader("Content-Type");

        // Common AJAX indicators
        if ("XMLHttpRequest".equals(requestedWith)) {
            return true;
        }

        // Check if requesting JSON
        if (accept != null && accept.contains("application/json")) {
            return true;
        }

        // Check content type for POST/PUT requests
        if (contentType != null &&
                (contentType.contains("application/json") ||
                        contentType.contains("application/x-www-form-urlencoded") ||
                        contentType.contains("multipart/form-data"))) {
            return true;
        }

        // Check if it's an API endpoint path
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/menu-items") ||
                path.startsWith("/manage_cart") ||
                path.startsWith("/orders") ||
                path.startsWith("/events")) {
            return true;
        }

        return false;
    }

    @Override
    public void destroy() {
        System.out.println("[DEBUG][AuthenticationFilter] Filter destroyed");
    }
}