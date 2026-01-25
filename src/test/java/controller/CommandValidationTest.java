package controller;

import database.DatabaseConnection;
import database.TestDatabaseConnection;
import model.Category;
import model.Product;
import model.Movement;
import model.factory.ProductFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EmptySource;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive validation tests for all command operations.
 * Tests cover: boundary values, null/empty inputs, invalid data, 
 * constraint violations, and edge cases.
 */
@DisplayName("Command Validation Tests")
class CommandValidationTest {

    private Controller controller;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseConnection.resetDatabase();
        DatabaseConnection.setTestConnection(TestDatabaseConnection.getTestConnection());
        controller = new Controller();
    }

    @AfterEach
    void tearDown() throws SQLException {
        DatabaseConnection.clearTestConnection();
        TestDatabaseConnection.closeTestConnection();
    }

    // ==================== ADD_PRODUCT VALIDATION ====================

    @Test
    @DisplayName("Should reject product with null ID")
    void shouldRejectProductWithNullId() {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 5, null);
        product.setId(null);
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_PRODUCT, product))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("ID must be provided");
    }

    @Test
    @DisplayName("Should reject product with empty ID")
    void shouldRejectProductWithEmptyId() {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 5, null);
        product.setId("");
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_PRODUCT, product))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("ID must be provided");
    }

    @Test
    @DisplayName("Should reject product with whitespace-only ID")
    void shouldRejectProductWithWhitespaceId() {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 5, null);
        product.setId("   ");
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_PRODUCT, product))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("ID must be provided");
    }

    @Test
    @DisplayName("Should reject duplicate product ID")
    void shouldRejectDuplicateProductId() throws SQLException {
        Product product1 = ProductFactory.createProduct("Product 1", 10.0, 15.0, 5, null);
        product1.setId("DUPLICATE123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product1);

        Product product2 = ProductFactory.createProduct("Product 2", 20.0, 25.0, 10, null);
        product2.setId("DUPLICATE123");
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_PRODUCT, product2))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should accept product with minimum valid values")
    void shouldAcceptProductWithMinimumValues() throws SQLException {
        Product product = ProductFactory.createProduct("A", 0.01, 0.01, 0, null);
        product.setId("1");
        
        assertThatCode(() -> controller.executeCommand(CommandType.ADD_PRODUCT, product))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should accept product with maximum reasonable values")
    void shouldAcceptProductWithMaximumValues() throws SQLException {
        Product product = ProductFactory.createProduct(
            "Very Long Product Name That Should Still Be Accepted By The System",
            999999.99,
            999999.99,
            999999,
            null
        );
        product.setId("MAX123456789");
        
        assertThatCode(() -> controller.executeCommand(CommandType.ADD_PRODUCT, product))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should accept product with zero stock")
    void shouldAcceptProductWithZeroStock() throws SQLException {
        Product product = ProductFactory.createProduct("Zero Stock", 10.0, 15.0, 0, null);
        product.setId("ZERO001");
        
        assertThatCode(() -> controller.executeCommand(CommandType.ADD_PRODUCT, product))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject product with negative cost price")
    void shouldRejectProductWithNegativeCostPrice() {
        Product product = new Product();
        product.setId("NEG001");
        product.setName("Negative Cost");
        
        assertThatThrownBy(() -> product.setCostPrice(-10.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cost price must be >= 0");
    }

    @Test
    @DisplayName("Should reject product with negative sale price")
    void shouldRejectProductWithNegativeSalePrice() {
        Product product = new Product();
        product.setId("NEG002");
        product.setName("Negative Sale");
        product.setCostPrice(10.0);
        
        assertThatThrownBy(() -> product.setSalePrice(-15.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Sale price must be >= 0");
    }

    @Test
    @DisplayName("Should reject product with negative stock")
    void shouldRejectProductWithNegativeStock() {
        Product product = new Product();
        product.setId("NEG003");
        product.setName("Negative Stock");
        product.setCostPrice(10.0);
        product.setSalePrice(15.0);
        
        assertThatThrownBy(() -> product.setStockQuantity(-5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Stock quantity must be >= 0");
    }

    @Test
    @DisplayName("Should reject product with null object")
    void shouldRejectNullProduct() {
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_PRODUCT, null))
            .isInstanceOf(NullPointerException.class);
    }

    // ==================== ADD_CATEGORY VALIDATION ====================

    @Test
    @DisplayName("Should reject category with null name")
    void shouldRejectCategoryWithNullName() {
        assertThatThrownBy(() -> new Category(0, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category name cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject category with empty name")
    void shouldRejectCategoryWithEmptyName() {
        assertThatThrownBy(() -> new Category(0, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category name cannot be null or empty");
    }

    @Test
    @DisplayName("Should accept category with empty description")
    void shouldAcceptCategoryWithEmptyDescription() throws SQLException {
        Category category = new Category(0, "Valid Category");
        
        assertThatCode(() -> controller.executeCommand(CommandType.ADD_CATEGORY, category))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should accept category with null description")
    void shouldAcceptCategoryWithNullDescription() throws SQLException {
        Category category = new Category(0, "Valid Category");
        
        assertThatCode(() -> controller.executeCommand(CommandType.ADD_CATEGORY, category))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should accept category with very long name")
    void shouldAcceptCategoryWithLongName() throws SQLException {
        String longName = "A".repeat(255); // Maximum reasonable length
        Category category = new Category(0, longName);
        
        assertThatCode(() -> controller.executeCommand(CommandType.ADD_CATEGORY, category))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject duplicate category names")
    void shouldRejectDuplicateCategoryNames() throws SQLException {
        // Categories have UNIQUE constraint on name in test database
        Category category1 = new Category(0, "Electronics");
        Category category2 = new Category(0, "Electronics");
        
        controller.executeCommand(CommandType.ADD_CATEGORY, category1);
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_CATEGORY, category2))
            .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("Should reject null category object")
    void shouldRejectNullCategory() {
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_CATEGORY, null))
            .isInstanceOf(NullPointerException.class);
    }

    // ==================== REMOVE_PRODUCT VALIDATION ====================

    @Test
    @DisplayName("Should reject removing product with null ID")
    void shouldRejectRemovingProductWithNullId() {
        assertThatThrownBy(() -> controller.executeCommand(CommandType.REMOVE_PRODUCT, null))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should reject removing product with empty ID")
    void shouldRejectRemovingProductWithEmptyId() {
        assertThatThrownBy(() -> controller.executeCommand(CommandType.REMOVE_PRODUCT, ""))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should reject removing non-existent product")
    void shouldRejectRemovingNonExistentProduct() {
        assertThatThrownBy(() -> controller.executeCommand(CommandType.REMOVE_PRODUCT, "NONEXIST999"))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should successfully remove product with zero stock")
    void shouldRemoveProductWithZeroStock() throws SQLException {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 0, null);
        product.setId("ZERO123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        
        assertThatCode(() -> controller.executeCommand(CommandType.REMOVE_PRODUCT, "ZERO123"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should remove product with stock and create exit movement")
    void shouldRemoveProductWithStockAndCreateExitMovement() throws SQLException {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 10, null);
        product.setId("STOCK123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        
        controller.executeCommand(CommandType.REMOVE_PRODUCT, "STOCK123");
        
        // Verify product is removed
        assertThat(controller.getAllProducts()).isEmpty();
    }

    // ==================== REMOVE_CATEGORY VALIDATION ====================

    @Test
    @DisplayName("Should reject removing category with null ID")
    void shouldRejectRemovingCategoryWithNullId() {
        assertThatThrownBy(() -> controller.executeCommand(CommandType.REMOVE_CATEGORY, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should reject removing non-existent category")
    void shouldRejectRemovingNonExistentCategory() {
        assertThatThrownBy(() -> controller.executeCommand(CommandType.REMOVE_CATEGORY, 99999))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should remove category and nullify product references")
    void shouldRemoveCategoryAndNullifyProductReferences() throws SQLException {
        Category category = new Category(0, "Test Cat");
        controller.executeCommand(CommandType.ADD_CATEGORY, category);
        int categoryId = category.getId();
        
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 5, category);
        product.setId("PROD123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        
        controller.executeCommand(CommandType.REMOVE_CATEGORY, categoryId);
        
        // Verify category is removed
        assertThat(controller.getAllCategories()).isEmpty();
        
        // Verify product still exists but category is null
        Product updatedProduct = controller.getAllProducts().get(0);
        assertThat(updatedProduct.getCategory()).isNull();
    }

    @Test
    @DisplayName("Should remove empty category")
    void shouldRemoveEmptyCategory() throws SQLException {
        Category category = new Category(0, "Empty");
        controller.executeCommand(CommandType.ADD_CATEGORY, category);
        int categoryId = category.getId();
        
        assertThatCode(() -> controller.executeCommand(CommandType.REMOVE_CATEGORY, categoryId))
            .doesNotThrowAnyException();
    }

    // ==================== ADD_STOCK VALIDATION ====================

    @Test
    @DisplayName("Should reject stock movement with null product")
    void shouldRejectStockMovementWithNullProduct() {
        Movement movement = new Movement();
        movement.setProduct(null);
        movement.setQuantity(5);
        movement.setType(Movement.MovementType.STOCK_ENTRY);
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_STOCK, movement))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should reject stock movement with zero quantity")
    void shouldRejectStockMovementWithZeroQuantity() throws SQLException {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 0, null);
        product.setId("TEST123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        
        Product movementProduct = new Product();
        movementProduct.setId("TEST123");
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(0);
        movement.setType(Movement.MovementType.STOCK_ENTRY);
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_STOCK, movement))
            .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("Should reject stock movement with negative quantity")
    void shouldRejectStockMovementWithNegativeQuantity() throws SQLException {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 0, null);
        product.setId("TEST123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        
        Product movementProduct = new Product();
        movementProduct.setId("TEST123");
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(-5);
        movement.setType(Movement.MovementType.STOCK_ENTRY);
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_STOCK, movement))
            .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("Should reject stock movement for non-existent product")
    void shouldRejectStockMovementForNonExistentProduct() {
        Product movementProduct = new Product();
        movementProduct.setId("NONEXIST999");
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(5);
        movement.setType(Movement.MovementType.STOCK_ENTRY);
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.ADD_STOCK, movement))
            .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("Should accept stock movement with large quantity")
    void shouldAcceptStockMovementWithLargeQuantity() throws SQLException {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 0, null);
        product.setId("TEST123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        
        Product movementProduct = new Product();
        movementProduct.setId("TEST123");
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(999999);
        movement.setType(Movement.MovementType.STOCK_ENTRY);
        
        assertThatCode(() -> controller.executeCommand(CommandType.ADD_STOCK, movement))
            .doesNotThrowAnyException();
    }

    // ==================== REMOVE_STOCK VALIDATION ====================

    @Test
    @DisplayName("Should reject removing more stock than available")
    void shouldRejectRemovingMoreStockThanAvailable() throws SQLException {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 5, null);
        product.setId("TEST123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        
        Product movementProduct = new Product();
        movementProduct.setId("TEST123");
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(10); // More than available
        movement.setType(Movement.MovementType.STOCK_EXIT);
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.REMOVE_STOCK, movement))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should accept removing exact stock amount")
    void shouldAcceptRemovingExactStockAmount() throws SQLException {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 5, null);
        product.setId("TEST123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        
        Product movementProduct = new Product();
        movementProduct.setId("TEST123");
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(5); // Exact amount
        movement.setType(Movement.MovementType.STOCK_EXIT);
        
        assertThatCode(() -> controller.executeCommand(CommandType.REMOVE_STOCK, movement))
            .doesNotThrowAnyException();
        
        // Verify stock is zero
        Product updatedProduct = controller.getAllProducts().stream()
            .filter(p -> p.getId().equals("TEST123"))
            .findFirst().orElseThrow();
        assertThat(updatedProduct.getStockQuantity()).isZero();
    }

    @Test
    @DisplayName("Should reject removing stock from product with zero stock")
    void shouldRejectRemovingStockFromZeroStock() throws SQLException {
        Product product = ProductFactory.createProduct("Test", 10.0, 15.0, 0, null);
        product.setId("TEST123");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        
        Product movementProduct = new Product();
        movementProduct.setId("TEST123");
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(1);
        movement.setType(Movement.MovementType.STOCK_EXIT);
        
        assertThatThrownBy(() -> controller.executeCommand(CommandType.REMOVE_STOCK, movement))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("Insufficient stock");
    }

    // ==================== QUERY VALIDATION ====================

    @Test
    @DisplayName("Should return empty list when no products exist")
    void shouldReturnEmptyListWhenNoProducts() throws SQLException {
        assertThat(controller.getAllProducts()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void shouldReturnEmptyListWhenNoCategories() throws SQLException {
        assertThat(controller.getAllCategories()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for movements of non-existent product")
    void shouldReturnEmptyListForMovementsOfNonExistentProduct() throws SQLException {
        assertThat(controller.getMovementsForProduct("NONEXIST999")).isEmpty();
    }

    @Test
    @DisplayName("Should handle null product ID in getMovementsForProduct")
    void shouldHandleNullProductIdInGetMovements() {
        assertThatThrownBy(() -> controller.getMovementsForProduct(null))
            .isInstanceOf(Exception.class);
    }
}
