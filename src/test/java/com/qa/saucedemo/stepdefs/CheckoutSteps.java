package com.qa.saucedemo.stepdefs;

import com.qa.saucedemo.pages.CheckoutPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class CheckoutSteps {

    private CheckoutPage checkoutPage() {
        return new CheckoutPage(DriverManager.getDriver());
    }

    @When("I check out with first name {string}, last name {string}, and postal code {string}")
    public void iCheckOutWithFirstNameLastNameAndPostalCode(
            String firstName, String lastName, String postalCode) {
        CheckoutPage checkoutPage = checkoutPage();
        checkoutPage.clickCheckout();
        checkoutPage.fillCheckoutInfo(firstName, lastName, postalCode);
        checkoutPage.clickContinue();
        checkoutPage.clickFinish();
    }

    @When("I attempt to check out with first name {string}, last name {string}, and postal code {string}")
    public void iAttemptToCheckOutWithFirstNameLastNameAndPostalCode(
            String firstName, String lastName, String postalCode) {
        // Stops after "Continue" - does NOT click "Finish". Used for
        // scenarios where a missing required field is expected to block
        // progress before the overview/finish step is ever reachable.
        checkoutPage().proceedThroughCheckoutInfo(firstName, lastName, postalCode);
    }

    @Then("I should see the checkout completion message {string}")
    public void iShouldSeeTheCheckoutCompletionMessage(String expectedMessage) {
        Assert.assertEquals(
                "Checkout completion header did not match expected message",
                expectedMessage, checkoutPage().getCompleteHeaderText());
    }

    @Then("I should see a validation error containing {string}")
    public void iShouldSeeAValidationErrorContaining(String expectedText) {
        String error = checkoutPage().getErrorMessage();
        Assert.assertTrue(
                "Expected validation error to contain: " + expectedText,
                error.contains(expectedText));
    }

    @Then("the checkout should complete successfully")
    public void theCheckoutShouldCompleteSuccessfully() {
        Assert.assertTrue(
                "Checkout was expected to complete successfully, but validation blocked it "
                        + "(likely due to a missing required field) - the order was never placed",
                checkoutPage().isOnCompletionPage());
    }

    @Then("I should see the back home button")
    public void iShouldSeeTheBackHomeButton() {
        Assert.assertTrue(
                "Expected the 'Back Home' button to be visible on the order confirmation page",
                checkoutPage().isBackHomeButtonVisible());
    }
}
