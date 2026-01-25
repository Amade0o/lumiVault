package controller;

/**
 * Enum representing all available command types in the system.
 * Commands are user actions that modify the system state.
 */
public enum CommandType {
    // Product operations
    ADD_PRODUCT,
    REMOVE_PRODUCT,
    UPDATE_PRICE,
    
    // Category operations
    ADD_CATEGORY,
    REMOVE_CATEGORY,
    
    // Stock operations
    ADD_STOCK,
    REMOVE_STOCK
}
