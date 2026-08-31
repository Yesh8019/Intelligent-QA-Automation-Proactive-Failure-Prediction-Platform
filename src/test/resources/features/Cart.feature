@cart
Feature: Cart
  As a logged-in saucedemo.com user
  I want to add and remove items from my cart
  So that I can prepare my order

  Background:
    Given I am on the saucedemo login page
    And I log in with username "standard_user" and password "secret_sauce"

  @smoke @positive
  Scenario: Adding one item updates the cart badge to 1
    When I add "Sauce Labs Backpack" to the cart
    And I go to the cart
    Then the cart badge should show 1 item
    And the cart should contain "Sauce Labs Backpack"

  @regression @positive
  Scenario: Adding multiple items updates the cart badge accordingly
    When I add "Sauce Labs Backpack" to the cart
    And I add "Sauce Labs Bike Light" to the cart
    And I go to the cart
    Then the cart badge should show 2 items
    And the cart should contain "Sauce Labs Backpack"
    And the cart should contain "Sauce Labs Bike Light"

  # INTENTIONAL FAILURE (Low): this scenario correctly verifies the remaining item
  # is still in the cart, then expects the wrong badge count after removing an item -
  # a minor/stale test-data mistake, not a real app bug. This gives the AI analysis
  # tool a LOW severity example, alongside a genuinely passing assertion for contrast.
  @regression @knownFailure
  Scenario: Cart badge count after removing an item
    When I add "Sauce Labs Backpack" to the cart
    And I add "Sauce Labs Bike Light" to the cart
    And I remove "Sauce Labs Backpack" from the cart
    And I go to the cart
    Then the cart should contain "Sauce Labs Bike Light"
    And the cart badge should show 5 items
