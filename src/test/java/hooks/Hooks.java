package hooks;

import drivermanager.DriverManagerClass;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.ConfigReader;
import utilities.ScreenshotUtil;

public class Hooks {

    private static final Logger LOGGER = LogManager.getLogger(Hooks.class);

    @Before
    public void setUp(Scenario scenario) {
        LOGGER.info(
                "Starting scenario: {} | env={} | browser={} | headless={}",
                scenario.getName(),
                ConfigReader.getExecutionEnvironment(),
                ConfigReader.getBrowser(),
                ConfigReader.isHeadless()
        );

        DriverManagerClass.DriverManager.initializeDriver(ConfigReader.getBrowser());
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            boolean shouldCapture = scenario.isFailed()
                    ? ConfigReader.isScreenshotOnFailureEnabled()
                    : false;

            if (shouldCapture) {
                ScreenshotUtil.ScreenshotCapture capture = ScreenshotUtil.captureScreenshot(scenario.getName());
                if (capture != null) {
                    LOGGER.info("Screenshot saved: {}", capture.filePath());

                    if (ConfigReader.isScreenshotAttachmentEnabled()) {
                        scenario.attach(capture.bytes(), "image/png", "final-state");
                    }
                }
            }
        } finally {
            DriverManagerClass.DriverManager.quitDriver();
            LOGGER.info("Finished scenario: {} | status={}", scenario.getName(), scenario.getStatus());
        }
    }
}
