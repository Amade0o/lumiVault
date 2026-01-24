package model.command;

import model.Movement;
import java.sql.SQLException;

public class UpdatePriceCommand implements Command {
    private Movement movement;

    public UpdatePriceCommand(Movement movement) {
        this.movement = movement;
    }
    
    @Override
    public void execute() throws SQLException {
        // TODO: Implement price update logic
        throw new UnsupportedOperationException("UpdatePriceCommand not yet implemented");
    }
}
