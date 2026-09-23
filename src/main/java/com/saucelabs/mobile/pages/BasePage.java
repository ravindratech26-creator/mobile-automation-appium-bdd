package com.saucelabs.mobile.pages;

import com.saucelabs.mobile.config.ConfigReader;
import com.saucelabs.mobile.config.Platform;
import com.saucelabs.mobile.driver.DriverManager;
import com.saucelabs.mobile.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;
import java.util.Map;

/**
 * Parent of every page object.
 *
 * <p>Uses {@link AppiumFieldDecorator} so each page declares BOTH platform locators on one field:
 * <pre>
 *   &#64;AndroidFindBy(accessibility = "test-LOGIN")
 *   &#64;iOSXCUITFindBy(accessibility = "test-LOGIN")
 *   private WebElement loginButton;
 * </pre>
 * The correct one is picked at runtime from the driver's platform - no if/else in page code.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final AppiumDriver driver;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        // Zero implicit wait in the decorator: all synchronisation goes through WaitUtils
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ZERO), this);
    }

    /** Every page states what "loaded" means - used by steps to assert navigation. */
    public abstract boolean isDisplayed();

    // ---------- Interactions (all wait first) ----------

    protected void tap(WebElement element, String name) {
        log.info("Tap '{}'", name);
        WaitUtils.waitForClickable(element).click();
    }

    protected void type(WebElement element, String text, String name) {
        log.info("Type into '{}'", name);   // value not logged: may be a password
        WebElement field = WaitUtils.waitForVisible(element);
        field.clear();
        field.sendKeys(text);
    }

    protected void tap(By locator, String name) {
        log.info("Tap '{}'", name);
        WaitUtils.waitForClickable(locator).click();
    }

    protected String textOf(WebElement element) {
        return WaitUtils.waitForVisible(element).getText().trim();
    }

    protected boolean isVisible(WebElement element) {
        return WaitUtils.isVisibleWithin(element, WaitUtils.defaultTimeout());
    }

    protected boolean isVisible(WebElement element, Duration timeout) {
        return WaitUtils.isVisibleWithin(element, timeout);
    }

    // ---------- Platform helpers ----------

    protected boolean isAndroid() {
        return ConfigReader.platform() == Platform.ANDROID;
    }

    /** Dynamic-locator equivalent of the @AndroidFindBy/@iOSXCUITFindBy pair. */
    protected By byPlatform(By android, By ios) {
        return isAndroid() ? android : ios;
    }

    protected void hideKeyboard() {
        try {
            if (driver instanceof HasOnScreenKeyboard kb && kb.isKeyboardShown()
                    && driver instanceof HidesKeyboard hk) {
                hk.hideKeyboard();
            }
        } catch (Exception e) {
            log.debug("Keyboard not hidden: {}", e.getMessage());
        }
    }

    /** Scrolls until the given accessibility id / text is on screen (native gesture per platform). */
    protected void scrollToText(String text) {
        log.info("Scroll to '{}'", text);
        if (isAndroid()) {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true))"
                            + ".scrollIntoView(new UiSelector().textContains(\"" + text + "\"))"));
        } else {
            driver.executeScript("mobile: scroll", Map.of("predicateString", "label CONTAINS '" + text + "'"));
        }
    }
}
