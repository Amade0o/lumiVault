package view;

import controller.Controller;
import model.factory.ProductFactory;
import database.DatabaseConnection;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import model.Category;
import model.Product;
import model.Movement;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;

/**
 * JavaFX application for BambooVault inventory system.
 * Modern Material Design UI with professional aesthetics.
 */
public class MainApp extends Application {

    private Controller controller;
    private TextArea outputArea;
    private BorderPane mainLayout;
    private VBox dashboardView;
    private PieChart inventoryChart;
    private BarChart<String, Number> categoryChart;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database connection
            DatabaseConnection.getInstance().getConnection();
            controller = new Controller();

            // Create main layout
            mainLayout = new BorderPane();
            mainLayout.getStyleClass().add("root");
            
            // Create header
            VBox header = createHeader();
            mainLayout.setTop(header);
            
            // Create dashboard view
            dashboardView = createDashboard();
            ScrollPane scrollPane = new ScrollPane(dashboardView);
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("scroll-pane");
            mainLayout.setCenter(scrollPane);

            // Create scene with CSS
            Scene scene = new Scene(mainLayout, 1100, 750);
            
            // Load CSS
            String css = getClass().getResource("/styles/app.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            // Configure stage
            primaryStage.setTitle("BambooVault - Sistema de Inventario");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(650);
            
            // Set application icon
            try {
                InputStream iconStream = getClass().getResourceAsStream("/images/bamboo-logo.png");
                if (iconStream != null) {
                    Image icon = new Image(iconStream);
                    primaryStage.getIcons().add(icon);
                }
            } catch (Exception e) {
                System.out.println("Could not load icon: " + e.getMessage());
            }
            
            primaryStage.show();
            
            // Apply entrance animation
            applyEntranceAnimation(dashboardView);

        } catch (SQLException e) {
            showError("Error de base de datos", "No se pudo conectar a la base de datos: " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        // Close database connection when app closes
        DatabaseConnection.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ==================== PRIVATE METHODS ====================
    
    /**
     * Creates the header with logo and title
     */
    private VBox createHeader() {
        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 4, 0, 0, 2);");
        header.setPadding(new Insets(30, 24, 30, 24));
        
        // Logo and title container
        HBox logoContainer = new HBox(20);
        logoContainer.setAlignment(Pos.CENTER);
        
        // Load bamboo logo
        try {
            InputStream logoStream = getClass().getResourceAsStream("/images/bamboo-logo.png");
            if (logoStream != null) {
                Image logo = new Image(logoStream);
                ImageView logoView = new ImageView(logo);
                logoView.setFitWidth(64);
                logoView.setFitHeight(64);
                logoView.setPreserveRatio(true);
                logoContainer.getChildren().add(logoView);
            } else {
                System.out.println("Logo resource not found: /images/bamboo-logo.png");
            }
        } catch (Exception e) {
            System.out.println("Could not load logo: " + e.getMessage());
        }
        
        // Title
        Label titleLabel = new Label("BambooVault");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 600; -fx-text-fill: #212121;");
        logoContainer.getChildren().add(titleLabel);
        
        // Subtitle
        Label subtitleLabel = new Label("Sistema de Gestión de Inventario");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575; -fx-alignment: center;");
        subtitleLabel.setAlignment(Pos.CENTER);
        
        header.getChildren().addAll(logoContainer, subtitleLabel);
        return header;
    }
    
    /**
     * Creates the main dashboard with cards
     */
    private VBox createDashboard() {
        VBox dashboard = new VBox(24);
        dashboard.getStyleClass().add("container-main");
        dashboard.setPadding(new Insets(24));
        
        // Statistics section
        VBox statsSection = createStatisticsSection();
        
        // Primary action cards section
        Label primaryActionsTitle = new Label("Acciones Principales");
        primaryActionsTitle.getStyleClass().add("title-section");
        primaryActionsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #212121; " +
                                     "-fx-alignment: center; -fx-padding: 16px 0 8px 0;");
        primaryActionsTitle.setMaxWidth(Double.MAX_VALUE);
        primaryActionsTitle.setAlignment(Pos.CENTER);
        
        GridPane primaryCardGrid = createPrimaryActionCards();
        
        // Secondary action cards section
        Label secondaryActionsTitle = new Label("Otras Acciones");
        secondaryActionsTitle.getStyleClass().add("title-section");
        secondaryActionsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #212121; " +
                                       "-fx-alignment: center; -fx-padding: 16px 0 8px 0;");
        secondaryActionsTitle.setMaxWidth(Double.MAX_VALUE);
        secondaryActionsTitle.setAlignment(Pos.CENTER);
        
