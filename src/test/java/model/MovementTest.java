package model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class MovementTest {

    @Test
    void shouldCreateMovementWithValidData() {
        // Given
        Product product = new Product();
        product.setId("1234567890123");
        LocalDateTime dateTime = LocalDateTime.now();

        // When
        Movement movement = new Movement();
        movement.setId(1);
        movement.setProduct(product);
        movement.setQuantity(10);
        movement.setType(Movement.MovementType.STOCK_ENTRY);
        movement.setDateTime(dateTime);

        // Then
        assertThat(movement.getId()).isEqualTo(1);
        assertThat(movement.getProduct()).isEqualTo(product);
        assertThat(movement.getQuantity()).isEqualTo(10);
        assertThat(movement.getType()).isEqualTo(Movement.MovementType.STOCK_ENTRY);
        assertThat(movement.getDateTime()).isEqualTo(dateTime);
    }

    @Test
    void shouldCreateEmptyMovement() {
        // When
        Movement movement = new Movement();

        // Then
        assertThat(movement.getId()).isEqualTo(0);
        assertThat(movement.getProduct()).isNull();
        assertThat(movement.getQuantity()).isNull();
        assertThat(movement.getType()).isNull();
        assertThat(movement.getDateTime()).isNull();
    }

    @Test
    void shouldSetAndGetProperties() {
        // Given
        Movement movement = new Movement();
        Product product = new Product();
        product.setId("9876543210987");
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 12, 0);

        // When
        movement.setId(10);
        movement.setProduct(product);
        movement.setQuantity(25);
        movement.setType(Movement.MovementType.STOCK_EXIT);
        movement.setDateTime(dateTime);

        // Then
        assertThat(movement.getId()).isEqualTo(10);
        assertThat(movement.getProduct()).isEqualTo(product);
        assertThat(movement.getQuantity()).isEqualTo(25);
        assertThat(movement.getType()).isEqualTo(Movement.MovementType.STOCK_EXIT);
        assertThat(movement.getDateTime()).isEqualTo(dateTime);
    }

    @Test
    void shouldAllowZeroQuantity() {
        // Given
        Movement movement = new Movement();

        // When & Then
        assertThatCode(() -> movement.setQuantity(0)).doesNotThrowAnyException();
        assertThat(movement.getQuantity()).isEqualTo(0);
    }

    @Test
    void shouldAllowNegativeQuantity() {
        // Given
        Movement movement = new Movement();

        // When & Then
        assertThatCode(() -> movement.setQuantity(-5)).doesNotThrowAnyException();
        assertThat(movement.getQuantity()).isEqualTo(-5);
    }

    @Test
    void shouldAllowLargeQuantities() {
        // Given
        Movement movement = new Movement();

        // When
        movement.setQuantity(1_000_000);

        // Then
        assertThat(movement.getQuantity()).isEqualTo(1_000_000);
    }

    @Test
    void shouldHandleEntryMovementType() {
        // Given
        Movement movement = new Movement();

        // When
        movement.setType(Movement.MovementType.STOCK_ENTRY);

        // Then
        assertThat(movement.getType()).isEqualTo(Movement.MovementType.STOCK_ENTRY);
    }

    @Test
    void shouldHandleExitMovementType() {
        // Given
        Movement movement = new Movement();

        // When
        movement.setType(Movement.MovementType.STOCK_EXIT);

        // Then
        assertThat(movement.getType()).isEqualTo(Movement.MovementType.STOCK_EXIT);
    }

    @Test
    void shouldHandleNullDateTime() {
        // Given
        Movement movement = new Movement();

        // When
        movement.setDateTime(null);

        // Then
        assertThat(movement.getDateTime()).isNull();
    }

    @Test
    void shouldHandleFutureDateTime() {
        // Given
        Movement movement = new Movement();
        LocalDateTime future = LocalDateTime.now().plusYears(1);

        // When
        movement.setDateTime(future);

        // Then
        assertThat(movement.getDateTime()).isEqualTo(future);
    }

    @Test
    void shouldHandlePastDateTime() {
        // Given
        Movement movement = new Movement();
        LocalDateTime past = LocalDateTime.now().minusYears(1);

        // When
        movement.setDateTime(past);

        // Then
        assertThat(movement.getDateTime()).isEqualTo(past);
    }
}