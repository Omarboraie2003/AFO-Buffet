package org.example.model.user;
import org.example.model.Order.OrderDAO;
import org.example.model.Order.OrderModel;
import org.example.util.DBConnection;
import java.sql.*;
import org.example.util.PasswordUtils;
import java.util.List;
import java.util.ArrayList;

public class UserDAO {

    // --- Validate user for login (now checks is_active) ---
    public String validateUser(String username, String password) {
        String sql = "SELECT password_hash, access_level FROM Users WHERE username = ? AND is_registered = 1 AND is_active = 1";
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

    public UserModel getUserById(int user_id) {
        String sqp = "select * from Users where user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqp)) {
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("access_level"),
                        rs.getBoolean("is_registered"),
                        rs.getBoolean("is_active"),
                        rs.getInt("cart_id")
                );
            } else {
                return null; // User not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Find user by email ---
    public UserModel findUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE username = ?";

        // Debug logging
        System.out.println("=== DEBUG findUserByEmail ===");
        System.out.println("Looking for email: '" + email + "'");
        System.out.println("SQL Query: " + sql);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            System.out.println("Executing query with parameter: '" + email + "'");

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Found user record!");

                // Debug each column
                try {
                    int userId = rs.getInt("user_id");
                    String username = rs.getString("username");
                    String passwordHash = rs.getString("password_hash");
                    String accessLevel = rs.getString("access_level");
                    boolean register = rs.getBoolean("is_registered");
                    boolean isActive = rs.getBoolean("is_active");
                    int cart_id = rs.getInt("cart_id");

                    System.out.println("User ID: " + userId);
                    System.out.println("Username: " + username);
                    System.out.println("Password Hash: " + (passwordHash != null ? "***HIDDEN***" : "NULL"));
                    System.out.println("Access Level: " + accessLevel);
                    System.out.println("Register Status: " + register);
                    System.out.println("Active Status: " + isActive);

                    UserModel user = new UserModel(userId, username, passwordHash, accessLevel, register, isActive, cart_id);
                    user.setActive(isActive);
                    System.out.println("UserModel created successfully");
                    return user;

                } catch (SQLException columnError) {
                    System.out.println("Error reading columns: " + columnError.getMessage());

                    // Let's see what columns are actually available
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    System.out.println("Available columns:");
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.println("  " + i + ": " + metaData.getColumnName(i) + " (" + metaData.getColumnTypeName(i) + ")");
                    }
                    throw columnError;
                }
            } else {
                System.out.println("No user found with email: '" + email + "'");

                // Let's see what users DO exist
                String debugSql = "SELECT username FROM Users LIMIT 5";
                try (PreparedStatement debugStmt = conn.prepareStatement(debugSql);
                     ResultSet debugRs = debugStmt.executeQuery()) {

                    System.out.println("Sample usernames in database:");
                    while (debugRs.next()) {
                        System.out.println("  - '" + debugRs.getString("username") + "'");
                    }
                } catch (SQLException debugError) {
                    System.out.println("Could not fetch sample usernames: " + debugError.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error in findUserByEmail: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }

        System.out.println("Returning null");
        System.out.println("=== END DEBUG ===");
        return null;
    }

    // --- Get all users (admin only) ---
    public List<UserModel> getAllUsers() {
        List<UserModel> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, access_level, is_registered, is_active, cart_id FROM Users ORDER BY username";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserModel user = new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("access_level"),
                        rs.getBoolean("is_registered"),
                        rs.getBoolean("is_active"),
                        rs.getInt("cart_id")
                );
                user.setActive(rs.getBoolean("is_active"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // --- Add new user with default password (admin only) ---
    public boolean addNewUser(String email, String role) {
        String sql = "INSERT INTO Users (username, password_hash, access_level, is_registered, is_active, cart_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, null);
            stmt.setString(3, role);
            stmt.setInt(4, 0); // register = 0 initially
            stmt.setInt(5, 1); // is_active = 1 by default
            stmt.setNull(6, Types.INTEGER); // cart_id = NULL initially

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

    // --- Bulk toggle user status (CORRECTED) ---
    public boolean bulkToggleUserStatus(List<Integer> userIds) {
        if (userIds.isEmpty()) return false;

        StringBuilder sql = new StringBuilder("UPDATE Users SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END WHERE user_id IN (");
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

    // --- Set the new password that the user entered while registering ---
    public boolean registerUser(UserModel user) {

        String sql = "UPDATE Users SET username = ?, password_hash = ?, access_level = ?, is_registered = ?, is_active = ?, cart_id = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            OrderDAO orderDAO = new OrderDAO();
            orderDAO.createCartForUser(user.getUserId());

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getAccessLevel());
            stmt.setBoolean(4, user.isIs_registered());
            stmt.setBoolean(5, user.isActive());
            stmt.setInt(6, orderDAO.checkIfCartExists(user.getUserId()));
            stmt.setInt(7, user.getUserId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Get user by username (for session management) ---
    public UserModel getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("access_level"),
                        rs.getBoolean("is_registered"),
                        rs.getBoolean("is_active"),
                        rs.getInt("cart_id")
                );

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Update user activity status ---
    public boolean updateUserActivityStatus(int userId, boolean isActive) {
        String sql = "UPDATE Users SET is_active = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, isActive ? 1 : 0); // Convert boolean to int for SQL Server BIT
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Check if user is active ---
    public boolean isUserActive(int userId) {
        String sql = "SELECT is_active FROM Users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("is_active") == 1; // For SQL Server BIT type
            }
            return false; // User not found

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Get only active users ---
    public List<UserModel> getActiveUsers() {
        List<UserModel> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, access_level, is_registered, is_active, cart_id FROM Users WHERE is_active = 1 ORDER BY username";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserModel user = new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("access_level"),
                        rs.getBoolean("is_registered"),
                        rs.getBoolean("is_active"),
                        rs.getInt("cart_id")
                );
                user.setActive(rs.getBoolean("is_active"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<UserModel> getInactiveUsers() {
        List<UserModel> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, access_level, is_registered, is_active, cart_id FROM Users WHERE is_active = 0 ORDER BY username";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserModel user = new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("access_level"),
                        rs.getBoolean("is_registered"),
                        rs.getBoolean("is_active"),
                        rs.getInt("cart_id")
                );
                user.setActive(rs.getBoolean("is_active"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void updateUserCartId(int userId, int cartId) {
        String sql = "UPDATE Users SET cart_id = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}