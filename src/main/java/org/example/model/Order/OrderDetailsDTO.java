package org.example.model.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Main DTO class for order details
public class OrderDetailsDTO {
    private int orderId;
    private String username;
    private String firstName;     // New field
    private String lastName;      // New field
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

    // Constructor with names
    public OrderDetailsDTO(int orderId, String username, String firstName, String lastName, LocalDateTime orderTime, String orderStatus) {
        this.orderId = orderId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.orderTime = orderTime;
        this.orderStatus = orderStatus;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

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

    // Convenience method to get full name
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            fullName.append(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName.trim());
        }
        return fullName.length() > 0 ? fullName.toString() : null;
    }

    // Helper method to get display name (full name if available, otherwise username)
    public String getDisplayName() {
        String fullName = getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        return username != null ? username : "Unknown Customer";
    }

    // Helper method to display order details
    public void showOrderDetails() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Username: " + username);
        System.out.println("Customer Name: " + getDisplayName());
        System.out.println("Order Time: " + orderTime);
        System.out.println("Order Status: " + orderStatus);
        System.out.println("Items (" + items.size() + "):");
        for (int i = 0; i < items.size(); i++) {
            System.out.println("  Item " + (i + 1) + ":");
            items.get(i).showItemDetails();
        }
    }
}