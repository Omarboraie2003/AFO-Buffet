package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.example.model.MenuItem.MenuDAO;
import org.example.model.MenuItem.MenuItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import javax.imageio.ImageIO;


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
            throws IOException {

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String category = request.getParameter("item_category");
        String availableParam = request.getParameter("is_available");
        String todaysSpecialParam = request.getParameter("todaysSpecial");
        String type = request.getParameter("item_type");
        String searchTerm = request.getParameter("search");
        String itemIdParam = request.getParameter("item_id");

        // Check for single item request by ID
        if (itemIdParam != null && !itemIdParam.trim().isEmpty()) {
            try {
                int itemId = Integer.parseInt(itemIdParam);
                MenuItem item = menuDAO.getMenuItemById(itemId);
                if (item != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(gson.toJson(List.of(item))); // Return as array for consistency
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(gson.toJson(List.of()));
                }
                return;
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(Map.of("error", "Invalid item ID format")));
                return;
            }
        }

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

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            if (available != null) {
                items = menuDAO.searchMenuItemsWithAvailability(searchTerm.trim(), available);
            } else {
                items = menuDAO.searchMenuItems(searchTerm.trim());
            }
        } else if (type != null && !type.isEmpty()) {
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
                items = menuDAO.getMenuItemsByCategory(category);
            }
        } else {
            if (available != null) {
                if (available) {
                    items = menuDAO.getAvailableMenuItems();
                } else {
                    items = menuDAO.getUnavailableMenuItems();
                }
            } else {
                items = menuDAO.getAllMenuItems();
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
                String name = request.getParameter("item_name");
                String description = request.getParameter("item_description");
                String category = request.getParameter("item_category");
                String type = request.getParameter("item_type");
                String availableStr = request.getParameter("is_available");

                boolean available = "true".equalsIgnoreCase(availableStr);

                String photoUrl = null;
                Part photoPart = request.getPart("photo");
                if (photoPart != null && photoPart.getSize() > 0) {
                    photoUrl = saveUploadedFile(photoPart, request);
                }

                newItem = new MenuItem(0, name, description, available, type, category, photoUrl, false);
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
                String idStr = request.getParameter("item_id");
                String name = request.getParameter("item_name");
                String description = request.getParameter("item_description");
                String category = request.getParameter("item_category");
                String type = request.getParameter("item_type");
                String availableStr = request.getParameter("is_available");
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

                updatedItem = new MenuItem(id, name, description, available, type, category, photoUrl, false);
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

        String idParam = request.getParameter("item_id");

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

        String idParam = request.getParameter("item_id");

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
        String uploadPath = request.getServletContext().getRealPath("/") + "Images";

        // Create directory if it doesn't exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        byte[] compressedImageData = compressImageTo700KB(filePart.getInputStream());

        // Save the compressed file
        Path filePath = Paths.get(uploadPath, uniqueFileName);
        Files.write(filePath, compressedImageData);

        // Return the URL path that can be used by the frontend
        return request.getContextPath() + "/Images/" + uniqueFileName;
    }

    private byte[] compressImageTo700KB(InputStream inputStream) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            throw new IOException("Invalid image file");
        }

        // Start with high quality and reduce if needed
        float quality = 0.9f;
        byte[] compressedData;
        int maxSizeKB = 700;

        do {
            // Resize image if it's too large (max 1920px width)
            BufferedImage resizedImage = resizeImage(originalImage, 1920);

            // Compress with current quality
            compressedData = compressImage(resizedImage, quality);

            // If still too large, reduce quality
            if (compressedData.length > maxSizeKB * 1024) {
                quality -= 0.1f;
            }

        } while (compressedData.length > maxSizeKB * 1024 && quality > 0.1f);

        return compressedData;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // If image is smaller than max width, return original
        if (originalWidth <= maxWidth) {
            return originalImage;
        }

        // Calculate new dimensions maintaining aspect ratio
        int newWidth = maxWidth;
        int newHeight = (originalHeight * maxWidth) / originalWidth;

        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    private byte[] compressImage(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Use ImageIO to write JPEG with specified quality
        var writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer available");
        }

        var writer = writers.next();
        var ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);

        var param = writer.getDefaultWriteParam();
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        writer.write(null, new javax.imageio.IIOImage(image, null, null), param);

        writer.dispose();
        ios.close();

        return baos.toByteArray();
    }


}