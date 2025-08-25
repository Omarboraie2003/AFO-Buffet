package org.example.controller;

import com.google.gson.Gson;
import org.example.model.MenuItem.MenuDAO;
import org.example.model.MenuItem.MenuItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/menu")
public class EmployeeMenuServlet extends HttpServlet {
    private MenuDAO menuDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        menuDAO = new MenuDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String category = request.getParameter("category");

        System.out.println("[DEBUG] MenuServlet doGet called with category: " + category);

        try {
            if (menuDAO == null) {
                System.err.println("[ERROR] menuDAO is null - not initialized properly");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"DAO not initialized\"}");
                return;
            }

            try {
                Connection testConn = DBConnection.getConnection();
                if (testConn == null) {
                    System.err.println("[ERROR] Database connection is null");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"error\":\"Database connection failed\"}");
                    return;
                }
                System.out.println("[DEBUG] Database connection test successful");
                testConn.close();
            } catch (SQLException dbTest) {
                System.err.println("[ERROR] Database connection test failed: " + dbTest.getMessage());
                dbTest.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Database connection failed: " + dbTest.getMessage() + "\"}");
                return;
            }

            List<MenuItem> items;

            if (category != null && !category.isEmpty()) {
                category = category.trim();
                System.out.println("[DEBUG] Fetching items for category: '" + category + "'");
                items = menuDAO.getMenuItemsByCategory(category);
            } else {
                System.out.println("[DEBUG] Fetching all available menu items");
                items = menuDAO.getAvailableMenuItems();
            }

            if (items == null) {
                System.err.println("[ERROR] DAO returned null items list");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"No items found\"}");
                return;
            }

            String json = gson.toJson(items);
            response.getWriter().write(json);

            System.out.println("[SUCCESS] DAO returned " + items.size() + " items for category '" + category + "'");

        } catch (SQLException e) {
            System.err.println("[SQL ERROR] Database error in MenuServlet: " + e.getMessage());
            System.err.println("[SQL ERROR] SQL State: " + e.getSQLState());
            System.err.println("[SQL ERROR] Error Code: " + e.getErrorCode());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error in MenuServlet: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error: " + e.getMessage() + "\"}");
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            MenuItem newItem = gson.fromJson(request.getReader(), MenuItem.class);
            menuDAO.addMenuItem(newItem);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\":\"Item added successfully\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
