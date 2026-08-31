package com.qa.saucedemo.stepdefs;

import com.qa.saucedemo.pages.InventoryPage;
import com.qa.saucedemo.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class LoginSteps {

    private LoginPage loginPage() {
        return new LoginPage(DriverManager.getDriver());
    }

    private InventoryPage inventoryPage() {
        return new InventoryPage(DriverManager.getDriver());
    }

    @Given("I am on the saucedemo login page")
    public void iAmOnTheSaucedemoLoginPage() {
        // Hooks.setUp() already navigates to BASE_URL before every scenario,
        // so there is nothing extra to do here - this step exists purely to
        // make the feature file read naturally.
    }

    @When("I log in with username {string} and password {string}")
    public void iLogInWithUsernameAndPassword(String username, String password) {
        LoginPage loginPage = loginPage();
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLogin();
    }

    @When("I fill in username {string} and password {string} and click the broken login button")
    public void iFillInUsernameAndPasswordAndClickTheBrokenLoginButton(String username, String password) {
        LoginPage loginPage = loginPage();
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        // This will throw NoSuchElementException because the locator is
        // deliberately wrong (see LoginPage.brokenLoginButtonLocator).
        loginPage.clickLoginUsingLocator(loginPage.brokenLoginButtonLocator);
    }

    @Then("I should be redirected away from the login page")
    public void iShouldBeRedirectedAwayFromTheLoginPage() {
        Assert.assertFalse(
                "User should be redirected away from the login page after a valid login",
                loginPage().isOnLoginPage());
    }

    @Then("I should see the products page header {string}")
    public void iShouldSeeTheProductsPageHeader(String expectedHeader) {
        Assert.assertEquals(
                "Products page header did not match expected value",
                expectedHeader, inventoryPage().getPageTitle());
    }

    @Then("I should see an error message containing {string}")
    public void iShouldSeeAnErrorMessageContaining(String expectedText) {
        String error = loginPage().getErrorMessage();
        Assert.assertTrue(
                "Expected error message to contain: " + expectedText,
                error.contains(expectedText));
    }

    @Then("I should remain on the login page")
    public void iShouldRemainOnTheLoginPage() {
        Assert.assertTrue(
                "User was expected to remain on the login page after a failed login attempt",
                loginPage().isOnLoginPage());
    }

    @Then("the login should succeed")
    public void theLoginShouldSucceed() {
        Assert.assertFalse(
                "Login was expected to succeed and redirect away from the login page",
                loginPage().isOnLoginPage());
    }
}
