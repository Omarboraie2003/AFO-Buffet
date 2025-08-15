package org.example.model.user;

import org.example.model.user.UserModel;
import org.example.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Create a new user
    public boolean addUser(UserModel user) {
        String sql = "INSERT INTO Users (username, password_hash, access_level) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getAccessLevel());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Validate login credentials
    public boolean validateUser(String username, String passwordHash) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash); // Already hashed before calling this method

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if a matching record is found

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Read all users
    public List<UserModel> getAllUsers() {
        List<UserModel> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserModel user = new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("access_level")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Get user by ID
    public UserModel getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("access_level")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update a user
    public boolean updateUser(UserModel user) {
        String sql = "UPDATE Users SET username = ?, password_hash = ?, access_level = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getAccessLevel());
            stmt.setInt(4, user.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a user
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }


    }
}
