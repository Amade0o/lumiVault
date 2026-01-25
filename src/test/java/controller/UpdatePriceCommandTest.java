package controller;

import model.Product;
import model.command.UpdatePriceCommand;
import database.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for UpdatePriceCommand.
 * Tests price update functionality and validations.
 */
class UpdatePriceCommandTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
        // Create a test product for price updates
        createTestProduct("TEST_PRICE_001", "Test Product", 10.0, 15.0);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up test data
        deleteTestProduct("TEST_PRICE_001");
    }

    @Test
    void shouldCreateUpdatePriceCommand() {
        // Given
        Product product = new Product();
        product.setId("TEST_PRICE_001");
        double newCostPrice = 12.0;
        double newSalePrice = 18.0;

        // When
        UpdatePriceCommand command = new UpdatePriceCommand(product, newCostPrice, newSalePrice);

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    void shouldUpdatePricesSuccessfully() throws SQLException {
        // Given
        Product product = new Product();
        product.setId("TEST_PRICE_001");
        double newCostPrice = 12.0;
        double newSalePrice = 18.0;
        UpdatePriceCommand command = new UpdatePriceCommand(product, newCostPrice, newSalePrice);

        // When
        command.execute();

        // Then
        double[] prices = getProductPrices("TEST_PRICE_001");
        assertThat(prices[0]).isEqualTo(newCostPrice);
        assertThat(prices[1]).isEqualTo(newSalePrice);
    }

    @Test
    void shouldRejectNegativeCostPrice() {
        // Given
        Product product = new Product();
        product.setId("TEST_PRICE_001");
        double newCostPrice = -5.0;
        double newSalePrice = 18.0;
        UpdatePriceCommand command = new UpdatePriceCommand(product, newCostPrice, newSalePrice);

        // When / Then
        assertThatThrownBy(command::execute)
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("precio de costo no puede ser negativo");
    }

    @Test
    void shouldRejectNegativeSalePrice() {
        // Given
        Product product = new Product();
        product.setId("TEST_PRICE_001");
        double newCostPrice = 12.0;
        double newSalePrice = -10.0;
        UpdatePriceCommand command = new UpdatePriceCommand(product, newCostPrice, newSalePrice);

        // When / Then
        assertThatThrownBy(command::execute)
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("precio de venta no puede ser negativo");
    }

    @Test
    void shouldRejectSalePriceLowerThanCostPrice() {
        // Given
        Product product = new Product();
        product.setId("TEST_PRICE_001");
        double newCostPrice = 20.0;
        double newSalePrice = 15.0;
        UpdatePriceCommand command = new UpdatePriceCommand(product, newCostPrice, newSalePrice);

        // When / Then
        assertThatThrownBy(command::execute)
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("precio de venta no puede ser menor que el precio de costo");
    }

    @Test
    void shouldRejectNonExistentProduct() {
        // Given
        Product product = new Product();
        product.setId("NONEXISTENT_ID");
        double newCostPrice = 12.0;
        double newSalePrice = 18.0;
        UpdatePriceCommand command = new UpdatePriceCommand(product, newCostPrice, newSalePrice);

        // When / Then
        assertThatThrownBy(command::execute)
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("no existe");
    }

    @Test
    void shouldRejectNullProductId() {
        // Given
        Product product = new Product();
        product.setId(null);
        double newCostPrice = 12.0;
        double newSalePrice = 18.0;
        UpdatePriceCommand command = new UpdatePriceCommand(product, newCostPrice, newSalePrice);

        // When / Then
        assertThatThrownBy(command::execute)
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("ID del producto es requerido");
    }

    @Test
    void shouldRejectEmptyProductId() {
        // Given
        Product product = new Product();
        product.setId("   ");
        double newCostPrice = 12.0;
        double newSalePrice = 18.0;
        UpdatePriceCommand command = new UpdatePriceCommand(product, newCostPrice, newSalePrice);

        // When / Then
        assertThatThrownBy(command::execute)
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("ID del producto es requerido");
    }

    @Test
    void shouldRecordPriceChangeMovement() throws SQLException {
        // Given
        Product product = new Product();
        product.setId("TEST_PRICE_001");
        double newCostPrice = 12.0;
        double newSalePrice = 18.0;
        UpdatePriceCommand command = new UpdatePriceCommand(product, newCostPrice, newSalePrice);

        // When
        command.execute();

        // Then
        boolean movementRecorded = checkPriceUpdateMovementExists("TEST_PRICE_001");
        assertThat(movementRecorded).isTrue();
    }

    @Test
    void shouldAllowSameCostAndSalePrice() throws SQLException {
        // Given
        Product product = new Product();
        product.setId("TEST_PRICE_001");
        double newPrice = 15.0;
        UpdatePriceCommand command = new UpdatePriceCommand(product, newPrice, newPrice);

        // When
        command.execute();

        // Then
        double[] prices = getProductPrices("TEST_PRICE_001");
        assertThat(prices[0]).isEqualTo(newPrice);
        assertThat(prices[1]).isEqualTo(newPrice);
    }

    // ==================== HELPER METHODS ====================

    private void createTestProduct(String id, String name, double costPrice, double salePrice) throws SQLException {
        String sql = """
            INSERT INTO producto (id_producto, nombre, precio_costo, precio_venta, cantidad_stock)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setDouble(3, costPrice);
            statement.setDouble(4, salePrice);
            statement.setInt(5, 0);
            statement.executeUpdate();
        }
    }

    private void deleteTestProduct(String id) throws SQLException {
        // Delete movements first (foreign key constraint)
        String deleteMovements = "DELETE FROM movimiento WHERE id_producto = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteMovements)) {
            statement.setString(1, id);
            statement.executeUpdate();
        }

        // Delete product
        String deleteProduct = "DELETE FROM producto WHERE id_producto = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteProduct)) {
            statement.setString(1, id);
            statement.executeUpdate();
        }
    }

    private double[] getProductPrices(String id) throws SQLException {
        String sql = "SELECT precio_costo, precio_venta FROM producto WHERE id_producto = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new double[]{
                        resultSet.getDouble("precio_costo"),
                        resultSet.getDouble("precio_venta")
                    };
                }
            }
        }
        return new double[]{0.0, 0.0};
    }

    private boolean checkPriceUpdateMovementExists(String productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM movimiento WHERE id_producto = ? AND tipo = 'PRICE_UPDATE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
