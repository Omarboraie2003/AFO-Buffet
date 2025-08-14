package org.example;

import org.example.dao.UserDAO;

public class Main {
    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();

        // Create
        //userDAO.addUser("omar", "MySecurePass123", "Employee");

        // Read
        userDAO.getAllUsers();

        // Update
        userDAO.updateUserAccessLevel(19, "Chef");

        // Delete
        //userDAO.deleteUser(2);

    }
}



