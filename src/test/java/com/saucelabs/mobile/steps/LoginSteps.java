package com.saucelabs.mobile.steps;

import com.saucelabs.mobile.config.ConfigReader;
import com.saucelabs.mobile.context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/** Authentication steps. Glue only: business flow + assertions, no locators or waits. */
public class LoginSteps {

    private final TestContext context;

    public LoginSteps(TestContext context) {
        this.context = context;
    }

    @Given("the user is on the login screen")
    public void theUserIsOnTheLoginScreen() {
        Assert.assertTrue(context.loginPage().isDisplayed(), "Login screen was not displayed");
    }

    /** Role -> credentials from config (user.standard, user.locked ...), never hard-coded in features. */
    @Given("the user logs in as a {string} user")
    public void theUserLogsInAs(String role) {
        String username = ConfigReader.get("user." + role);
        context.loginPage().loginAs(username, ConfigReader.get("user.password"));
    }

    @When("the user logs in with username {string} and password {string}")
    public void theUserLogsInWith(String username, String password) {
        context.loginPage().loginAs(username, password);
    }

    @Then("the login error {string} is displayed")
    public void theLoginErrorIsDisplayed(String expected) {
        Assert.assertTrue(context.loginPage().isErrorDisplayed(), "Login error was not displayed");
        Assert.assertEquals(context.loginPage().getErrorMessage(), expected, "Login error text");
    }

    @Then("the user remains on the login screen")
    @Then("the login screen is displayed")
    public void theLoginScreenIsDisplayed() {
        Assert.assertTrue(context.loginPage().isDisplayed(), "Login screen was not displayed");
    }

    @When("the user logs out")
    public void theUserLogsOut() {
        context.productsPage().openMenu().logout();
    }
}
