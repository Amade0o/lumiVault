package controller;

import model.Category;
import model.Product;
import model.Movement;
import model.command.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Main controller that coordinates commands and business logic.
 * Acts as the entry point for all inventory operations.
 */
public class Controller {
    
    public Controller() {
    }
    
    // ==================== PRODUCT OPERATIONS ====================
    
    /**
     * Adds a new product to the inventory.
     * 
     * @param product The product to add (ID must be provided)
     * @return The product ID
     * @throws SQLException if a database error occurs
     */
    public String addProduct(Product product) throws SQLException {
        Command addCommand = new AddProductCommand(product);
        String productId = (String) addCommand.execute();
        return productId;
    }
    
    /**
     * Gets all products from the inventory.
     * 
     * @return List of all products
     * @throws SQLException if a database error occurs
     */
    public List<Product> getAllProducts() throws SQLException {
        ListCommand command = buildListProductsCommand();
        return (List<Product>) command.execute();
    }
    
    /**
     * Removes a product from the inventory.
     * 
     * @param productId The ID of the product to remove
     * @return true if removal was successful
     * @throws SQLException if a database error occurs
     */
    public boolean removeProduct(String productId) throws SQLException {
        Command removeCommand = new RemoveCommand(RemoveCommand.EntityType.PRODUCT, productId);
        return (Boolean) removeCommand.execute();
    }
    
    // ==================== CATEGORY OPERATIONS ====================
    
    /**
     * Adds a new category.
     * 
     * @param category The category to add
     * @return The generated category ID
     * @throws SQLException if a database error occurs
     */
    public int addCategory(Category category) throws SQLException {
        Command addCommand = new AddCategoryCommand(category);
        return (Integer) addCommand.execute();
    }
    
    /**
     * Gets all categories.
     * 
     * @return List of all categories
     * @throws SQLException if a database error occurs
     */
    public List<Category> getAllCategories() throws SQLException {
        ListCommand command = buildListCategoriesCommand();
        return (List<Category>) command.execute();
    }
    
    /**
     * Removes a category.
     * Products with this category will have their category set to NULL.
     * 
     * @param categoryId The ID of the category to remove
     * @return true if removal was successful
     * @throws SQLException if a database error occurs
     */
    public boolean removeCategory(int categoryId) throws SQLException {
        Command removeCommand = new RemoveCommand(RemoveCommand.EntityType.CATEGORY, categoryId);
        return (Boolean) removeCommand.execute();
    }
    
    // ==================== STOCK MOVEMENT OPERATIONS ====================
    
    /**
     * Records a stock movement (entry or exit).
     * 
     * @param movement The stock movement to record
     * @return The generated movement ID
     * @throws SQLException if a database error occurs
     */
    public int recordStockMovement(Movement movement) throws SQLException {
        Command addStockCommand = new AddStockCommand(movement);
        return (Integer) addStockCommand.execute();
    }
    
    /**
     * Gets all stock movements for a specific product.
     * 
     * @param productId The product ID
     * @return List of movements for the product
     * @throws SQLException if a database error occurs
     */
    public List<Movement> getMovementsForProduct(String productId) throws SQLException {
        return MovementCommand.getMovementsForProduct(productId);
    }
    
    /**
     * Gets all stock movements in the system.
     * 
     * @return List of all movements
     * @throws SQLException if a database error occurs
     */
    public List<Movement> getAllMovements() throws SQLException {
        return MovementCommand.getAllMovements();
    }
    
    // ==================== COMMAND EXECUTION ====================
    
    /**
     * Executes a command by name.
     * Currently supports: "listProducts", "listCategories"
     * 
     * @param commandName The name of the command to execute
     * @return The result of the command execution
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the command is not found
     */
    public Object executeCommand(String commandName) throws SQLException {
        switch (commandName) {
            case "listProducts":
                return getAllProducts();
            case "listCategories":
                return getAllCategories();
            default:
                throw new IllegalArgumentException("Command not found: " + commandName);
        }
    }
    
    /**
     * Gets all available command names.
     * 
     * @return Array of command names
     */
    public String[] getAvailableCommands() {
        return new String[]{"listProducts", "listCategories"};
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Builds a command for listing products.
     */
    private ListCommand buildListProductsCommand() {
        return new ListCommand(ListCommand.EntityType.PRODUCT);
    }
    
    /**
     * Builds a command for listing categories.
     */
    private ListCommand buildListCategoriesCommand() {
        return new ListCommand(ListCommand.EntityType.CATEGORY);
    }
}
