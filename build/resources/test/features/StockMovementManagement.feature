Feature: Stock Movement Management
  As a store manager
  I want to track stock movements
  So that I can monitor inventory changes

  Background:
    Given the inventory system is initialized
    And a product "Test Product" exists with initial stock 10

  Scenario: Record stock entry
    When I record a stock entry of 5 units for "Test Product"
    Then the movement should be recorded successfully
    And the stock movement history should show the entry

  Scenario: Record stock exit
    When I record a stock exit of 3 units for "Test Product"
    Then the movement should be recorded successfully
    And the stock movement history should show the exit

  Scenario: View movement history for product
    Given the following movements exist for "Test Product":
      | type  | quantity |
      | ENTRY | 10       |
      | EXIT  | 3        |
      | ENTRY | 5        |
    When I request the movement history for "Test Product"
    Then I should see 3 movements
    And the movements should be ordered by date

  Scenario: Multiple products with movements
    Given a product "Product A" exists with stock 5
    And a product "Product B" exists with stock 8
    When I record movements for both products
    Then each product should have its own movement history
    And movement histories should not be mixed

  Scenario: Movement with large quantity
    When I record a stock entry of 10000 units for "Test Product"
    Then the movement should be recorded successfully
    And the quantity should be stored correctly

  Scenario: Movement with minimum quantity
    When I record a stock entry of 1 unit for "Test Product"
    Then the movement should be recorded successfully

  Scenario: Try to record movement for non-existent product
    When I try to record a movement for product ID 999
    Then the movement recording should fail

  Scenario: Movement with zero quantity should fail
    When I try to record a movement with quantity 0
    Then the movement recording should fail with validation error

  Scenario: Movement with negative quantity should fail
    When I try to record a movement with quantity -5
    Then the movement recording should fail with validation error