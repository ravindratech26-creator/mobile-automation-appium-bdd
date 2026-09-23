package com.saucelabs.mobile.steps;

import com.saucelabs.mobile.context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/** Add-to-cart / cart management steps. */
public class CartSteps {

    private final TestContext context;

    public CartSteps(TestContext context) {
        this.context = context;
    }

    @Given("the user adds {string} to the cart")
    public void theUserAddsToTheCart(String product) {
        context.productsPage().addToCart(product);
        Assert.assertTrue(context.productsPage().isRemoveButtonShownFor(product),
                "REMOVE button not shown for '" + product + "' after adding it");
    }

    @Then("the cart badge shows {int}")
    public void theCartBadgeShows(int expected) {
        Assert.assertTrue(context.productsPage().waitForCartCount(expected),
                "Cart badge expected " + expected + " but was " + context.productsPage().cartCount());
    }

    @When("the user opens the cart")
    public void theUserOpensTheCart() {
        context.productsPage().openCart();
        Assert.assertTrue(context.cartPage().isDisplayed(), "Cart screen was not displayed");
    }

    @Then("the cart contains {string} priced {string}")
    public void theCartContains(String product, String price) {
        Assert.assertTrue(context.cartPage().itemNames().contains(product),
                "Cart items " + context.cartPage().itemNames() + " do not contain '" + product + "'");
        Assert.assertEquals(context.cartPage().priceOf(product), price, "Price of " + product);
    }

    @When("the user removes the item from the cart")
    public void theUserRemovesTheItem() {
        context.cartPage().removeFirstItem();
    }

    @Then("the cart is empty")
    public void theCartIsEmpty() {
        Assert.assertTrue(context.cartPage().waitUntilEmpty(),
                "Cart still contains " + context.cartPage().itemNames());
    }
}
