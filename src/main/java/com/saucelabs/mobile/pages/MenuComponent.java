package com.saucelabs.mobile.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Side drawer menu. Modelled as a component (not a page) because it overlays any
 * logged-in screen; pages that own the burger button return it from openMenu().
 */
public class MenuComponent extends BasePage {

    @AndroidFindBy(accessibility = "test-LOGOUT")
    @iOSXCUITFindBy(accessibility = "test-LOGOUT")
    private WebElement logoutItem;

    @AndroidFindBy(accessibility = "test-Close")
    @iOSXCUITFindBy(accessibility = "test-Close")
    private WebElement closeButton;

    @Override
    public boolean isDisplayed() {
        return isVisible(logoutItem);
    }

    /** Waits for the drawer animation to settle (clickable) before tapping. */
    public void logout() {
        tap(logoutItem, "LOGOUT");
    }

    public void close() {
        tap(closeButton, "Close menu");
    }
}
