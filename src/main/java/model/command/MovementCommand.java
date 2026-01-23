package model.command;

import database.DatabaseConnection;
import model.Category;
import model.Movement;
import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Command for querying movements in the database.
 * Implements the Command Pattern for read operations.
 */
public class MovementCommand {

    /**
     * Gets all movements for a specific product.
     */
    public static List<Movement> getMovementsForProduct(String productId) throws SQLException {
        String selectMovementsSql = """
            SELECT m.id_movimiento, m.id_producto, m.id_categoria, m.cantidad, m.tipo, m.descripcion, m.fecha,
                   p.id_producto, p.nombre,
                   c.id_categoria, c.nombre as categoria_nombre
            FROM movimiento m
            LEFT JOIN producto p ON m.id_producto = p.id_producto
            LEFT JOIN categoria c ON m.id_categoria = c.id_categoria
            WHERE m.id_producto = ?
            ORDER BY m.fecha DESC
            """;

        List<Movement> movements = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(selectMovementsSql)) {
            statement.setString(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Movement movement = mapResultSetToMovement(resultSet);
                    movements.add(movement);
                }
            }
        }
        return movements;
    }

    /**
     * Gets all movements in the system.
     */
    public static List<Movement> getAllMovements() throws SQLException {
        String selectAllMovementsSql = """
            SELECT m.id_movimiento, m.id_producto, m.id_categoria, m.cantidad, m.tipo, m.descripcion, m.fecha,
                   p.id_producto, p.nombre,
                   c.id_categoria, c.nombre as categoria_nombre
            FROM movimiento m
            LEFT JOIN producto p ON m.id_producto = p.id_producto
            LEFT JOIN categoria c ON m.id_categoria = c.id_categoria
            ORDER BY m.fecha DESC
            """;

        List<Movement> movements = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(selectAllMovementsSql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Movement movement = mapResultSetToMovement(resultSet);
                movements.add(movement);
            }
        }
        return movements;
    }

    /**
     * Maps a ResultSet row to a Movement object.
     */
    private static Movement mapResultSetToMovement(ResultSet resultSet) throws SQLException {
        Movement movement = new Movement();
        movement.setId(resultSet.getInt("id_movimiento"));

        String productId = resultSet.getString("id_producto");
        if (!resultSet.wasNull()) {
            Product product = new Product();
            product.setId(productId);
            product.setName(resultSet.getString("nombre"));
            movement.setProduct(product);
        }

        int categoryId = resultSet.getInt("id_categoria");
        if (!resultSet.wasNull()) {
            Category category = new Category();
            category.setId(categoryId);
            category.setName(resultSet.getString("categoria_nombre"));
            movement.setCategory(category);
        }

        int quantity = resultSet.getInt("cantidad");
        if (!resultSet.wasNull()) {
            movement.setQuantity(quantity);
        }

        movement.setType(Movement.MovementType.valueOf(resultSet.getString("tipo")));
        movement.setDescription(resultSet.getString("descripcion"));

        String dateTimeString = resultSet.getString("fecha");
        if (dateTimeString != null) {
            movement.setDateTime(LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return movement;
    }
}