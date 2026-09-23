package com.saucelabs.mobile.listeners;

import com.saucelabs.mobile.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Re-runs a failed scenario up to {@code retry.count} times (config / -Dretry.count / RETRY_COUNT).
 *
 * <p>Retries absorb environmental noise (device hiccups, slow cold starts) - they are NOT a fix for
 * a flaky test. Every retry is logged at WARN and shows in the report as a skipped attempt,
 * so "passed on retry" is visible and can be investigated. Set retry.count=0 to disable.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LogManager.getLogger(RetryAnalyzer.class);
    private int attempt = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = Integer.parseInt(ConfigReader.get("retry.count", "0").trim());
        if (attempt < maxRetries) {
            attempt++;
            LOG.warn("RETRY {}/{} - '{}' failed: {}", attempt, maxRetries, scenarioName(result),
                    result.getThrowable() == null ? "unknown" : result.getThrowable().getMessage());
            return true;
        }
        return false;
    }

    /** Cucumber passes the scenario as the first data-provider parameter. */
    private static String scenarioName(ITestResult result) {
        Object[] params = result.getParameters();
        return params.length > 0 ? String.valueOf(params[0]).replace("\"", "") : result.getName();
    }
}
