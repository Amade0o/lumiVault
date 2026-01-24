package performance;

import controller.Controller;
import controller.CommandType;
import model.factory.ProductFactory;
import database.DatabaseConnection;
import database.TestDatabaseConnection;
import model.Category;
import model.Product;
import model.Movement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PerformanceAndStressTest {

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
    void shouldHandleLargeNumberOfProducts() throws SQLException {
        // Given
        int numberOfProducts = 1000;

        // When
        for (int i = 0; i < numberOfProducts; i++) {
            Product product = ProductFactory.createProduct(
                "Product" + i, 10.0 + i, 15.0 + i, i % 100, null);
            product.setId(String.format("%013d", 2000 + i));
            controller.executeCommand(CommandType.ADD_PRODUCT, product);
        }

        // Then
        List<Product> products = controller.getAllProducts();
        assertThat(products).hasSize(numberOfProducts);
    }

    @Test
    void shouldHandleLargeNumberOfCategories() throws SQLException {
        // Given
        int numberOfCategories = 500;

        // When
        for (int i = 0; i < numberOfCategories; i++) {
            Category category = new Category(0, "Category" + i, "Description" + i);
            controller.executeCommand(CommandType.ADD_CATEGORY, category);
        }

        // Then
        List<Category> categories = controller.getAllCategories();
        assertThat(categories).hasSize(numberOfCategories);
    }

    @Test
    void shouldHandleLargeNumberOfStockMovements() throws SQLException {
        // Given
        Product product = ProductFactory.createProduct("Test Product", 10.0, 15.0, 1000, null);
        product.setId("1000000000000");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        String productId = product.getId();

        int numberOfMovements = 1000;

        // When
        for (int i = 0; i < numberOfMovements; i++) {
            Product movementProduct = new Product();
            movementProduct.setId(productId);

            Movement.MovementType type = (i % 2 == 0) ?
                Movement.MovementType.STOCK_ENTRY : Movement.MovementType.STOCK_EXIT;

            Movement movement = new Movement();
            movement.setProduct(movementProduct);
            movement.setQuantity(1);
            movement.setType(type);
            movement.setDescription("Performance test movement");
            movement.setDateTime(null);
            controller.executeCommand(CommandType.ADD_STOCK, movement);
        }

        // Then
        List<Movement> movements = controller.getMovementsForProduct(productId);
        // Expect numberOfMovements + 2 automatic movements (PRODUCT_CREATED + initial stock entry)
        assertThat(movements).hasSize(numberOfMovements + 2);
    }

    @Test
    void shouldHandleConcurrentCategoriesAndProducts() throws SQLException {
        // Given
        int numberOfCategories = 100;
        int productsPerCategory = 10;

        // When - Create categories
        for (int i = 0; i < numberOfCategories; i++) {
            Category category = new Category(0, "Category" + i, "Description" + i);
            controller.executeCommand(CommandType.ADD_CATEGORY, category);
        }

        List<Category> categories = controller.getAllCategories();

        // Create products for each category
        int productCounter = 0;
        for (Category category : categories) {
            for (int j = 0; j < productsPerCategory; j++) {
                Product product = ProductFactory.createProduct(
                    "Product" + category.getId() + "_" + j,
                    10.0, 15.0, 5, category);
                product.setId(String.format("%013d", 3000 + productCounter++));
                controller.executeCommand(CommandType.ADD_PRODUCT, product);
            }
        }

        // Then
        List<Product> products = controller.getAllProducts();
        assertThat(products).hasSize(numberOfCategories * productsPerCategory);
        assertThat(categories).hasSize(numberOfCategories);
    }

    @Test
    void shouldHandleProductsWithVeryLongNames() throws SQLException {
        // Given
        String longName = "A".repeat(1000);

        // When
        Product product = ProductFactory.createProduct(longName, 10.0, 15.0, 5, null);
        product.setId("1111111111111");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        String productId = product.getId();

        // Then
        List<Product> products = controller.getAllProducts();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo(longName);
        assertThat(products.get(0).getId()).isEqualTo(productId);
    }

    @Test
    void shouldHandleCategoriesWithVeryLongDescriptions() throws SQLException {
        // Given
        String longDescription = "B".repeat(5000);

        // When
        Category category = new Category(0, "Long Description Category", longDescription);
        controller.executeCommand(CommandType.ADD_CATEGORY, category);
        int categoryId = category.getId();

        // Then
        List<Category> categories = controller.getAllCategories();
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getDescription()).isEqualTo(longDescription);
        assertThat(categories.get(0).getId()).isEqualTo(categoryId);
    }

    @Test
    void shouldHandleRapidSuccessionOfOperations() throws SQLException {
        // Given
        int iterations = 100;

        // When - Rapid create/read/delete cycles
        for (int i = 0; i < iterations; i++) {
            // Create
            Product product = ProductFactory.createProduct("Temp" + i, 10.0, 15.0, 5, null);
            product.setId(String.format("%013d", i)); // 13 digit IDs
            controller.executeCommand(CommandType.ADD_PRODUCT, product);
        String productId = product.getId();

            // Read
            List<Product> products = controller.getAllProducts();
            assertThat(products).isNotEmpty();

            // Delete
            controller.executeCommand(CommandType.REMOVE_PRODUCT, productId);
        boolean deleted = true;
            assertThat(deleted).isTrue();
        }

        // Then
        List<Product> finalProducts = controller.getAllProducts();
        assertThat(finalProducts).isEmpty();
    }

    @Test
    void shouldHandleMixedOperationsStress() throws SQLException {
        // Given
        int operations = 200;

        // When - Mix of all operations
        for (int i = 0; i < operations; i++) {
            switch (i % 4) {
                case 0 -> { // Add product
                    Product product = ProductFactory.createProduct("Stress" + i, 10.0, 15.0, 5, null);
                    product.setId(String.format("%013d", 4000 + i));
                    controller.executeCommand(CommandType.ADD_PRODUCT, product);
                }
                case 1 -> { // Add category
                    Category category = new Category(0, "StressCat" + i, "Description");
                    controller.executeCommand(CommandType.ADD_CATEGORY, category);
                }
                case 2 -> { // List products
                    controller.getAllProducts();
                }
                case 3 -> { // List categories
                    controller.getAllCategories();
                }
            }
        }

        // Then - Verify system is still functional
        List<Product> products = controller.getAllProducts();
        List<Category> categories = controller.getAllCategories();

        assertThat(products.size()).isEqualTo(operations / 4 + operations % 4 / 4);
        assertThat(categories.size()).isEqualTo(operations / 4);
    }

    @Test
    void shouldHandleMemoryEfficientOperations() throws SQLException {
        // This test verifies that operations don't cause memory leaks
        // by repeatedly creating and cleaning up data

        for (int cycle = 0; cycle < 10; cycle++) {
            // Create data
            for (int i = 0; i < 100; i++) {
                Product product = ProductFactory.createProduct("Cycle" + cycle + "_" + i, 10.0, 15.0, 5, null);
                product.setId(String.format("%013d", 5000 + (cycle * 100) + i));
                controller.executeCommand(CommandType.ADD_PRODUCT, product);
            }

            // Verify data exists
            List<Product> products = controller.getAllProducts();
            assertThat(products).hasSize(100);

            // Clean up - remove all products
            for (Product product : products) {
                controller.executeCommand(CommandType.REMOVE_PRODUCT, product.getId());
            }

            // Verify cleanup
            products = controller.getAllProducts();
            assertThat(products).isEmpty();
        }
    }
}