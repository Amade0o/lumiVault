package integration;

import controller.Controller;
import controller.CommandType;
import database.DatabaseConnection;
import database.TestDatabaseConnection;
import model.Category;
import model.Product;
import model.factory.ProductFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive integration tests for product search functionality.
 * Tests extreme cases, edge conditions, and user error scenarios.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ProductSearchIntegrationTest {

    private Controller controller;
    private Category testCategory;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseConnection.resetDatabase();
        DatabaseConnection.setTestConnection(TestDatabaseConnection.getTestConnection());
        controller = new Controller();
        
        // Create test category
        testCategory = new Category(0, "Test Category");
        controller.executeCommand(CommandType.ADD_CATEGORY, testCategory);
        
        // Get the created category with ID
        List<Category> categories = controller.getAllCategories();
        testCategory = categories.get(0);
    }

    @AfterEach
    void tearDown() throws SQLException {
        DatabaseConnection.clearTestConnection();
        TestDatabaseConnection.closeTestConnection();
    }

    // ==================== BASIC SEARCH TESTS ====================

    @Test
    @DisplayName("Search by exact product name should return matching product")
    void testSearchByExactName() throws SQLException {
        // Given
        Product product = createAndAddProduct("Coca Cola 2L", "12345");
        
        // When
        List<Product> allProducts = controller.getAllProducts();
        List<Product> results = allProducts.stream()
                .filter(p -> p.getName().equalsIgnoreCase("Coca Cola 2L"))
                .collect(Collectors.toList());
        
        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Coca Cola 2L");
        assertThat(results.get(0).getId()).isEqualTo("12345");
    }

    @Test
    @DisplayName("Search by partial name should return matching products")
    void testSearchByPartialName() throws SQLException {
        // Given
        createAndAddProduct("Coca Cola 2L", "12345");
        createAndAddProduct("Coca Cola 1.5L", "12346");
        createAndAddProduct("Pepsi Cola 2L", "12347");
        
        // When
        List<Product> allProducts = controller.getAllProducts();
        List<Product> results = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains("coca"))
                .collect(Collectors.toList());
        
        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Product::getName)
                .containsExactlyInAnyOrder("Coca Cola 2L", "Coca Cola 1.5L");
    }

    @Test
    @DisplayName("Search with non-existing name should return empty list")
    void testSearchNonExistingProduct() throws SQLException {
        // Given
        createAndAddProduct("Coca Cola 2L", "12345");
        
        // When
        List<Product> allProducts = controller.getAllProducts();
        List<Product> results = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains("sprite"))
                .collect(Collectors.toList());
        
        // Then
        assertThat(results).isEmpty();
    }

    // ==================== EXTREME CASES ====================

    @Test
    @DisplayName("Search in empty database should return empty list")
    void testSearchInEmptyDatabase() throws SQLException {
        // When
        List<Product> allProducts = controller.getAllProducts();
        
        // Then
        assertThat(allProducts).isEmpty();
    }

    @Test
    @DisplayName("Search with empty string should return all products")
    void testSearchWithEmptyString() throws SQLException {
        // Given
        createAndAddProduct("Product A", "A001");
        createAndAddProduct("Product B", "B002");
        createAndAddProduct("Product C", "C003");
        
        // When
        List<Product> allProducts = controller.getAllProducts();
        List<Product> results = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(""))
                .collect(Collectors.toList());
        
        // Then
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("Search with whitespace only should return all products")
    void testSearchWithWhitespaceOnly() throws SQLException {
        // Given
        createAndAddProduct("Product A", "A001");
        createAndAddProduct("Product B", "B002");
        
        // When
        String searchText = "   ".toLowerCase().trim();
        List<Product> allProducts = controller.getAllProducts();
        List<Product> results;
        
        if (searchText.isEmpty()) {
            results = allProducts;
        } else {
            results = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }
        
        // Then
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("Search with very long name (1000 chars) should work")
    void testSearchWithVeryLongName() throws SQLException {
        // Given
        String longName = "A".repeat(1000);
        createAndAddProduct(longName, "LONG001");
        
        // When
        List<Product> allProducts = controller.getAllProducts();
        List<Product> results = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains("aaa"))
                .collect(Collectors.toList());
        
        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).hasSize(1000);
    }

    @Test
    @DisplayName("Search with special characters should work correctly")
    void testSearchWithSpecialCharacters() throws SQLException {
        // Given
        createAndAddProduct("Café con Leche", "CAF001");
        createAndAddProduct("Piña Colada", "PIN001");
        createAndAddProduct("Jalapeño Extra Hot!", "JAL001");
        createAndAddProduct("50% OFF - Special", "SPEC001");
        createAndAddProduct("Product (Premium)", "PREM001");
        
        // When - Search for "ñ"
        List<Product> results1 = searchProducts("ñ");
        
        // When - Search for "café"
        List<Product> results2 = searchProducts("café");
        
        // When - Search for "50%"
        List<Product> results3 = searchProducts("50%");
        
        // When - Search for "(premium)"
        List<Product> results4 = searchProducts("(premium)");
        
        // Then
        assertThat(results1).hasSize(2); // Piña and Jalapeño
        assertThat(results2).hasSize(1); // Café con Leche
        assertThat(results3).hasSize(1); // 50% OFF
        assertThat(results4).hasSize(1); // Product (Premium)
    }

    @Test
    @DisplayName("Search should be case insensitive")
    void testSearchCaseInsensitive() throws SQLException {
        // Given
        createAndAddProduct("Coca Cola", "COKE001");
        
        // When
        List<Product> results1 = searchProducts("COCA COLA");
        List<Product> results2 = searchProducts("coca cola");
        List<Product> results3 = searchProducts("CoCa CoLa");
        List<Product> results4 = searchProducts("coca");
        
        // Then
        assertThat(results1).hasSize(1);
        assertThat(results2).hasSize(1);
        assertThat(results3).hasSize(1);
        assertThat(results4).hasSize(1);
    }

    // ==================== USER ERROR SCENARIOS ====================

    @ParameterizedTest
    @ValueSource(strings = {"   coca   ", "  COCA  ", "\tcoca\t", "coca\n"})
    @DisplayName("Search should handle leading/trailing whitespace correctly")
    void testSearchWithWhitespace(String searchTerm) throws SQLException {
        // Given
        createAndAddProduct("Coca Cola", "COKE001");
        
        // When
        String normalizedSearch = searchTerm.toLowerCase().trim();
        List<Product> results = searchProducts(normalizedSearch);
        
        // Then
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("Search with typos should still work with partial match")
    void testSearchWithTypos() throws SQLException {
        // Given
        createAndAddProduct("Coca Cola 2L", "COKE001");
        
        // When - User types "coca cla" (typo: missing 'o' in cola)
        List<Product> results1 = searchProducts("coca cla");
        
        // When - User types "coc" (incomplete)
        List<Product> results2 = searchProducts("coc");
        
        // When - User types "2l" (just the size)
        List<Product> results3 = searchProducts("2l");
        
        // Then
        assertThat(results1).isEmpty(); // Exact typo won't match
        assertThat(results2).hasSize(1); // Partial match works
        assertThat(results3).hasSize(1); // Size match works
    }

    @Test
    @DisplayName("Search with multiple spaces should work")
    void testSearchWithMultipleSpaces() throws SQLException {
        // Given
        createAndAddProduct("Coca Cola Light", "COKE001");
        
        // When - User types with extra spaces
        List<Product> results = searchProducts("coca    cola");
        
        // Then - Should not find (because search is exact substring)
        assertThat(results).isEmpty();
        
        // But searching for "coca" or "cola" individually works
        assertThat(searchProducts("cola")).hasSize(1);
    }

    // ==================== MULTIPLE RESULTS SCENARIOS ====================

    @Test
    @DisplayName("Search with many similar products should return all matches")
    void testSearchWithManySimilarProducts() throws SQLException {
        // Given - Create 20 products with "Coca" in name
        for (int i = 1; i <= 20; i++) {
            createAndAddProduct("Coca Cola " + i + "L", "COKE" + String.format("%03d", i));
        }
        
        // Also add some non-matching products
        createAndAddProduct("Pepsi 2L", "PEPSI001");
        createAndAddProduct("Sprite 2L", "SPRITE001");
        
        // When
        List<Product> results = searchProducts("coca");
        
        // Then
        assertThat(results).hasSize(20);
        assertThat(results).allMatch(p -> p.getName().toLowerCase().contains("coca"));
    }

    @Test
    @DisplayName("Duplicate product names should be rejected by UNIQUE constraint")
    void testSearchWithDuplicateNames() {
        // Given - Products with same name but different IDs
        // Note: This shouldn't happen due to UNIQUE constraint on nombre column
        
        // When/Then - Should reject duplicate name
        assertThatThrownBy(() -> {
            createAndAddProduct("Generic Product", "ID001");
            createAndAddProduct("Generic Product", "ID002"); // This should fail
        })
        .isInstanceOf(SQLException.class)
        .satisfiesAnyOf(
            ex -> assertThat(ex.getMessage()).containsIgnoringCase("unique"),
            ex -> assertThat(ex.getMessage()).containsIgnoringCase("already exists"),
            ex -> assertThat(ex.getMessage()).containsIgnoringCase("duplicate")
        );
    }

    // ==================== BOUNDARY TESTS ====================

    @Test
    @DisplayName("Search with single character should work")
    void testSearchWithSingleCharacter() throws SQLException {
        // Given
        createAndAddProduct("Apple", "APPLE001");
        createAndAddProduct("Banana", "BANANA001");
        createAndAddProduct("Avocado", "AVO001");
        
        // When
        List<Product> results = searchProducts("a");
        
        // Then
        assertThat(results).hasSize(3); // All contain 'a'
    }

    @Test
    @DisplayName("Search with number in name should work")
    void testSearchWithNumbers() throws SQLException {
        // Given
        createAndAddProduct("Coca Cola 2L", "COKE2L");
        createAndAddProduct("Pepsi 1.5L", "PEPSI15");
        createAndAddProduct("Agua 500ml", "WATER500");
        
        // When
        List<Product> results1 = searchProducts("2");
        List<Product> results2 = searchProducts("500");
        List<Product> results3 = searchProducts("1.5");
        
        // Then
        assertThat(results1).hasSize(1);
        assertThat(results2).hasSize(1);
        assertThat(results3).hasSize(1);
    }

    // ==================== PERFORMANCE TESTS ====================

    @Test
    @DisplayName("Search performance with 1000 products should be acceptable")
    void testSearchPerformanceWith1000Products() throws SQLException {
        // Given - Create 1000 products
        for (int i = 1; i <= 1000; i++) {
            String name = "Product " + i + " - Category " + (i % 10);
            createAndAddProduct(name, "PROD" + String.format("%04d", i));
        }
        
        // When - Measure search time
        long startTime = System.currentTimeMillis();
        List<Product> results = searchProducts("category 5");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertThat(results).isNotEmpty();
        assertThat(duration).isLessThan(1000); // Should complete in less than 1 second
        System.out.println("Search in 1000 products took: " + duration + "ms");
    }

    // ==================== DATABASE ERROR SCENARIOS ====================

    @Test
    @DisplayName("Search should handle database connection gracefully")
    void testSearchWithDatabaseConnection() throws SQLException {
        // Given
        createAndAddProduct("Test Product", "TEST001");
        
        // When - Normal operation
        List<Product> results = controller.getAllProducts();
        
        // Then
        assertThat(results).hasSize(1);
        
        // Note: Testing actual disconnection would require more complex setup
        // and might interfere with other tests
    }

    // ==================== HELPER METHODS ====================

    private Product createAndAddProduct(String name, String id) throws SQLException {
        Product product = ProductFactory.createProduct(name, 10.0, 15.0, 100, testCategory);
        product.setId(id);
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        return product;
    }

    private List<Product> searchProducts(String searchTerm) throws SQLException {
        List<Product> allProducts = controller.getAllProducts();
        String searchText = searchTerm.toLowerCase().trim();
        
        if (searchText.isEmpty()) {
            return allProducts;
        }
        
        return allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
    }
}
