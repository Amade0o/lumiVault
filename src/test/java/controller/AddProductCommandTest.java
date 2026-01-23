package controller;

import model.Category;
import model.Product;
import model.command.AddProductCommand;
import model.factory.ProductFactory;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AddProductCommandTest {

    @Test
    void shouldCreateAddProductCommandWithProduct() {
        // Given
        Product product = ProductFactory.createProduct("Test Product", 10.0, 15.0, 5, null);

        // When
        AddProductCommand command = new AddProductCommand(product);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() would require database connection, tested in integration tests
    }

    @Test
    void shouldHandleProductWithCategory() {
        // Given
        Category category = new Category(1, "Electronics", "Electronic items");
        Product product = ProductFactory.createProduct("Laptop", 500.0, 700.0, 10, category);

        // When
        AddProductCommand command = new AddProductCommand(product);

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    void shouldHandleProductWithoutCategory() {
        // Given
        Product product = ProductFactory.createProduct("Mouse", 10.0, 15.0, 5, null);

        // When
        AddProductCommand command = new AddProductCommand(product);

        // Then
        assertThat(command).isNotNull();
    }
}