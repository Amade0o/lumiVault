Feature: Product Management
  As a store manager
  I want to manage products in my inventory
  So that I can keep track of what I sell

  Background:
    Given the inventory system is initialized

  Scenario: Add a new product
    When I add a product with name "Laptop", cost price 500.00, sale price 700.00, and stock 10
    Then the product should be saved successfully
    And I should be able to retrieve the product from inventory

  Scenario: Add product with category
    Given a category "Electronics" exists
    When I add a product "Mouse" with cost price 10.00, sale price 15.00, stock 5, and category "Electronics"
    Then the product should be saved with the category
    And the product should appear in category listings

  Scenario: List all products
    Given the following products exist:
      | name     | cost_price | sale_price | stock |
      | Laptop   | 500.00     | 700.00     | 10    |
      | Mouse    | 10.00      | 15.00      | 5     |
      | Keyboard | 20.00      | 30.00      | 8     |
    When I request all products
    Then I should see 3 products
    And the products should include "Laptop", "Mouse", and "Keyboard"

  Scenario: Remove existing product
    Given a product "Old Product" exists with stock 5
    When I remove the product "Old Product"
    Then the product should be removed from inventory
    And the product should not appear in listings

  Scenario: Try to remove non-existent product
    When I try to remove a product with ID 999
    Then the removal should fail
    And I should get an error message

  Scenario: Product with zero stock
    When I add a product "Out of Stock" with cost price 10.00, sale price 15.00, and stock 0
    Then the product should be saved successfully
    And the stock should be 0

  Scenario: Product with zero prices
    When I add a product "Free Item" with cost price 0.00, sale price 0.00, and stock 1
    Then the product should be saved successfully
    And both prices should be 0.00