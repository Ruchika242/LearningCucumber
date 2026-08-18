package utilities;

import drivermanager.DriverManagerClass;
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

    public static ScreenshotCapture captureScreenshot(String scenarioName) {
        WebDriver driver = DriverManagerClass.DriverManager.getDriver();
        if (driver == null) {
            return null;
        }

        byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        String fileName = sanitizeScenarioName(scenarioName)
                + "_"
                + LocalDateTime.now().format(FILE_TS)
                + "_T"
                + Thread.currentThread().getId()
                + ".png";

        Path outputDirectory = Path.of(ConfigReader.getScreenshotDirectory());
        Path outputFile = outputDirectory.resolve(fileName);

        try {
            Files.createDirectories(outputDirectory);
            Files.write(outputFile, bytes);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save screenshot: " + outputFile, e);
        }

        return new ScreenshotCapture(outputFile.toString(), bytes);
    }

    private static String sanitizeScenarioName(String scenarioName) {
        if (scenarioName == null || scenarioName.trim().isEmpty()) {
            return "scenario";
        }

        return scenarioName
                .trim()
                .replaceAll("[^a-zA-Z0-9_-]", "_")
                .replaceAll("_+", "_");
    }

    public record ScreenshotCapture(String filePath, byte[] bytes) {
    }
}