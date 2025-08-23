package org.example.controller;

import com.google.gson.Gson;
import org.example.model.MenuItem.MenuDAO;
import org.example.model.MenuItem.MenuItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/menu-items")
public class MenuServlet extends HttpServlet {

    private final MenuDAO menuDAO = new MenuDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String category = request.getParameter("category");
        String availableParam = request.getParameter("available");

        Boolean available = null;
        if (availableParam != null) {
            available = "true".equalsIgnoreCase(availableParam);
        }

        List<MenuItem> items;

        try {
            if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
                // Filter by specific category
                if (available != null) {
                    if (available) {
                        items = menuDAO.getMenuItemsByCategoryAndAvailability(category, true);
                    } else {
                        items = menuDAO.getUnavailableMenuItemsByCategory(category);
                    }
                } else {
                    items = menuDAO.getMenuItemsByCategory(category); // All items in category
                }
            } else {
                // No category filter or "All" selected
                if (available != null) {
                    if (available) {
                        items = menuDAO.getAvailableMenuItems();
                    } else {
                        items = menuDAO.getUnavailableMenuItems();
                    }
                } else {
                    items = menuDAO.getAllMenuItems(); // All items regardless of availability
                }
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(items));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error\"}");
            e.printStackTrace();
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        try {
            MenuItem newItem = gson.fromJson(request.getReader(), MenuItem.class);

            if (newItem.getName() == null || newItem.getName().trim().isEmpty() ||
                    newItem.getDescription() == null || newItem.getDescription().trim().isEmpty() ||
                    newItem.getPrice() <= 0 ||
                    newItem.getCategory() == null || newItem.getCategory().trim().isEmpty() ||
                    newItem.getType() == null || newItem.getType().trim().isEmpty()) {

                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", "Invalid input")
                ));
                return;
            }

            boolean added = menuDAO.addMenuItem(newItem);

            if (added) {
                response.getWriter().write(gson.toJson(Map.of("success", true)));
            } else {
                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", "Failed to add item")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Server error")
            ));
        }
    }
}

