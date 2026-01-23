package controller;

import model.Category;
import model.Product;
import model.factory.ProductFactory;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ProductFactoryTest {

    @Test
    void shouldCreateProductWithAllParameters() {
        // Given
        Category category = new Category(1, "Electronics", "Electronic items");

        // When
        Product product = ProductFactory.createProduct("Laptop", 500.0, 700.0, 10, category);

        // Then
        assertThat(product.getName()).isEqualTo("Laptop");
        assertThat(product.getCostPrice()).isEqualTo(500.0);
        assertThat(product.getSalePrice()).isEqualTo(700.0);
        assertThat(product.getStockQuantity()).isEqualTo(10);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getId()).isNull(); // Not set by factory
    }

    @Test
    void shouldCreateProductWithNullCategory() {
        // When
        Product product = ProductFactory.createProduct("Mouse", 10.0, 15.0, 5, null);

        // Then
        assertThat(product.getName()).isEqualTo("Mouse");
        assertThat(product.getCostPrice()).isEqualTo(10.0);
        assertThat(product.getSalePrice()).isEqualTo(15.0);
        assertThat(product.getStockQuantity()).isEqualTo(5);
        assertThat(product.getCategory()).isNull();
    }

    @Test
    void shouldThrowExceptionForNullName() {
        // When & Then
        assertThatThrownBy(() -> ProductFactory.createProduct(null, 10.0, 15.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionForEmptyName() {
        // When & Then
        assertThatThrownBy(() -> ProductFactory.createProduct("", 10.0, 15.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionForBlankName() {
        // When & Then
        assertThatThrownBy(() -> ProductFactory.createProduct("   ", 10.0, 15.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionForNegativeCostPrice() {
        // When & Then
        assertThatThrownBy(() -> ProductFactory.createProduct("Test", -10.0, 15.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cost price must be >= 0");
    }

    @Test
    void shouldThrowExceptionForNegativeSalePrice() {
        // When & Then
        assertThatThrownBy(() -> ProductFactory.createProduct("Test", 10.0, -15.0, 5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Sale price must be >= 0");
    }

    @Test
    void shouldThrowExceptionForNegativeStock() {
        // When & Then
        assertThatThrownBy(() -> ProductFactory.createProduct("Test", 10.0, 15.0, -5, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Stock quantity must be >= 0");
    }

    @Test
    void shouldAllowZeroPrices() {
        // When
        Product product = ProductFactory.createProduct("Free Item", 0.0, 0.0, 10, null);

        // Then
        assertThat(product.getCostPrice()).isEqualTo(0.0);
        assertThat(product.getSalePrice()).isEqualTo(0.0);
    }

    @Test
    void shouldAllowZeroStock() {
        // When
        Product product = ProductFactory.createProduct("Out of Stock", 10.0, 15.0, 0, null);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(0);
    }

    @Test
    void shouldHandleLargeValues() {
        // When
        Product product = ProductFactory.createProduct("Expensive Item",
            1000000.99, 2000000.99, 999999, null);

        // Then
        assertThat(product.getCostPrice()).isEqualTo(1000000.99);
        assertThat(product.getSalePrice()).isEqualTo(2000000.99);
        assertThat(product.getStockQuantity()).isEqualTo(999999);
    }

    @Test
    void shouldHandleDecimalPrices() {
        // When
        Product product = ProductFactory.createProduct("Precise Price",
            10.99, 15.49, 1, null);

        // Then
        assertThat(product.getCostPrice()).isEqualTo(10.99);
        assertThat(product.getSalePrice()).isEqualTo(15.49);
    }

    @Test
    void shouldHandleSpecialCharactersInName() {
        // When
        Product product = ProductFactory.createProduct("Product@#$%^&*()",
            10.0, 15.0, 5, null);

        // Then
        assertThat(product.getName()).isEqualTo("Product@#$%^&*()");
    }

    @Test
    void shouldHandleUnicodeCharactersInName() {
        // When
        Product product = ProductFactory.createProduct("Producto ñáéíóú",
            10.0, 15.0, 5, null);

        // Then
        assertThat(product.getName()).isEqualTo("Producto ñáéíóú");
    }
}