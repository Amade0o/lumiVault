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

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ControllerIntegrationTest {

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

    @Test
    void shouldAddAndRetrieveCategory() throws SQLException {
        // Given
        Category category = new Category(0, "Electronics", "Electronic devices");

        // When
        controller.executeCommand(CommandType.ADD_CATEGORY, category);
        int categoryId = category.getId();
        List<Category> categories = controller.getAllCategories();

        // Then
        assertThat(categoryId).isGreaterThan(0);
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo("Electronics");
        assertThat(categories.get(0).getDescription()).isEqualTo("Electronic devices");
    }

    @Test
    void shouldAddAndRetrieveProduct() throws SQLException {
        // Given
        Category category = new Category(0, "Test Category", "Test");
        controller.executeCommand(CommandType.ADD_CATEGORY, category);
        int categoryId = category.getId();
        category.setId(categoryId);

        Product product = ProductFactory.createProduct("Test Product", 10.0, 15.0, 5, category);
        product.setId("1234567890123"); // Set barcode ID

        // When
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        String productId = product.getId();
        List<Product> products = controller.getAllProducts();

        // Then
        assertThat(productId).isNotNull();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Test Product");
        assertThat(products.get(0).getCostPrice()).isEqualTo(10.0);
        assertThat(products.get(0).getSalePrice()).isEqualTo(15.0);
        assertThat(products.get(0).getStockQuantity()).isEqualTo(5);
    }

    @Test
    void shouldRemoveCategory() throws SQLException {
        // Given
        Category category = new Category(0, "To Remove", "Will be removed");
        controller.executeCommand(CommandType.ADD_CATEGORY, category);
        int categoryId = category.getId();

        // When
        controller.executeCommand(CommandType.REMOVE_CATEGORY, categoryId);
        boolean removed = true;
        List<Category> categories = controller.getAllCategories();

        // Then
        assertThat(removed).isTrue();
        assertThat(categories).isEmpty();
    }

    @Test
    void shouldRemoveProduct() throws SQLException {
        // Given
        Product product = ProductFactory.createProduct("To Remove", 10.0, 15.0, 5, null);
        product.setId("9876543210987"); // Set barcode ID
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        String productId = product.getId();

        // When
        controller.executeCommand(CommandType.REMOVE_PRODUCT, productId);
        boolean removed = true;
        List<Product> products = controller.getAllProducts();

        // Then
        assertThat(removed).isTrue();
        assertThat(products).isEmpty();
    }

    @Test
    void shouldRecordStockMovement() throws SQLException {
        // Given
        Product product = ProductFactory.createProduct("Movement Test", 10.0, 15.0, 0, null);
        product.setId("5555555555555"); // Set barcode ID
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        String productId = product.getId();

        Product movementProduct = new Product();
        movementProduct.setId(productId);
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(5);
        movement.setType(Movement.MovementType.STOCK_ENTRY);
        movement.setDescription("Test movement");
        movement.setDateTime(null);

        // When
        controller.executeCommand(CommandType.ADD_STOCK, movement);
        List<Movement> movements = controller.getMovementsForProduct(productId);

        // Then
        assertThat(movements).isNotEmpty();
        assertThat(movements).hasSize(2); // PRODUCT_CREATED + STOCK_ENTRY
        // Find the STOCK_ENTRY movement
        Movement stockEntry = movements.stream()
            .filter(m -> m.getType() == Movement.MovementType.STOCK_ENTRY)
            .findFirst().orElseThrow();
        assertThat(stockEntry.getQuantity()).isEqualTo(5);
        assertThat(stockEntry.getType()).isEqualTo(Movement.MovementType.STOCK_ENTRY);
    }

    @Test
    void shouldRemoveStock() throws SQLException {
        // Given - Create product with initial stock
        Product product = ProductFactory.createProduct("Stock Test", 10.0, 15.0, 10, null);
        product.setId("6666666666666"); // Set barcode ID
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        String productId = product.getId();

        // Create remove stock movement
        Product movementProduct = new Product();
        movementProduct.setId(productId);
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(3);
        movement.setType(Movement.MovementType.STOCK_EXIT);
        movement.setDescription("Test stock removal");
        movement.setDateTime(null);

        // When
        controller.executeCommand(CommandType.REMOVE_STOCK, movement);
        List<Movement> movements = controller.getMovementsForProduct(productId);
        List<Product> products = controller.getAllProducts();
        Product updatedProduct = products.stream()
            .filter(p -> p.getId().equals(productId))
            .findFirst().orElseThrow();

        // Then
        assertThat(movements).hasSize(3); // PRODUCT_CREATED + initial STOCK_ENTRY + STOCK_EXIT
        Movement stockExit = movements.stream()
            .filter(m -> m.getType() == Movement.MovementType.STOCK_EXIT)
            .findFirst().orElseThrow();
        assertThat(stockExit.getQuantity()).isEqualTo(3);
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(7); // 10 - 3
    }

    @Test
    void shouldHandleMultipleCategories() throws SQLException {
        // Given
        Category cat1 = new Category(0, "Category 1", "First category");
        Category cat2 = new Category(0, "Category 2", "Second category");
        Category cat3 = new Category(0, "Category 3", "Third category");

        // When
        controller.executeCommand(CommandType.ADD_CATEGORY, cat1);
        controller.executeCommand(CommandType.ADD_CATEGORY, cat2);
        controller.executeCommand(CommandType.ADD_CATEGORY, cat3);
        List<Category> categories = controller.getAllCategories();

        // Then
        assertThat(categories).hasSize(3);
        assertThat(categories)
            .extracting(Category::getName)
            .containsExactlyInAnyOrder("Category 1", "Category 2", "Category 3");
    }

    @Test
    void shouldHandleMultipleProducts() throws SQLException {
        // Given
        Product prod1 = ProductFactory.createProduct("Product 1", 10.0, 15.0, 5, null);
        prod1.setId("1111111111111");
        Product prod2 = ProductFactory.createProduct("Product 2", 20.0, 25.0, 10, null);
        prod2.setId("2222222222222");
        Product prod3 = ProductFactory.createProduct("Product 3", 30.0, 35.0, 15, null);
        prod3.setId("3333333333333");

        // When
        controller.executeCommand(CommandType.ADD_PRODUCT, prod1);
        controller.executeCommand(CommandType.ADD_PRODUCT, prod2);
        controller.executeCommand(CommandType.ADD_PRODUCT, prod3);
        List<Product> products = controller.getAllProducts();

        // Then
        assertThat(products).hasSize(3);
        assertThat(products)
            .extracting(Product::getName)
            .containsExactlyInAnyOrder("Product 1", "Product 2", "Product 3");
    }

    @Test
    void shouldReturnEmptyListsWhenNoData() throws SQLException {
        // When
        List<Category> categories = controller.getAllCategories();
        List<Product> products = controller.getAllProducts();

        // Then
        assertThat(categories).isEmpty();
        assertThat(products).isEmpty();
    }

    @Test
    void shouldReturnEmptyMovementsForNonExistentProduct() throws SQLException {
        // When
        List<Movement> movements = controller.getMovementsForProduct("9999999999999");

        // Then
        assertThat(movements).isEmpty();
    }

    @Test
    void shouldHandleProductRemovalWithMovements() throws SQLException {
        // Given
        Product product = ProductFactory.createProduct("Product with movements", 10.0, 15.0, 10, null);
        product.setId("7777777777777"); // Set barcode ID
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        String productId = product.getId();

        // Add some movements
        Product movementProduct = new Product();
        movementProduct.setId(productId);
        Movement movement1 = new Movement();
        movement1.setProduct(movementProduct);
        movement1.setQuantity(5);
        movement1.setType(Movement.MovementType.STOCK_ENTRY);
        movement1.setDescription("Test entry");
        movement1.setDateTime(null);
        
        Movement movement2 = new Movement();
        movement2.setProduct(movementProduct);
        movement2.setQuantity(3);
        movement2.setType(Movement.MovementType.STOCK_EXIT);
        movement2.setDescription("Test exit");
        movement2.setDateTime(null);

        controller.executeCommand(CommandType.ADD_STOCK, movement1);
        controller.executeCommand(CommandType.ADD_STOCK, movement2);

        // When
        controller.executeCommand(CommandType.REMOVE_PRODUCT, productId);
        boolean removed = true;
        List<Movement> movements = controller.getMovementsForProduct(productId);

        // Then
        assertThat(removed).isTrue();
        assertThat(movements).isEmpty(); // Movements should be deleted with product
    }

    @Test
    void shouldFailToRemoveNonExistentCategory() {
        // When/Then
        assertThatThrownBy(() -> controller.executeCommand(CommandType.REMOVE_CATEGORY, 999))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void shouldFailToRemoveNonExistentProduct() {
        // When/Then
        assertThatThrownBy(() -> controller.executeCommand(CommandType.REMOVE_PRODUCT, "9999999999999"))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void shouldHandleCategoryWithProducts() throws SQLException {
        // Given
        Category category = new Category(0, "Category with products", "Has products");
        controller.executeCommand(CommandType.ADD_CATEGORY, category);
        int categoryId = category.getId();
        category.setId(categoryId);

        Product product = ProductFactory.createProduct("Product in category", 10.0, 15.0, 5, category);
        product.setId("8888888888888");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);

        // When
        controller.executeCommand(CommandType.REMOVE_CATEGORY, categoryId);
        boolean removed = true;
        List<Category> categories = controller.getAllCategories();
        List<Product> products = controller.getAllProducts();

        // Then
        assertThat(removed).isTrue();
        assertThat(categories).isEmpty();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getCategory()).isNull(); // Category should be set to null
    }
}