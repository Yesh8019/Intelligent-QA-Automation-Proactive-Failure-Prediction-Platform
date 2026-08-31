@login
Feature: Login
  As a saucedemo.com user
  I want to log in
  So that I can access the store

  Background:
    Given I am on the saucedemo login page

  @smoke @positive @criticalPath
  Scenario: Valid credentials log the user in successfully
    When I log in with username "standard_user" and password "secret_sauce"
    Then I should be redirected away from the login page
    And I should see the products page header "Products"
    And the cart badge should show 0 items

  @regression @negative
  Scenario: Invalid credentials show an error message
    When I log in with username "invalid_user" and password "wrong_password"
    Then I should see an error message containing "Username and password do not match"
    And I should remain on the login page

  @regression @negative
  Scenario: Locked out user is blocked with a clear message
    When I log in with username "locked_out_user" and password "secret_sauce"
    Then I should see an error message containing "locked out"
    And I should remain on the login page

  # INTENTIONAL FAILURE (Critical): the login button locator is deliberately wrong,
  # simulating a broken login flow after a UI change. Login is a critical user path,
  # so this failure should be classified as CRITICAL severity by the AI analysis tool.
  @regression @criticalPath @knownFailure
  Scenario: Login fails due to a broken button locator
    When I fill in username "standard_user" and password "secret_sauce" and click the broken login button
    Then the login should succeed
