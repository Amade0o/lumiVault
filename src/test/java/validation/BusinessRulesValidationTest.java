package validation;

import model.factory.ProductFactory;
import model.Category;
import model.Product;
import model.Movement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class BusinessRulesValidationTest {

    @Test
    void shouldRejectEmptyProductName() {
        assertThatThrownBy(() -> ProductFactory.createProduct("", 10.0, 15.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot be null or empty");
    }

    @Test
    void shouldRejectNullProductName() {
        assertThatThrownBy(() -> ProductFactory.createProduct(null, 10.0, 15.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", " \t \n "})
    void shouldRejectWhitespaceOnlyProductName(String whitespaceName) {
        assertThatThrownBy(() -> ProductFactory.createProduct(whitespaceName, 10.0, 15.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot be null or empty");
    }

    @Test
    void shouldRejectNegativeCostPrice() {
        assertThatThrownBy(() -> ProductFactory.createProduct("Test", -1.0, 15.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cost price must be >= 0");
    }

    @Test
    void shouldRejectNegativeSalePrice() {
        assertThatThrownBy(() -> ProductFactory.createProduct("Test", 10.0, -1.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Sale price must be >= 0");
    }

    @Test
    void shouldRejectNegativeStock() {
        assertThatThrownBy(() -> ProductFactory.createProduct("Test", 10.0, 15.0, -1, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Stock quantity must be >= 0");
    }

    @Test
    void shouldAcceptZeroValues() {
        Product product = ProductFactory.createProduct("Zero Values", 0.0, 0.0, 0, null);
        assertThat(product.getCostPrice()).isEqualTo(0.0);
        assertThat(product.getSalePrice()).isEqualTo(0.0);
        assertThat(product.getStockQuantity()).isEqualTo(0);
    }

    @Test
    void shouldAcceptVeryLargeValues() {
        Product product = ProductFactory.createProduct("Expensive", 999999.99, 1999999.99, 999999, null);
        assertThat(product.getCostPrice()).isEqualTo(999999.99);
        assertThat(product.getSalePrice()).isEqualTo(1999999.99);
        assertThat(product.getStockQuantity()).isEqualTo(999999);
    }

    @Test
    void shouldAcceptDecimalPrices() {
        Product product = ProductFactory.createProduct("Precise", 10.99, 15.49, 1, null);
        assertThat(product.getCostPrice()).isEqualTo(10.99);
        assertThat(product.getSalePrice()).isEqualTo(15.49);
    }

    @Test
    void shouldTrimProductName() {
        Product product = ProductFactory.createProduct("  Test Product  ", 10.0, 15.0, 5, null);
        assertThat(product.getName()).isEqualTo("Test Product");
    }

    @Test
    void shouldRejectEmptyCategoryName() {
        Category category = new Category();
        assertThatThrownBy(() -> category.setName(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Category name cannot be null or empty");
    }

    @Test
    void shouldRejectNullCategoryName() {
        Category category = new Category();
        assertThatThrownBy(() -> category.setName(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Category name cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", " \t \n "})
    void shouldRejectWhitespaceOnlyCategoryName(String whitespaceName) {
        Category category = new Category();
        assertThatThrownBy(() -> category.setName(whitespaceName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Category name cannot be null or empty");
    }

    @Test
    void shouldAcceptNullCategoryDescription() {
        Category category = new Category(0, "Test");
    }

    @Test
    void shouldAcceptEmptyCategoryDescription() {
        Category category = new Category(0, "Test");
    }

    @Test
    void shouldAllowZeroMovementQuantity() {
        Movement movement = new Movement();
        movement.setQuantity(0);
        assertThat(movement.getQuantity()).isEqualTo(0);
    }

    @Test
    void shouldAllowNegativeMovementQuantity() {
        Movement movement = new Movement();
        movement.setQuantity(-1);
        assertThat(movement.getQuantity()).isEqualTo(-1);
    }

    @Test
    void shouldAcceptMinimumMovementQuantity() {
        Movement movement = new Movement();
        movement.setQuantity(1);
        assertThat(movement.getQuantity()).isEqualTo(1);
    }

    @Test
    void shouldAcceptLargeMovementQuantity() {
        Movement movement = new Movement();
        movement.setQuantity(1000000);
        assertThat(movement.getQuantity()).isEqualTo(1000000);
    }

    @Test
    void shouldHandleVeryLongProductNames() {
        String longName = "A".repeat(10000);
        Product product = ProductFactory.createProduct(longName, 10.0, 15.0, 5, null);
        assertThat(product.getName()).isEqualTo(longName);
    }

    @Test
    void shouldHandleVeryLongCategoryNames() {
        String longName = "B".repeat(5000);
        Category category = new Category(0, longName);
        assertThat(category.getName()).isEqualTo(longName);
    }

    @Test
    void shouldHandleVeryLongDescriptions() {
        String longDescription = "C".repeat(10000);
        Category category = new Category(0, "Test");
    }

    @Test
    void shouldHandleUnicodeCharacters() {
        String unicodeName = "Producto ñáéíóú 中文 العربية";
        Product product = ProductFactory.createProduct(unicodeName, 10.0, 15.0, 5, null);
        assertThat(product.getName()).isEqualTo(unicodeName);
    }

    @Test
    void shouldHandleSpecialCharactersInNames() {
        String specialName = "Product@#$%^&*()[]{}|;:,.<>?";
        Product product = ProductFactory.createProduct(specialName, 10.0, 15.0, 5, null);
        assertThat(product.getName()).isEqualTo(specialName);
    }

    @Test
    void shouldHandleExtremeDecimalValues() {
        Product product = ProductFactory.createProduct("Extreme", 0.01, 999999.99, 1, null);
        assertThat(product.getCostPrice()).isEqualTo(0.01);
        assertThat(product.getSalePrice()).isEqualTo(999999.99);
    }

    @Test
    void shouldHandleScientificNotationPrices() {
        // This tests if the system can handle prices that might come in scientific notation
        double costPrice = 1e-2; // 0.01
        double salePrice = 1e6;  // 1000000.0
        Product product = ProductFactory.createProduct("Scientific", costPrice, salePrice, 1, null);
        assertThat(product.getCostPrice()).isEqualTo(0.01);
        assertThat(product.getSalePrice()).isEqualTo(1000000.0);
    }
}