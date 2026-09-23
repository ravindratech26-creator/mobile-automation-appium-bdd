package com.saucelabs.mobile.config;

/** Supported mobile platforms. Parsed from -Dplatform / PLATFORM (case-insensitive). */
public enum Platform {
    ANDROID,
    IOS;

    public static Platform from(String value) {
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Unsupported platform '" + value + "'. Use 'android' or 'ios'.", e);
        }
    }
}
