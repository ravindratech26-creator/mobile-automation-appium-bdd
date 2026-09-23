package com.saucelabs.mobile.pages;

import com.saucelabs.mobile.utils.WaitUtils;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

import java.util.List;

/** "YOUR CART" screen. */
public class CartPage extends BasePage {

    @AndroidFindBy(accessibility = "test-Cart Content")
    @iOSXCUITFindBy(accessibility = "test-Cart Content")
    private WebElement cartContent;

    /** First text in each item's description block is the product name. */
    @AndroidFindBy(xpath = "//*[@content-desc='test-Description']/android.widget.TextView[1]")
    @iOSXCUITFindBy(iOSClassChain = "**/XCUIElementTypeOther[`name == 'test-Description'`]/XCUIElementTypeStaticText[1]")
    private List<WebElement> itemNames;

    @AndroidFindBy(xpath = "//*[@content-desc='test-Price']/android.widget.TextView")
    @iOSXCUITFindBy(iOSClassChain = "**/XCUIElementTypeOther[`name == 'test-Price'`]/XCUIElementTypeStaticText")
    private List<WebElement> itemPrices;

    @AndroidFindBy(accessibility = "test-REMOVE")
    @iOSXCUITFindBy(accessibility = "test-REMOVE")
    private List<WebElement> removeButtons;

    @AndroidFindBy(accessibility = "test-CONTINUE SHOPPING")
    @iOSXCUITFindBy(accessibility = "test-CONTINUE SHOPPING")
    private WebElement continueShoppingButton;

    @Override
    public boolean isDisplayed() {
        return isVisible(cartContent);
    }

    public List<String> itemNames() {
        return itemNames.stream().map(e -> e.getText().trim()).toList();
    }

    /** Price of the named item, matched by position within the cart list. */
    public String priceOf(String productName) {
        int index = itemNames().indexOf(productName);
        if (index < 0) {
            throw new IllegalStateException("'" + productName + "' is not in the cart: " + itemNames());
        }
        return itemPrices.get(index).getText().trim();
    }

    public CartPage removeFirstItem() {
        tap(removeButtons.get(0), "REMOVE");
        return this;
    }

    /** List re-renders after REMOVE - poll until it is empty. */
    public boolean waitUntilEmpty() {
        return WaitUtils.until(itemNames::isEmpty);
    }

    public ProductsPage continueShopping() {
        tap(continueShoppingButton, "CONTINUE SHOPPING");
        return new ProductsPage();
    }
}
