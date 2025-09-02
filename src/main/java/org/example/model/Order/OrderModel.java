package org.example.model.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class OrderModel {
    private int orderId;
    private int user_id;
    private LocalDateTime orderDate;
    private String order_status;
    private ArrayList<Integer> item_ids;
    private ArrayList<Integer> item_options;
    private ArrayList<Integer> item_notes_ids;




    public OrderModel() {}

    public OrderModel(int orderId, int user_id, LocalDateTime orderDate, String order_status) {
        this.orderId = orderId;
        this.user_id = user_id;
        this.orderDate = orderDate;
        this.order_status = order_status;
        this.item_ids = new ArrayList<>();
        this.item_options = new ArrayList<>();
        this.item_notes_ids = new ArrayList<>();
    }

    public OrderModel(int user_id, LocalDateTime orderDate, String status) {
        this.user_id = user_id;
        this.orderDate = orderDate;
        this.order_status = order_status;
        this.item_ids = new ArrayList<>();
        this.item_options = new ArrayList<>();
        this.item_notes_ids = new ArrayList<>();
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return order_status; }
    public void setStatus(String order_status) { this.order_status = order_status; }

    public ArrayList<Integer> getOrderItemIds() { return item_ids; }
    public void setOrderItemIds(ArrayList<Integer> menuItemIds) { this.item_ids = menuItemIds; }
    public void addOrderItemId(int menuItemId) { this.item_ids.add(menuItemId); }
    public void removeOrderItemId(int menuItemId) { this.item_ids.remove(Integer.valueOf(menuItemId)); }

    public ArrayList<Integer> getItemOptions() { return item_options; }
    public void setItemOptions(ArrayList<Integer> item_options) { this.item_options = item_options; }

    public ArrayList<Integer> getItemNotesIds() { return item_notes_ids; }
    public void setItemNotesIds(ArrayList<Integer> item_notes_ids) { this.item_notes_ids = item_notes_ids; }



    public void showOrderDetails() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Employee ID: " + user_id);
        System.out.println("Order Date: " + orderDate);
        System.out.println("Status: " + order_status);
        System.out.println("Menu Item IDs: " + item_ids);
    }
}