        GridPane secondaryCardGrid = createSecondaryActionCards();
        
        // Recent activity section
        VBox activitySection = createActivitySection();
        
        dashboard.getChildren().addAll(statsSection, primaryActionsTitle, primaryCardGrid, 
                                       secondaryActionsTitle, secondaryCardGrid, activitySection);
        return dashboard;
    }
    
    /**
     * Creates statistics section with charts
     */
    private VBox createStatisticsSection() {
        VBox section = new VBox(16);
        
        Label sectionTitle = new Label("Resumen del Inventario");
        sectionTitle.getStyleClass().add("title-section");
        
        HBox chartsContainer = new HBox(16);
        chartsContainer.setAlignment(Pos.CENTER);
        
        // Inventory distribution pie chart
        inventoryChart = createInventoryPieChart();
        VBox pieChartCard = wrapInCard(inventoryChart, "Distribución por Categoría");
        
        // Category bar chart
        categoryChart = createCategoryBarChart();
        VBox barChartCard = wrapInCard(categoryChart, "Stock por Categoría");
        
        chartsContainer.getChildren().addAll(pieChartCard, barChartCard);
        HBox.setHgrow(pieChartCard, Priority.ALWAYS);
        HBox.setHgrow(barChartCard, Priority.ALWAYS);
        
        section.getChildren().addAll(sectionTitle, chartsContainer);
        
        // Update charts with actual data after creation
        updateAllCharts();
        
        return section;
    }
    
    /**
     * Creates a pie chart showing inventory distribution
     */
    private PieChart createInventoryPieChart() {
        PieChart chart = new PieChart();
        chart.setTitle("");
        chart.setLegendVisible(true);
        chart.setPrefSize(400, 300);
        chart.getStyleClass().add("chart");
        
        // Add initial empty data
        PieChart.Data emptyData = new PieChart.Data("Sin datos", 1);
        chart.getData().add(emptyData);
        
        return chart;
    }
    
    /**
     * Creates a bar chart showing stock by category
     */
    private BarChart<String, Number> createCategoryBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Categoría");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Cantidad en Stock");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("");
        chart.setLegendVisible(false);
        chart.setPrefSize(400, 300);
        chart.getStyleClass().add("chart");
        
        // Add initial empty data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Stock");
        series.getData().add(new XYChart.Data<>("Sin datos", 0));
        chart.getData().add(series);
        
        return chart;
    }
    
    /**
     * Updates the inventory pie chart with current data
     */
    private void updateInventoryChart() {
        try {
            List<Category> categories = controller.getAllCategories();
            List<Product> products = controller.getAllProducts();
            
            Map<String, Integer> categoryCount = new HashMap<>();
            
            for (Product product : products) {
                String categoryName = product.getCategory() != null ? 
                    product.getCategory().getName() : "Sin categoría";
                categoryCount.put(categoryName, 
                    categoryCount.getOrDefault(categoryName, 0) + 1);
            }
            
            inventoryChart.getData().clear();
            for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
                PieChart.Data data = new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")", 
                    entry.getValue()
                );
                inventoryChart.getData().add(data);
            }
            
            if (inventoryChart.getData().isEmpty()) {
                PieChart.Data emptyData = new PieChart.Data("Sin datos", 1);
                inventoryChart.getData().add(emptyData);
            }
            
        } catch (SQLException e) {
            System.out.println("Error updating chart: " + e.getMessage());
        }
    }
    
    /**
     * Updates the category bar chart with current data
     */
    private void updateCategoryChart() {
        try {
            List<Product> products = controller.getAllProducts();
            
            Map<String, Integer> categoryStock = new HashMap<>();
            
            for (Product product : products) {
                String categoryName = product.getCategory() != null ? 
                    product.getCategory().getName() : "Sin categoría";
                categoryStock.put(categoryName, 
                    categoryStock.getOrDefault(categoryName, 0) + product.getStockQuantity());
            }
            
            categoryChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Stock");
            
            for (Map.Entry<String, Integer> entry : categoryStock.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            
            categoryChart.getData().add(series);
            
        } catch (SQLException e) {
            System.out.println("Error updating chart: " + e.getMessage());
        }
    }
    
    /**
     * Wraps a node in a card container
     */
    private VBox wrapInCard(javafx.scene.Node node, String title) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));
        
        if (title != null && !title.isEmpty()) {
            Label cardTitle = new Label(title);
            cardTitle.getStyleClass().add("title-section");
            cardTitle.setStyle("-fx-font-size: 16px;");
            card.getChildren().add(cardTitle);
        }
        
        card.getChildren().add(node);
        VBox.setVgrow(node, Priority.ALWAYS);
        
        return card;
    }
    
    /**
     * Creates the grid of primary action cards
     */
    private GridPane createPrimaryActionCards() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(8));
        grid.setAlignment(Pos.CENTER);
        
        // Configure columns with fixed width to avoid overlap
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(320);
            col.setPrefWidth(340);
            col.setMaxWidth(400);
            col.setHgrow(Priority.SOMETIMES);
            grid.getColumnConstraints().add(col);
        }
        
        // Configure row with fixed height
        RowConstraints row = new RowConstraints();
        row.setMinHeight(200);
        row.setPrefHeight(200);
        row.setMaxHeight(200);
        row.setVgrow(Priority.NEVER);
        grid.getRowConstraints().add(row);
        
        // Create primary action cards
        VBox searchCard = createActionCard("🔍", "Buscar Productos", 
            "Buscar y filtrar productos existentes",
            () -> showSearch());
        
        VBox productCard = createActionCard("📦", "Gestión de Productos", 
            "Agregar y editar productos del inventario",
            () -> showProductManagement());
        
        VBox categoryCard = createActionCard("📁", "Gestión de Categorías", 
            "Crear y administrar categorías de productos",
            () -> showCategoryManagement());
        
        // Add cards to grid
        grid.add(searchCard, 0, 0);
        grid.add(productCard, 1, 0);
        grid.add(categoryCard, 2, 0);
        
        return grid;
    }
    
    /**
     * Creates the grid of secondary action cards
     */
    private GridPane createSecondaryActionCards() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(8));
        grid.setAlignment(Pos.CENTER);
        
        // Configure columns with fixed width to avoid overlap
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(320);
            col.setPrefWidth(340);
            col.setMaxWidth(400);
            col.setHgrow(Priority.SOMETIMES);
            grid.getColumnConstraints().add(col);
        }
        
        // Configure row with fixed height
        RowConstraints row = new RowConstraints();
        row.setMinHeight(200);
        row.setPrefHeight(200);
        row.setMaxHeight(200);
        row.setVgrow(Priority.NEVER);
        grid.getRowConstraints().add(row);
        
        // Create secondary action cards
        VBox stockCard = createActionCard("📊", "Movimientos de Stock", 
            "Registrar entradas y salidas de inventario",
            () -> showStockManagement());
        
        VBox reportsCard = createActionCard("📈", "Reportes", 
            "Ver reportes y estadísticas del inventario",
            () -> showReports());
        
        VBox settingsCard = createActionCard("⚙️", "Configuración", 
            "Ajustes y preferencias del sistema",
            () -> showSettings());
        
        // Add cards to grid
        grid.add(stockCard, 0, 0);
        grid.add(reportsCard, 1, 0);
        grid.add(settingsCard, 2, 0);
        
        return grid;
    }
    
    /**
     * Creates an action card with icon, title, description and action
     */
    private VBox createActionCard(String icon, String title, String description, Runnable action) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.12), 10, 0, 0, 2);");
        card.setMinHeight(200);
        card.setPrefHeight(200);
        card.setMaxHeight(200);
        card.setMaxWidth(Double.MAX_VALUE);
        
        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 40px;");
        
        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: 600; -fx-text-fill: #212121;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Description
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);
        descLabel.setPrefHeight(40);
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Action button inside the card
        Button actionBtn = new Button("Abrir");
        actionBtn.setStyle("-fx-background-color: #2D5016; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-font-weight: 500; " +
                          "-fx-padding: 10px 20px; -fx-background-radius: 4px; " +
                          "-fx-cursor: hand;");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setMinWidth(100);
        actionBtn.setOnAction(e -> action.run());
        
        // Hover effect for button
        actionBtn.setOnMouseEntered(e -> 
            actionBtn.setStyle("-fx-background-color: #3A6B1D; -fx-text-fill: white; " +
                              "-fx-font-size: 14px; -fx-font-weight: 500; " +
                              "-fx-padding: 10px 20px; -fx-background-radius: 4px; " +
                              "-fx-cursor: hand;"));
        actionBtn.setOnMouseExited(e -> 
            actionBtn.setStyle("-fx-background-color: #2D5016; -fx-text-fill: white; " +
                              "-fx-font-size: 14px; -fx-font-weight: 500; " +
                              "-fx-padding: 10px 20px; -fx-background-radius: 4px; " +
                              "-fx-cursor: hand;"));
        
        card.getChildren().addAll(iconLabel, titleLabel, descLabel, spacer, actionBtn);
        
        // Card hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 14, 0, 0, 5); " +
                         "-fx-cursor: hand;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.12), 10, 0, 0, 2);");
        });
        
        return card;
    }
    
    /**
     * Creates the recent activity section
     */
    private VBox createActivitySection() {
        VBox section = new VBox(12);
        
        Label sectionTitle = new Label("Actividad Reciente");
        sectionTitle.getStyleClass().add("title-section");
        
        // Output area for messages
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(8);
        outputArea.setWrapText(true);
        outputArea.getStyleClass().add("text-area");
        
        Button clearBtn = new Button("Limpiar Actividad");
        clearBtn.getStyleClass().add("button-secondary");
        clearBtn.setOnAction(e -> outputArea.clear());
        
        HBox buttonBox = new HBox(clearBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        VBox activityCard = new VBox(12);
        activityCard.getStyleClass().add("card");
        activityCard.getChildren().addAll(outputArea, buttonBox);
        
        section.getChildren().addAll(sectionTitle, activityCard);
        return section;
    }

    
    /**
     * Applies smooth entrance animation
     */
    private void applyEntranceAnimation(javafx.scene.Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(600), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
    
    /**
     * Applies hover animation to cards
     */
    private void applyHoverAnimation(javafx.scene.Node node, boolean isEntering) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
        if (isEntering) {
            scale.setToX(1.02);
            scale.setToY(1.02);
        } else {
            scale.setToX(1.0);
            scale.setToY(1.0);
        }
        scale.play();
    }
    
    /**
     * Applies click animation to buttons
     */
    private void applyClickAnimation(javafx.scene.Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), node);
        scale.setToX(0.95);
        scale.setToY(0.95);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }
    
    /**
     * Updates all charts with current data
     */
    private void updateAllCharts() {
        updateInventoryChart();
        updateCategoryChart();
    }
    
    // ========== Action Handlers ==========
    
    private void showCategoryManagement() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Gestión de Categorías");
        dialog.setHeaderText("Administrar Categorías del Inventario");
        
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        
        // Buttons for category actions
        Button addCatBtn = new Button("➕ Agregar Nueva Categoría");
        addCatBtn.setMaxWidth(Double.MAX_VALUE);
        addCatBtn.setOnAction(e -> {
            addCategory();
            dialog.close();
        });
        
        Button listCatBtn = new Button("📋 Listar Todas las Categorías");
        listCatBtn.setMaxWidth(Double.MAX_VALUE);
        listCatBtn.setOnAction(e -> {
            listCategories();
            dialog.close();
        });
        
        content.getChildren().addAll(addCatBtn, listCatBtn);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void showProductManagement() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Gestión de Productos");
        dialog.setHeaderText("Administrar Productos del Inventario");
        
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        
        Button addProdBtn = new Button("➕ Agregar Nuevo Producto");
        addProdBtn.setMaxWidth(Double.MAX_VALUE);
        addProdBtn.setOnAction(e -> {
            addProduct();
            dialog.close();
        });
        
        Button listProdBtn = new Button("📋 Listar Todos los Productos");
        listProdBtn.setMaxWidth(Double.MAX_VALUE);
        listProdBtn.setOnAction(e -> {
            listProducts();
            dialog.close();
        });
        
        content.getChildren().addAll(addProdBtn, listProdBtn);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void showStockManagement() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Movimientos de Stock");
        dialog.setHeaderText("Registrar Entradas y Salidas");
        
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        
        Button addStockBtn = new Button("➕ Agregar Stock (Entrada)");
        addStockBtn.setMaxWidth(Double.MAX_VALUE);
        addStockBtn.setOnAction(e -> {
            addStock();
            dialog.close();
        });
        
        Button removeStockBtn = new Button("➖ Quitar Stock (Salida)");
        removeStockBtn.setMaxWidth(Double.MAX_VALUE);
        removeStockBtn.setOnAction(e -> {
            removeStock();
            dialog.close();
        });
        
        content.getChildren().addAll(addStockBtn, removeStockBtn);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void showReports() {
        updateAllCharts();
        appendOutput("📊 Gráficos actualizados con los datos más recientes");
    }
    
    private void showSearch() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Producto por ID");
        dialog.setHeaderText("Búsqueda de Producto por ID");
        dialog.setContentText("ID del producto:");

        dialog.showAndWait().ifPresent(idStr -> {
            try {
                String id = idStr.trim();
                List<Product> allProducts = controller.getAllProducts();
                List<Product> results = allProducts.stream()
                    .filter(p -> p.getId().equals(id))
                    .toList();

                if (results.isEmpty()) {
                    appendOutput("🔍 No se encontró ningún producto con ID: " + id);
                } else {
                    appendOutput("🔍 Resultado para ID '" + id + "':");
                    for (Product p : results) {
                        String categoryName = p.getCategory() != null ?
                            p.getCategory().getName() : "Sin categoría";
                        appendOutput(String.format("  • %s (ID: %s) | Stock: %d | Precio: $%.2f | %s",
                            p.getName(), p.getId(), p.getStockQuantity(), p.getSalePrice(), categoryName));
                    }
                }
            } catch (SQLException e) {
                showError("Error", "Error en la búsqueda: " + e.getMessage());
            }
        });
    }
    
    private void showSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración");
        alert.setHeaderText("Configuración del Sistema");
        alert.setContentText("Funcionalidad de configuración próximamente disponible.\n\n" +
            "Base de datos: SQLite\n" +
            "Versión: 1.0.0\n" +
            "UI: Material Design");
        alert.showAndWait();
    }
    
    // ========== Category Operations ==========
    
    private void addCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Agregar Categoría");
        dialog.setHeaderText("Nueva Categoría");
        dialog.setContentText("Nombre de la categoría:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    Category category = new Category(0, name.trim(), "");
                    int id = controller.addCategory(category);
                    appendOutput("✓ Categoría creada: " + name + " (ID: " + id + ")");
                    updateAllCharts();
                } catch (SQLException e) {
                    showError("Error", "No se pudo crear la categoría: " + e.getMessage());
                }
            }
        });
    }

    private void addProduct() {
        try {
            // Get available categories first
            List<Category> categories = controller.getAllCategories();
            if (categories.isEmpty()) {
                showError("Error", "No hay categorías disponibles. Por favor crea una categoría primero.");
                return;
            }

            // Dialog for product ID
            TextInputDialog idDialog = new TextInputDialog();
            idDialog.setTitle("Agregar Producto");
            idDialog.setHeaderText("Nuevo Producto");
            idDialog.setContentText("ID del producto (código de barras):");

            idDialog.showAndWait().ifPresent(productId -> {
                if (!productId.trim().isEmpty()) {
                    // Dialog for product name
                    TextInputDialog nameDialog = new TextInputDialog();
                    nameDialog.setTitle("Agregar Producto");
                    nameDialog.setHeaderText("Nuevo Producto");
                    nameDialog.setContentText("Nombre del producto:");

                    nameDialog.showAndWait().ifPresent(name -> {
                        if (!name.trim().isEmpty()) {
                            // Dialog for cost price
                            TextInputDialog costDialog = new TextInputDialog("0.0");
                            costDialog.setTitle("Precio de Costo");
                            costDialog.setHeaderText("Precio de Costo");
                            costDialog.setContentText("Precio de costo:");

                            costDialog.showAndWait().ifPresent(costStr -> {
                                try {
                                    double costPrice = Double.parseDouble(costStr.trim());

                                    // Dialog for sale price
                                    TextInputDialog saleDialog = new TextInputDialog(String.valueOf(costPrice * 1.5));
                                    saleDialog.setTitle("Precio de Venta");
                                    saleDialog.setHeaderText("Precio de Venta");
                                    saleDialog.setContentText("Precio de venta:");

                                    saleDialog.showAndWait().ifPresent(saleStr -> {
                                        try {
                                            double salePrice = Double.parseDouble(saleStr.trim());

                                            // Dialog for initial stock
                                            TextInputDialog stockDialog = new TextInputDialog("0");
                                            stockDialog.setTitle("Stock Inicial");
                                            stockDialog.setHeaderText("Stock Inicial");
                                            stockDialog.setContentText("Cantidad inicial en stock:");

                                            stockDialog.showAndWait().ifPresent(stockStr -> {
                                                try {
                                                    int stock = Integer.parseInt(stockStr.trim());

                                                    // Category selection dialog
                                                    ChoiceDialog<Category> categoryDialog = new ChoiceDialog<>();
                                                    categoryDialog.setTitle("Seleccionar Categoría");
                                                    categoryDialog.setHeaderText("Categoría del Producto");
                                                    categoryDialog.setContentText("Selecciona la categoría:");
                                                    categoryDialog.getItems().addAll(categories);
                                                    categoryDialog.setSelectedItem(categories.get(0)); // Default to first category

                                                    categoryDialog.showAndWait().ifPresent(category -> {
                                                        try {
                                                            Product product = ProductFactory.createProduct(name.trim(), costPrice, salePrice, stock, category);
                                                            product.setId(productId.trim()); // Set the barcode ID
                                                            String id = controller.addProduct(product);
                                                            appendOutput("✓ Producto creado: " + name.trim() +
                                                                " (ID: " + id + ", Categoría: " + category.getName() + ")");
                                                            updateAllCharts();
                                                        } catch (SQLException e) {
                                                            showError("Error", "No se pudo crear el producto: " + e.getMessage());
                                                        }
                                                    });

                                                } catch (NumberFormatException e) {
                                                    showError("Error", "Stock debe ser un número entero válido.");
                                                }
                                            });

                                        } catch (NumberFormatException e) {
                                            showError("Error", "Precio de venta debe ser un número válido.");
                                        }
                                    });

                                } catch (NumberFormatException e) {
                                    showError("Error", "Precio de costo debe ser un número válido.");
                                }
                            });
                        }
                    });

                } else {
                    showError("Error", "ID no puede estar vacío.");
                }
            });

        } catch (SQLException e) {
            showError("Error", "Error al obtener categorías: " + e.getMessage());
        }
    }

    private void listProducts() {
        try {
            List<Product> products = controller.getAllProducts();
            if (products.isEmpty()) {
                appendOutput("No hay productos registrados.");
            } else {
                appendOutput("=== PRODUCTOS ===");
                for (Product p : products) {
                    String categoryName = p.getCategory() != null ? p.getCategory().getName() : "Sin categoría";
                    appendOutput(String.format("ID: %s | %s | Stock: %d | Precio: $%.2f | Categoría: %s",
                        p.getId(), p.getName(), p.getStockQuantity(), p.getSalePrice(), categoryName));
                }
            }
        } catch (SQLException e) {
            showError("Error", "No se pudieron listar los productos: " + e.getMessage());
        }
    }

    private void listCategories() {
        try {
            List<Category> categories = controller.getAllCategories();
            if (categories.isEmpty()) {
                appendOutput("No hay categorías registradas.");
            } else {
                appendOutput("=== CATEGORÍAS ===");
                for (Category c : categories) {
                    appendOutput(String.format("ID: %d | %s | %s",
                        c.getId(), c.getName(), c.getDescription()));
                }
            }
        } catch (SQLException e) {
            showError("Error", "No se pudieron listar las categorías: " + e.getMessage());
        }
    }

    private void addStock() {
        // Simple stock addition for existing products
        TextInputDialog productIdDialog = new TextInputDialog();
        productIdDialog.setTitle("Agregar Stock");
        productIdDialog.setHeaderText("Agregar Stock a Producto");
        productIdDialog.setContentText("ID del producto (código de barras):");

        productIdDialog.showAndWait().ifPresent(productId -> {
            if (!productId.trim().isEmpty()) {
                TextInputDialog quantityDialog = new TextInputDialog("1");
                quantityDialog.setTitle("Agregar Stock");
                quantityDialog.setHeaderText("Cantidad a agregar");
                quantityDialog.setContentText("Cantidad:");

                quantityDialog.showAndWait().ifPresent(quantityStr -> {
                    try {
                        int quantity = Integer.parseInt(quantityStr.trim());
                        Product product = new Product();
                        product.setId(productId.trim());

                        Movement movement = new Movement();
                        movement.setProduct(product);
                        movement.setQuantity(quantity);
                        movement.setType(Movement.MovementType.STOCK_ENTRY);
                        movement.setDescription("Entrada de stock desde interfaz");
                        movement.setDateTime(null); // Will use current time

                        int movementId = controller.recordStockMovement(movement);
                        appendOutput("✓ Stock agregado: " + quantity + " unidades al producto ID " + productId);
                        updateAllCharts();
                    } catch (NumberFormatException ex) {
                        showError("Error", "Cantidad inválida");
                    } catch (SQLException ex) {
                        showError("Error", "No se pudo agregar stock: " + ex.getMessage());
                    }
                });
            } else {
                showError("Error", "ID de producto no puede estar vacío");
            }
        });
    }

    private void removeStock() {
        // Simple stock removal for existing products
        TextInputDialog productIdDialog = new TextInputDialog();
        productIdDialog.setTitle("Quitar Stock");
        productIdDialog.setHeaderText("Quitar Stock de Producto");
        productIdDialog.setContentText("ID del producto (código de barras):");

        productIdDialog.showAndWait().ifPresent(productId -> {
            if (!productId.trim().isEmpty()) {
                TextInputDialog quantityDialog = new TextInputDialog("1");
                quantityDialog.setTitle("Quitar Stock");
                quantityDialog.setHeaderText("Cantidad a quitar");
                quantityDialog.setContentText("Cantidad:");

                quantityDialog.showAndWait().ifPresent(quantityStr -> {
                    try {
                        int quantity = Integer.parseInt(quantityStr.trim());
                        Product product = new Product();
                        product.setId(productId.trim());

                        Movement movement = new Movement();
                        movement.setProduct(product);
                        movement.setQuantity(quantity);
                        movement.setType(Movement.MovementType.STOCK_EXIT);
                        movement.setDescription("Salida de stock desde interfaz");
                        movement.setDateTime(null); // Will use current time

                        int movementId = controller.recordStockMovement(movement);
                        appendOutput("✓ Stock quitado: " + quantity + " unidades del producto ID " + productId);
                        updateAllCharts();
                    } catch (NumberFormatException ex) {
                        showError("Error", "Cantidad inválida");
                    } catch (SQLException ex) {
                        showError("Error", "No se pudo quitar stock: " + ex.getMessage());
                    }
                });
            } else {
                showError("Error", "ID de producto no puede estar vacío");
            }
        });
    }

    private void appendOutput(String text) {
        outputArea.appendText(text + "\n");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}