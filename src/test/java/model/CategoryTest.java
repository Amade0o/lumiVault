package model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CategoryTest {

    @Test
    void shouldCreateCategoryWithValidData() {
        // Given & When
        Category category = new Category(1, "Electronics");

        // Then
        assertThat(category.getId()).isEqualTo(1);
        assertThat(category.getName()).isEqualTo("Electronics");
    }

    @Test
    void shouldCreateEmptyCategory() {
        // When
        Category category = new Category();

        // Then
        assertThat(category.getId()).isEqualTo(0);
        assertThat(category.getName()).isNull();
    }

    @Test
    void shouldSetAndGetProperties() {
        // Given
        Category category = new Category();

        // When
        category.setId(5);
        category.setName("Test Category");

        // Then
        assertThat(category.getId()).isEqualTo(5);
        assertThat(category.getName()).isEqualTo("Test Category");
    }

    @Test
    void shouldRejectNullName() {
        // Given
        Category category = new Category();

        // When & Then
        assertThatThrownBy(() -> category.setName(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Category name cannot be null or empty");
    }

    @Test
    void shouldRejectEmptyName() {
        // Given
        Category category = new Category();

        // When & Then
        assertThatThrownBy(() -> category.setName(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Category name cannot be null or empty");
    }

    @Test
    void shouldRejectBlankName() {
        // Given
        Category category = new Category();

        // When & Then
        assertThatThrownBy(() -> category.setName("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Category name cannot be null or empty");
    }

    @Test
    void shouldAllowNullDescription() {
        // Given
        Category category = new Category();

        // When

        // Then
    }

    @Test
    void shouldAllowEmptyDescription() {
        // Given
        Category category = new Category();

        // When

        // Then
    }

    @Test
    void shouldHandleLongNames() {
        // Given
        String longName = "A".repeat(1000);
        String longDescription = "B".repeat(2000);

        // When
        Category category = new Category(1, longName);

        // Then
        assertThat(category.getName()).isEqualTo(longName);
    }
}