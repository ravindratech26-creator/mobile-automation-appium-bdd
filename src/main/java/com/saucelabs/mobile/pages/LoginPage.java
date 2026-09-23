package com.saucelabs.mobile.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Login screen of the Sauce Labs sample app.
 *
 * <p>Locator strategy: accessibility id first (the app exposes the same "test-*" ids on
 * Android content-desc and iOS name), so one id serves both platforms and survives layout
 * changes. XPath/class chain are used only where no id exists (error text inside its container).
 */
public class LoginPage extends BasePage {

    @AndroidFindBy(accessibility = "test-Username")
    @iOSXCUITFindBy(accessibility = "test-Username")
    private WebElement usernameField;

    @AndroidFindBy(accessibility = "test-Password")
    @iOSXCUITFindBy(accessibility = "test-Password")
    private WebElement passwordField;

    @AndroidFindBy(accessibility = "test-LOGIN")
    @iOSXCUITFindBy(accessibility = "test-LOGIN")
    private WebElement loginButton;

    @AndroidFindBy(xpath = "//*[@content-desc='test-Error message']/android.widget.TextView")
    @iOSXCUITFindBy(iOSClassChain = "**/XCUIElementTypeOther[`name == 'test-Error message'`]/**/XCUIElementTypeStaticText")
    private WebElement errorMessage;

    @Override
    public boolean isDisplayed() {
        return isVisible(loginButton);
    }

    public LoginPage enterUsername(String username) {
        type(usernameField, username, "Username");
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordField, password, "Password");
        return this;
    }

    public void tapLogin() {
        hideKeyboard();   // keyboard can cover the button on small screens
        tap(loginButton, "LOGIN");
    }

    /** Convenience flow used by background/precondition steps. */
    public void loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        tapLogin();
    }

    public boolean isErrorDisplayed() {
        return isVisible(errorMessage);
    }

    public String getErrorMessage() {
        return textOf(errorMessage);
    }
}
