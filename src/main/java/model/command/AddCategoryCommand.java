package model.command;

import database.DatabaseConnection;
import model.Category;
import model.Movement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Command to add categories to the database.
 * Implements the Command Pattern.
 */
public class AddCategoryCommand implements Command {

    private Category categoryToAdd;

    public AddCategoryCommand(Category category) {
        this.categoryToAdd = category;
    }

    /**
     * Executes the add category command.
     *
     * @throws SQLException if a database error occurs
     */
    @Override
    public void execute() throws SQLException {
        insertCategory();
    }

    /**
     * Inserts a category into the database.
     */
    private int insertCategory() throws SQLException {
        // Validate category data
        validateCategory();
        
        String insertCategorySql = """
            INSERT INTO categoria (nombre)
            VALUES (?)
            """;

        Connection connection = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(
                insertCategorySql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, categoryToAdd.getName());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    categoryToAdd.setId(generatedId);
                    
                    recordCategoryCreationMovement(generatedId);
                    
                    return generatedId;
                }
            }
        }
        return -1;
    }
    
    /**
     * Records a category creation movement.
     */
    private void recordCategoryCreationMovement(int categoryId) throws SQLException {
        Category categoryWithId = new Category();
        categoryWithId.setId(categoryId);
        categoryWithId.setName(categoryToAdd.getName());
        
        Movement categoryCreatedMovement = new Movement();

        categoryCreatedMovement.setType(Movement.MovementType.CATEGORY_CREATED);
        categoryCreatedMovement.setDescription("Categoría creada");
        categoryCreatedMovement.setDateTime(LocalDateTime.now());
        
        AddStockCommand movementCommand = new AddStockCommand(categoryCreatedMovement);
        movementCommand.execute();
    }
    
    /**
     * Validates category data before insertion.
     * Throws SQLException for invalid data.
     */
    private void validateCategory() throws SQLException {
        if (categoryToAdd.getName() == null || categoryToAdd.getName().trim().isEmpty()) {
            throw new SQLException("Category name cannot be null or empty");
        }
    }
}
