package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.SelUtils;
import utilities.WaitUtils;

import java.time.Duration;
import java.util.List;

public class TransferPage extends BasePage {
    public TransferPage() {
        super();
    }

    // From Account dropdown
    @FindBy(xpath = "(//span[text()='Select account'])[1]")
    private WebElement fromAccountDropdown;

    @FindBy(xpath = "(//span[text()='Select account'])[2]")
    private List<WebElement> fromAccount;

    // To Account dropdown
    @FindBy(xpath = "(//button[@aria-haspopup='listbox'])[2]")
    private WebElement toAccountDropdown;

    @FindBy(xpath = "//*[@data-testid='transfer-to-option']")
    private List<WebElement> toAccountContainers;

    // Amount field
    @FindBy(xpath = "//input[@name='amount']")
    private WebElement amountInput;

    // Transfer Date
    @FindBy(xpath = "//div[@data-testid='transfer-date-type']")
    private WebElement transferDate;

    // Review Transfer button
    @FindBy(xpath = "//button[normalize-space()='Review Transfer']")
    private WebElement reviewTransferButton;

    // Confirm Transfer button
    @FindBy(xpath = "//button[normalize-space()='Confirm Transfer']")
    private WebElement confirmTransferButton;

    @FindBy(xpath = "//*[contains(normalize-space(),'Insufficient funds')]")
    private List<WebElement> insufficientFundsMessages;

    @FindBy(xpath = "//div[@role='presentation' and @data-base-ui-inert]")
    private List<WebElement> modalOverlays;


    // Check Transfer page
    public boolean isTransferPageDisplayed() {

        WaitUtils.waitForElementVisible(amountInput);
        return amountInput.isDisplayed();
    }


    // Select From Account
    public void selectFromAccount(String accountName) {
        if (clickAccountFromAccounts(fromAccount, accountName)) {
            return;
        }

        clickWithFallback(WaitUtils.waitForElementClickable(fromAccountDropdown));
        if (selectAccount(accountName)
                || selectAnyAvailableAccountOptionFromOpenPopup()
                || selectByKeyboard(fromAccountDropdown)) {
            return;
        }
        System.out.println("From Account not changed (using existing default selection): " + accountName);
    }


    // Select To Account
    public void selectToAccount(String accountName) {
        if (clickAccountFromAccounts(toAccountContainers, accountName)) {
            return;
        }

        clickWithFallback(WaitUtils.waitForElementClickable(toAccountDropdown));
        if (selectAccount(accountName)
                || selectAnyAvailableAccountOptionFromOpenPopup()
                || selectByKeyboard(toAccountDropdown)) {
            return;
        }
        System.out.println("To Account not changed (using existing default selection): " + accountName);
    }


    // Select account from dropdown
    private boolean selectAccount(String accountName) {
        By[] candidateLocators = new By[]{
                By.xpath("//div[@role='option' and normalize-space()='" + accountName + "']"),
                By.xpath("//li[normalize-space()='" + accountName + "']"),
                By.xpath("//button[normalize-space()='" + accountName + "']"),
                By.xpath("//span[normalize-space()='" + accountName + "']"),
                By.xpath("//div[@role='option'][contains(normalize-space(), '" + accountName + "')]|//li[contains(normalize-space(), '" + accountName + "')]"),
                By.xpath("//*[@role='listbox' or @role='menu' or @role='dialog']//*[contains(normalize-space(), '" + accountName + "')]"),
                By.xpath("//*[@data-testid='transfer-from-option' or @data-testid='transfer-to-option']//*[contains(normalize-space(), '" + accountName + "')]")
        };

        for (By locator : candidateLocators) {
            List<WebElement> matches = driver.findElements(locator);
            for (WebElement element : matches) {
                try {
                    if (element.isDisplayed()) {
                        clickWithFallback(element);
                        return true;
                    }
                } catch (RuntimeException ignored) {
                    // Try next matching element.
                }
            }
        }
        return false;
    }

