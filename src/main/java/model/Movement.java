package model;

import java.time.LocalDateTime;

/**
 * Represents a general movement/operation in the system.
 * Tracks all important operations for audit purposes.
 */
public class Movement {

    private int id;
    private Product product;        // Optional: only for product-related movements
    private Category category;      // Optional: only for category-related movements
    private Integer quantity;       // Optional: only for stock movements
    private MovementType type;
    private String description;     // Optional: additional details
    private LocalDateTime dateTime;

    /**
     * Type of system movement/operation.
     */
    public enum MovementType {
        // Product operations
        PRODUCT_CREATED,
        PRODUCT_UPDATED,
        PRODUCT_DELETED,

        // Category operations
        CATEGORY_CREATED,
        CATEGORY_UPDATED,
        CATEGORY_DELETED,

        // Stock operations
        STOCK_ENTRY,   // Increases stock (purchase/return)
        STOCK_EXIT     // Decreases stock (sale/loss)
    }

    /** Empty constructor */
    public Movement() {
    }

    /** Full constructor with all fields */
    public Movement(int id, Product product, Category category, Integer quantity,
                    MovementType type, String description, LocalDateTime dateTime) {
        this.id = id;
        this.product = product;
        this.category = category;
        this.quantity = quantity;
        this.type = type;
        this.description = description;
        this.dateTime = dateTime;
    }

    // ==================== GETTERS ====================
    
    public int getId() { 
        return id; 
    }
    
    public Product getProduct() { 
        return product; 
    }
    
    public Category getCategory() { 
        return category; 
    }
    
    public Integer getQuantity() { 
        return quantity; 
    }
    
    public MovementType getType() { 
        return type; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public LocalDateTime getDateTime() { 
        return dateTime; 
    }

    // ==================== SETTERS ====================
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public void setProduct(Product product) { 
        this.product = product; 
    }
    
    public void setCategory(Category category) { 
        this.category = category; 
    }
    
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
    }
    
    public void setType(MovementType type) { 
        this.type = type; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public void setDateTime(LocalDateTime dateTime) { 
        this.dateTime = dateTime; 
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Movement{id=").append(id);
        result.append(", type=").append(type);
        if (product != null) {
            result.append(", product='").append(product.getName()).append("'");
        }
        if (category != null) {
            result.append(", category='").append(category.getName()).append("'");
        }
        if (quantity != null) {
            result.append(", quantity=").append(quantity);
        }
        if (description != null) {
            result.append(", description='").append(description).append("'");
        }
        if (dateTime != null) {
            result.append(", dateTime=").append(dateTime);
        }
        result.append("}");
        return result.toString();
    }
}
