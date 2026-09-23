@cart
Feature: Shopping cart
  As a logged-in Swag Labs customer
  I want to manage products in my cart
  So that I can buy what I have chosen

  Background:
    Given the user is on the login screen
    And the user logs in as a "standard" user
    And the products screen is displayed

  @smoke @regression
  Scenario: Add a product to the cart
    When the user adds "Sauce Labs Backpack" to the cart
    Then the cart badge shows 1
    When the user opens the cart
    Then the cart contains "Sauce Labs Backpack" priced "$29.99"

  @regression
  Scenario: Remove a product from the cart
    Given the user adds "Sauce Labs Backpack" to the cart
    And the user opens the cart
    When the user removes the item from the cart
    Then the cart is empty
