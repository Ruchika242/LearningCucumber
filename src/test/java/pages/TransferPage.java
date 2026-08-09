package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TransferPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Headless UI listbox buttons
    private final By accountDropdownButtons =
            By.cssSelector("button[id*='headlessui-listbox-button']");

    // Visible Headless UI listbox options
    private final By visibleListboxOptions =
            By.cssSelector("[role='option']:not([aria-hidden='true'])");

    public TransferPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "transfer-amount")
    private WebElement amountTextbox;

    @FindBy(xpath = "//button[normalize-space()='Review Transfer']")
    private WebElement reviewTransferButton;

    @FindBy(xpath = "//button[normalize-space()='Confirm Transfer']")
    private WebElement confirmTransferButton;

    @FindBy(xpath = "//h1[contains(normalize-space(),'Transfer Successful')]")
    private WebElement transferSuccessfulMessageText;


    // =========================================================
    // FROM ACCOUNT
    // =========================================================

    public void selectFromAccount(String accountName) {

        openAccountDropdownByIndex(0);

        selectAccountOption(accountName);
    }


    // =========================================================
    // TO ACCOUNT
    // =========================================================

    public void selectToAccount(String accountName) {

        openAccountDropdownByIndex(1);

        selectAccountOption(accountName);
    }


    // =========================================================
    // OPEN ACCOUNT DROPDOWN
    // =========================================================

    private void openAccountDropdownByIndex(int index) {

        wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(
                        accountDropdownButtons,
                        index
                )
        );

        List<WebElement> dropdownButtons =
                driver.findElements(accountDropdownButtons);

        WebElement dropdownButton =
                dropdownButtons.get(index);

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        dropdownButton
                )
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                dropdownButton
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();",
                dropdownButton
        );

        // Make sure the dropdown is actually opened
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        visibleListboxOptions
                )
        );
    }


    // =========================================================
    // SELECT ACCOUNT OPTION
    // =========================================================

    private void selectAccountOption(String accountName) {

        List<WebElement> options = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        visibleListboxOptions
                )
        );

        for (WebElement option : options) {

            String optionText =
                    option.getText()
                            .replaceAll("\\s+", " ")
                            .trim();

            System.out.println(
                    "Available account option: " + optionText
            );

            /*
             * Example:
             *
             * Everyday Checking — $4,240.00
             * High-Yield Savings — $12,810.00
             * Pipeline — $10.00
             *
             * We only compare the account name portion.
             */
            if (optionText.startsWith(accountName)) {

                wait.until(
                        ExpectedConditions.elementToBeClickable(
                                option
                        )
                );

                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});",
                        option
                );

                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].click();",
                        option
                );

                return;
            }
        }

        throw new RuntimeException(
                "Account option not found: " + accountName
        );
    }


    // =========================================================
    // TRANSFER AMOUNT
    // =========================================================

    public void enterAmount(String amount) {

        WebElement amountField = wait.until(
                ExpectedConditions.visibilityOf(
                        amountTextbox
                )
        );

        amountField.clear();
        amountField.sendKeys(amount);
    }


    // =========================================================
    // REVIEW TRANSFER
    // =========================================================

    public void clickOnReviewTransferButton() {

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        reviewTransferButton
                )
        ).click();
    }


    // =========================================================
    // CONFIRM TRANSFER
    // =========================================================

    public void clickOnConfirmTransferButton() {

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        confirmTransferButton
                )
        ).click();
    }


    // =========================================================
    // SUCCESS MESSAGE
    // =========================================================

    public String getTransferSuccessfulMessageText() {

        return wait.until(
                ExpectedConditions.visibilityOf(
                        transferSuccessfulMessageText
                )
        ).getText();
    }
}