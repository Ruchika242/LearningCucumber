package utilities;

import drivermanager.DriverManager;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.BasePage;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class SelUtils extends BasePage implements WebDriver, JavascriptExecutor {

    private SelUtils() {
        // Utility class
    }

    public static void clickElement(WebElement element) {
        WaitUtils.waitForElementClickable(element).click();
    }

    @Override
    public void get(String url) {
        WaitUtils.waitForUrlContains(url);

    }

    public static void sendKeys(WebElement element, String text) {
        WaitUtils.waitForElementVisible(element);
        element.clear();
        element.sendKeys(text);
    }

    public static void clearAndSendKeys(WebElement element, String text) {
        WaitUtils.waitForElementVisible(element);
        element.clear();
        element.sendKeys(text);
    }

    public static void enterText(WebElement element, String text) {
        WaitUtils.waitForElementVisible(element);
        element.sendKeys(text);
    }

    @Override
    public @Nullable String getCurrentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }

    @Override
    public @Nullable String getTitle() {
        return DriverManager.getDriver().getTitle();
    }

    @Override
    public List<WebElement> findElements(By element) {
        return WaitUtils.waitForElementsPresent(element);
    }

    @Override
    public WebElement findElement(By element) {
        return WaitUtils.waitForElementsPresent(element).get(0);
    }

    @Override
    public @Nullable String getPageSource() {
        return WaitUtils.waitForPageSource();
    }

    @Override
    public void close() {
        DriverManager.getDriver().close();
    }

    @Override
    public void quit() {

    }

    @Override
    public Set<String> getWindowHandles() {
        return Set.of();
    }

    @Override
    public String getWindowHandle() {
        return "";
    }

    @Override
    public TargetLocator switchTo() {
        return null;
    }

    @Override
    public Navigation navigate() {
        return null;
    }

    @Override
    public Options manage() {
        return null;
    }


    @Override
    public @Nullable Object executeScript(String script, @Nullable Object... args) {
        return null;
    }

    @Override
    public @Nullable Object executeAsyncScript(String script, @Nullable Object... args) {
        return null;
    }
}
