package org.example.controller;


import org.example.model.MenuItem.MenuDAO;
import org.example.model.MenuItem.MenuItem;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void addItem(String name, String desc, double price, boolean available, String type,String category) throws SQLException {
        MenuItem item = new MenuItem(0, name, desc, price, available, type,category);
        menuDAO.addMenuItem(item);
    }

    // ✅ Show menu for employee (group by category)
    public void showMenuForEmployee() throws SQLException {
        List<MenuItem> items = menuDAO.getAvailableMenuItems();

        // Group items by category
        Map<String, List<MenuItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory));

        for (String category : grouped.keySet()) {
            System.out.println("\n=== " + category.toUpperCase() + " ===");
            for (MenuItem item : grouped.get(category)) {
                System.out.println(item.getType() + " | " + item.getName() + " - " + item.getPrice());
            }
        }
    }

    // ✅ Show menu for chef (include type + category)
    public void showMenuForChef() throws SQLException {
        for (MenuItem item : menuDAO.getAllMenuItems()) {
            System.out.println(
                    item.getId() + " | " +
                            item.getCategory() + " | " +
                            item.getType() + " | " +
                            item.getName() + " | " +
                            item.getPrice() + " | Available: " + item.isAvailable()
            );
        }
    }

    // ✅ Update item with type
    public void updateItem(int id, String name, String description, double price, boolean available, String type, String category) throws SQLException {
        MenuItem item = new MenuItem(id, name, description, price, available, type, category );
        menuDAO.updateMenuItem(item);
    }

    public void deleteItem(int id) throws SQLException {
        menuDAO.deleteMenuItem(id);
    }
}
