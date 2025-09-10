package org.example.model.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Main DTO class for order details
public class OrderDetailsDTO {
    private int orderId;
    private String username;
    private LocalDateTime orderTime;
    private String orderStatus;
    private List<OrderItemDetail> items;

    // Constructors
    public OrderDetailsDTO() {
        this.items = new ArrayList<>();
    }

    public OrderDetailsDTO(int orderId, String username, LocalDateTime orderTime, String orderStatus) {
        this.orderId = orderId;
        this.username = username;
        this.orderTime = orderTime;
        this.orderStatus = orderStatus;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public List<OrderItemDetail> getItems() { return items; }
    public void setItems(List<OrderItemDetail> items) { this.items = items; }

    // Helper method to add items
    public void addItem(OrderItemDetail item) {
        this.items.add(item);
    }

    // Helper method to display order details
    public void showOrderDetails() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Username: " + username);
        System.out.println("Order Time: " + orderTime);
        System.out.println("Order Status: " + orderStatus);
        System.out.println("Items (" + items.size() + "):");
        for (int i = 0; i < items.size(); i++) {
            System.out.println("  Item " + (i + 1) + ":");
            items.get(i).showItemDetails();
        }
    }
}

