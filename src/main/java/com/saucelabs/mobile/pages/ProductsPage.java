package com.saucelabs.mobile.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/** Product catalogue - the landing screen after a successful login. */
public class ProductsPage extends BasePage {

    @AndroidFindBy(accessibility = "test-PRODUCTS")
    @iOSXCUITFindBy(accessibility = "test-PRODUCTS")
    private WebElement productsHeader;

    @AndroidFindBy(accessibility = "test-Menu")
    @iOSXCUITFindBy(accessibility = "test-Menu")
    private WebElement menuButton;

    @Override
    public boolean isDisplayed() {
        return isVisible(productsHeader);
    }

    public MenuComponent openMenu() {
        tap(menuButton, "Menu");
        return new MenuComponent();
    }
}
