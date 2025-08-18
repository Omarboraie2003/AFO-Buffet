package org.example.controller;


import org.example.model.user.MenuDAO;
import org.example.model.user.MenuItem;

import java.sql.SQLException;

public class MenuController {
    private MenuDAO menuDAO;

    // Constructor that creates a new DAO
    public MenuController() {
        this.menuDAO = new MenuDAO(); // FIX: create new instance
    }

    // Or allow injection from outside
    public MenuController(MenuDAO menuDAO) {
        this.menuDAO = menuDAO;
    }

    public void addItem(String name, String desc, double price, boolean available) throws SQLException {
        MenuItem item = new MenuItem(0, name, desc, price, available);
        menuDAO.addMenuItem(item);
    }

    public void showMenuForEmployee() throws SQLException {
        for (MenuItem item : menuDAO.getAvailableMenuItems()) {
            System.out.println(item.getName() + " - " + item.getPrice());
        }
    }

    public void showMenuForChef() throws SQLException {
        for (MenuItem item : menuDAO.getAllMenuItems()) {
            System.out.println(item.getId() + " | " + item.getName() + " | " + item.getPrice() + " | Available: " + item.isAvailable());
        }
    }

    public void updateItem(int id, String name,String description, double price,  boolean available) throws SQLException {
        MenuItem item = new MenuItem(id, name, description, price, available);
        menuDAO.updateMenuItem(item);
    }

    public void deleteItem(int id) throws SQLException {
        menuDAO.deleteMenuItem(id);
    }
}
