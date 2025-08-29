package org.example.model.user;
import org.example.util.DBConnection;
import java.sql.*;

public class UserDAO {


    // --- Validate user for login ---
    public String validateUser(String username, String password) {
        String sql = "SELECT access_level FROM Users WHERE username = ? AND password_Hash = ? AND register = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("access_level"); // Return the access level
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


    // --- Register user (update existing record) ---


    // --- Add new user (admin only) ---
    public boolean addUser(String email, String passwordHash) {
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

    // Add this method to your UserDAO class
    public String getUserAccessLevel(String username, String password) {
        String sql = "SELECT access_level FROM Users WHERE username = ? AND password_Hash = ? AND register = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("access_level");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}

