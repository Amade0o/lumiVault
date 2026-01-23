package model;

/**
 * Represents a product category in the inventory system.
 * Categories help organize products (e.g., "Blanquería", "Regalería").
 */
public class Category {
    
    private int id;
    private String name;
    private String description;

    /** Empty constructor */
    public Category() {
    }

    /** Full constructor with all fields */
    public Category(int id, String name, String description) {
        this.id = id;
        setName(name);
        this.description = description;
    }

    // ==================== GETTERS ====================
    
    public int getId() { 
        return id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public String getDescription() { 
        return description; 
    }

    // ==================== SETTERS ====================
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        this.name = name;
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }

    /** Returns name for display in UI components like ComboBox */
    @Override
    public String toString() {
        return name;
    }
}
