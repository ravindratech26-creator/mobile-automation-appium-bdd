package com.saucelabs.mobile.pages;

import com.saucelabs.mobile.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Product catalogue - the landing screen after a successful login. */
public class ProductsPage extends BasePage {

    @AndroidFindBy(accessibility = "test-PRODUCTS")
    @iOSXCUITFindBy(accessibility = "test-PRODUCTS")
    private WebElement productsHeader;

    @AndroidFindBy(accessibility = "test-Menu")
    @iOSXCUITFindBy(accessibility = "test-Menu")
    private WebElement menuButton;

    @AndroidFindBy(accessibility = "test-Cart")
    @iOSXCUITFindBy(accessibility = "test-Cart")
    private WebElement cartButton;

    /** Badge text lives inside the cart icon and only exists when the cart is not empty. */
    @AndroidFindBy(xpath = "//*[@content-desc='test-Cart']//android.widget.TextView")
    @iOSXCUITFindBy(iOSClassChain = "**/XCUIElementTypeOther[`name == 'test-Cart'`]/**/XCUIElementTypeStaticText")
    private List<WebElement> cartBadge;

    @Override
    public boolean isDisplayed() {
        return isVisible(productsHeader);
    }

    public MenuComponent openMenu() {
        tap(menuButton, "Menu");
        return new MenuComponent();
    }

    /**
     * Adds a product by its visible name - scoped to that product's card, so the test never
     * depends on list position (which changes with sorting or new catalogue items).
     */
    public ProductsPage addToCart(String productName) {
        scrollToText(productName);
        tap(productButton(productName, "test-ADD TO CART"), "ADD TO CART: " + productName);
        return this;
    }

    public boolean isRemoveButtonShownFor(String productName) {
        By remove = productButton(productName, "test-REMOVE");
        return WaitUtils.until(() -> !driver.findElements(remove).isEmpty());
    }

    /** 0 when the badge is absent (empty cart). */
    public int cartCount() {
        return cartBadge.isEmpty() ? 0 : Integer.parseInt(cartBadge.get(0).getText().trim());
    }

    /** Badge updates asynchronously after a tap - poll instead of reading once. */
    public boolean waitForCartCount(int expected) {
        return WaitUtils.until(() -> cartCount() == expected);
    }

    public CartPage openCart() {
        tap(cartButton, "Cart");
        return new CartPage();
    }

    private By productButton(String productName, String buttonId) {
        return byPlatform(
                AppiumBy.xpath("//*[@content-desc='test-Item'][.//*[@text='" + productName + "']]"
                        + "//*[@content-desc='" + buttonId + "']"),
                AppiumBy.xpath("//XCUIElementTypeOther[@name='test-Item'][.//XCUIElementTypeStaticText[@label='"
                        + productName + "']]//XCUIElementTypeOther[@name='" + buttonId + "']"));
    }
}
