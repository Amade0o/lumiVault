package controller;

import model.command.RemoveProductCommand;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RemoveProductCommand.
 * Integration tests with database are in ControllerIntegrationTest.
 */
class RemoveProductCommandTest {

    @Test
    void shouldCreateRemoveProductCommandWithProductId() {
        // Given
        String productId = "PROD001";

        // When
        RemoveProductCommand command = new RemoveProductCommand(productId);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() would require database connection, tested in integration tests
    }

    @Test
    void shouldHandleProductIdWithSpaces() {
        // Given
        String productId = "PROD 001";

        // When
        RemoveProductCommand command = new RemoveProductCommand(productId);

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    void shouldHandleProductIdWithSpecialCharacters() {
        // Given
        String productId = "PROD-001_TEST";

        // When
        RemoveProductCommand command = new RemoveProductCommand(productId);

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    void shouldHandleEmptyProductId() {
        // Given
        String productId = "";

        // When
        RemoveProductCommand command = new RemoveProductCommand(productId);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() should throw SQLException, tested in integration tests
    }

    @Test
    void shouldHandleNullProductId() {
        // Given
        String productId = null;

        // When
        RemoveProductCommand command = new RemoveProductCommand(productId);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() should throw SQLException, tested in integration tests
    }

    @Test
    void shouldHandleLongProductId() {
        // Given
        String productId = "VERY_LONG_PRODUCT_ID_123456789012345678901234567890";

        // When
        RemoveProductCommand command = new RemoveProductCommand(productId);

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    void shouldHandleNumericProductId() {
        // Given
        String productId = "123456789";

        // When
        RemoveProductCommand command = new RemoveProductCommand(productId);

        // Then
        assertThat(command).isNotNull();
    }
}
