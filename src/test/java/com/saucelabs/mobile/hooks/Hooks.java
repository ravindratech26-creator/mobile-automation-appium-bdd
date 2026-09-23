package com.saucelabs.mobile.hooks;

import com.saucelabs.mobile.config.ConfigReader;
import com.saucelabs.mobile.driver.DriverManager;
import com.saucelabs.mobile.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.nio.charset.StandardCharsets;

/**
 * Scenario lifecycle: one fresh app session per scenario (full isolation, no shared state),
 * and failure evidence attached to the report at the exact step that failed.
 */
public class Hooks {

    private static final Logger LOG = LogManager.getLogger(Hooks.class);

    @Before(order = 0)
    public void startSession(Scenario scenario) {
        // Tag every log line from this thread with the scenario name (see log4j2.xml pattern)
        ThreadContext.put("scenario", scenario.getName());
        LOG.info("START  [{}] {} {}", ConfigReader.platform(), scenario.getName(), scenario.getSourceTagNames());
        DriverManager.startDriver();
    }

    /** Runs after every step; captures evidence only for the step that just failed. */
    @AfterStep
    public void captureFailedStep(Scenario scenario) {
        if (!scenario.isFailed() || !DriverManager.hasDriver()) {
            return;
        }
        if (!ConfigReader.getBoolean("screenshot.on.failure", true)) {
            return;
        }
        byte[] png = ScreenshotUtils.capture();
        if (png.length > 0) {
            scenario.attach(png, "image/png", "Failure screenshot");
            ScreenshotUtils.save(png, scenario.getName());
        }
        scenario.attach(ScreenshotUtils.pageSource().getBytes(StandardCharsets.UTF_8),
                "text/xml", "Page source at failure");
    }

    @After(order = 0)
    public void endSession(Scenario scenario) {
        try {
            LOG.info("END    {} -> {}", scenario.getName(), scenario.getStatus());
        } finally {
            DriverManager.quitDriver();   // always release the device, even after a crash
            ThreadContext.clearAll();
        }
    }
}
