package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties DEFAULT_PROPS = new Properties();
    private static final Properties ENV_PROPS = new Properties();
    private static final String EXECUTION_ENV;

    static {
        loadProperties(DEFAULT_PROPS, "config.properties");

        String defaultEnv = DEFAULT_PROPS.getProperty("execution.env", "qa").trim();
        EXECUTION_ENV = resolveRaw("env", "TEST_ENV", defaultEnv).toLowerCase();

        loadProperties(ENV_PROPS, "config-" + EXECUTION_ENV + ".properties");
    }

    private ConfigReader() {
        // Utility class — not instantiable
    }

    public static String getBrowser() {
        return resolve("browser", "BROWSER", "chrome").toLowerCase();
    }

    public static boolean isHeadless() {
        return toBoolean(resolve("headless", "HEADLESS", "false"));
    }

    public static String getBaseUrl() {
        return resolve("base.url", "APP_BASE_URL", "https://qaplayground.com/bank/login");
    }
    public static boolean isGridEnabled() {
        return toBoolean(resolve("grid.enabled", "GRID_ENABLED", "false"));
    }

    public static String getGridUrl() {
        return resolve("grid.url", "GRID_URL", "http://10.0.0.153:4444");
    }

    public static String getUsername() {
        return resolve("app.username", "APP_USERNAME", "standard_user");
    }

    public static String getPassword() {
        return resolve("app.password", "APP_PASSWORD", "bank_sauce");
    }

    public static String getInvalidUsername() {
        return resolve("app.invalid.username", "APP_INVALID_USERNAME", "invalid_user");
    }

    public static String getInvalidPassword() {
        return resolve("app.invalid.password", "APP_INVALID_PASSWORD", "invalid_password");
    }

    public static int getExplicitWaitTimeout() {
        return toInt(resolve("wait.timeout", "WAIT_TIMEOUT", "15"), 15);
    }

    public static int getPageLoadTimeout() {
        return toInt(resolve("page.load.timeout", "PAGE_LOAD_TIMEOUT", "30"), 30);
    }

    public static int getScriptTimeout() {
        return toInt(resolve("script.timeout", "SCRIPT_TIMEOUT", "30"), 30);
    }

    public static int getImplicitWaitTimeout() {
        return toInt(resolve("implicit.wait.timeout", "IMPLICIT_WAIT_TIMEOUT", "0"), 0);
    }

    public static boolean isScreenshotOnFailureEnabled() {
        return toBoolean(resolve("screenshot.on.failure", "SCREENSHOT_ON_FAILURE", "true"));
    }

    public static boolean isScreenshotOnPassEnabled() {
        return toBoolean(resolve("screenshot.on.pass", "SCREENSHOT_ON_PASS", "false"));
    }

    public static boolean isScreenshotAttachmentEnabled() {
        return toBoolean(resolve("screenshot.attach.to.report", "SCREENSHOT_ATTACH_TO_REPORT", "true"));
    }

    public static String getScreenshotDirectory() {
        return resolve("screenshot.output.dir", "SCREENSHOT_OUTPUT_DIR", "target/screenshots");
    }

    public static String getExecutionEnvironment() {
        return EXECUTION_ENV;
    }

    private static String resolve(String propertyKey, String envKey, String defaultValue) {
        String value = resolveRaw(propertyKey, envKey, null);
        if (value != null) {
            return value;
        }

        value = ENV_PROPS.getProperty(propertyKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        value = DEFAULT_PROPS.getProperty(propertyKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return defaultValue;
    }

    private static String resolveRaw(String propertyKey, String envKey, String defaultValue) {
        String value = System.getProperty(propertyKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        value = System.getenv(envKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return defaultValue;
    }

    private static void loadProperties(Properties target, String resourceName) {
        try (InputStream input = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (input != null) {
                target.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + resourceName, e);
        }
    }

    private static int toInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean toBoolean(String value) {
        return Boolean.parseBoolean(value);
    }
}
