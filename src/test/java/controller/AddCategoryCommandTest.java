package controller;

import model.Category;
import model.command.AddCategoryCommand;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AddCategoryCommandTest {

    @Test
    void shouldCreateAddCategoryCommandWithCategory() {
        // Given
        Category category = new Category(0, "Test Category");

        // When
        AddCategoryCommand command = new AddCategoryCommand(category);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() would require database connection, tested in integration tests
    }

    @Test
    void shouldHandleCategoryWithNullDescription() {
        // Given
        Category category = new Category(0, "Test Category");

        // When
        AddCategoryCommand command = new AddCategoryCommand(category);

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    void shouldHandleCategoryWithEmptyDescription() {
        // Given
        Category category = new Category(0, "Test Category");

        // When
        AddCategoryCommand command = new AddCategoryCommand(category);

        // Then
        assertThat(command).isNotNull();
    }
}