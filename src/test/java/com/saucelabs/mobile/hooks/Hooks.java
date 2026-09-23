package com.saucelabs.mobile.hooks;

import com.saucelabs.mobile.config.ConfigReader;
import com.saucelabs.mobile.driver.BrowserStack;
import com.saucelabs.mobile.driver.DriverManager;
import com.saucelabs.mobile.utils.AllureEnvironmentWriter;
import com.saucelabs.mobile.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.nio.charset.StandardCharsets;

/**
 * Scenario lifecycle: one fresh app session per scenario (full isolation, no shared state),
 * and failure evidence attached to the report.
 *
 * <p>@After hooks run in DESCENDING order: captureFailure (1) runs before endSession (0),
 * so evidence is taken while the session is still alive.
 */
public class Hooks {

    private static final Logger LOG = LogManager.getLogger(Hooks.class);

    @Before(order = 0)
    public void startSession(Scenario scenario) {
        // Tag every log line from this thread with the scenario name (see log4j2.xml pattern)
        ThreadContext.put("scenario", scenario.getName());
        LOG.info("START  [{}] {} {}", ConfigReader.platform(), scenario.getName(), scenario.getSourceTagNames());
        DriverManager.startDriver();
        AllureEnvironmentWriter.record(DriverManager.getDriver());
    }

    /**
     * Captures evidence once per failed scenario. After a step fails Cucumber skips the remaining
     * steps without touching the device, so the screen here is the failure screen. Using @After
     * instead of @AfterStep also keeps the hook out of the report's step list.
     */
    @After(order = 1)
    public void captureFailure(Scenario scenario) {
        if (!scenario.isFailed() || !DriverManager.hasDriver()
                || !ConfigReader.getBoolean("screenshot.on.failure", true)) {
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
            if (DriverManager.hasDriver()) {
                BrowserStack.markSession(DriverManager.getDriver(), !scenario.isFailed(),
                        scenario.getName() + " -> " + scenario.getStatus());
            }
        } finally {
            DriverManager.quitDriver();   // always release the device, even after a crash
            ThreadContext.clearAll();
        }
    }
}
