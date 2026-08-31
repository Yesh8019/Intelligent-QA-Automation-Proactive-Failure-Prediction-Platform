package com.qa.saucedemo.stepdefs;

import org.openqa.selenium.WebDriver;

/**
 * Holds the single WebDriver instance for the currently running scenario.
 *
 * Cucumber creates a new instance of each step definition class per scenario,
 * so step classes (LoginSteps, CartSteps, CheckoutSteps) can't share state
 * through normal instance fields the way a single TestNG test class could.
 * This static holder lets Hooks.java create the driver once per scenario,
 * and every step definition class reads the SAME driver via getDriver().
 */
public class DriverManager {

    private static WebDriver driver;

    public static void setDriver(WebDriver webDriver) {
        driver = webDriver;
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
