package model.command;

import database.DatabaseConnection;
import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Command to update product prices in the database.
 * Updates both cost price and sale price for a specific product.
 * Records the price change in the movement history.
 */
public class UpdatePriceCommand implements Command {
    
    private Product product;
    private double newCostPrice;
    private double newSalePrice;
    
    /**
     * Creates a new UpdatePriceCommand.
     * 
     * @param product The product with ID set
     * @param newCostPrice The new cost price
     * @param newSalePrice The new sale price
     */
    public UpdatePriceCommand(Product product, double newCostPrice, double newSalePrice) {
        this.product = product;
        this.newCostPrice = newCostPrice;
        this.newSalePrice = newSalePrice;
    }
    
    @Override
    public void execute() throws SQLException {
        validatePrices();
        validateProductExists();
        
        double oldCostPrice = getCurrentCostPrice();
        double oldSalePrice = getCurrentSalePrice();
        
        updateProductPrices();
        recordPriceChangeMovement(oldCostPrice, oldSalePrice);
    }
    
    /**
     * Validates that prices are positive.
     */
    private void validatePrices() throws SQLException {
        if (newCostPrice < 0) {
            throw new SQLException("El precio de costo no puede ser negativo");
        }
        if (newSalePrice < 0) {
            throw new SQLException("El precio de venta no puede ser negativo");
        }
        if (newSalePrice < newCostPrice) {
            throw new SQLException("El precio de venta no puede ser menor que el precio de costo");
        }
    }
    
    /**
     * Validates that the product exists in the database.
     */
    private void validateProductExists() throws SQLException {
        if (product.getId() == null || product.getId().trim().isEmpty()) {
            throw new SQLException("El ID del producto es requerido");
        }
        
        String checkSql = "SELECT id_producto FROM producto WHERE id_producto = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(checkSql)) {
            statement.setString(1, product.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Producto con ID " + product.getId() + " no existe");
                }
            }
        }
    }
    
    /**
     * Gets the current cost price from the database.
     */
    private double getCurrentCostPrice() throws SQLException {
        String selectSql = "SELECT precio_costo FROM producto WHERE id_producto = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setString(1, product.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("precio_costo");
                }
            }
        }
        return 0.0;
    }
    
    /**
     * Gets the current sale price from the database.
     */
    private double getCurrentSalePrice() throws SQLException {
        String selectSql = "SELECT precio_venta FROM producto WHERE id_producto = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setString(1, product.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("precio_venta");
                }
            }
        }
        return 0.0;
    }
    
    /**
     * Updates the product prices in the database.
     */
    private void updateProductPrices() throws SQLException {
        String updateSql = """
            UPDATE producto 
            SET precio_costo = ?, precio_venta = ? 
            WHERE id_producto = ?
            """;
        
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setDouble(1, newCostPrice);
            statement.setDouble(2, newSalePrice);
            statement.setString(3, product.getId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el precio del producto");
            }
        }
    }
    
    /**
     * Records the price change in the movement history.
     */
    private void recordPriceChangeMovement(double oldCostPrice, double oldSalePrice) throws SQLException {
        String insertMovementSql = """
            INSERT INTO movimiento (id_producto, tipo, cantidad, fecha, descripcion)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(insertMovementSql)) {
            statement.setString(1, product.getId());
            statement.setString(2, "PRICE_UPDATE");
            statement.setInt(3, 0);
            statement.setString(4, LocalDateTime.now().toString());
            
            String observations = String.format(
                "Actualización de precios: Costo $%.2f → $%.2f, Venta $%.2f → $%.2f",
                oldCostPrice, newCostPrice, oldSalePrice, newSalePrice
            );
            statement.setString(5, observations);
            
            statement.executeUpdate();
        }
   }
}
