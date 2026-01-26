package model.command;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Command to remove a category from the database.
 * Products with this category will have their category set to NULL.
 * Implements the Command Pattern.
 */
public class RemoveCategoryCommand implements Command {
    
    private final int categoryId;
    
    public RemoveCategoryCommand(int categoryId) {
        this.categoryId = categoryId;
    }
    
    /**
     * Executes the remove category command.
     * 
     * @throws SQLException if a database error occurs
     */
    @Override
    public void execute() throws SQLException {
        deleteCategory();
    }
    
    /**
     * Deletes a category from the database.
     * Products with this category will have their category set to NULL.
     */
    private void deleteCategory() throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        // Update products to remove category reference
        String updateProductsCategorySql = """
            UPDATE producto SET id_categoria = NULL WHERE id_categoria = ?
            """;
        
        try (PreparedStatement updateStatement = connection.prepareStatement(updateProductsCategorySql)) {
            updateStatement.setInt(1, categoryId);
            updateStatement.executeUpdate();
        }
        
        // Delete the category
        String deleteCategorySql = "DELETE FROM categoria WHERE id_categoria = ?";
        
        int affectedRows;
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteCategorySql)) {
            deleteStatement.setInt(1, categoryId);
            affectedRows = deleteStatement.executeUpdate();
        }
        
        if (affectedRows == 0) {
            throw new SQLException("Category not found: " + categoryId);
        }
    }
}