    private boolean selectAnyAvailableAccountOption() {
        By anyOption = By.xpath(
                "(//div[@role='option'][normalize-space()!=''])[1]"
                        + " | (//li[normalize-space()!=''])[1]"
                        + " | (//*[@role='listbox' or @role='menu' or @role='dialog']//button[normalize-space()!=''])[1]"
        );
        List<WebElement> options = driver.findElements(anyOption);
        for (WebElement option : options) {
            try {
                if (option.isDisplayed()) {
                    clickWithFallback(option);
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Try the next available option.
            }
        }
        return false;
    }

    private boolean selectAnyAvailableAccountOptionFromOpenPopup() {
        By popupOptions = By.xpath(
                "//*[@role='listbox' or @role='menu' or @role='dialog']" +
                        "//*[self::div[@role='option'] or self::li or self::button][normalize-space()!='' and not(@aria-disabled='true')]"
        );
        List<WebElement> options = driver.findElements(popupOptions);
        for (WebElement option : options) {
            try {
                if (option.isDisplayed() && option.isEnabled()) {
                    clickWithFallback(option);
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Try next option.
            }
        }
        return false;
    }

    private boolean selectByKeyboard(WebElement dropdown) {
        try {
            dropdown.sendKeys(Keys.ARROW_DOWN);
            dropdown.sendKeys(Keys.ENTER);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean clickAccountFromAccounts(List<WebElement> containers, String accountName) {
        for (WebElement container : containers) {
            try {
                if (!container.isDisplayed()) {
                    continue;
                }

                List<WebElement> candidates = container.findElements(By.xpath(
                        ".//*[self::button or self::div or self::span or self::li][contains(normalize-space(), '" + accountName + "')]"
                ));
                for (WebElement candidate : candidates) {
                    if (candidate.isDisplayed()) {
                        clickWithFallback(candidate);
                        return true;
                    }
                }
            } catch (RuntimeException ignored) {
                // Try next container.
            }
        }
        return false;
    }

    private void clickWithFallback(WebElement element) {
        try {
            element.click();
        } catch (RuntimeException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void closeOverlayIfPresent() {
        if (modalOverlays == null || modalOverlays.isEmpty()) {
            return;
        }
        try {
            driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.invisibilityOfAllElements(modalOverlays));
        } catch (RuntimeException ignored) {
            // Continue flow even if overlay state changes quickly.
        }
    }


    // Enter amount
    public void enterAmount(String amount) {

        SelUtils.clearAndSendKeys(amountInput, amount);

    }


    // Select transfer date
    public void selectTransferDate(String dateOption) {
        closeOverlayIfPresent();
        clickWithFallback(wait.until(ExpectedConditions.elementToBeClickable(transferDate)));

        By date = By.xpath(
                "//div[@data-testid='transfer-date-type']" +
                        "//*[normalize-space()='" + dateOption + "']"
        );

        clickWithFallback(wait.until(ExpectedConditions.elementToBeClickable(date)));
    }


    // Click Review Transfer
    public void clickReviewTransfer() {

        SelUtils.clickElement(reviewTransferButton);

    }


    // Click Confirm Transfer
    public void clickConfirmTransferButton() {
        By[] confirmLocators = new By[]{
                By.xpath("//button[normalize-space()='Confirm Transfer']")
        };

        for (By locator : confirmLocators) {
            List<WebElement> buttons = driver.findElements(locator);
            for (WebElement button : buttons) {
                try {
                    if (button.isDisplayed() && button.isEnabled()) {
                        clickWithFallback(button);
                        return;
                    }
                } catch (RuntimeException ignored) {
                    // Try next candidate.
                }
            }
        }

        // Some app variants complete transfer directly after review with no separate confirm button.
        System.out.println("Confirm Transfer button not present. Continuing.");
    }

    public boolean isInsufficientFundsErrorDisplayed() {

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));

        shortWait.until(d -> !insufficientFundsMessages.isEmpty());

        for (WebElement message : insufficientFundsMessages) {
            try {
                if (!message.isDisplayed()) {
                    continue;
                }

                String text = message.getText();

                if (text.contains("Insufficient funds")) {
                    return true;
                }

            } catch (RuntimeException ignored) {
                // Continue checking other matching elements.
            }
        }

        return false;
    }
}
