package com.saucelabs.mobile.utils;

import com.saucelabs.mobile.config.ConfigReader;
import com.saucelabs.mobile.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/**
 * Explicit, condition-based waits. The framework never uses Thread.sleep or implicit waits:
 * every interaction waits for the exact state it needs (visible, clickable, gone).
 */
public final class WaitUtils {

    private WaitUtils() {
    }

    public static WebElement waitForVisible(WebElement element) {
        return waitForVisible(element, defaultTimeout());
    }

    public static WebElement waitForVisible(WebElement element, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.visibilityOf(element));
    }

    public static WebElement waitForClickable(WebElement element) {
        return wait(defaultTimeout()).until(ExpectedConditions.elementToBeClickable(element));
    }

    /** For dynamic locators built at runtime (e.g. "the ADD TO CART button of product X"). */
    public static WebElement waitForVisible(By locator) {
        return wait(defaultTimeout()).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(By locator) {
        return wait(defaultTimeout()).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean waitForInvisible(WebElement element) {
        return wait(defaultTimeout()).until(ExpectedConditions.invisibilityOf(element));
    }

    /** Non-throwing check - for optional UI (e.g. validation errors) and assertions. */
    public static boolean isVisibleWithin(WebElement element, Duration timeout) {
        try {
            waitForVisible(element, timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Polls any UI state (e.g. "badge shows 2") until true or timeout; never throws. */
    public static boolean until(BooleanSupplier condition) {
        try {
            return wait(defaultTimeout()).until(d -> condition.getAsBoolean());
        } catch (TimeoutException e) {
            return false;
        }
    }

    public static Duration defaultTimeout() {
        return Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds"));
    }

    private static Wait<WebDriver> wait(Duration timeout) {
        return new FluentWait<WebDriver>(DriverManager.getDriver())
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(ConfigReader.getInt("poll.interval.millis")))
                // React Native re-renders often: retry instead of failing on transient lookups
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }
}
