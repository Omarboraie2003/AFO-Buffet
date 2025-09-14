package org.example.model.Order;

import java.util.ArrayList;

public class TestOrderDAO {

    public static void main(String[] args) {
        System.out.println("=== Testing getAllOrderDetails() ===\n");

        try {
            // Call the method
            ArrayList<OrderDetailsDTO> orders = OrderDAO.getAllOrderDetails();

            // Basic validation
            if (orders == null) {
                System.out.println("❌ ERROR: Method returned null!");
                return;
            }

            System.out.println("✅ Method executed successfully!");
            System.out.println("📊 Total orders retrieved: " + orders.size());

            if (orders.isEmpty()) {
                System.out.println("ℹ️  No orders found in database (or no orders with items)");
                return;
            }

            // Display summary
            System.out.println("\n=== ORDER SUMMARY ===");
            int totalItems = 0;

            for (int i = 0; i < orders.size(); i++) {
                OrderDetailsDTO order = orders.get(i);
                System.out.println((i + 1) + ". Order ID: " + order.getOrderId() +
                        " | User: " + order.getUsername() +
                        " | Items: " + order.getItems().size() +
                        " | Status: " + order.getOrderStatus());
                totalItems += order.getItems().size();
            }

            System.out.println("\n📈 STATISTICS:");
            System.out.println("   Total Orders: " + orders.size());
            System.out.println("   Total Items: " + totalItems);
            System.out.println("   Average Items per Order: " +
                    String.format("%.1f", (double)totalItems / orders.size()));

            // Display first order details (if exists)
            if (!orders.isEmpty()) {
                System.out.println("\n=== DETAILED VIEW (First Order) ===");
                orders.get(0).showOrderDetails();
            }

            // Display first 3 orders briefly
            System.out.println("\n=== FIRST 3 ORDERS (Brief) ===");
            for (int i = 0; i < Math.min(3, orders.size()); i++) {
                OrderDetailsDTO order = orders.get(i);
                System.out.println("\nOrder " + (i + 1) + ":");
                System.out.println("  ID: " + order.getOrderId() +
                        ", User: " + order.getUsername() +
                        ", Time: " + order.getOrderTime() +
                        ", Status: " + order.getOrderStatus());

                for (int j = 0; j < order.getItems().size(); j++) {
                    var item = order.getItems().get(j);
                    System.out.println("    Item " + (j + 1) + ": " +
                            item.getItemName() + " (" +
                            item.getTypeOfBread() + ") x" +
                            item.getQuantity() +
                            (item.getItemNote() != null ? " - " + item.getItemNote() : ""));
                }
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR occurred during testing:");
            System.out.println("   Message: " + e.getMessage());
            System.out.println("   Type: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }

        System.out.println("\n=== Test Complete ===");
    }

    // Additional test method to check specific aspects
    public static void testDataIntegrity() {
        System.out.println("\n=== DATA INTEGRITY TEST ===");

        try {
            ArrayList<OrderDetailsDTO> orders = OrderDAO.getAllOrderDetails();

            if (orders == null || orders.isEmpty()) {
                System.out.println("No data to test integrity");
                return;
            }

            boolean hasIssues = false;

            for (OrderDetailsDTO order : orders) {
                // Check for null values
                if (order.getOrderId() <= 0) {
                    System.out.println("⚠️  Invalid Order ID: " + order.getOrderId());
                    hasIssues = true;
                }

                if (order.getUsername() == null || order.getUsername().trim().isEmpty()) {
                    System.out.println("⚠️  Missing username for Order ID: " + order.getOrderId());
                    hasIssues = true;
                }

                if (order.getOrderTime() == null) {
                    System.out.println("⚠️  Missing order time for Order ID: " + order.getOrderId());
                    hasIssues = true;
                }

                if (order.getItems() == null || order.getItems().isEmpty()) {
                    System.out.println("⚠️  Order has no items - Order ID: " + order.getOrderId());
                    hasIssues = true;
                }
            }

            if (!hasIssues) {
                System.out.println("✅ All data integrity checks passed!");
            }

        } catch (Exception e) {
            System.out.println("❌ Error during integrity test: " + e.getMessage());
        }
    }
}