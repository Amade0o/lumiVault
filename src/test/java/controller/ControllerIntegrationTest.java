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
        int categoryId = controller.addCategory(category);
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
        int categoryId = controller.addCategory(category);
        category.setId(categoryId);

        Product product = ProductFactory.createProduct("Test Product", 10.0, 15.0, 5, category);
        product.setId("1234567890123"); // Set barcode ID

        // When
        String productId = controller.addProduct(product);
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
        int categoryId = controller.addCategory(category);

        // When
        boolean removed = controller.removeCategory(categoryId);
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
        String productId = controller.addProduct(product);

        // When
        boolean removed = controller.removeProduct(productId);
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
        String productId = controller.addProduct(product);

        Product movementProduct = new Product();
        movementProduct.setId(productId);
        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(5);
        movement.setType(Movement.MovementType.STOCK_ENTRY);
        movement.setDescription("Test movement");
        movement.setDateTime(null);

        // When
        int movementId = controller.recordStockMovement(movement);
        List<Movement> movements = controller.getMovementsForProduct(productId);

        // Then
        assertThat(movementId).isGreaterThan(0);
        assertThat(movements).hasSize(2); // PRODUCT_CREATED + STOCK_ENTRY
        // Find the STOCK_ENTRY movement
        Movement stockEntry = movements.stream()
            .filter(m -> m.getType() == Movement.MovementType.STOCK_ENTRY)
            .findFirst().orElseThrow();
        assertThat(stockEntry.getQuantity()).isEqualTo(5);
        assertThat(stockEntry.getType()).isEqualTo(Movement.MovementType.STOCK_ENTRY);
    }

    @Test
    void shouldHandleMultipleCategories() throws SQLException {
        // Given
        Category cat1 = new Category(0, "Category 1", "First category");
        Category cat2 = new Category(0, "Category 2", "Second category");
        Category cat3 = new Category(0, "Category 3", "Third category");

        // When
        controller.addCategory(cat1);
        controller.addCategory(cat2);
        controller.addCategory(cat3);
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
        controller.addProduct(prod1);
        controller.addProduct(prod2);
        controller.addProduct(prod3);
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
        String productId = controller.addProduct(product);

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

        controller.recordStockMovement(movement1);
        controller.recordStockMovement(movement2);

        // When
        boolean removed = controller.removeProduct(productId);
        List<Movement> movements = controller.getMovementsForProduct(productId);

        // Then
        assertThat(removed).isTrue();
        assertThat(movements).isEmpty(); // Movements should be deleted with product
    }

    @Test
    void shouldFailToRemoveNonExistentCategory() throws SQLException {
        // When
        boolean removed = controller.removeCategory(999);

        // Then
        assertThat(removed).isFalse();
    }

    @Test
    void shouldFailToRemoveNonExistentProduct() throws SQLException {
        // When
        boolean removed = controller.removeProduct("9999999999999");

        // Then
        assertThat(removed).isFalse();
    }

    @Test
    void shouldHandleCategoryWithProducts() throws SQLException {
        // Given
        Category category = new Category(0, "Category with products", "Has products");
        int categoryId = controller.addCategory(category);
        category.setId(categoryId);

        Product product = ProductFactory.createProduct("Product in category", 10.0, 15.0, 5, category);
        product.setId("8888888888888");
        controller.addProduct(product);

        // When
        boolean removed = controller.removeCategory(categoryId);
        List<Category> categories = controller.getAllCategories();
        List<Product> products = controller.getAllProducts();

        // Then
        assertThat(removed).isTrue();
        assertThat(categories).isEmpty();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getCategory()).isNull(); // Category should be set to null
    }
}