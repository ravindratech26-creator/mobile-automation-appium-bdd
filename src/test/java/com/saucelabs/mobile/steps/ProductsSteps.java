package com.saucelabs.mobile.steps;

import com.saucelabs.mobile.context.TestContext;
import io.cucumber.java.en.Then;
import org.testng.Assert;

/** Product catalogue steps. */
public class ProductsSteps {

    private final TestContext context;

    public ProductsSteps(TestContext context) {
        this.context = context;
    }

    @Then("the products screen is displayed")
    public void theProductsScreenIsDisplayed() {
        Assert.assertTrue(context.productsPage().isDisplayed(), "Products screen was not displayed");
    }
}
