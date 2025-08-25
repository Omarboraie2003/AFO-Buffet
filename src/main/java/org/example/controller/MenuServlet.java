package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.example.model.MenuItem.MenuDAO;
import org.example.model.MenuItem.MenuItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet({"/menu-items", "/menu-items/*"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 50,      // 50MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class MenuServlet extends HttpServlet {

    private final MenuDAO menuDAO = new MenuDAO();
    private final Gson gson = new Gson();

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, enctype, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String category = request.getParameter("category");
        String availableParam = request.getParameter("available");
        String todaysSpecialParam = request.getParameter("todaysSpecial");
        String type = request.getParameter("type");

        if ("true".equalsIgnoreCase(todaysSpecialParam)) {
            MenuItem special = menuDAO.getTodaysSpecial();
            if (special != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(List.of(special)));
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(List.of()));
            }
            return;
        }

        Boolean available = null;
        if (availableParam != null) {
            available = "true".equalsIgnoreCase(availableParam);
        }

        List<MenuItem> items;

        if (type != null && !type.isEmpty()) {
            if (available != null) {
                items = menuDAO.getMenuItemsByTypeAndAvailability(type, available);
            } else {
                items = menuDAO.getMenuItemsByType(type);
            }
        } else if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
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

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(items));

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if ("/set-special".equals(pathInfo)) {
            handleSetSpecial(request, response);
            return;
        }

        if ("/clear-special".equals(pathInfo)) {
            handleClearSpecial(request, response);
            return;
        }

        try {
            MenuItem newItem;

            // Check if request is multipart (contains file upload)
            String contentType = request.getContentType();
            if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
                // Handle multipart form data
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String category = request.getParameter("category");
                String type = request.getParameter("type");
                String availableStr = request.getParameter("available");

                boolean available = "true".equalsIgnoreCase(availableStr);

                String photoUrl = null;
                Part photoPart = request.getPart("photo");
                if (photoPart != null && photoPart.getSize() > 0) {
                    photoUrl = saveUploadedFile(photoPart, request);
                }

                newItem = new MenuItem(0, name, description, available, type, category, false, photoUrl);
            } else {
                // Handle JSON data (for backward compatibility)
                newItem = gson.fromJson(request.getReader(), MenuItem.class);
            }

            String validationError = validateMenuItem(newItem, false);
            if (validationError != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", validationError)
                ));
                return;
            }

            try {
                if (menuDAO.isItemNameExists(newItem.getName())) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.getWriter().write(gson.toJson(
                            Map.of("success", false, "message", "An item with this name already exists. Please choose a different name.")
                    ));
                    return;
                }
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", "Database error while checking for duplicates")
                ));
                return;
            }

            boolean added = menuDAO.addMenuItem(newItem);

            if (added) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(Map.of("success", true)));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", "Failed to add item")
                ));
            }

        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Invalid JSON format")
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Server error: " + e.getMessage())
            ));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            MenuItem updatedItem;

            String contentType = request.getContentType();
            if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
                // Handle multipart form data
                String idStr = request.getParameter("id");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String category = request.getParameter("category");
                String type = request.getParameter("type");
                String availableStr = request.getParameter("available");
                String removePhotoStr = request.getParameter("removePhoto");

                int id = Integer.parseInt(idStr);
                boolean available = "true".equalsIgnoreCase(availableStr);
                boolean removePhoto = "true".equalsIgnoreCase(removePhotoStr);

                String photoUrl = null;
                Part photoPart = request.getPart("photo");
                if (photoPart != null && photoPart.getSize() > 0) {
                    photoUrl = saveUploadedFile(photoPart, request);
                } else if (removePhoto) {
                    photoUrl = null;
                } else {
                    // If no new photo uploaded and not removing, keep the existing photo URL
                    MenuItem existingItem = menuDAO.getMenuItemById(id);
                    if (existingItem != null) {
                        photoUrl = existingItem.getPhotoUrl();
                    }
                }

                updatedItem = new MenuItem(id, name, description, available, type, category, false, photoUrl);
            } else {
                // Handle JSON data
                updatedItem = gson.fromJson(request.getReader(), MenuItem.class);
            }

            String validationError = validateMenuItem(updatedItem, true);
            if (validationError != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", validationError)
                ));
                return;
            }

            boolean updated = menuDAO.updateMenuItem(updatedItem);

            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(Map.of("success", true)));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", "Item not found or failed to update")
                ));
            }

        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Invalid JSON format")
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Server error: " + e.getMessage())
            ));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Item ID is required")
            ));
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            System.out.println("Attempting to delete item with ID: " + id);
            boolean deleted = menuDAO.deleteMenuItem(id);
            System.out.println("DAO deleteMenuItem returned: " + deleted);

            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(Map.of("success", true)));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", "Item not found")
                ));
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Invalid item ID format")
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Server error: " + e.getMessage())
            ));
        }
    }

    private String validateMenuItem(MenuItem item, boolean requireId) {
        if (item == null) {
            return "Menu item data is required";
        }

        if (requireId && item.getId() <= 0) {
            return "Valid item ID is required";
        }

        if (item.getName() == null || item.getName().trim().isEmpty()) {
            return "Item name is required";
        }

        if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
            return "Item description is required";
        }

        if (item.getCategory() == null || item.getCategory().trim().isEmpty()) {
            return "Item category is required";
        }

        if (item.getType() == null || item.getType().trim().isEmpty()) {
            return "Item type is required";
        }

        return null; // No validation errors
    }

    private void handleSetSpecial(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idParam = request.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Item ID is required")
            ));
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            boolean success = menuDAO.setTodaysSpecial(id);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(Map.of("success", true)));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", "Item not found or failed to set as special")
                ));
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Invalid item ID format")
            ));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Server error: " + e.getMessage())
            ));
        }
    }

    private void handleClearSpecial(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            boolean success = menuDAO.clearTodaysSpecial();

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(Map.of("success", true)));
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(
                        Map.of("success", false, "message", "No special was set to clear")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(
                    Map.of("success", false, "message", "Server error: " + e.getMessage())
            ));
        }
    }

    private String saveUploadedFile(Part filePart, HttpServletRequest request) throws IOException {
        String fileName = filePart.getSubmittedFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }

        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }

        if (filePart.getSize() > 50 * 1024 * 1024) { // Increased from 10MB to 50MB limit
            throw new IOException("File size exceeds 50MB limit"); // Updated error message
        }

        // Create unique filename to avoid conflicts
        String fileExtension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = fileName.substring(lastDotIndex);
        }

        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueFileName = System.currentTimeMillis() + "_" + sanitizedFileName;

        // Get the real path to the webapp directory
        String uploadPath = request.getServletContext().getRealPath("/") + "images" + File.separator + "menu-items";

        // Create directory if it doesn't exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file
        Path filePath = Paths.get(uploadPath, uniqueFileName);
        try (InputStream inputStream = filePart.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return the URL path that can be used by the frontend
        return request.getContextPath() + "/images/menu-items/" + uniqueFileName;
    }
}
