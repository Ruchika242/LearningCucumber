package hooks;

import drivermanager.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utilities.BrowserContext;
import utilities.ConfigReader;
import utilities.ScreenshotUtil;
import utilities.ScreenshotUtil.ScreenshotCapture;

public class Hooks {

    @Before
    public void setup(Scenario scenario) {

        String browser = BrowserContext.getBrowser();

        System.out.println("==============================================");
        System.out.println("Scenario Started : " + scenario.getName());
        System.out.println("Browser          : " + browser);
        System.out.println("Thread           : " + Thread.currentThread().threadId());
        System.out.println("==============================================");

        DriverManager.launchBrowser(browser);
    }

    @After
    public void tearDown(Scenario scenario) {

        try {

            boolean isFailed = scenario.isFailed();
            boolean captureOnFail = isFailed && ConfigReader.isScreenshotOnFailureEnabled();
            boolean captureOnPass = !isFailed && ConfigReader.isScreenshotOnPassEnabled();

            if (captureOnFail || captureOnPass) {
                takeAndAttachScreenshot(scenario);
            }

        } catch (Exception e) {

            System.out.println("Unable to capture screenshot: " + e.getMessage());

        } finally {

            DriverManager.quitDriver();
            System.out.println("Scenario Completed: " + scenario.getName());
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private static void takeAndAttachScreenshot(Scenario scenario) {

        String browser = BrowserContext.getBrowserSafe();

        // Save to target/screenshots/<browser>/<scenario>_<ts>_T<thread>.png
        ScreenshotCapture capture = ScreenshotUtil.captureScreenshot(scenario.getName(), browser);

        if (capture == null) {
            System.out.println("Screenshot capture returned null – driver may have quit already.");
            return;
        }

        System.out.println("Screenshot saved : " + capture.filePath());

        // Also embed bytes into Cucumber report if attachment is enabled
        if (ConfigReader.isScreenshotAttachmentEnabled()) {
            scenario.attach(capture.bytes(), "image/png", "Screenshot - " + scenario.getName());
        }
    }
}