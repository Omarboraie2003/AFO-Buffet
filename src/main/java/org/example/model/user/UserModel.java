package org.example.model.user;

public class UserModel {

    private int userId;
    private String username;
    private String passwordHash;
    private String accessLevel;

    // --- Constructors ---

    /**
     * Default constructor.
     * Needed for frameworks like Hibernate, JSP/Servlets, or tools that use reflection
     * to create an object without passing parameters.
     */

    public UserModel() {
    }

    /**
     * Full constructor including userId.
     * Use this when retrieving an existing user from the database.
     */

    public UserModel(int userId, String username, String passwordHash, String accessLevel) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.accessLevel = accessLevel;
    }

    /**
     * Constructor without userId.
     * Use this when creating a new user that has not yet been stored in the database.
     * The userId will be generated later by the database (auto-increment).
     */

    public UserModel(String username, String passwordHash, String accessLevel) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.accessLevel = accessLevel;
    }

    // --- Getters and Setters ---
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    // --- toString() for easy printing ---
    @Override
    public String toString() {
        return "UserModel{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", accessLevel='" + accessLevel + '\'' +
                '}';
    }
}
