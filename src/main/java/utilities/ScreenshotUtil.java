package utilities;

import drivermanager.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtil() {
        // Utility class
    }

    /**
     * Captures a screenshot, saves it to
     * {@code <screenshotDir>/<browser>/<sanitizedName>_<timestamp>_T<threadId>.png},
     * and returns the raw bytes for Cucumber report attachment.
     *
     * @param scenarioName human-readable scenario name (used in the file name)
     * @param browser      browser name (chrome / firefox / edge) – used as a subfolder
     * @return {@link ScreenshotCapture} with the absolute file path and raw PNG bytes,
     *         or {@code null} if the driver is unavailable
     */
    public static ScreenshotCapture captureScreenshot(String scenarioName, String browser) {

        WebDriver driver;
        try {
            driver = DriverManager.getDriver();
        } catch (IllegalStateException ignored) {
            // Driver not initialised – nothing to capture.
            return null;
        }

        byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

        String safeScenario = sanitize(scenarioName);
        String safeBrowser  = sanitize(browser == null ? "unknown" : browser);
        String timestamp    = LocalDateTime.now().format(FILE_TS);
        long   threadId     = Thread.currentThread().threadId();
        String fileName     = safeScenario + "_" + timestamp + "_T" + threadId + ".png";

        // Organise by browser sub-folder: target/screenshots/chrome/...
        Path outputDir  = Path.of(ConfigReader.getScreenshotDirectory()).resolve(safeBrowser);
        Path outputFile = outputDir.resolve(fileName);

        try {
            Files.createDirectories(outputDir);
            Files.write(outputFile, bytes);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save screenshot to: " + outputFile, e);
        }

        return new ScreenshotCapture(outputFile.toAbsolutePath().toString(), bytes);
    }

    /** Backward-compatible overload without browser (uses "unknown" sub-folder). */
    public static ScreenshotCapture captureScreenshot(String scenarioName) {
        return captureScreenshot(scenarioName, null);
    }

    private static String sanitize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "scenario";
        }
        return value.trim()
                    .replaceAll("[^a-zA-Z0-9_-]", "_")
                    .replaceAll("_+", "_");
    }

    public record ScreenshotCapture(String filePath, byte[] bytes) {}
}