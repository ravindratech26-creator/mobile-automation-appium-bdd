package com.saucelabs.mobile.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Layered, read-only configuration.
 *
 * <p>Resolution order (later wins):
 * <ol>
 *   <li>config/common.properties</li>
 *   <li>config/{platform}.properties          e.g. android.properties</li>
 *   <li>config/{env}.properties               e.g. browserstack.properties (optional)</li>
 *   <li>config/{platform}-{env}.properties    e.g. ios-browserstack.properties (optional)</li>
 *   <li>Environment variable                  explicit.wait.seconds -> EXPLICIT_WAIT_SECONDS</li>
 *   <li>JVM system property                   -Dexplicit.wait.seconds=20</li>
 * </ol>
 * Values may reference env vars / system properties with ${NAME}.
 */
public final class ConfigReader {

    private static final String CONFIG_DIR = "config/";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    private static final Platform PLATFORM = Platform.from(bootstrap("platform", "android"));
    private static final String ENV = bootstrap("env", "local").trim().toLowerCase();
    private static final Properties PROPS = load();

    private ConfigReader() {
    }

    public static Platform platform() {
        return PLATFORM;
    }

    public static String env() {
        return ENV;
    }

    public static boolean isCloud() {
        return !"local".equals(ENV);
    }

    /** Required value - fails fast with a clear message if missing. */
    public static String get(String key) {
        String value = get(key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing config key '" + key + "' for platform="
                    + PLATFORM + ", env=" + ENV);
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(toEnvName(key));
        }
        if (value == null) {
            value = PROPS.getProperty(key);
        }
        return value == null ? defaultValue : resolvePlaceholders(value);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key).trim());
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, null);
        return value == null || value.isBlank() ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    // ------------------------------------------------------------------

    private static Properties load() {
        Properties props = new Properties();
        String platform = PLATFORM.name().toLowerCase();
        loadInto(props, "common", true);
        loadInto(props, platform, true);
        if (!"local".equals(ENV)) {
            loadInto(props, ENV, true);
            loadInto(props, platform + "-" + ENV, false);
        }
        return props;
    }

    private static void loadInto(Properties props, String name, boolean required) {
        String path = CONFIG_DIR + name + ".properties";
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                if (required) {
                    throw new IllegalStateException("Config file not found on classpath: " + path);
                }
                return;
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }

    /** platform/env are needed before files load, so read them from -D or env var only. */
    private static String bootstrap(String key, String defaultValue) {
        String value = System.getProperty(key, System.getenv(toEnvName(key)));
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String toEnvName(String key) {
        return key.toUpperCase().replace('.', '_').replace('-', '_');
    }

    private static String resolvePlaceholders(String value) {
        Matcher m = PLACEHOLDER.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            String replacement = System.getProperty(name, System.getenv(name));
            if (replacement == null) {
                throw new IllegalStateException("Config placeholder ${" + name + "} is not set");
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
