package com.saucelabs.mobile.driver;

import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds one Appium session per thread so scenarios can run in parallel
 * without sharing a driver. Pages and hooks only ever call {@link #getDriver()}.
 */
public final class DriverManager {

    private static final Logger LOG = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void startDriver() {
        if (DRIVER.get() != null) {
            LOG.warn("Driver already running on this thread - reusing it");
            return;
        }
        DRIVER.set(DriverFactory.createDriver());
    }

    public static AppiumDriver getDriver() {
        AppiumDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("Driver not started. Was the @Before hook executed?");
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static void quitDriver() {
        AppiumDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
            LOG.info("Driver session closed");
        } catch (Exception e) {
            LOG.warn("Error while quitting driver: {}", e.getMessage());
        } finally {
            DRIVER.remove();
        }
    }
}
