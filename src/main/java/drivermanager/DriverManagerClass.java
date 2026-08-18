package drivermanager;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import utilities.ConfigReader;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

public class DriverManagerClass {

    public static class DriverManager {

        private static final Logger LOGGER =
                LogManager.getLogger(DriverManager.class);

        /*
         * ThreadLocal ensures each parallel scenario
         * gets its own WebDriver instance.
         */
        private static final ThreadLocal<WebDriver> driverThreadLocal =
                new ThreadLocal<>();

        private DriverManager() {
        }

        // =========================================================
        // INITIALIZE DRIVER
        // =========================================================

        public static void initializeDriver(String browser) {

            // Close existing driver for the current thread
            if (driverThreadLocal.get() != null) {
                LOGGER.warn(
                        "Existing WebDriver found. Closing old session."
                );
                quitDriver();
            }

            String browserName =
                    browser == null
                            ? "chrome"
                            : browser.trim().toLowerCase();

            boolean headless = ConfigReader.isHeadless();

            WebDriver driver;

            try {

                /*
                 * Decide whether execution is LOCAL or GRID
                 * based on config.properties.
                 */
                if (ConfigReader.isGridEnabled()) {

                    String gridUrl = ConfigReader.getGridUrl();

                    URL remoteGridUrl = toGridUrl(gridUrl);

                    LOGGER.info(
                            "Launching {} on Selenium Grid: {} | Thread: {}",
                            browserName,
                            remoteGridUrl,
                            Thread.currentThread().getName()
                    );

                    driver = createGridDriver(
                            browserName,
                            headless,
                            remoteGridUrl
                    );

                } else {

                    LOGGER.info(
                            "Launching {} locally | Thread: {}",
                            browserName,
                            Thread.currentThread().getName()
                    );

                    driver = createLocalDriver(
                            browserName,
                            headless
                    );
                }

            } catch (Exception e) {

                LOGGER.error(
                        "Failed to initialize WebDriver for browser: {}",
                        browserName,
                        e
                );

                throw new RuntimeException(
                        "Unable to initialize WebDriver for browser: "
                                + browserName,
                        e
                );
            }

            // =====================================================
            // TIMEOUTS
            // =====================================================

            driver.manage()
                    .timeouts()
                    .pageLoadTimeout(
                            Duration.ofSeconds(
                                    ConfigReader.getPageLoadTimeout()
                            )
                    );

            driver.manage()
                    .timeouts()
                    .scriptTimeout(
                            Duration.ofSeconds(
                                    ConfigReader.getScriptTimeout()
                            )
                    );

            driver.manage()
                    .timeouts()
                    .implicitlyWait(
                            Duration.ofSeconds(
                                    ConfigReader.getImplicitWaitTimeout()
                            )
                    );

            /*
             * Store driver in ThreadLocal.
             */
            driverThreadLocal.set(driver);

            LOGGER.info(
                    "WebDriver initialized successfully. " +
                            "Browser={}, Grid={}, Headless={}, Thread={}",
                    browserName,
                    ConfigReader.isGridEnabled(),
                    headless,
                    Thread.currentThread().getName()
            );
        }


        // =========================================================
        // LOCAL DRIVER
        // =========================================================

        private static WebDriver createLocalDriver(
                String browser,
                boolean headless) {

            switch (browser) {

                case "chrome" -> {

                    WebDriverManager
                            .chromedriver()
                            .setup();

                    ChromeOptions options =
                            new ChromeOptions();

                    options.addArguments(
                            "--remote-allow-origins=*"
                    );

                    if (headless) {

                        options.addArguments(
                                "--headless=new"
                        );

                        options.addArguments(
                                "--window-size=1920,1080"
                        );

                    } else {

                        options.addArguments(
                                "--start-maximized"
                        );
                    }

                    return new ChromeDriver(options);
                }


                case "firefox" -> {

                    WebDriverManager
                            .firefoxdriver()
                            .setup();

                    FirefoxOptions options =
                            new FirefoxOptions();

                    if (headless) {
                        options.addArguments("-headless");
                    }

                    FirefoxDriver driver =
                            new FirefoxDriver(options);

                    if (!headless) {
                        driver.manage()
                                .window()
                                .maximize();
                    }

                    return driver;
                }


                case "edge" -> {

                    WebDriverManager
                            .edgedriver()
                            .setup();

                    EdgeOptions options =
                            new EdgeOptions();

                    if (headless) {

                        options.addArguments(
                                "--headless=new"
                        );

                        options.addArguments(
                                "--window-size=1920,1080"
                        );

                    } else {

                        options.addArguments(
                                "--start-maximized"
                        );
                    }

                    return new EdgeDriver(options);
                }


                default -> throw new IllegalArgumentException(
                        "Unsupported browser: "
                                + browser
                                + ". Supported browsers: "
                                + "chrome, firefox, edge."
                );
            }
        }


        // =========================================================
        // SELENIUM GRID DRIVER
        // =========================================================

        private static WebDriver createGridDriver(
                String browser,
                boolean headless,
                URL gridUrl) {

            switch (browser) {

                case "chrome" -> {

                    ChromeOptions options =
                            new ChromeOptions();

                    options.addArguments(
                            "--remote-allow-origins=*"
                    );

                    if (headless) {

                        options.addArguments(
                                "--headless=new"
                        );

                        options.addArguments(
                                "--window-size=1920,1080"
                        );
                    }

                    return new RemoteWebDriver(
                            gridUrl,
                            options
                    );
                }


                case "firefox" -> {

                    FirefoxOptions options =
                            new FirefoxOptions();

                    if (headless) {
                        options.addArguments("-headless");
                    }

                    return new RemoteWebDriver(
                            gridUrl,
                            options
                    );
                }


                case "edge" -> {

                    EdgeOptions options =
                            new EdgeOptions();

                    if (headless) {

                        options.addArguments(
                                "--headless=new"
                        );

                        options.addArguments(
                                "--window-size=1920,1080"
                        );
                    }

                    return new RemoteWebDriver(
                            gridUrl,
                            options
                    );
                }


                default -> throw new IllegalArgumentException(
                        "Unsupported Grid browser: "
                                + browser
                                + ". Supported browsers: "
                                + "chrome, firefox, edge."
                );
            }
        }


        // =========================================================
        // GRID URL
        // =========================================================

        private static URL toGridUrl(String gridUrl) {

            try {

                return URI
                        .create(gridUrl)
                        .toURL();

            } catch (Exception e) {

                throw new IllegalStateException(
                        "Invalid Selenium Grid URL: "
                                + gridUrl,
                        e
                );
            }
        }


        // =========================================================
        // GET DRIVER
        // =========================================================

        public static WebDriver getDriver() {
            return driverThreadLocal.get();
        }


        // =========================================================
        // QUIT DRIVER
        // =========================================================

        public static void quitDriver() {

            WebDriver driver =
                    driverThreadLocal.get();

            if (driver != null) {

                try {

                    driver.quit();

                    LOGGER.info(
                            "WebDriver session closed. Thread={}",
                            Thread.currentThread().getName()
                    );

                } finally {

                    /*
                     * Important for parallel execution.
                     */
                    driverThreadLocal.remove();
                }
            }
        }
    }
}