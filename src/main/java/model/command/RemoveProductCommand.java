package model.command;

import database.DatabaseConnection;
import model.Movement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Command to remove a product from the database.
 * Implements the Command Pattern.
 */
public class RemoveProductCommand implements Command {
    
    private final String productId;
    
    public RemoveProductCommand(String productId) {
        this.productId = productId;
    }
    
    /**
     * Executes the remove product command.
     * 
     * @throws SQLException if a database error occurs
     */
    @Override
    public void execute() throws SQLException {
        deleteProduct();
    }
    
    private void deleteProduct() throws SQLException {
        ProductInfo productInfo = getProductInfo(productId);
        if (productInfo == null) {
            throw new SQLException("Product not found: " + productId);
        }
        
        if (productInfo.stockQuantity > 0) {
            try {
                recordExitMovement(productInfo);
            } catch (SQLException exception) {
                System.err.println("Warning: Could not record exit movement for product ID " + productId + ": " + exception.getMessage());
            }
        }
        
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        String deleteMovementsSql = "DELETE FROM movimiento WHERE id_producto = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteMovementsSql)) {
            statement.setString(1, productId);
            statement.executeUpdate();
        }
        
        String deleteProductSql = "DELETE FROM producto WHERE id_producto = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(deleteProductSql)) {
            statement.setString(1, productId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Product not found: " + productId);
            }
        }
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
