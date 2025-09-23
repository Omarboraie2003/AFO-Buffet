package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.model.Order.OrderDAO;
import org.example.model.Order.OrderDetailsDTO;
import org.example.model.Order.OrderItemDetail;
import org.example.model.ItemInOrder.ItemInOrderDAO;
import org.example.model.ItemInOrder.ItemInOrderModel;



import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/orders", "/orders/*"})
public class OrdersServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Gson gson;

    public OrdersServlet() {
        // Create Gson with custom LocalDateTime serializer
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>)
                        (src, typeOfSrc, context) -> context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set response headers for JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        try {
            // Check if this is a request for order history
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.equals("/history")) {
                // Get completed orders for history view
                ArrayList<OrderDetailsDTO> completedOrders = OrderDAO.getCompletedOrderDetails();
                List<FrontendOrderDTO> frontendOrders = convertToFrontendFormat(completedOrders);
                String jsonResponse = gson.toJson(frontendOrders);
                response.getWriter().write(jsonResponse);
                System.out.println("[OrdersServlet] Returned " + frontendOrders.size() + " completed orders to frontend");
                return;
            }
            // ✅ Add a reorder endpoint
            if (pathInfo != null && pathInfo.startsWith("/reorder")) {
                String orderIdParam = request.getParameter("orderId");
                if (orderIdParam == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Missing orderId parameter\"}");
                    return;
                }

                int orderId = Integer.parseInt(orderIdParam);

                // Call DAO method to fetch the items for reorder
                List<ItemInOrderModel> itemsToReorder = ItemInOrderDAO.getItemsInOrder2(orderId);

                if (itemsToReorder == null || itemsToReorder.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"No items found for reorder\"}");
                    return;
                }

                String jsonResponse = gson.toJson(itemsToReorder);
                response.getWriter().write(jsonResponse);

                System.out.println("[OrdersServlet] Returned reorder items for orderId=" + orderId);
                return;
            }


            // Get all active order details (excluding completed)
            ArrayList<OrderDetailsDTO> orderDetails = OrderDAO.getAllOrderDetails();

            // Convert to frontend-compatible format
            List<FrontendOrderDTO> frontendOrders = convertToFrontendFormat(orderDetails);

            // Convert to JSON and send response
            String jsonResponse = gson.toJson(frontendOrders);
            response.getWriter().write(jsonResponse);

            // Log for debugging
            System.out.println("[OrdersServlet] Returned " + frontendOrders.size() + " active orders to frontend");

        } catch (Exception e) {
            // Handle errors gracefully
            System.err.println("[OrdersServlet] Error retrieving orders: " + e.getMessage());
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Failed to retrieve orders\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Convert OrderDetailsDTO to format expected by frontend
     */
    private List<FrontendOrderDTO> convertToFrontendFormat(ArrayList<OrderDetailsDTO> orderDetails) {
        List<FrontendOrderDTO> result = new ArrayList<>();

        for (OrderDetailsDTO order : orderDetails) {
            FrontendOrderDTO frontendOrder = new FrontendOrderDTO();
            frontendOrder.orderId = order.getOrderId();
            frontendOrder.employeeId = extractUserIdFromUsername(order.getUsername());
            frontendOrder.username = order.getUsername();


            frontendOrder.firstName = order.getFirstName();
            frontendOrder.lastName = order.getLastName();

            frontendOrder.orderDate = order.getOrderTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            frontendOrder.status = order.getOrderStatus();

            // Convert items to the format expected by frontend
            frontendOrder.items = new ArrayList<>();
            for (OrderItemDetail item : order.getItems()) {
                FrontendItemDTO frontendItem = new FrontendItemDTO();

                frontendItem.itemName = item.getItemName();
                frontendItem.typeOfBread = item.getTypeOfBread();
                frontendItem.quantity = item.getQuantity();
                frontendItem.itemNote = item.getItemNote();
                frontendOrder.items.add(frontendItem);
            }

            result.add(frontendOrder);
        }

        return result;
    }
    /**
     * Extract user ID from username - you might need to adjust this based on your username format
     * Or you could modify your DTO to include user_id directly
     */
    private int extractUserIdFromUsername(String username) {
        // This is a placeholder - you might want to:
        // 1. Modify your getAllOrderDetails query to also return user_id
        // 2. Or implement a lookup method to get user_id from username
        // 3. Or use a hash code as a temporary solution
        return Math.abs(username.hashCode() % 1000); // Temporary solution
    }

    // Inner classes for frontend-compatible JSON structure
    private static class FrontendOrderDTO {
        public int orderId;
        public int employeeId;
        public String username;
        public String firstName;    // Make sure this exists
        public String lastName;     // Make sure this exists
        public String orderDate;
        public String status;
        public List<FrontendItemDTO> items;
    }

    private static class FrontendItemDTO {
        public String itemName;
        public String typeOfBread;
        public String quantity;
        public String itemNote;
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set response headers
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Get the full request URI and extract the path after the servlet
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();

        // Extract the path after /orders
        String fullPath = requestURI.substring(contextPath.length() + servletPath.length());

        System.out.println("[OrdersServlet] PUT Request - Full Path: " + fullPath);

        if (fullPath != null && fullPath.contains("/status")) {
            // Extract order ID from path like "/3/status"
            String[] pathParts = fullPath.split("/");
            if (pathParts.length >= 2) {
                try {
                    int orderId = Integer.parseInt(pathParts[1]);

                    // Read the request body to get the new status
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    while ((line = request.getReader().readLine()) != null) {
                        buffer.append(line);
                    }
                    String requestBody = buffer.toString();
                    System.out.println("[OrdersServlet] Request body: " + requestBody);

                    // Parse the JSON to get the status
                    String newStatus = "accepted"; // Default
                    if (requestBody.contains("\"status\"")) {
                        int statusStart = requestBody.indexOf("\"status\"") + 9;
                        int statusEnd = requestBody.indexOf("\"", statusStart + 1);
                        if (statusEnd > statusStart) {
                            newStatus = requestBody.substring(statusStart + 1, statusEnd);
                        }
                    }

                    // Convert "ready" status to "completed"
                    if ("ready".equals(newStatus)) {
                        newStatus = "completed";
                        System.out.println("[OrdersServlet] Converting 'ready' status to 'completed' for order " + orderId);
                    }

                    System.out.println("[OrdersServlet] Updating order " + orderId + " to status: " + newStatus);

                    // Update the database directly
                    boolean updated = OrderDAO.updateOrderStatus(orderId, newStatus);

                    if (updated) {
                        // Clean up old completed orders every time we complete a new one
                        if ("completed".equals(newStatus)) {
                            OrderDAO.cleanupOldCompletedOrders();
                        }

                        response.getWriter().write("{\"success\": true, \"message\": \"Order " + orderId + " status updated to " + newStatus + "\"}");
                        System.out.println("[OrdersServlet] Successfully updated order " + orderId + " in database");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"success\": false, \"error\": \"Order " + orderId + " not found or could not be updated\"}");
                        System.err.println("[OrdersServlet] Failed to update order " + orderId + " in database");
                    }
                    return;

                } catch (NumberFormatException e) {
                    System.err.println("[OrdersServlet] Invalid order ID in path: " + fullPath);
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Invalid order ID\", \"path\": \"" + fullPath + "\"}");
                    return;
                } catch (Exception e) {
                    System.err.println("[OrdersServlet] Error updating order status: " + e.getMessage());
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"success\": false, \"error\": \"Failed to update status\", \"message\": \"" + e.getMessage() + "\"}");
                    return;
                }
            }
        }

        System.err.println("[OrdersServlet] Invalid PUT request format: " + fullPath);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\": \"Invalid request format\", \"expectedFormat\": \"/orderId/status\", \"receivedPath\": \"" + fullPath + "\"}");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set response headers
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Get the full request URI and extract the path after the servlet
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();

        // Extract the path after /orders
        String fullPath = requestURI.substring(contextPath.length() + servletPath.length());

        System.out.println("[OrdersServlet] DELETE Request - Full Path: " + fullPath);

        if (fullPath != null && fullPath.startsWith("/")) {
            try {
                int orderId = Integer.parseInt(fullPath.substring(1));
                System.out.println("[OrdersServlet] Deleting order: " + orderId);

                // Delete the order directly using DAO
                boolean deleted = OrderDAO.deleteOrder(orderId);

                if (deleted) {
                    response.getWriter().write("{\"success\": true, \"message\": \"Order " + orderId + " deleted successfully\"}");
                    System.out.println("[OrdersServlet] Successfully deleted order " + orderId + " from database");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"success\": false, \"error\": \"Order " + orderId + " not found or could not be deleted\"}");
                    System.err.println("[OrdersServlet] Failed to delete order " + orderId + " from database");
                }
                return;

            } catch (NumberFormatException e) {
                System.err.println("[OrdersServlet] Invalid order ID in path: " + fullPath);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid order ID\", \"path\": \"" + fullPath + "\"}");
                return;
            } catch (Exception e) {
                System.err.println("[OrdersServlet] Error deleting order: " + e.getMessage());
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"success\": false, \"error\": \"Failed to delete order\", \"message\": \"" + e.getMessage() + "\"}");
                return;
            }
        }

        System.err.println("[OrdersServlet] Invalid DELETE request format: " + fullPath);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\": \"Order ID required\", \"receivedPath\": \"" + fullPath + "\"}");
    }
}