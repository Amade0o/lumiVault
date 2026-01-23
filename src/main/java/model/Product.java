package model;

/**
 * Represents a product in the inventory system.
 * Contains pricing, stock quantity and category information.
 */
public class Product {
    
    private String id;
    private String name;
    private double costPrice;
    private double salePrice;
    private int stockQuantity;
    private Category category;

    /** Empty constructor required for Factory pattern */
    public Product() {
    }

    /** Full constructor with all fields */
    public Product(String id, String name, double costPrice, double salePrice, 
                   int stockQuantity, Category category) {
        this.id = id;
        this.name = name;
        setCostPrice(costPrice);
        setSalePrice(salePrice);
        setStockQuantity(stockQuantity);
        this.category = category;
    }

    // ==================== GETTERS ====================
    
    public String getId() { 
        return id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public double getCostPrice() { 
        return costPrice; 
    }
    
    public double getSalePrice() { 
        return salePrice; 
    }
    
    public int getStockQuantity() { 
        return stockQuantity; 
    }
    
    public Category getCategory() { 
        return category; 
    }

    // ==================== SETTERS (with validation) ====================
    
    public void setId(String id) { 
        this.id = id; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public void setCostPrice(double costPrice) {
        if (costPrice < 0) {
            throw new IllegalArgumentException("Cost price must be >= 0");
        }
        this.costPrice = costPrice;
    }
    
    public void setSalePrice(double salePrice) {
        if (salePrice < 0) {
            throw new IllegalArgumentException("Sale price must be >= 0");
        }
        this.salePrice = salePrice;
    }
    
    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must be >= 0");
        }
        this.stockQuantity = stockQuantity;
    }
    
    public void setCategory(Category category) { 
        this.category = category; 
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', stock=" + stockQuantity + "}";
    }
}
