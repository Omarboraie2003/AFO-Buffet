package org.example.dao;

import java.sql.*;

public class UserDAO {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BuffetDB;encrypt=false";
    private static final String USER = "sa";
    private static final String PASSWORD = "MySecurePass123";

    // Create (Insert)
    public void addUser(String username, String passwordHash, String accessLevel) {
        String sql = "INSERT INTO Users (username, password_hash, access_level) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, accessLevel);
            stmt.executeUpdate();
            System.out.println("✅ User added successfully!");

        } catch (SQLException e) {
            System.out.println("❌ Error adding user: " + e.getMessage());
        }
    }

    // Read (Select)
    public void getAllUsers() {
        String sql = "SELECT * FROM Users";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                        rs.getInt("user_id") + " | " +
                                rs.getString("username") + " | " +
                                rs.getString("access_level")
                );
            }

        } catch (SQLException e) {
            System.out.println("❌ Error reading users: " + e.getMessage());
        }
    }

    // Update
    public void updateUserAccessLevel(int userId, String newAccessLevel) {
        String sql = "UPDATE Users SET access_level = ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newAccessLevel);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ User updated successfully!");
            } else {
                System.out.println("⚠ No user found with ID: " + userId);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error updating user: " + e.getMessage());
        }
    }

    // Delete
    public void deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ User deleted successfully!");
            } else {
                System.out.println("⚠ No user found with ID: " + userId);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error deleting user: " + e.getMessage());
        }
    }
}
