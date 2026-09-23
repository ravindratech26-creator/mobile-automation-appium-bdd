package com.saucelabs.mobile.utils;

import com.saucelabs.mobile.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Captures failure evidence. Never throws: a broken session must not hide the real test failure.
 */
public final class ScreenshotUtils {

    private static final Logger LOG = LogManager.getLogger(ScreenshotUtils.class);
    private static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {
    }

    /** @return PNG bytes, or an empty array if the session is gone. */
    public static byte[] capture() {
        try {
            return DriverManager.getDriver().getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            LOG.warn("Screenshot not captured: {}", e.getMessage());
            return new byte[0];
        }
    }

    /** Saves PNG bytes to target/screenshots/<name>_<timestamp>.png (kept as CI artefacts). */
    public static Path save(byte[] png, String name) {
        if (png.length == 0) {
            return null;
        }
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            Path file = SCREENSHOT_DIR.resolve(sanitize(name) + "_" + LocalDateTime.now().format(STAMP) + ".png");
            Files.write(file, png);
            LOG.info("Screenshot saved: {}", file.toAbsolutePath());
            return file;
        } catch (IOException e) {
            LOG.warn("Screenshot not saved: {}", e.getMessage());
            return null;
        }
    }

    public static String pageSource() {
        try {
            return DriverManager.getDriver().getPageSource();
        } catch (Exception e) {
            return "Page source unavailable: " + e.getMessage();
        }
    }

    private static String sanitize(String name) {
        String safe = name.replaceAll("[^a-zA-Z0-9-_]+", "_");
        return safe.length() > 80 ? safe.substring(0, 80) : safe;
    }
}
