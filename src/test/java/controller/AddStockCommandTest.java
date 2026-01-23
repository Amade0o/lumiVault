package controller;

import model.Movement;
import model.Product;
import model.command.AddStockCommand;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AddStockCommandTest {

    @Test
    void shouldCreateAddStockCommandWithEntryMovement() {
        // Given
        Product product = new Product();
        product.setId("1234567890123");
        Movement movement = new Movement();
        movement.setProduct(product);
        movement.setQuantity(10);
        movement.setType(Movement.MovementType.STOCK_ENTRY);

        // When
        AddStockCommand command = new AddStockCommand(movement);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() would require database connection, tested in integration tests
    }

    @Test
    void shouldCreateAddStockCommandWithExitMovement() {
        // Given
        Product product = new Product();
        product.setId("1234567890123");
        Movement movement = new Movement();
        movement.setProduct(product);
        movement.setQuantity(5);
        movement.setType(Movement.MovementType.STOCK_EXIT);

        // When
        AddStockCommand command = new AddStockCommand(movement);

        // Then
        assertThat(command).isNotNull();
        // Note: execute() would require database connection, tested in integration tests
    }
}