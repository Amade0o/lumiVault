package model.command;

import database.DatabaseConnection;
import model.Movement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Command to add stock movements (entries/exits) to the database.
 * Updates product stock automatically based on movement type.
 * Implements the Command Pattern for user actions.
 */
public class AddStockCommand implements Command {

    private Movement movement;

    public AddStockCommand(Movement movement) {
        this.movement = movement;
    }

    /**
     * Executes the add stock command.
     *
     * @throws SQLException if a database error occurs
     */
    @Override
    public void execute() throws SQLException {
        insertMovement();
    }

    /**
     * Inserts a movement into the database and updates product stock.
     */
    private int insertMovement() throws SQLException {
        // Validate movement data
        validateMovement();
        
        String insertMovementSql = """
            INSERT INTO movimiento (id_producto, cantidad, tipo, descripcion, fecha)
            VALUES (?, ?, ?, ?, ?)
            """;

        Connection connection = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(
                insertMovementSql, Statement.RETURN_GENERATED_KEYS)) {

            if (movement.getProduct() != null) {
                statement.setString(1, movement.getProduct().getId());
            } else {
                statement.setNull(1, java.sql.Types.VARCHAR);
            }

            if (movement.getQuantity() != null) {
                statement.setInt(2, movement.getQuantity());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }

            statement.setString(3, movement.getType().toString());
            statement.setString(4, movement.getDescription());

            if (movement.getDateTime() != null) {
                statement.setString(5, movement.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                statement.setString(5, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    movement.setId(generatedId);

                    if (movement.getType() == Movement.MovementType.STOCK_ENTRY ||
                        movement.getType() == Movement.MovementType.STOCK_EXIT) {
                        updateProductStock(connection);
                    }

                    return generatedId;
                }
            }
        }
        return -1;
    }

    /**
     * Updates the product stock based on the movement type.
     * Only applies to STOCK_ENTRY and STOCK_EXIT movements.
     */
    private void updateProductStock(Connection connection) throws SQLException {
        if (movement.getProduct() == null || movement.getQuantity() == null) {
            return;
        }

        // Check insufficient stock for exits
        if (movement.getType() == Movement.MovementType.STOCK_EXIT) {
            int currentStock = getCurrentStock(connection);
            if (currentStock < movement.getQuantity()) {
                throw new SQLException("Insufficient stock for product " + movement.getProduct().getId() + 
                    ". Current: " + currentStock + ", Requested: " + movement.getQuantity());
            }
        }

        String updateStockSql;
        int quantity = movement.getQuantity();

        if (movement.getType() == Movement.MovementType.STOCK_ENTRY) {
            updateStockSql = "UPDATE producto SET cantidad_stock = cantidad_stock + ? WHERE id_producto = ?";
        } else { //STOCK_EXIT
            updateStockSql = "UPDATE producto SET cantidad_stock = cantidad_stock - ? WHERE id_producto = ?";
        }

        try (PreparedStatement updateStatement = connection.prepareStatement(updateStockSql)) {
            updateStatement.setInt(1, quantity);
            updateStatement.setString(2, movement.getProduct().getId());

            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Product with ID " + movement.getProduct().getId() + " not found");
            }
        }
    }
    
    /**
     * Gets the current stock of a product.
     */
    private int getCurrentStock(Connection connection) throws SQLException {
        String query = "SELECT cantidad_stock FROM producto WHERE id_producto = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, movement.getProduct().getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cantidad_stock");
                }
            }
        }
        return 0;
    }
    
    /**
     * Validates movement data before insertion.
     * Throws SQLException for invalid data.
     */
    private void validateMovement() throws SQLException {
        // For stock entry/exit movements, validate quantity
        if (movement.getType() == Movement.MovementType.STOCK_ENTRY ||
            movement.getType() == Movement.MovementType.STOCK_EXIT) {
            
            if (movement.getProduct() == null) {
                throw new SQLException("Stock movement must have a product");
            }
            if (movement.getQuantity() == null || movement.getQuantity() <= 0) {
                throw new SQLException("Movement quantity must be greater than zero");
            }
        }
    }
}