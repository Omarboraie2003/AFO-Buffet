import org.example.model.user.UserDAO;
import org.example.model.user.UserModel;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;


@WebServlet({"/account-management", "/account-management/*"})
public class AccountManagementServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String statusFilter = request.getParameter("status");
            List<UserModel> users;

            if ("active".equals(statusFilter)) {
                users = userDAO.getActiveUsers();
            } else if ("inactive".equals(statusFilter)) {
                users = userDAO.getInactiveUsers();
            } else {
                users = userDAO.getAllUsers(); // default for "all" or null
            }

            String jsonResponse = gson.toJson(users);

            PrintWriter out = response.getWriter();
            out.print(jsonResponse);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error loading users");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            switch (action) {
                case "add":
                    handleAddUser(request, response);
                    break;
                case "delete":
                    handleDeleteUser(request, response);
                    break;
                case "toggle":
                    toggleUserActivity(request, response);
                    break;
                case "bulkDelete":
                    handleBulkDelete(request, response);
                    break;
                case "bulkToggle":
                    handleBulkToggle(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Invalid action");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Server error: " + e.getMessage());
        }
    }

    private void handleAddUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String email = request.getParameter("email");
        String role = request.getParameter("role");

        if (email == null || email.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Email is required");
            return;
        }

        if (role == null || (!role.equals("chef") && !role.equals("employee"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid role. Must be 'chef' or 'employee'");
            return;
        }

        // Check if email already exists
        if (userDAO.emailExists(email)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("User with this email already exists");
            return;
        }

        boolean success = userDAO.addNewUser(email, role);

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("User added successfully");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to add user");
        }
    }

    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userIdStr = request.getParameter("userId");

        if (userIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("User ID is required");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr);
            boolean success = userDAO.deleteUser(userId);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("User deleted successfully");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to delete user");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid user ID");
        }
    }

    private void toggleUserActivity(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userIdStr = request.getParameter("userId");

        if (userIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("User ID is required");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr);

            // Get current status and toggle it
            boolean currentStatus = userDAO.isUserActive(userId);
            boolean newStatus = !currentStatus;

            boolean success = userDAO.updateUserActivityStatus(userId, newStatus);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("User status updated successfully");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to update user status");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid user ID");
        }
    }

    private void handleBulkDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userIdsStr = request.getParameter("userIds");

        if (userIdsStr == null || userIdsStr.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("User IDs are required");
            return;
        }

        try {
            List<Integer> userIds = new ArrayList<>();
            String[] idStrings = userIdsStr.split(",");

            for (String idStr : idStrings) {
                userIds.add(Integer.parseInt(idStr.trim()));
            }

            boolean success = userDAO.bulkDeleteUsers(userIds);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Users deleted successfully");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to delete users");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid user IDs");
        }
    }

    private void handleBulkToggle(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userIdsStr = request.getParameter("userIds");

        if (userIdsStr == null || userIdsStr.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("User IDs are required");
            return;
        }

        try {
            List<Integer> userIds = new ArrayList<>();
            String[] idStrings = userIdsStr.split(",");

            for (String idStr : idStrings) {
                userIds.add(Integer.parseInt(idStr.trim()));
            }

            boolean success = userDAO.bulkToggleUserStatus(userIds);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("User status updated successfully");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to update user status");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid user IDs");
        }
    }
}
