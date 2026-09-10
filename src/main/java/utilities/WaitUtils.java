package utilities;

import drivermanager.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WaitUtils {

    private WaitUtils() {
        // Utility class
    }

    private static WebDriverWait getWait() {
        WebDriver driver = DriverManager.getDriver();
        return new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getExplicitWaitTimeout()));
    }




    public static WebElement waitForElementClickable(WebElement element) {
        return getWait().until(ExpectedConditions.elementToBeClickable(element));
    }

    public static String waitForPageSource() {
        return getWait().until(driver -> driver.getPageSource());
    }

    public static List<WebElement> waitForElementsPresent(By element){
        return getWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(element));
    }

    public static boolean waitForElementPresent(By element) {
        return getWait().until(ExpectedConditions.presenceOfElementLocated(element)).isDisplayed();
    }

    public static boolean waitForUrlContains(String partialUrl) {
        return getWait().until(ExpectedConditions.urlContains(partialUrl));
    }

    public static boolean waitForElementVisible(WebElement element) {
        return getWait().until(ExpectedConditions.visibilityOf(element)).isDisplayed();
    }

    public static boolean waitForElementsVisible(List<WebElement> elements) {
        return getWait().until(ExpectedConditions.visibilityOfAllElements(elements)).size() > 0;
    }

    public static boolean waitForElementNotVisible(WebElement element) {
        return getWait().until(ExpectedConditions.invisibilityOf(element));
    }

    public static boolean waitForElementNotPresent(WebElement element) {
        return getWait().until(ExpectedConditions.not(ExpectedConditions.visibilityOf(element)));
    }
}


