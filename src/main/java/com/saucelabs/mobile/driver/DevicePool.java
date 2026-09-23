package com.saucelabs.mobile.driver;

import com.saucelabs.mobile.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Hands out one device per running scenario for local parallel execution.
 *
 * <p>Configured with {@code devices=emulator-5554,emulator-5556} (config, -Ddevices or DEVICES).
 * Each device gets a fixed index, used to derive unique per-session ports (UiAutomator2
 * systemPort / iOS wdaLocalPort) so sessions on one Appium server never collide.
 * When {@code devices} is empty the pool is disabled and the single configured device is used.
 */
public final class DevicePool {

    private static final Logger LOG = LogManager.getLogger(DevicePool.class);
    private static final List<String> DEVICES = Arrays.stream(ConfigReader.get("devices", "").split(","))
            .map(String::trim).filter(s -> !s.isEmpty()).toList();
    private static final BlockingQueue<Device> FREE = new LinkedBlockingQueue<>();

    static {
        for (int i = 0; i < DEVICES.size(); i++) {
            FREE.add(new Device(DEVICES.get(i), i));
        }
    }

    private DevicePool() {
    }

    public record Device(String udid, int index) {
    }

    public static boolean isEnabled() {
        return !DEVICES.isEmpty();
    }

    /** Blocks until a device is free - so threads > devices queue instead of colliding. */
    static Device acquire() {
        try {
            Device device = FREE.poll(10, TimeUnit.MINUTES);
            if (device == null) {
                throw new IllegalStateException("No free device within 10 minutes. Pool: " + DEVICES);
            }
            LOG.info("Acquired device {} (slot {})", device.udid(), device.index());
            return device;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a device", e);
        }
    }

    static void release(Device device) {
        if (device != null) {
            FREE.add(device);
            LOG.info("Released device {}", device.udid());
        }
    }
}
