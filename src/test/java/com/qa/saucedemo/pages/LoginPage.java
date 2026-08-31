package com.qa.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Represents the saucedemo.com login page.
 * Holds the element locators and actions available on this page.
 */
public class LoginPage {

    private final WebDriver driver;

    private final By usernameField = By.id("user-name");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorMessage = By.cssSelector("h3[data-test='error']");

    // Intentionally WRONG locator used only by one failing test (see LoginTests)
    // to simulate a real "element not found" style failure (e.g. after a UI change).
    public final By brokenLoginButtonLocator = By.id("login-button-does-not-exist");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void enterUsername(String username) {
        driver.findElement(usernameField).sendKeys(username);
    }

    public void enterPassword(String password) {
        driver.findElement(passwordField).sendKeys(password);
    }

    public void clickLogin() {
        driver.findElement(loginButton).click();
    }

    public void clickLoginUsingLocator(By locator) {
        driver.findElement(locator).click();
    }

    public String getErrorMessage() {
        return driver.findElement(errorMessage).getText();
    }

    public boolean isOnLoginPage() {
        return driver.findElements(loginButton).size() > 0;
    }
}
