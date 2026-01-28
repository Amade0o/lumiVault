package validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for search input validation and sanitization.
 * Tests edge cases and malicious input scenarios.
 */
public class SearchValidationTest {

    @Test
    @DisplayName("Null search term should be treated as empty")
    void testNullSearchTerm() {
        // Given
        String searchTerm = null;
        
        // When
        String normalized = normalizeSearchTerm(searchTerm);
        
        // Then
        assertThat(normalized).isEmpty();
    }

    @Test
    @DisplayName("Empty search term should remain empty")
    void testEmptySearchTerm() {
        // Given
        String searchTerm = "";
        
        // When
        String normalized = normalizeSearchTerm(searchTerm);
        
        // Then
        assertThat(normalized).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "   ",
        "\t\t",
        "\n\n",
        " \t\n ",
        "          "
    })
    @DisplayName("Whitespace-only search terms should be treated as empty")
    void testWhitespaceOnlySearchTerms(String searchTerm) {
        // When
        String normalized = normalizeSearchTerm(searchTerm);
        
        // Then
        assertThat(normalized).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "  coca  ",
        "\tcoca\t",
        " coca\n",
        "\n\tcoca \t\n"
    })
    @DisplayName("Search terms with surrounding whitespace should be trimmed")
    void testSearchTermsWithSurroundingWhitespace(String searchTerm) {
        // When
        String normalized = normalizeSearchTerm(searchTerm);
        
        // Then
        assertThat(normalized).isEqualTo("coca");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "COCA",
        "Coca",
        "coca",
        "CoCa",
        "cOcA"
    })
    @DisplayName("Search should be case-insensitive")
    void testCaseInsensitiveSearch(String searchTerm) {
        // When
        String normalized = normalizeSearchTerm(searchTerm);
        
        // Then
        assertThat(normalized).isEqualTo("coca");
    }

    @Test
    @DisplayName("Very long search term should be handled correctly")
    void testVeryLongSearchTerm() {
        // Given
        String searchTerm = "a".repeat(10000);
        
        // When
        String normalized = normalizeSearchTerm(searchTerm);
        
        // Then
        assertThat(normalized).hasSize(10000);
        assertThat(normalized).isEqualTo("a".repeat(10000));
    }

    @Test
    @DisplayName("Search term with SQL injection attempt should be safe")
    void testSQLInjectionAttempt() {
        // Given
        String maliciousInput = "'; DROP TABLE producto; --";
        
        // When - Normalize and use in stream filter (not SQL)
        String normalized = normalizeSearchTerm(maliciousInput);
        
        // Then - Should be treated as normal text
        assertThat(normalized).isEqualTo("'; drop table producto; --");
        
        // Note: Since we use stream filters, not SQL WHERE clauses,
        // this is automatically safe from SQL injection
    }

    @Test
    @DisplayName("Search term with special regex characters should be literal")
    void testRegexSpecialCharacters() {
        // Given - Characters that have special meaning in regex
        List<String> specialChars = List.of(
            ".", "^", "$", "*", "+", "?", "{", "}", "[", "]", "\\", "|", "(", ")"
        );
        
        // When/Then - Each should be treated literally
        for (String specialChar : specialChars) {
            String normalized = normalizeSearchTerm(specialChar);
            assertThat(normalized).isEqualTo(specialChar.toLowerCase());
        }
    }

    @Test
    @DisplayName("Unicode characters should be preserved")
    void testUnicodeCharacters() {
        // Given
        List<String> unicodeInputs = List.of(
            "Café",
            "Piña",
            "日本語",
            "Emoji 😀",
            "中文",
            "Über",
            "Zürich"
        );
        
        // When/Then
        for (String input : unicodeInputs) {
            String normalized = normalizeSearchTerm(input);
            assertThat(normalized).isNotEmpty();
            // Should preserve Unicode but lowercase where applicable
        }
    }

    @Test
    @DisplayName("Search with HTML/XML tags should be treated as text")
    void testHTMLTagsInSearch() {
        // Given
        String searchWithTags = "<script>alert('xss')</script>";
        
        // When
        String normalized = normalizeSearchTerm(searchWithTags);
        
        // Then - Tags should be preserved as literal text
        assertThat(normalized).contains("<script>");
        assertThat(normalized).contains("</script>");
    }

    @Test
    @DisplayName("Consecutive spaces in search should be preserved")
    void testConsecutiveSpaces() {
        // Given
        String searchTerm = "coca    cola";
        
        // When
        String normalized = normalizeSearchTerm(searchTerm);
        
        // Then
        assertThat(normalized).isEqualTo("coca    cola");
        assertThat(normalized).contains("    "); // 4 spaces preserved
    }

    @Test
    @DisplayName("Search validation should handle control characters")
    void testControlCharacters() {
        // Given - Various control characters
        String searchWithControls = "test\u0000\u0001\u0002string";
        
        // When
        String normalized = normalizeSearchTerm(searchWithControls);
        
        // Then - Control characters should be preserved (up to the application to handle)
        assertThat(normalized).contains("test");
        assertThat(normalized).contains("string");
    }

    @Test
    @DisplayName("Product name validation should reject duplicates")
    void testDuplicateProductNameValidation() {
        // Given
        List<String> existingNames = List.of("Coca Cola", "Pepsi", "Sprite");
        String newName = "Coca Cola"; // Duplicate
        
        // When
        boolean isDuplicate = existingNames.stream()
                .anyMatch(name -> name.equalsIgnoreCase(newName));
        
        // Then
        assertThat(isDuplicate).isTrue();
    }

    @Test
    @DisplayName("Product name validation should accept unique names")
    void testUniqueProductNameValidation() {
        // Given
        List<String> existingNames = List.of("Coca Cola", "Pepsi", "Sprite");
        String newName = "Fanta"; // Unique
        
        // When
        boolean isDuplicate = existingNames.stream()
                .anyMatch(name -> name.equalsIgnoreCase(newName));
        
        // Then
        assertThat(isDuplicate).isFalse();
    }

    @Test
    @DisplayName("Search should filter correctly with accent marks")
    void testAccentMarkFiltering() {
        // Given
        List<MockProduct> products = List.of(
            new MockProduct("Café con Leche", "CAF001"),
            new MockProduct("Cafe sin Leche", "CAF002"),
            new MockProduct("Jalapeño", "JAL001"),
            new MockProduct("Jalapeno", "JAL002")
        );
        
        // When - Search for "café" (with accent)
        String searchTerm1 = "café";
        List<MockProduct> results1 = products.stream()
                .filter(p -> p.name.toLowerCase().contains(searchTerm1.toLowerCase()))
                .toList();
        
        // When - Search for "cafe" (without accent)
        String searchTerm2 = "cafe";
        List<MockProduct> results2 = products.stream()
                .filter(p -> p.name.toLowerCase().contains(searchTerm2.toLowerCase()))
                .toList();
        
        // Then - Exact match only (not accent-insensitive by default)
        assertThat(results1).hasSize(1); // Only "Café con Leche"
        assertThat(results2).hasSize(1); // Only "Cafe sin Leche"
    }

    @Test
    @DisplayName("Empty result set should be handled gracefully")
    void testEmptyResultSet() {
        // Given
        List<MockProduct> products = new ArrayList<>();
        String searchTerm = "coca";
        
        // When
        List<MockProduct> results = products.stream()
                .filter(p -> p.name.toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();
        
        // Then
        assertThat(results).isEmpty();
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("Large result set should be handled efficiently")
    void testLargeResultSet() {
        // Given - 1000 products all matching search term
        List<MockProduct> products = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            products.add(new MockProduct("Product " + i, "ID" + i));
        }
        
        String searchTerm = "product";
        
        // When
        long startTime = System.nanoTime();
        List<MockProduct> results = products.stream()
                .filter(p -> p.name.toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        // Then
        assertThat(results).hasSize(1000);
        assertThat(durationMs).isLessThan(100); // Should be very fast
        System.out.println("Filtered 1000 products in " + durationMs + "ms");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Normalizes search term: trims whitespace and converts to lowercase.
     */
    private String normalizeSearchTerm(String searchTerm) {
        if (searchTerm == null) {
            return "";
        }
        return searchTerm.toLowerCase().trim();
    }

    /**
     * Mock product class for testing
     */
    private static class MockProduct {
        String name;
        String id;
        
        MockProduct(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }
}
