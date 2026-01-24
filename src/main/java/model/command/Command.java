package model.command;

import java.sql.SQLException;

/**
 * Command interface for the Command Pattern.
 * All user actions (add, remove) implement this interface.
 */
public interface Command {
    
    /**
     * Executes the command.
     * 
     * @throws SQLException if a database error occurs
     */
    void execute() throws SQLException;
}
