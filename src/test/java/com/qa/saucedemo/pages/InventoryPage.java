package com.qa.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the product listing page (shown right after login), the
 * shopping cart icon/badge, and the cart contents page (they share the
 * same "inventory_item_name" element structure on saucedemo.com).
 */
public class InventoryPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By inventoryList = By.className("inventory_item");
    private final By cartBadge = By.className("shopping_cart_badge");
    private final By cartIcon = By.className("shopping_cart_link");
    private final By pageTitle = By.className("title");
    private final By itemNames = By.className("inventory_item_name");

    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void addItemToCartByName(String productName) {
        // saucedemo generates an "Add to cart" button id from the product name,
        // e.g. "add-to-cart-sauce-labs-backpack"
        String buttonId = "add-to-cart-" + productName.toLowerCase()
                .replace(" ", "-");
        driver.findElement(By.id(buttonId)).click();
    }

    public void removeItemFromCartByName(String productName) {
        String buttonId = "remove-" + productName.toLowerCase()
                .replace(" ", "-");
        driver.findElement(By.id(buttonId)).click();
    }

    public int getCartItemCount() {
        List<?> badges = driver.findElements(cartBadge);
        if (badges.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(driver.findElement(cartBadge).getText());
    }

    public void goToCart() {
        driver.findElement(cartIcon).click();
    }

    public int getInventoryItemCount() {
        return driver.findElements(inventoryList).size();
    }

    /**
     * Reads the page header text ("Products" on the inventory page).
     * Explicit wait guards against reading this immediately after the
     * login redirect, before the new page has finished rendering.
     */
    public String getPageTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle)).getText();
    }

    /**
     * Returns the names of every item currently listed - works on both the
     * inventory (product listing) page AND the cart contents page, since
     * both use the same "inventory_item_name" class on saucedemo.com.
     * Explicit wait guards against reading this immediately after
     * navigating to the cart, before the page has finished rendering.
     */
    public List<String> getCartItemNames() {
        List<WebElement> items = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(itemNames));
        return items.stream().map(WebElement::getText).collect(Collectors.toList());
    }
}
