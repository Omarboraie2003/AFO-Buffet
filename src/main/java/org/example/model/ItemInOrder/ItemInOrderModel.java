package org.example.model.ItemInOrder;

public class ItemInOrderModel {
    private int id;
    private int item_id;
    private String typeOfBread;
    private String quantity;
    private String note;

    public ItemInOrderModel() {}
    public ItemInOrderModel(int id, int item_id, String typeOfBread, String quantity, String note) {
        this.id = id;
        this.item_id = item_id;
        this.typeOfBread = typeOfBread;
        this.quantity = quantity;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return item_id; }
    public void setItemId(int item_id) { this.item_id = item_id; }

    public String getTypeOfBread() { return typeOfBread; }
    public void setTypeOfBread(String typeOfBread) { this.typeOfBread = typeOfBread; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
