package model.factory;

import model.Category;
import model.Product;

/**
 * Factory class for creating Product instances.
 * Provides methods to create products with validation.
 */
public class ProductFactory {
    
    /**
     * Creates a new product with all fields.
     * 
     * @param name Product name
     * @param costPrice Cost price (must be >= 0)
     * @param salePrice Sale price (must be >= 0)
     * @param stockQuantity Initial stock quantity (must be >= 0)
     * @param category Product category (can be null)
     * @return A new Product instance
     * @throws IllegalArgumentException if validation fails
     */
    public static Product createProduct(String name, double costPrice, double salePrice,
                                         int stockQuantity, Category category) {
        validateProductName(name);
        validateCostPrice(costPrice);
        validateSalePrice(salePrice);
        validateStockQuantity(stockQuantity);
        
        Product product = new Product();
        product.setName(name.trim());
        product.setCostPrice(costPrice);
        product.setSalePrice(salePrice);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        
        return product;
    }
    
    /**
     * Creates a new product without category.
     * 
     * @param name Product name
     * @param costPrice Cost price
     * @param salePrice Sale price
     * @param stockQuantity Initial stock quantity
     * @return A new Product instance
     */
    public static Product createProduct(String name, double costPrice, 
                                         double salePrice, int stockQuantity) {
        return createProduct(name, costPrice, salePrice, stockQuantity, null);
    }
    
    /**
     * Creates a product with only name and prices (stock = 0).
     * Useful when adding a new product that hasn't arrived yet.
     * 
     * @param name Product name
     * @param costPrice Cost price
     * @param salePrice Sale price
     * @return A new Product instance with stock = 0
     */
    public static Product createProductWithoutStock(String name, double costPrice, 
                                                     double salePrice) {
        return createProduct(name, costPrice, salePrice, 0, null);
    }
    
    /**
     * Validates that product name is not null or empty.
     */
    private static void validateProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
    }
    
    private static void validateCostPrice(double costPrice) {
        if (costPrice < 0) {
            throw new IllegalArgumentException("Cost price must be >= 0");
        }
    }
    
    private static void validateSalePrice(double salePrice) {
        if (salePrice < 0) {
            throw new IllegalArgumentException("Sale price must be >= 0");
        }
    }
    
    private static void validateStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must be >= 0");
        }
    }
}
