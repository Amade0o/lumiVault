Feature: Inventory Reports
  As a store manager
  I want to see inventory reports
  So that I can make informed business decisions

  Background:
    Given the inventory system is initialized

  Scenario: Empty inventory report
    When I request an inventory report
    Then the report should show no products
    And the report should show no categories
    And total inventory value should be 0

  Scenario: Inventory with single product
    Given a product "Single Product" exists with cost price 10.00, sale price 15.00, and stock 5
    When I request an inventory report
    Then the report should show 1 product
    And total inventory value should be 50.00 (cost) and 75.00 (sale)
    And the report should include product details

  Scenario: Inventory with multiple products
    Given the following products exist:
      | name    | cost_price | sale_price | stock |
      | Product A | 10.00      | 15.00      | 5     |
      | Product B | 20.00      | 30.00      | 3     |
      | Product C | 5.00       | 8.00       | 10    |
    When I request an inventory report
    Then the report should show 3 products
    And total cost value should be 100.00
    And total sale value should be 145.00

  Scenario: Inventory with categories
    Given the following categories exist:
      | name        | description       |
      | Electronics | Electronic items  |
      | Books       | Reading materials |
    And products exist in each category
    When I request an inventory report
    Then the report should show products grouped by category
    And each category should show its products

  Scenario: Low stock report
    Given products with various stock levels:
      | name       | stock |
      | Low Stock  | 2     |
      | Normal     | 10    |
      | Out of Stock | 0     |
    When I request a low stock report with threshold 5
    Then the report should show "Low Stock" and "Out of Stock" products
    And should not show "Normal" product

  Scenario: Product search
    Given multiple products exist with different names
    When I search for products containing "lap"
    Then I should see products with "laptop" in the name
    And should not see products without "lap" in the name

  Scenario: Category summary report
    Given categories with multiple products each
    When I request a category summary
    Then I should see each category with product count
    And total value per category
    And average stock per category