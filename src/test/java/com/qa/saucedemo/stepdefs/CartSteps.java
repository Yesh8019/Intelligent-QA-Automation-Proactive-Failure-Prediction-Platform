package com.qa.saucedemo.stepdefs;

import com.qa.saucedemo.pages.InventoryPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class CartSteps {

    private InventoryPage inventoryPage() {
        return new InventoryPage(DriverManager.getDriver());
    }

    @When("I add {string} to the cart")
    public void iAddToTheCart(String productName) {
        inventoryPage().addItemToCartByName(productName);
    }

    @And("I remove {string} from the cart")
    public void iRemoveFromTheCart(String productName) {
        inventoryPage().removeItemFromCartByName(productName);
    }

    @And("I go to the cart")
    public void iGoToTheCart() {
        inventoryPage().goToCart();
    }

    @Then("the cart badge should show {int} item")
    @Then("the cart badge should show {int} items")
    public void theCartBadgeShouldShowItems(int expectedCount) {
        Assert.assertEquals(
                "Cart badge count did not match expected value",
                expectedCount, inventoryPage().getCartItemCount());
    }

    @Then("the cart should contain {string}")
    public void theCartShouldContain(String productName) {
        Assert.assertTrue(
                "Expected the cart to contain: " + productName,
                inventoryPage().getCartItemNames().contains(productName));
    }
}
