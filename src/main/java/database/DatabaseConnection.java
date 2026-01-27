package database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton class for managing SQLite database connection.
 * The .db file is created automatically if it doesn't exist.
 */
public class DatabaseConnection {
    
    private static final String DATABASE_FILE_NAME = "lumivault.db";
    private static final String DATA_FOLDER_NAME = "data";
    private static final String BACKUPS_FOLDER_NAME = "backups";
    private static final int MAX_BACKUPS = 20;
    
    private static DatabaseConnection instance;
    private Connection connection;
    private static Connection testConnection;
    
    private DatabaseConnection() {
    }
    
    /**
     * Gets the unique instance of DatabaseConnection.
     * Thread-safe implementation.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Gets the database connection.
     * Creates the database and tables if they don't exist.
     */
    public Connection getConnection() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            return testConnection;
        }
        
        if (connection == null || connection.isClosed()) {
            String databasePath = buildDatabasePath();
            String connectionUrl = "jdbc:sqlite:" + databasePath;
            
            connection = DriverManager.getConnection(connectionUrl);
            System.out.println("✓ Connected to SQLite: " + databasePath);
            
            createAutomaticBackup(databasePath);
            createTablesIfNotExist();
        }
        return connection;
    }
    
    /**
     * Closes the database connection.
     * Should be called when the application shuts down.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ SQLite connection closed");
            }
        } catch (SQLException exception) {
            System.err.println("Error closing connection: " + exception.getMessage());
        }
    }
    
    /**
     * Clears all data from all tables for testing purposes.
     * This method deletes all records from categoria, producto, and movimiento tables.
     */
    public void clearAllTables() throws SQLException {
        if (connection == null || connection.isClosed()) {
            getConnection();
        }
        
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = OFF");
            
            statement.execute("DELETE FROM movimiento");
            statement.execute("DELETE FROM producto");
            statement.execute("DELETE FROM categoria");
            
            statement.execute("DELETE FROM sqlite_sequence WHERE name='categoria'");
            statement.execute("DELETE FROM sqlite_sequence WHERE name='producto'");
            statement.execute("DELETE FROM sqlite_sequence WHERE name='movimiento'");
            
            statement.execute("PRAGMA foreign_keys = ON");
            
            System.out.println("✓ All tables cleared for testing");
        }
    }
    
    /**
     * Builds the database file path in a portable way.
     * Uses relative path from the application directory.
     */
    private String buildDatabasePath() {
        String applicationDirectory = System.getProperty("user.dir");
        
        File dataDirectory = new File(applicationDirectory, DATA_FOLDER_NAME);
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        
        return dataDirectory.getPath() + File.separator + DATABASE_FILE_NAME;
    }
    
    /**
     * Creates all required tables if they don't exist.
     */
    private void createTablesIfNotExist() throws SQLException {
        String createCategoryTableSql = """
            CREATE TABLE IF NOT EXISTS categoria (
                id_categoria INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE
            );
            """;
        
        String createProductTableSql = """
            CREATE TABLE IF NOT EXISTS producto (
                id_producto TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                precio_costo REAL NOT NULL DEFAULT 0,
                precio_venta REAL NOT NULL DEFAULT 0,
                cantidad_stock INTEGER NOT NULL DEFAULT 0,
                id_categoria INTEGER,
                FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
            );
            """;
        
        String createMovementTableSql = """
            CREATE TABLE IF NOT EXISTS movimiento (
                id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,
                id_producto TEXT,
                cantidad INTEGER,
                tipo TEXT NOT NULL,
                descripcion TEXT,
                fecha TEXT NOT NULL,
                FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
            );
            """;
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(createCategoryTableSql);
            statement.execute(createProductTableSql);
            statement.execute(createMovementTableSql);
            System.out.println("✓ Tables verified/created successfully");
        }
    }
    
    /**
     * Sets a test connection to be used instead of the production database.
     * This is for testing purposes only.
     */
    public static void setTestConnection(Connection testConn) {
        testConnection = testConn;
    }
    
    /**
     * Clears the test connection and returns to using the production database.
     */
    public static void clearTestConnection() {
        testConnection = null;
    }
    
    /**
     * Creates an automatic backup of the database with timestamp.
     * Backups are stored in data/backups/ folder.
     */
    private void createAutomaticBackup(String databasePath) {
        File dbFile = new File(databasePath);
        
        // Solo hacer backup si el archivo existe y tiene contenido
        if (!dbFile.exists() || dbFile.length() == 0) {
            return;
        }
        
        try {
            String applicationDirectory = System.getProperty("user.dir");
            File backupsDirectory = new File(applicationDirectory, DATA_FOLDER_NAME + File.separator + BACKUPS_FOLDER_NAME);
            
            if (!backupsDirectory.exists()) {
                backupsDirectory.mkdirs();
            }
            
            // Crear nombre con fecha: lumivault_2026-01-27_14-30-45.db
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String backupFileName = "lumivault_" + timestamp + ".db";
            File backupFile = new File(backupsDirectory, backupFileName);
            
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✓ Backup created: " + backupFileName);
            
            // Limpiar backups viejos
            cleanOldBackups(backupsDirectory);
            
        } catch (IOException e) {
            System.err.println("⚠ Warning: Could not create backup - " + e.getMessage());
        }
    }
    
    /**
     * Keeps only the most recent MAX_BACKUPS files and deletes older ones.
     */
    private void cleanOldBackups(File backupsDirectory) {
        File[] backupFiles = backupsDirectory.listFiles((dir, name) -> name.startsWith("lumivault_") && name.endsWith(".db"));
        
        if (backupFiles == null || backupFiles.length <= MAX_BACKUPS) {
            return;
        }
        
        // Ordenar por fecha de modificación (más antiguos primero)
        java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        
        // Eliminar los más viejos
        int toDelete = backupFiles.length - MAX_BACKUPS;
        for (int i = 0; i < toDelete; i++) {
            if (backupFiles[i].delete()) {
                System.out.println("✓ Old backup deleted: " + backupFiles[i].getName());
            }
        }
    }
}
