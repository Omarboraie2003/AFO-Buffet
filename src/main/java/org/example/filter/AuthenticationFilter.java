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

        System.out.println("[DEBUG][AuthenticationFilter] Checking access to: " + path);

        // ✅ Allow public resources without session
        if (path.equals("/login.html") ||
                path.equals("/register.html") ||
                path.startsWith("/login") ||
                path.startsWith("/register") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
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

        // ✅ If not logged in → redirect to login
        if (!isLoggedIn) {
            System.out.println("[DEBUG][AuthenticationFilter] User not authenticated → redirecting to login");
            httpResponse.sendRedirect(contextPath + "/login.html");
            return;
        }

        // ✅ Role-based access control
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

        System.out.println("[DEBUG][AuthenticationFilter] Access granted for " + username + " to " + path);

        // ✅ Continue filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("[DEBUG][AuthenticationFilter] Filter destroyed");
    }
}
