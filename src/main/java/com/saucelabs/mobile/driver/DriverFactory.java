package com.saucelabs.mobile.driver;

import com.saucelabs.mobile.config.ConfigReader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Builds a platform-specific Appium driver purely from configuration.
 * Adding a new platform/provider = new options method, no test changes.
 */
final class DriverFactory {

    private static final Logger LOG = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    static AppiumDriver createDriver() {
        URL serverUrl = serverUrl();
        LOG.info("Starting {} session on {} [env={}]",
                ConfigReader.platform(), serverUrl, ConfigReader.env());

        AppiumDriver driver = switch (ConfigReader.platform()) {
            case ANDROID -> new AndroidDriver(serverUrl, androidOptions());
            case IOS -> new IOSDriver(serverUrl, iosOptions());
        };

        LOG.info("Session started: {}", driver.getSessionId());
        return driver;
    }

    private static UiAutomator2Options androidOptions() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(ConfigReader.get("device.name"))
                .setApp(appPath())
                .setAppWaitActivity(ConfigReader.get("app.wait.activity", "*"))
                .setAppWaitForLaunch(ConfigReader.getBoolean("app.wait.for.launch", true))
                .setNoReset(ConfigReader.getBoolean("no.reset", false))
                .setAutoGrantPermissions(ConfigReader.getBoolean("auto.grant.permissions", true))
                .setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getInt("new.command.timeout")))
                .setUiautomator2ServerLaunchTimeout(millis("uia2.server.launch.timeout", 60000))
                .setUiautomator2ServerInstallTimeout(millis("uia2.server.install.timeout", 60000))
                .setAdbExecTimeout(millis("adb.exec.timeout", 60000));
        setIfPresent("platform.version", options::setPlatformVersion);
        setIfPresent("app.package", options::setAppPackage);
        setIfPresent("app.activity", options::setAppActivity);
        return options;
    }

    private static XCUITestOptions iosOptions() {
        XCUITestOptions options = new XCUITestOptions()
                .setDeviceName(ConfigReader.get("device.name"))
                .setApp(appPath())
                .setBundleId(ConfigReader.get("bundle.id"))
                .setNoReset(ConfigReader.getBoolean("no.reset", false))
                .setAutoAcceptAlerts(ConfigReader.getBoolean("auto.accept.alerts", true))
                .setWdaLaunchTimeout(Duration.ofMillis(Long.parseLong(ConfigReader.get("wda.launch.timeout"))))
                .setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getInt("new.command.timeout")));
        setIfPresent("platform.version", options::setPlatformVersion);
        return options;
    }

    /** Local paths are made absolute (Appium resolves relative to its own cwd); cloud ids pass through. */
    private static String appPath() {
        String app = ConfigReader.get("app.path");
        if (app.startsWith("bs://") || app.startsWith("http")) {
            return app;
        }
        Path path = Path.of(app).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new IllegalStateException("App not found at " + path
                    + ". Download it into apps/ (see README) or pass -Dapp.path=...");
        }
        return path.toString();
    }

    private static URL serverUrl() {
        String url = ConfigReader.get("appium.server.url");
        try {
            return URI.create(url).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new IllegalStateException("Invalid appium.server.url: " + url, e);
        }
    }

    private static Duration millis(String key, long defaultMillis) {
        return Duration.ofMillis(Long.parseLong(ConfigReader.get(key, String.valueOf(defaultMillis)).trim()));
    }

    private static void setIfPresent(String key, java.util.function.Consumer<String> setter) {
        String value = ConfigReader.get(key, "");
        if (!value.isBlank()) {
            setter.accept(value);
        }
    }
}
