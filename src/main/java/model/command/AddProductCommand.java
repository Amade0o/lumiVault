package model.command;

import database.DatabaseConnection;
import model.Product;
import model.Movement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Command to add products to the database.
 * Implements the Command Pattern.
 */
public class AddProductCommand implements Command {
    
    private Product productToAdd;
    
    public AddProductCommand(Product product) {
        this.productToAdd = product;
    }
    
    /**
     * Executes the add product command.
     * 
     * @throws SQLException if a database error occurs
     */
    @Override
    public void execute() throws SQLException {
        insertProduct();
    }
    
    /**
     * Inserts a product into the database.
     */
    private String insertProduct() throws SQLException {
        // Validate product data
        validateProduct();
        
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        boolean hasExistingId = productToAdd.getId() != null && !productToAdd.getId().trim().isEmpty();
        
        if (hasExistingId && productIdExists(productToAdd.getId())) {
            throw new SQLException("Product ID " + productToAdd.getId() + " already exists");
        }
        
        if (!hasExistingId) {
            throw new SQLException("Product ID must be provided (e.g., barcode)");
        }
        
        String insertProductSql = """
            INSERT INTO producto (id_producto, nombre, precio_costo, precio_venta, cantidad_stock, id_categoria)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement statement = connection.prepareStatement(insertProductSql)) {
            statement.setString(1, productToAdd.getId());
            statement.setString(2, productToAdd.getName());
            statement.setDouble(3, productToAdd.getCostPrice());
            statement.setDouble(4, productToAdd.getSalePrice());
            statement.setInt(5, 0);
            
            if (productToAdd.getCategory() != null) {
                statement.setInt(6, productToAdd.getCategory().getId());
            } else {
                statement.setNull(6, java.sql.Types.INTEGER);
            }
            
            statement.executeUpdate();
        }
        
        recordProductCreationMovement();
        
        if (productToAdd.getStockQuantity() > 0) {
            recordInitialStockMovement();
        }
        
        return productToAdd.getId();
    }
    
    /**
     * Records the product creation movement.
     */
    private void recordProductCreationMovement() throws SQLException {
        Movement productCreatedMovement = new Movement();
        productCreatedMovement.setProduct(productToAdd);
        productCreatedMovement.setType(Movement.MovementType.PRODUCT_CREATED);
        productCreatedMovement.setDescription("Producto creado");
        productCreatedMovement.setDateTime(LocalDateTime.now());

        AddStockCommand productCreatedCommand = new AddStockCommand(productCreatedMovement);
        productCreatedCommand.execute();
    }
    
    /**
     * Records the initial stock entry movement.
     */
    private void recordInitialStockMovement() throws SQLException {
        Movement stockEntryMovement = new Movement();
        stockEntryMovement.setProduct(productToAdd);
        stockEntryMovement.setQuantity(productToAdd.getStockQuantity());
        stockEntryMovement.setType(Movement.MovementType.STOCK_ENTRY);
        stockEntryMovement.setDescription("Stock inicial al crear producto");
        stockEntryMovement.setDateTime(LocalDateTime.now());

        AddStockCommand stockCommand = new AddStockCommand(stockEntryMovement);
        stockCommand.execute();
    }
    
    /**
     * Checks if a product ID already exists in the database.
     */
    private boolean productIdExists(String productId) throws SQLException {
        String checkIdSql = "SELECT COUNT(*) FROM producto WHERE id_producto = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(checkIdSql)) {
            statement.setString(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Validates product data before insertion.
     * Throws SQLException for invalid data.
     */
    private void validateProduct() throws SQLException {
        if (productToAdd.getCostPrice() < 0) {
            throw new SQLException("Cost price cannot be negative");
        }
        if (productToAdd.getSalePrice() < 0) {
            throw new SQLException("Sale price cannot be negative");
        }
        if (productToAdd.getStockQuantity() < 0) {
            throw new SQLException("Stock quantity cannot be negative");
        }
        
        // Validate unique product name
        if (productNameExists(productToAdd.getName())) {
            throw new SQLException("Product name '" + productToAdd.getName() + "' already exists");
        }
    }
    
    /**
     * Checks if a product name already exists in the database.
     */
    private boolean productNameExists(String productName) throws SQLException {
        String checkNameSql = "SELECT COUNT(*) FROM producto WHERE nombre = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(checkNameSql)) {
            statement.setString(1, productName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
