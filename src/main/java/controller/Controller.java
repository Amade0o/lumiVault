package controller;

import model.Category;
import model.Product;
import model.Movement;
import model.command.*;
import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller that coordinates commands and business logic.
 * Acts as the entry point for all inventory operations.
 * Commands modify state, queries are direct public methods.
 */
public class Controller {
    
    public Controller() {
    }
    
    // ==================== COMMAND EXECUTION ====================
    
    /**
     * Executes a command (user action that modifies state).
     * 
     * @param type The command type to execute
     * @param data The data for the command (Product, Category, ID, etc.)
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the command type is not recognized
     */
    public void executeCommand(CommandType type, Object data) throws SQLException {
        Command command;
        
        switch (type) {
            case ADD_PRODUCT:
                command = new AddProductCommand((Product) data);
                break;
            case REMOVE_PRODUCT:
                command = new RemoveProductCommand((String) data);
                break;
            case ADD_CATEGORY:
                command = new AddCategoryCommand((Category) data);
                break;
            case REMOVE_CATEGORY:
                command = new RemoveCategoryCommand((Integer) data);
                break;
            case ADD_STOCK:
            case REMOVE_STOCK:
                command = new AddStockCommand((Movement) data);
                break;
            default:
                throw new IllegalArgumentException("Command type not recognized: " + type);
        }
        
        command.execute();
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Gets all products from the inventory.
     * 
     * @return List of all products
     * @throws SQLException if a database error occurs
     */
    public List<Product> getAllProducts() throws SQLException {
        String selectAllProductsSql = """
            SELECT p.id_producto, p.nombre, p.precio_costo, p.precio_venta, 
                   p.cantidad_stock, p.id_categoria,
                   c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion
            FROM producto p
            LEFT JOIN categoria c ON p.id_categoria = c.id_categoria
            """;
        
        List<Product> products = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(selectAllProductsSql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getString("id_producto"));
                product.setName(resultSet.getString("nombre"));
                product.setCostPrice(resultSet.getDouble("precio_costo"));
                product.setSalePrice(resultSet.getDouble("precio_venta"));
                product.setStockQuantity(resultSet.getInt("cantidad_stock"));
                
                int categoryId = resultSet.getInt("id_categoria");
                if (!resultSet.wasNull()) {
                    Category category = new Category();
                    category.setId(categoryId);
                    category.setName(resultSet.getString("categoria_nombre"));
                    category.setDescription(resultSet.getString("categoria_descripcion"));
                    product.setCategory(category);
                }
                
                products.add(product);
            }
        }
        return products;
    }
    
    /**
     * Gets all categories.
     * 
     * @return List of all categories
     * @throws SQLException if a database error occurs
     */
    public List<Category> getAllCategories() throws SQLException {
        String selectAllCategoriesSql = """
            SELECT id_categoria, nombre, descripcion
            FROM categoria
            """;
        
        List<Category> categories = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(selectAllCategoriesSql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Category category = new Category();
                category.setId(resultSet.getInt("id_categoria"));
                category.setName(resultSet.getString("nombre"));
                category.setDescription(resultSet.getString("descripcion"));
                categories.add(category);
            }
        }
        return categories;
    }
    
    /**
     * Gets all stock movements for a specific product.
     * 
     * @param productId The product ID
     * @return List of movements for the product
     * @throws SQLException if a database error occurs
     */
    public List<Movement> getMovementsForProduct(String productId) throws SQLException {        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }        return MovementCommand.getMovementsForProduct(productId);
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
}
