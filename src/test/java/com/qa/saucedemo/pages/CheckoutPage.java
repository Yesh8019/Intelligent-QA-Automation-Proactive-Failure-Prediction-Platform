package com.qa.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Represents the checkout flow: cart page -> checkout info form ->
 * overview page -> completion page.
 *
 * NOTE: explicit waits are used after every navigation-triggering click
 * (checkout, continue, finish). The 5s implicit wait configured in Hooks
 * only helps when an element is slow to appear on an ALREADY-loaded page -
 * it does not account for the page navigation itself still being in
 * progress.
 */
public class CheckoutPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By checkoutButton = By.id("checkout");
    private final By firstNameField = By.id("first-name");
    private final By lastNameField = By.id("last-name");
    private final By postalCodeField = By.id("postal-code");
    private final By continueButton = By.id("continue");
    private final By finishButton = By.id("finish");
    private final By completeHeader = By.className("complete-header");
    private final By backHomeButton = By.id("back-to-products");
    private final By errorMessage = By.cssSelector("h3[data-test='error']");

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void clickCheckout() {
        wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)).click();
    }

    public void fillCheckoutInfo(String firstName, String lastName, String postalCode) {
        // Wait for the checkout info page to actually finish loading after
        // the "Checkout" click navigated us here.
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameField))
                .sendKeys(firstName);
        driver.findElement(lastNameField).sendKeys(lastName);
        driver.findElement(postalCodeField).sendKeys(postalCode);
    }

    public void clickContinue() {
        driver.findElement(continueButton).click();
    }

    public void clickFinish() {
        // Wait for the overview page to load after "Continue".
        // If a required field (e.g. postal code) was missing, the app shows
        // a validation error and stays on the SAME page - "finish" never
        // appears, so this wait times out with a clear, descriptive
        // TimeoutException instead of an ambiguous NoSuchElementException.
        wait.until(ExpectedConditions.elementToBeClickable(finishButton)).click();
    }

    /**
     * Runs the checkout flow through "Continue" only - stops BEFORE clicking
     * "Finish". Used by scenarios that expect validation to block progress
     * (e.g. a missing required field), where clicking "Finish" would never
     * be reachable and shouldn't be attempted.
     */
    public void proceedThroughCheckoutInfo(String firstName, String lastName, String postalCode) {
        clickCheckout();
        fillCheckoutInfo(firstName, lastName, postalCode);
        clickContinue();
    }

    /**
     * Fast, non-throwing check for whether the order-complete page is
     * currently showing. Deliberately does NOT wait/retry - if validation
     * blocked checkout, the complete-header element simply won't exist yet,
     * and we want that to read as "false" immediately rather than as a
     * 15-second timeout exception.
     */
    public boolean isOnCompletionPage() {
        return !driver.findElements(completeHeader).isEmpty();
    }

    public String getCompleteHeaderText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(completeHeader)).getText();
    }

    public boolean isBackHomeButtonVisible() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(backHomeButton)).isDisplayed();
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }
}
