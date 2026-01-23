Feature: Category Management
  As a store manager
  I want to organize products into categories
  So that I can better manage my inventory

  Background:
    Given the inventory system is initialized

  Scenario: Add a new category
    When I add a category with name "Electronics" and description "Electronic devices"
    Then the category should be saved successfully
    And I should be able to retrieve the category

  Scenario: Add category without description
    When I add a category with name "Books" and no description
    Then the category should be saved successfully
    And the category description should be null

  Scenario: List all categories
    Given the following categories exist:
      | name        | description          |
      | Electronics | Electronic devices   |
      | Books       | Reading materials    |
      | Clothing    | Apparel and accessories |
    When I request all categories
    Then I should see 3 categories
    And the categories should include "Electronics", "Books", and "Clothing"

  Scenario: Remove empty category
    Given a category "Empty Category" exists with no products
    When I remove the category "Empty Category"
    Then the category should be removed successfully

  Scenario: Remove category with products
    Given a category "Electronics" exists
    And the category has products
    When I remove the category "Electronics"
    Then the category should be removed
    And the products should remain but without category

  Scenario: Try to remove non-existent category
    When I try to remove a category with ID 999
    Then the removal should fail

  Scenario: Category with long description
    When I add a category with name "Long Description" and a very long description
    Then the category should be saved successfully
    And the full description should be preserved