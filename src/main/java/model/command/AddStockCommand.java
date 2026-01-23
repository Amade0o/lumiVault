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
 * Implements the Command Pattern.
 */
public class AddStockCommand implements Command {

    private Movement movement;

    public AddStockCommand(Movement movement) {
        this.movement = movement;
    }

    /**
     * Executes the add stock command.
     *
     * @return The generated movement ID
     * @throws SQLException if a database error occurs
     */
    @Override
    public Integer execute() throws SQLException {
        return insertMovement();
    }

    /**
     * Inserts a movement into the database and updates product stock.
     */
    private int insertMovement() throws SQLException {
        String insertMovementSql = """
            INSERT INTO movimiento (id_producto, id_categoria, cantidad, tipo, descripcion, fecha)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        Connection connection = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(
                insertMovementSql, Statement.RETURN_GENERATED_KEYS)) {

            if (movement.getProduct() != null) {
                statement.setString(1, movement.getProduct().getId());
            } else {
                statement.setNull(1, java.sql.Types.VARCHAR);
            }

            if (movement.getCategory() != null) {
                statement.setInt(2, movement.getCategory().getId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }

            if (movement.getQuantity() != null) {
                statement.setInt(3, movement.getQuantity());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }

            statement.setString(4, movement.getType().toString());
            statement.setString(5, movement.getDescription());

            if (movement.getDateTime() != null) {
                statement.setString(6, movement.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                statement.setString(6, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
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
}