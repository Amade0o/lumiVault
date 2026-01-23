package model.command;

import java.sql.SQLException;

/**
 * Command interface for the Command Pattern.
 * All operations (add, list, remove) implement this interface.
 */
public interface Command {
    
    /**
     * Executes the command and returns a result.
     * 
     * @return The result of the command execution
     * @throws SQLException if a database error occurs
     */
    Object execute() throws SQLException;
}