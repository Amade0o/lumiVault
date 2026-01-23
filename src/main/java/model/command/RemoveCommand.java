package model.command;

import database.DatabaseConnection;
import model.Movement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Command to remove products or categories from the database.
 * Implements the Command Pattern.
 */
public class RemoveCommand implements Command {
    
    public enum EntityType {
        PRODUCT,
        CATEGORY
    }
    
    private final EntityType entityType;
    private final Object entityId; // Can be String (for product) or int (for category)
    
    public RemoveCommand(EntityType entityType, String productId) {
        this.entityType = entityType;
        this.entityId = productId;
    }
    
    public RemoveCommand(EntityType entityType, int categoryId) {
        this.entityType = entityType;
        this.entityId = categoryId;
    }
    
    /**
     * Executes the remove command.
     * 
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    @Override
    public Boolean execute() throws SQLException {
        return switch (entityType) {
            case PRODUCT -> deleteProduct();
            case CATEGORY -> deleteCategory();
        };
    }
    
    private boolean deleteProduct() throws SQLException {
        ProductInfo productInfo = getProductInfo((String) entityId);
        if (productInfo == null) {
            return false;
        }
        
        if (productInfo.stockQuantity > 0) {
            try {
                recordExitMovement(productInfo);
            } catch (SQLException exception) {
                System.err.println("Warning: Could not record exit movement for product ID " + entityId + ": " + exception.getMessage());
            }
        }
        
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        String deleteMovementsSql = "DELETE FROM movimiento WHERE id_producto = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteMovementsSql)) {
            statement.setString(1, (String) entityId);
            statement.executeUpdate();
        }
        
        String deleteProductSql = "DELETE FROM producto WHERE id_producto = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(deleteProductSql)) {
            statement.setString(1, (String) entityId);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Deletes a category from the database.
     * Products with this category will have their category set to NULL.
     */
    private boolean deleteCategory() throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        boolean isSQLite = connection.getMetaData().getDriverName().toLowerCase().contains("sqlite");
        
        if (isSQLite) {
            try (PreparedStatement pragmaStatement = connection.prepareStatement("PRAGMA foreign_keys = OFF")) {
                pragmaStatement.execute();
            }
        }
        
        String deleteMovementsSql = "DELETE FROM movimiento WHERE id_categoria = ?";
        try (PreparedStatement deleteMovementsStatement = connection.prepareStatement(deleteMovementsSql)) {
            deleteMovementsStatement.setInt(1, (Integer) entityId);
            deleteMovementsStatement.executeUpdate();
        }
        
        String updateProductsCategorySql = """
            UPDATE producto SET id_categoria = NULL WHERE id_categoria = ?
            """;
        
        String deleteCategorySql = "DELETE FROM categoria WHERE id_categoria = ?";
        
        try (PreparedStatement updateStatement = connection.prepareStatement(updateProductsCategorySql)) {
            updateStatement.setInt(1, (Integer) entityId);
            updateStatement.executeUpdate();
        }
        
        int affectedRows;
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteCategorySql)) {
            deleteStatement.setInt(1, (Integer) entityId);
            affectedRows = deleteStatement.executeUpdate();
        }
        
        if (isSQLite) {
            try (PreparedStatement pragmaStatement = connection.prepareStatement("PRAGMA foreign_keys = ON")) {
                pragmaStatement.execute();
            }
        }
        
        return affectedRows > 0;
    }
    
    private static class ProductInfo {
        String id;
        String name;
        int stockQuantity;
        
        ProductInfo(String id, String name, int stockQuantity) {
            this.id = id;
            this.name = name;
            this.stockQuantity = stockQuantity;
        }
    }
    
    private ProductInfo getProductInfo(String productId) throws SQLException {
        String selectProductSql = "SELECT id_producto, nombre, cantidad_stock FROM producto WHERE id_producto = ?";
        
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(selectProductSql)) {
            statement.setString(1, productId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new ProductInfo(
                        resultSet.getString("id_producto"),
                        resultSet.getString("nombre"),
                        resultSet.getInt("cantidad_stock")
                    );
                }
            }
        }
        return null;
    }
    
    private void recordExitMovement(ProductInfo productInfo) throws SQLException {
        model.Product product = new model.Product();
        product.setId(productInfo.id);
        product.setName(productInfo.name);
        
        Movement exitMovement = new Movement();
        exitMovement.setProduct(product);
        exitMovement.setQuantity(productInfo.stockQuantity);
        exitMovement.setType(Movement.MovementType.STOCK_EXIT);
        exitMovement.setDescription("Producto eliminado - salida de stock");
        exitMovement.setDateTime(LocalDateTime.now());
        
        AddStockCommand movementCommand = new AddStockCommand(exitMovement);
        movementCommand.execute();
    }
}
