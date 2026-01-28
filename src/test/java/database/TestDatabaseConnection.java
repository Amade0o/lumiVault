package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test database connection using H2 in-memory database for unit testing.
 * This replaces the singleton DatabaseConnection for testing purposes.
 */
public class TestDatabaseConnection {

    private static Connection testConnection;

    /**
     * Gets a test connection to H2 in-memory database.
     * Creates tables if they don't exist.
     */
    public static Connection getTestConnection() throws SQLException {
        if (testConnection == null || testConnection.isClosed()) {
            testConnection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
            createTables(testConnection);
        }
        return testConnection;
    }

    /**
     * Creates the necessary tables for testing.
     */
    private static void createTables(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create categoria table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoria (
                    id_categoria INTEGER PRIMARY KEY AUTO_INCREMENT,
                    nombre TEXT NOT NULL UNIQUE,
                    descripcion TEXT
                )
                """);

            // Create producto table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS producto (
                    id_producto VARCHAR(13) PRIMARY KEY,
                    nombre TEXT NOT NULL UNIQUE,
                    precio_costo DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    precio_venta DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    cantidad_stock INTEGER NOT NULL DEFAULT 0,
                    id_categoria INTEGER,
                    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE SET NULL
                )
                """);

            // Create movimiento table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS movimiento (
                    id_movimiento INTEGER PRIMARY KEY AUTO_INCREMENT,
                    id_producto VARCHAR(13),
                    id_categoria INTEGER,
                    cantidad INTEGER,
                    tipo VARCHAR(50),
                    descripcion VARCHAR(500),
                    fecha VARCHAR(30),
                    FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
                    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
                )
                """);
        }
    }

    /**
     * Closes the test connection and cleans up.
     */
    public static void closeTestConnection() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
            testConnection = null;
        }
    }

    /**
     * Resets the database by dropping and recreating all tables.
     */
    public static void resetDatabase() throws SQLException {
        Connection conn = getTestConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS movimiento");
            stmt.execute("DROP TABLE IF EXISTS producto");
            stmt.execute("DROP TABLE IF EXISTS categoria");
        }
        createTables(conn);
    }
}