package org.example.model.user;
import org.example.util.DBConnection;
import java.sql.*;
import org.example.util.PasswordUtils;
import java.util.List;
import java.util.ArrayList;

public class UserDAO {

    // --- Validate user for login ---
    public String validateUser(String username, String password) {
        String sql = "SELECT password_hash, access_level FROM Users WHERE username = ? AND register = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                // Use PasswordUtils to verify the plain text password against stored hash
                if (PasswordUtils.verifyPassword(password, storedHash)) {
                    return rs.getString("access_level");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if user not found or error
    }

    // --- Find user by email ---
    public UserModel findUserByEmail(String email) {
        String sql = "SELECT username, password_hash, access_level, register FROM Users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserModel(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("access_level"),
                        rs.getBoolean("register")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Register user (update existing record) --- (admin only)
    public boolean registerUser(String email, String passwordHash) {
        String sql = "UPDATE Users SET password_hash = ?, register = ? WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setBoolean(2, true);
            stmt.setString(3, email);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Add new user (admin only) ---
    public boolean addUser(String email, String passwordHash) {
        String sql = "INSERT INTO Users (username, password_hash, access_level, register) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, passwordHash);
            stmt.setString(3, "user"); // default role
            stmt.setInt(4, 0); // register = 0 initially

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Get user access level ---
    public String getUserAccessLevel(String username, String password) {
        String sql = "SELECT password_hash, access_level FROM Users WHERE username = ? AND register = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                // Use PasswordUtils to verify the plain text password against stored hash
                if (PasswordUtils.verifyPassword(password, storedHash)) {
                    return rs.getString("access_level");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Get all users (admin only) ---
    public List<UserModel> getAllUsers() {
        List<UserModel> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, access_level, register FROM Users ORDER BY username";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserModel user = new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("access_level"),
                        rs.getBoolean("register")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // --- Add new user with default password (admin only) ---
    public boolean addNewUser(String email, String role) {
        String defaultPassword = "temppass123";
        String hashedPassword = PasswordUtils.hashPassword(defaultPassword);
        String sql = "INSERT INTO Users (username, password_hash, access_level, register) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.setInt(4, 0); // register = 0 initially

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Delete user (hard delete) ---
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Toggle user active/inactive status ---
    public boolean toggleUserStatus(int userId) {
        String sql = "UPDATE Users SET register = CASE WHEN register = 1 THEN 0 ELSE 1 END WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Bulk delete users ---
    public boolean bulkDeleteUsers(List<Integer> userIds) {
        if (userIds.isEmpty()) return false;

        StringBuilder sql = new StringBuilder("DELETE FROM Users WHERE user_id IN (");
        for (int i = 0; i < userIds.size(); i++) {
            sql.append("?");
            if (i < userIds.size() - 1) sql.append(",");
        }
        sql.append(")");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < userIds.size(); i++) {
                stmt.setInt(i + 1, userIds.get(i));
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Bulk toggle user status ---
    public boolean bulkToggleUserStatus(List<Integer> userIds) {
        if (userIds.isEmpty()) return false;

        StringBuilder sql = new StringBuilder("UPDATE Users SET register = CASE WHEN register = 1 THEN 0 ELSE 1 END WHERE user_id IN (");
        for (int i = 0; i < userIds.size(); i++) {
            sql.append("?");
            if (i < userIds.size() - 1) sql.append(",");
        }
        sql.append(")");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < userIds.size(); i++) {
                stmt.setInt(i + 1, userIds.get(i));
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Check if email already exists ---
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
