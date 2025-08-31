package org.example.controller;


import org.example.model.MenuItem.MenuDAO;
import org.example.model.MenuItem.MenuItem;

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

    // ✅ Add item with type
    public void addItem(String name, String desc, boolean available, String type) throws SQLException {
        MenuItem item = new MenuItem(0, name, desc, available, type);
        menuDAO.addMenuItem(item);
    }

    // ✅ Show menu for employee (include type)
    public void showMenuForEmployee() throws SQLException {
        for (MenuItem item : menuDAO.getAvailableMenuItems()) {
            System.out.println(item.getType() + " | " + item.getName());
        }
    }

    // ✅ Show menu for chef (include type)
    public void showMenuForChef() throws SQLException {
        for (MenuItem item : menuDAO.getAllMenuItems()) {
            System.out.println(
                    item.getId() + " | " +
                            item.getType() + " | " +
                            item.getName() + " | "
            );
        }
    }

    // ✅ Update item with type
    public void updateItem(int id, String name, String description, double price, boolean available, String type) throws SQLException {
        MenuItem item = new MenuItem(id, name, description, available, type);
        menuDAO.updateMenuItem(item);
    }

    public void deleteItem(int id) throws SQLException {
        menuDAO.deleteMenuItem(id);
    }
}
