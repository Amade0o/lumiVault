package model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductWithValidData() {
        // Given
        Category category = new Category(1, "Electronics");

        // When
        Product product = new Product("1234567890123", "Laptop", 500.0, 700.0, 10, category);

        // Then
        assertThat(product.getId()).isEqualTo("1234567890123");
        assertThat(product.getName()).isEqualTo("Laptop");
        assertThat(product.getCostPrice()).isEqualTo(500.0);
        assertThat(product.getSalePrice()).isEqualTo(700.0);
        assertThat(product.getStockQuantity()).isEqualTo(10);
        assertThat(product.getCategory()).isEqualTo(category);
    }

    @Test
    void shouldCreateEmptyProduct() {
        // When
        Product product = new Product();

        // Then
        assertThat(product.getId()).isNull();
        assertThat(product.getName()).isNull();
        assertThat(product.getCostPrice()).isEqualTo(0.0);
        assertThat(product.getSalePrice()).isEqualTo(0.0);
        assertThat(product.getStockQuantity()).isEqualTo(0);
        assertThat(product.getCategory()).isNull();
    }

    @Test
    void shouldSetAndGetProperties() {
        // Given
        Product product = new Product();
        Category category = new Category(1, "Test");

        // When
        product.setId("9876543210987");
        product.setName("Test Product");
        product.setCostPrice(100.0);
        product.setSalePrice(150.0);
        product.setStockQuantity(25);
        product.setCategory(category);

        // Then
        assertThat(product.getId()).isEqualTo("9876543210987");
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getCostPrice()).isEqualTo(100.0);
        assertThat(product.getSalePrice()).isEqualTo(150.0);
        assertThat(product.getStockQuantity()).isEqualTo(25);
        assertThat(product.getCategory()).isEqualTo(category);
    }

    @Test
    void shouldThrowExceptionForNegativeCostPrice() {
        // Given
        Product product = new Product();

        // When & Then
        assertThatThrownBy(() -> product.setCostPrice(-10.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cost price must be >= 0");
    }

    @Test
    void shouldThrowExceptionForNegativeSalePrice() {
        // Given
        Product product = new Product();

        // When & Then
        assertThatThrownBy(() -> product.setSalePrice(-10.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Sale price must be >= 0");
    }

    @Test
    void shouldThrowExceptionForNegativeStockQuantity() {
        // Given
        Product product = new Product();

        // When & Then
        assertThatThrownBy(() -> product.setStockQuantity(-5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Stock quantity must be >= 0");
    }

    @Test
    void shouldAllowZeroValues() {
        // Given
        Product product = new Product();

        // When
        product.setCostPrice(0.0);
        product.setSalePrice(0.0);
        product.setStockQuantity(0);

        // Then
        assertThat(product.getCostPrice()).isEqualTo(0.0);
        assertThat(product.getSalePrice()).isEqualTo(0.0);
        assertThat(product.getStockQuantity()).isEqualTo(0);
    }
}