package org.example.model.MenuItem;

public class MenuItem {
    private int  item_id;
    private String item_name;
    private String item_description;
    private boolean is_available;
    private String item_type;       // existing field
    private String item_category ;   // new field
    private String photo_url;    // new field
    private boolean is_special;



    // Constructors
    public MenuItem() {}

    public MenuItem(int  item_id, String item_name, String item_description, boolean is_available, String item_type, String item_category , String photo_url,boolean is_special) {
        this. item_id =  item_id;
        this.item_name = item_name;
        this.item_description = item_description;
        this.is_available = is_available;
        this.item_type = item_type;
        this.item_category  = item_category ;
        this.photo_url = photo_url;
        this.is_special=is_special;
    }

    // Getters & Setters
    public int getId() { return  item_id; }
    public void setId(int  item_id) { this. item_id =  item_id; }

    public String getName() { return item_name; }
    public void setName(String item_name) { this.item_name = item_name; }

    public String getDescription() { return item_description; }
    public void setDescription(String item_description) { this.item_description = item_description; }



    public boolean isAvailable() { return is_available; }
    public void setAvailable(boolean available) { this.is_available = is_available; }

    public String getType() { return item_type; }
    public void setType(String item_type) { this.item_type = item_type; }

    public String getCategory() { return item_category ; }
    public void setCategory(String item_category ) { this.item_category  = item_category ; }

    public String getPhotoUrl() { return photo_url; }
    public void setPhotoUrl(String photo_url) { this.photo_url = photo_url; }

    public boolean isSpecial() { return is_special; }
    public void setSpecial(boolean is_special) { this.is_special = is_special; }
}
