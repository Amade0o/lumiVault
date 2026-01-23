package view;

import javafx.application.Application;

/**
 * Main entry point for the BambooVault application.
 * Launches either the JavaFX GUI or console application based on arguments.
 */
public class Main {
    
    public static void main(String[] args) {
        // Check if console mode is requested
        if (args.length > 0 && "console".equals(args[0])) {
            ConsoleApp.main(args);
        } else {
            // Launch JavaFX application
            Application.launch(MainApp.class, args);
        }
    }
}
