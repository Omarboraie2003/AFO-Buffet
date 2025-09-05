package org.example.model.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class OrderModel {
    private int orderId;
    private int user_id;
    private int numberOfItems;
    private String order_status;
    private String order_note;
    private LocalDateTime orderDate;
    private ArrayList<Integer> item_in_order_ids; // key: index, value: item_in_order_id

    public OrderModel() {}
    public OrderModel(int user_id, LocalDateTime orderDate, String status) {
        this.user_id = user_id;
        this.orderDate = orderDate;
        this.order_status = status;
        this.order_note = null;
        this.numberOfItems = 0;
        this.item_in_order_ids = new ArrayList<>();
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return order_status; }
    public void setStatus(String status) { this.order_status = status; }

    public void addItemToOrder(int ItemInOrderId) {
        this.item_in_order_ids.add(numberOfItems, ItemInOrderId);
        this.numberOfItems = this.item_in_order_ids.size();
    }
//    public void removeItemFromOrder(int menuItemId) {
//        this.item_ids.remove(Integer.valueOf(menuItemId));
//        this.numberOfItems = this.item_ids.size();
//    }

    public int getNumberOfItems() { return numberOfItems; }
    public void setNumberOfItems(int numberOfItems) { this.numberOfItems = numberOfItems; }


    public String getOrderNote() { return order_note; }
    public void setOrderNote(String order_note) { this.order_note = order_note; }

    public void showOrderDetails() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Employee ID: " + user_id);
        System.out.println("Order Date: " + orderDate);
        System.out.println("Status: " + order_status);
        System.out.println("Number of Items: " + numberOfItems);
        System.out.println("Item in Order IDs: " + item_in_order_ids);
        System.out.println("Order Note: " + order_note);
    }
}

