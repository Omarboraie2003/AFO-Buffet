package org.example.model.ItemInOrder;

public class ItemInOrderModel {
    private int item_in_order_id;
    private int item_id;
    private String type_of_bread;
    private String quantity;
    private String note;

    public ItemInOrderModel() {}
    public ItemInOrderModel(int item_in_order_id, int item_id, String type_of_bread, String quantity, String note) {
        this.item_in_order_id = item_in_order_id;
        this.item_id = item_id;
        this.type_of_bread = type_of_bread;
        this.quantity = quantity;
        this.note = note;
    }

    public int getItemInOrderId() { return item_in_order_id; }
    public void setItemInOrderId(int item_in_order_id) { this.item_in_order_id = item_in_order_id; }

    public int getItemId() { return item_id; }
    public void setItemId(int item_id) { this.item_id = item_id; }

    public String getTypeOfBread() { return type_of_bread; }
    public void setTypeOfBread(String type_of_bread) { this.type_of_bread = type_of_bread; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public void showInfo() {
        System.out.println("ItemInOrder ID: " + item_in_order_id);
        System.out.println("Item ID: " + item_id);
        System.out.println("Type of Bread: " + type_of_bread);
        System.out.println("Quantity: " + quantity);
        System.out.println("Note: " + note);
    }
}
