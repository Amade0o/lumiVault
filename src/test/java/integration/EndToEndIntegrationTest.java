package integration;

import controller.Controller;
import controller.CommandType;
import model.factory.ProductFactory;
import database.DatabaseConnection;
import database.TestDatabaseConnection;
import model.Category;
import model.Product;
import model.Movement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end integration tests that simulate real-world usage scenarios.
 * These tests cover complete user workflows from setup to reporting.
 */
class EndToEndIntegrationTest {

    private Controller controller;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseConnection.resetDatabase();
        DatabaseConnection.setTestConnection(TestDatabaseConnection.getTestConnection());
        controller = new Controller();
    }

    @AfterEach
    void tearDown() throws SQLException {
        DatabaseConnection.clearTestConnection();
        TestDatabaseConnection.closeTestConnection();
    }

    @Test
    void shouldCompleteFullInventoryLifecycle() throws SQLException {
        // === PHASE 1: System Setup ===
        // Create categories
        Category electronics = new Category(0, "Electronics", "Electronic devices and accessories");
        Category clothing = new Category(0, "Clothing", "Apparel and fashion items");
        Category books = new Category(0, "Books", "Reading materials and publications");

        controller.executeCommand(CommandType.ADD_CATEGORY, electronics);
        int electronicsId = electronics.getId();
        controller.executeCommand(CommandType.ADD_CATEGORY, clothing);
        int clothingId = clothing.getId();
        controller.executeCommand(CommandType.ADD_CATEGORY, books);
        int booksId = books.getId();

        // === PHASE 2: Initial Inventory Setup ===
        // Add initial products
        Category electronicsCat = new Category(electronicsId, "Electronics", "");
        Category clothingCat = new Category(clothingId, "Clothing", "");
        Category booksCat = new Category(booksId, "Books", "");

        Product laptop = ProductFactory.createProduct("Gaming Laptop", 800.0, 1200.0, 5, electronicsCat);
        laptop.setId("2001000000001");
        Product mouse = ProductFactory.createProduct("Wireless Mouse", 25.0, 45.0, 20, electronicsCat);
        mouse.setId("2002000000002");
        Product tshirt = ProductFactory.createProduct("Cotton T-Shirt", 15.0, 25.0, 50, clothingCat);
        tshirt.setId("2003000000003");
        Product novel = ProductFactory.createProduct("Science Fiction Novel", 12.0, 18.0, 30, booksCat);
        novel.setId("2004000000004");

        controller.executeCommand(CommandType.ADD_PRODUCT, laptop);
        controller.executeCommand(CommandType.ADD_PRODUCT, mouse);
        controller.executeCommand(CommandType.ADD_PRODUCT, tshirt);
        controller.executeCommand(CommandType.ADD_PRODUCT, novel);

        // === PHASE 3: Stock Movements ===
        // Simulate sales and purchases
        recordMovement("Gaming Laptop", 2, Movement.MovementType.STOCK_EXIT); // Sold 2 laptops
        recordMovement("Wireless Mouse", 5, Movement.MovementType.STOCK_EXIT); // Sold 5 mice
        recordMovement("Cotton T-Shirt", 10, Movement.MovementType.STOCK_EXIT); // Sold 10 t-shirts
        recordMovement("Science Fiction Novel", 3, Movement.MovementType.STOCK_EXIT); // Sold 3 novels

        // Restock some items
        recordMovement("Gaming Laptop", 3, Movement.MovementType.STOCK_ENTRY); // Restocked 3 laptops
        recordMovement("Wireless Mouse", 15, Movement.MovementType.STOCK_ENTRY); // Restocked 15 mice

        // === PHASE 4: Verification ===
        // Check final inventory
        List<Product> products = controller.getAllProducts();
        assertThat(products).hasSize(4);

        // Verify stock levels after movements
        Product finalLaptop = products.stream()
            .filter(p -> p.getName().equals("Gaming Laptop"))
            .findFirst().orElseThrow();
        assertThat(finalLaptop.getStockQuantity()).isEqualTo(6); // 5 + 3 - 2

        Product finalMouse = products.stream()
            .filter(p -> p.getName().equals("Wireless Mouse"))
            .findFirst().orElseThrow();
        assertThat(finalMouse.getStockQuantity()).isEqualTo(30); // 20 + 15 - 5

        // Check movement history
        List<Movement> laptopMovements = controller.getMovementsForProduct(finalLaptop.getId());
        // Product creation registers 2 movements (PRODUCT_CREATED + initial stock), plus 2 manual movements = 4 total
        assertThat(laptopMovements).hasSize(4);

        // === PHASE 5: Category Management ===
        // Remove a category with products (should set products' category to null)
        controller.executeCommand(CommandType.REMOVE_CATEGORY, clothingId);

        // Verify products still exist but category is null
        products = controller.getAllProducts();
        Product finalTshirt = products.stream()
            .filter(p -> p.getName().equals("Cotton T-Shirt"))
            .findFirst().orElseThrow();
        assertThat(finalTshirt.getCategory()).isNull();

        // === PHASE 6: Product Removal ===
        // Remove a product
        controller.executeCommand(CommandType.REMOVE_PRODUCT, finalTshirt.getId());

        // Final verification
        products = controller.getAllProducts();
        assertThat(products).hasSize(3); // One product removed

        List<Category> categories = controller.getAllCategories();
        assertThat(categories).hasSize(2); // One category removed
    }

    @Test
    void shouldHandleComplexBusinessScenario() throws SQLException {
        // Scenario: Online store inventory management

        // Setup categories
        Category electronics = new Category(0, "Electronics", "Tech products");
        Category accessories = new Category(0, "Accessories", "Product accessories");
        Category consumables = new Category(0, "Consumables", "Consumable items");

        controller.executeCommand(CommandType.ADD_CATEGORY, electronics);
        int electronicsId = electronics.getId();
        controller.executeCommand(CommandType.ADD_CATEGORY, accessories);
        int accessoriesId = accessories.getId();
        controller.executeCommand(CommandType.ADD_CATEGORY, consumables);
        int consumablesId = consumables.getId();

        // Setup products with relationships
        Category electronicsCat = new Category(electronicsId, "Electronics", "");
        Category accessoriesCat = new Category(accessoriesId, "Accessories", "");

        // Main products
        Product phone = ProductFactory.createProduct("Smartphone", 300.0, 500.0, 10, electronicsCat);
        phone.setId("2005000000005");
        Product charger = ProductFactory.createProduct("Phone Charger", 15.0, 25.0, 50, accessoriesCat);
        charger.setId("2006000000006");
        Product headphones = ProductFactory.createProduct("Wireless Headphones", 50.0, 80.0, 25, electronicsCat);
        headphones.setId("2007000000007");

        // Add products
        controller.executeCommand(CommandType.ADD_PRODUCT, phone);
        String phoneId = phone.getId();
        controller.executeCommand(CommandType.ADD_PRODUCT, charger);
        String chargerId = charger.getId();
        controller.executeCommand(CommandType.ADD_PRODUCT, headphones);
        String headphonesId = headphones.getId();

        // Simulate business day: multiple sales and restocks
        // Morning sales
        recordMovement("Smartphone", 3, Movement.MovementType.STOCK_EXIT);
        recordMovement("Phone Charger", 5, Movement.MovementType.STOCK_EXIT);
        recordMovement("Wireless Headphones", 2, Movement.MovementType.STOCK_EXIT);

        // Afternoon restock
        recordMovement("Smartphone", 5, Movement.MovementType.STOCK_ENTRY);
        recordMovement("Phone Charger", 10, Movement.MovementType.STOCK_ENTRY);

        // Evening sales
        recordMovement("Smartphone", 2, Movement.MovementType.STOCK_EXIT);
        recordMovement("Wireless Headphones", 1, Movement.MovementType.STOCK_EXIT);

        // Verify final state
        List<Product> products = controller.getAllProducts();

        Product finalPhone = findProductByName(products, "Smartphone");
        assertThat(finalPhone.getStockQuantity()).isEqualTo(10); // 10 + 5 - 3 - 2

        Product finalCharger = findProductByName(products, "Phone Charger");
        assertThat(finalCharger.getStockQuantity()).isEqualTo(55); // 50 + 10 - 5

        Product finalHeadphones = findProductByName(products, "Wireless Headphones");
        assertThat(finalHeadphones.getStockQuantity()).isEqualTo(22); // 25 - 2 - 1

        // Verify movement counts (includes automatic movements: PRODUCT_CREATED + initial stock)
        assertThat(controller.getMovementsForProduct(phoneId)).hasSize(5); // 3 manual + 2 automatic
        assertThat(controller.getMovementsForProduct(chargerId)).hasSize(4); // 2 manual + 2 automatic
        assertThat(controller.getMovementsForProduct(headphonesId)).hasSize(4); // 2 manual + 2 automatic
    }

    @Test
    void shouldHandleDataIntegrityAcrossOperations() throws SQLException {
        // Test referential integrity and data consistency

        // Create category and product
        Category category = new Category(0, "Test Category", "For integrity testing");
        controller.executeCommand(CommandType.ADD_CATEGORY, category);
        int categoryId = category.getId();

        Category categoryRef = new Category(categoryId, "Test Category", "");
        Product product = ProductFactory.createProduct("Integrity Product", 10.0, 15.0, 5, categoryRef);
        product.setId("2008000000008");
        controller.executeCommand(CommandType.ADD_PRODUCT, product);
        String productId = product.getId();

        // Add movements
        recordMovement("Integrity Product", 3, Movement.MovementType.STOCK_ENTRY);

        // Verify all data is consistent
        List<Product> products = controller.getAllProducts();
        List<Category> categories = controller.getAllCategories();
        List<Movement> movements = controller.getMovementsForProduct(productId);

        assertThat(products).hasSize(1);
        assertThat(categories).hasSize(1);
        // Expect 3 movements: PRODUCT_CREATED + initial stock entry + manual stock entry
        assertThat(movements).hasSize(3);

        // Verify relationships
        Product retrievedProduct = products.get(0);
        assertThat(retrievedProduct.getCategory().getId()).isEqualTo(categoryId);
        assertThat(movements.get(0).getProduct().getId()).isEqualTo(productId);

        // Remove product and verify movements are cleaned up
        controller.executeCommand(CommandType.REMOVE_PRODUCT, productId);
        movements = controller.getMovementsForProduct(productId);
        assertThat(movements).isEmpty();

        // Product should be gone, category should remain
        products = controller.getAllProducts();
        categories = controller.getAllCategories();
        assertThat(products).isEmpty();
        assertThat(categories).hasSize(1);
    }

    private void recordMovement(String productName, int quantity, Movement.MovementType type) throws SQLException {
        List<Product> products = controller.getAllProducts();
        Product product = products.stream()
            .filter(p -> p.getName().equals(productName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productName));

        Product movementProduct = new Product();
        movementProduct.setId(product.getId());

        Movement movement = new Movement();
        movement.setProduct(movementProduct);
        movement.setQuantity(quantity);
        movement.setType(type);
        movement.setDescription("Test movement");
        movement.setDateTime(null);
        controller.executeCommand(CommandType.ADD_STOCK, movement);
    }

    private Product findProductByName(List<Product> products, String name) {
        return products.stream()
            .filter(p -> p.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + name));
    }
}