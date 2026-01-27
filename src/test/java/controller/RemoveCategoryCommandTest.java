package controller;

import model.command.RemoveCategoryCommand;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RemoveCategoryCommand.
 * Integration tests with database are in ControllerIntegrationTest.
 */
class RemoveCategoryCommandTest {

    @Test
    void shouldCreateRemoveCategoryCommandWithCategoryId() {
        // Given
        int categoryId = 1;

        // When
        RemoveCategoryCommand command = new RemoveCategoryCommand(categoryId);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() would require database connection, tested in integration tests
    }

    @Test
    void shouldHandleZeroCategoryId() {
        // Given
        int categoryId = 0;

        // When
        RemoveCategoryCommand command = new RemoveCategoryCommand(categoryId);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() should throw SQLException or not find category, tested in integration tests
    }

    @Test
    void shouldHandleNegativeCategoryId() {
        // Given
        int categoryId = -1;

        // When
        RemoveCategoryCommand command = new RemoveCategoryCommand(categoryId);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() should throw SQLException or not find category, tested in integration tests
    }

    @Test
    void shouldHandleLargeCategoryId() {
        // Given
        int categoryId = Integer.MAX_VALUE;

        // When
        RemoveCategoryCommand command = new RemoveCategoryCommand(categoryId);

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    void shouldHandlePositiveCategoryId() {
        // Given
        int categoryId = 999;

        // When
        RemoveCategoryCommand command = new RemoveCategoryCommand(categoryId);

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    void shouldHandleMinimumCategoryId() {
        // Given
        int categoryId = Integer.MIN_VALUE;

        // When
        RemoveCategoryCommand command = new RemoveCategoryCommand(categoryId);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() should throw SQLException or not find category, tested in integration tests
    }
}
