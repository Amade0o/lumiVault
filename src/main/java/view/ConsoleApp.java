package view;

import controller.Controller;
import model.factory.ProductFactory;
import database.DatabaseConnection;
import model.Category;
import model.Product;
import model.Movement;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based interactive application for testing LumiVault inventory system.
 * Provides a menu-driven interface to test all system functionalities.
 */
public class ConsoleApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static Controller controller;

    public static void main(String[] args) {
        System.out.println("=== LumiVault - Console Test Interface ===\n");

        try {
            // Initialize database connection
            DatabaseConnection.getInstance().getConnection();
            controller = new Controller();

            // Run interactive menu
            runInteractiveMenu();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always close connection when done
            DatabaseConnection.getInstance().closeConnection();
            scanner.close();
        }
    }

    /**
     * Runs the interactive menu for testing the system.
     */
    private static void runInteractiveMenu() throws SQLException {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    addProduct();
                    break;
                case 2:
                    addCategory();
                    break;
                case 3:
                    listProducts();
                    break;
                case 4:
                    listCategories();
                    break;
                case 5:
                    removeProduct();
                    break;
                case 6:
                    removeCategory();
                    break;
                case 7:
                    recordStockMovement();
                    break;
                case 8:
                    viewStockMovements();
                    break;
                case 0:
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }

            if (running) {
                System.out.println("\n" + "=".repeat(50));
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== LumiVault Menu ===");
        System.out.println("1. Add Product");
        System.out.println("2. Add Category");
        System.out.println("3. List All Products");
        System.out.println("4. List All Categories");
        System.out.println("5. Remove Product");
        System.out.println("6. Remove Category");
        System.out.println("7. Record Stock Movement");
        System.out.println("8. View Stock Movements for Product");
        System.out.println("0. Exit");
        System.out.println("========================");
    }

    private static void addProduct() {
        try {
            System.out.println("\n--- Add New Product ---");

            String productId = getStringInput("Product ID (barcode): ");
            String name = getStringInput("Product name: ");
            double costPrice = getDoubleInput("Cost price: ");
            double salePrice = getDoubleInput("Sale price: ");
            int stock = getIntInput("Initial stock: ");

            // Show available categories
            List<Category> categories = controller.getAllCategories();
            if (categories.isEmpty()) {
                System.out.println("No categories available. Please add a category first.");
                return;
            }

            System.out.println("Available categories:");
            for (Category cat : categories) {
                System.out.println(cat.getId() + ". " + cat.getName());
            }

            int categoryId = getIntInput("Category ID (0 for no category): ");
            Category category = categoryId > 0 ? new Category(categoryId, "", "") : null;

            Product product = ProductFactory.createProduct(name, costPrice, salePrice, stock, category);
            product.setId(productId); // Set the barcode ID
            String id = controller.addProduct(product);

            System.out.println("✓ Product created successfully with ID: " + id);

        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }

    private static void addCategory() {
        try {
            System.out.println("\n--- Add New Category ---");

            String name = getStringInput("Category name: ");
            String description = getStringInput("Description: ");

            Category category = new Category(0, name, description);
            int id = controller.addCategory(category);

            System.out.println("✓ Category created successfully with ID: " + id);

        } catch (Exception e) {
            System.out.println("Error adding category: " + e.getMessage());
        }
    }

    private static void listProducts() {
        try {
            System.out.println("\n--- All Products ---");

            List<Product> products = controller.getAllProducts();

            if (products.isEmpty()) {
                System.out.println("No products found.");
                return;
            }

            for (Product product : products) {
                System.out.printf("ID: %d | %s | Stock: %d | Price: $%.2f | Category: %s%n",
                    product.getId(),
                    product.getName(),
                    product.getStockQuantity(),
                    product.getSalePrice(),
                    product.getCategory() != null ? product.getCategory().getName() : "None");
            }

        } catch (Exception e) {
            System.out.println("Error listing products: " + e.getMessage());
        }
    }

    private static void listCategories() {
        try {
            System.out.println("\n--- All Categories ---");

            List<Category> categories = controller.getAllCategories();

            if (categories.isEmpty()) {
                System.out.println("No categories found.");
                return;
            }

            for (Category category : categories) {
                System.out.printf("ID: %d | %s | %s%n",
                    category.getId(),
                    category.getName(),
                    category.getDescription());
            }

        } catch (Exception e) {
            System.out.println("Error listing categories: " + e.getMessage());
        }
    }

    private static void removeProduct() {
        try {
            System.out.println("\n--- Remove Product ---");

            String id = getStringInput("Product ID (barcode) to remove: ");
            boolean success = controller.removeProduct(id);

            if (success) {
                System.out.println("✓ Product removed successfully.");
            } else {
                System.out.println("✗ Product not found or could not be removed.");
            }

        } catch (Exception e) {
            System.out.println("Error removing product: " + e.getMessage());
        }
    }

    private static void removeCategory() {
        try {
            System.out.println("\n--- Remove Category ---");

            int id = getIntInput("Category ID to remove: ");
            boolean success = controller.removeCategory(id);

            if (success) {
                System.out.println("✓ Category removed successfully.");
            } else {
                System.out.println("✗ Category not found or could not be removed.");
            }

        } catch (Exception e) {
            System.out.println("Error removing category: " + e.getMessage());
        }
    }

    private static void recordStockMovement() {
        try {
            System.out.println("\n--- Record Stock Movement ---");

            String productId = getStringInput("Product ID (barcode): ");
            int quantity = getIntInput("Quantity: ");
            String type = getStringInput("Type (ENTRY/EXIT): ").toUpperCase();

            Movement.MovementType movementType;
            try {
                movementType = Movement.MovementType.valueOf(type);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type. Use ENTRY or EXIT.");
                return;
            }

            Product product = new Product();
            product.setId(productId);
            Movement movement = new Movement();
            movement.setProduct(product);
            movement.setQuantity(quantity);
            movement.setType(movementType);
            movement.setDescription("Stock movement from console");
            movement.setDateTime(null); // Will use current time
            int id = controller.recordStockMovement(movement);

            System.out.println("✓ Stock movement recorded with ID: " + id);

        } catch (Exception e) {
            System.out.println("Error recording stock movement: " + e.getMessage());
        }
    }

    private static void viewStockMovements() {
        try {
            System.out.println("\n--- Stock Movements for Product ---");

            String productId = getStringInput("Product ID (barcode): ");
            List<Movement> movements = controller.getMovementsForProduct(productId);

            if (movements.isEmpty()) {
                System.out.println("No movements found for this product.");
                return;
            }

            for (Movement movement : movements) {
                System.out.printf("ID: %d | Type: %s | Quantity: %d | Date: %s%n",
                    movement.getId(),
                    movement.getType(),
                    movement.getQuantity(),
                    movement.getDateTime());
            }

        } catch (Exception e) {
            System.out.println("Error viewing stock movements: " + e.getMessage());
        }
    }

    // Helper methods for input
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}