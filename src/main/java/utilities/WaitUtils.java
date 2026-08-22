package utilities;

import drivermanager.DriverManagerClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtils {

    private WaitUtils() {
        // Utility class
    }

    private static WebDriverWait getWait() {
        WebDriver driver = drivermanager.DriverManagerClass.getDriver();
        return new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getExplicitWaitTimeout()));
    }


    public static WebElement waitForElementVisible(WebElement element) {
        return getWait().until(ExpectedConditions.visibilityOf(element));
    }

    public static WebElement waitForElementClickable(WebElement element) {
        return getWait().until(ExpectedConditions.elementToBeClickable(element));
    }

    public static boolean waitForElementPresent(By element) {
        return getWait().until(ExpectedConditions.presenceOfElementLocated(element)).isDisplayed();
    }

    public static boolean waitForUrlContains(String partialUrl) {
        return getWait().until(ExpectedConditions.urlContains(partialUrl));
    }
}


