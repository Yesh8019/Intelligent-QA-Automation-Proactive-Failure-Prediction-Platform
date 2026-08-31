@checkout
Feature: Checkout
  As a logged-in saucedemo.com user
  I want to complete checkout
  So that I can purchase my items

  Background:
    Given I am on the saucedemo login page
    And I log in with username "standard_user" and password "secret_sauce"
    And I add "Sauce Labs Backpack" to the cart
    And I go to the cart

  @smoke @positive @criticalPath
  Scenario: Completing checkout with valid info shows a confirmation
    When I check out with first name "John", last name "Doe", and postal code "12345"
    Then I should see the checkout completion message "Thank you for your order!"
    And I should see the back home button
    And the cart badge should show 0 items

  # INTENTIONAL FAILURE (High): the first assertion below is a genuine, correct check -
  # the app DOES show this validation error, and that assertion will pass. The second
  # assertion deliberately expects checkout to have completed anyway, which is wrong -
  # simulating a broken/regressed checkout validation flow. Checkout is a
  # revenue-critical path, so this should be classified as HIGH severity.
  @regression @criticalPath @knownFailure
  Scenario: Checkout with a missing postal code
    When I attempt to check out with first name "John", last name "Doe", and postal code ""
    Then I should see a validation error containing "Postal Code is required"
    And the checkout should complete successfully
