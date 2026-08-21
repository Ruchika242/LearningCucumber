package drivermanager;

import io.github.bonigarcia.wdm.WebDriverManager;
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

public class DriverManagerClass {

    private static final ThreadLocal<WebDriver> driver =
            new ThreadLocal<>();

    public static void launchBrowser(String browser) {

        try {

            WebDriver webDriver;
            String normalizedBrowser = browser.toLowerCase().trim();
            boolean gridEnabled = ConfigReader.isGridEnabled();
            String gridUrl = ConfigReader.getGridUrl();
            boolean headless = ConfigReader.isHeadless();

            switch (normalizedBrowser) {

                case "chrome":

                    ChromeOptions chromeOptions = buildChromeOptions(headless);
                    webDriver = gridEnabled
                            ? new RemoteWebDriver(new URL(gridUrl), chromeOptions)
                            : createLocalChromeDriver(chromeOptions);

                    break;

                case "firefox":

                    FirefoxOptions firefoxOptions = buildFirefoxOptions(headless);
                    webDriver = gridEnabled
                            ? new RemoteWebDriver(new URL(gridUrl), firefoxOptions)
                            : createLocalFirefoxDriver(firefoxOptions);

                    break;

                case "edge":

                    EdgeOptions edgeOptions = buildEdgeOptions(headless);
                    webDriver = gridEnabled
                            ? new RemoteWebDriver(new URL(gridUrl), edgeOptions)
                            : createLocalEdgeDriver(edgeOptions);

                    break;

                default:

                    throw new IllegalArgumentException(
                            "Invalid browser: " + browser
                    );
            }

            applyTimeouts(webDriver);

            driver.set(webDriver);

            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "BROWSER STARTED: "
                            + normalizedBrowser
            );

            System.out.println(
                    "Execution Mode: "
                            + (gridEnabled ? "GRID" : "LOCAL")
                            + " | URL: "
                            + (gridEnabled ? gridUrl : "N/A")
            );

            System.out.println(
                    "THREAD: "
                            + Thread.currentThread().threadId()
            );

            System.out.println(
                    "=============================================="
            );

        } catch (MalformedURLException e) {

            throw new RuntimeException(
                    "Invalid Selenium Grid URL: "
                            + ConfigReader.getGridUrl(),
                    e
            );
        }
    }

    private static ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
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
        // Selenium 4.6+ bundles Selenium Manager which auto-detects the locally
        // installed Edge version and resolves msedgedriver without any network call
        // to msedgedriver.azureedge.net — WebDriverManager is intentionally skipped here.
        return new EdgeDriver(options);
    }

    private static void applyTimeouts(WebDriver webDriver) {
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getImplicitWaitTimeout()));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getPageLoadTimeout()));
        webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(ConfigReader.getScriptTimeout()));
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

            System.out.println(
                    "Browser closed | Thread: "
                            + Thread.currentThread().threadId()
            );
        }
    }
}