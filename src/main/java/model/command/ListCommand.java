package model.command;

import database.DatabaseConnection;
import model.Category;
import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to list products or categories from the database.
 * Implements the Command Pattern.
 */
public class ListCommand implements Command {
    
    public enum EntityType {
        PRODUCT,
        CATEGORY
    }
    
    private final EntityType entityType;
    
    public ListCommand(EntityType entityType) {
        this.entityType = entityType;
    }
    
    /**
     * Executes the list command.
     * 
     * @return A list of products or categories
     * @throws SQLException if a database error occurs
     */
    @Override
    public List<?> execute() throws SQLException {
        return switch (entityType) {
            case PRODUCT -> getAllProducts();
            case CATEGORY -> getAllCategories();
        };
    }
    
    private List<Product> getAllProducts() throws SQLException {
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
                Product product = mapResultSetToProduct(resultSet);
                products.add(product);
            }
        }
        return products;
    }
    
    private List<Category> getAllCategories() throws SQLException {
        String selectAllCategoriesSql = """
            SELECT id_categoria, nombre, descripcion
            FROM categoria
            """;
        
        List<Category> categories = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(selectAllCategoriesSql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Category category = mapResultSetToCategory(resultSet);
                categories.add(category);
            }
        }
        return categories;
    }
    
    private Product mapResultSetToProduct(ResultSet resultSet) throws SQLException {
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
        
        return product;
    }
    
    private Category mapResultSetToCategory(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setId(resultSet.getInt("id_categoria"));
        category.setName(resultSet.getString("nombre"));
        category.setDescription(resultSet.getString("descripcion"));
        return category;
    }
}
