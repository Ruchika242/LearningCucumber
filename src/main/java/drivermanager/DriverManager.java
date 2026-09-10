package drivermanager;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import utilities.ConfigReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class DriverManager {

    private static final ThreadLocal<WebDriver> driver =
            new ThreadLocal<>();

    public static void launchBrowser(String browser) {
        try {
            String normalizedBrowser = normalizeBrowser(browser);
            boolean gridEnabled = ConfigReader.isGridEnabled();
            String gridUrl = ConfigReader.getGridUrl();
            boolean headless = ConfigReader.isHeadless();
            WebDriver webDriver = createDriver(normalizedBrowser, gridEnabled, gridUrl, headless);

            applyTimeouts(webDriver);
            driver.set(webDriver);
            logLaunchDetails(normalizedBrowser, gridEnabled, gridUrl);

        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    "Invalid Selenium Grid URL: "
                            + ConfigReader.getGridUrl(),
                    e
            );
        }
    }

    private static String normalizeBrowser(String browser) {
        if (browser == null || browser.isBlank()) {
            return "chrome";
        }
        return browser.trim().toLowerCase();
    }

    private static WebDriver createDriver(String browser, boolean gridEnabled, String gridUrl, boolean headless)
            throws MalformedURLException {
        switch (browser) {
            case "chrome": {
                ChromeOptions options = buildChromeOptions(headless);
                return gridEnabled ? createRemoteDriver(gridUrl, options) : createLocalChromeDriver(options);
            }
            case "firefox": {
                FirefoxOptions options = buildFirefoxOptions(headless);
                return gridEnabled ? createRemoteDriver(gridUrl, options) : createLocalFirefoxDriver(options);
            }
            case "edge": {
                EdgeOptions options = buildEdgeOptions(headless);
                return gridEnabled ? createRemoteDriver(gridUrl, options) : createLocalEdgeDriver(options);
            }
            default:
                throw new IllegalArgumentException("Invalid browser: " + browser);
        }
    }

    private static WebDriver createRemoteDriver(String gridUrl, Capabilities options) throws MalformedURLException {
        URL grid = new URL(gridUrl);
        return new RemoteWebDriver(grid, options);
    }

    private static void logLaunchDetails(String browser, boolean gridEnabled, String gridUrl) {
        System.out.println("==============================================");
        System.out.println("BROWSER STARTED: " + browser);
        System.out.println("Execution Mode: " + (gridEnabled ? "GRID" : "LOCAL")
                + " | URL: " + (gridEnabled ? gridUrl : "N/A"));
        System.out.println("THREAD: " + Thread.currentThread().threadId());
        System.out.println("==============================================");
    }

    private static ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        return options;
    }

    private static FirefoxOptions buildFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static EdgeOptions buildEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        return options;
    }

    private static WebDriver createLocalChromeDriver(ChromeOptions options) {
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(options);
    }

    private static WebDriver createLocalFirefoxDriver(FirefoxOptions options) {
        WebDriverManager.firefoxdriver().setup();
        return new FirefoxDriver(options);
    }

    private static WebDriver createLocalEdgeDriver(EdgeOptions options) {
        String explicitDriverPath = ConfigReader.getEdgeDriverPath();
        if (explicitDriverPath != null && !explicitDriverPath.isBlank()) {
            System.setProperty("webdriver.edge.driver", explicitDriverPath.trim());
            return new EdgeDriver(options);
        }

        try {
            WebDriverManager.edgedriver().setup();
            return new EdgeDriver(options);
        } catch (RuntimeException wdmFailure) {
            // Fallback allows execution with an already-installed driver on PATH.
            try {
                return new EdgeDriver(options);
            } catch (RuntimeException localFailure) {
                localFailure.addSuppressed(wdmFailure);
                throw new RuntimeException(
                        "Unable to initialize Edge driver. WebDriverManager could not download the driver and local fallback failed. "
                                + "Set -Dwebdriver.edge.driver=<absolute-path-to-msedgedriver.exe> or enable Grid execution.",
                        localFailure
                );
            }
        }
    }

    private static void applyTimeouts(WebDriver webDriver) {
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getImplicitWaitTimeout()));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getPageLoadTimeout()));
        //webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(ConfigReader.getScriptTimeout()));
    }

    public static WebDriver getDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver == null) {
            throw new IllegalStateException(
                    "WebDriver is not initialized for thread: "
                            + Thread.currentThread().threadId()
            );
        }
        return webDriver;
    }

    public static void quitDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            try {
                webDriver.quit();
            } finally {
                driver.remove();
            }
            System.out.println("Browser closed | Thread: " + Thread.currentThread().threadId());
        }
    }
}