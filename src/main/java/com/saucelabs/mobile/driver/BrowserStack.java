package com.saucelabs.mobile.driver;

import com.saucelabs.mobile.config.ConfigReader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.options.BaseOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.HashMap;
import java.util.Map;

/**
 * BrowserStack App Automate support (-Denv=browserstack).
 *
 * <p>Credentials come ONLY from env vars / CI secrets:
 * BROWSERSTACK_USERNAME -> browserstack.username, BROWSERSTACK_ACCESS_KEY -> browserstack.access.key.
 * Device, OS version and bs:// app id come from {platform}-browserstack.properties.
 */
public final class BrowserStack {

    private static final Logger LOG = LogManager.getLogger(BrowserStack.class);

    private BrowserStack() {
    }

    public static boolean isActive() {
        return "browserstack".equals(ConfigReader.env());
    }

    /** Adds the vendor block to any platform's options. */
    static void apply(BaseOptions<?> options) {
        Map<String, Object> bstack = new HashMap<>();
        bstack.put("userName", ConfigReader.get("browserstack.username"));
        bstack.put("accessKey", ConfigReader.get("browserstack.access.key"));
        bstack.put("projectName", ConfigReader.get("bs.project.name"));
        bstack.put("buildName", ConfigReader.get("bs.build.name"));
        // Scenario name (set by Hooks) makes each cloud session searchable on the dashboard
        bstack.put("sessionName", ThreadContext.get("scenario") == null ? "session" : ThreadContext.get("scenario"));
        bstack.put("debug", ConfigReader.getBoolean("bs.debug", true));
        bstack.put("networkLogs", ConfigReader.getBoolean("bs.network.logs", false));
        bstack.put("video", ConfigReader.getBoolean("bs.video", true));
        bstack.put("idleTimeout", ConfigReader.getInt("new.command.timeout"));
        options.setCapability("bstack:options", bstack);
    }

    /**
     * Reports the Cucumber result to BrowserStack; without this every cloud session shows as
     * "completed" whatever the test outcome. Never throws - reporting must not mask the real result.
     */
    public static void markSession(AppiumDriver driver, boolean passed, String reason) {
        if (!isActive()) {
            return;
        }
        String safeReason = reason == null ? "" : reason.replace("\"", "'").replace("\n", " ");
        if (safeReason.length() > 250) {
            safeReason = safeReason.substring(0, 250);
        }
        try {
            driver.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": "
                    + "{\"status\":\"" + (passed ? "passed" : "failed") + "\", \"reason\": \"" + safeReason + "\"}}");
        } catch (Exception e) {
            LOG.warn("Could not set BrowserStack session status: {}", e.getMessage());
        }
    }
}
