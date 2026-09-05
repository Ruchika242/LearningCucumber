package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class TransferPage extends BasePage {

    public TransferPage() {
        super();
    }

    // From Account dropdown
    @FindBy(xpath = "(//button[@aria-haspopup='listbox'])[1]")
    private WebElement fromAccountDropdown;

    // To Account dropdown
    @FindBy(xpath = "(//button[@aria-haspopup='listbox'])[2]")
    private WebElement toAccountDropdown;

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


    // Check Transfer page
    public boolean isTransferPageDisplayed() {

        wait.until(ExpectedConditions.visibilityOf(amountInput));

        return amountInput.isDisplayed();
    }


    // Select From Account
    public void selectFromAccount(String accountName) {

        wait.until(ExpectedConditions.elementToBeClickable(fromAccountDropdown))
                .click();

        selectAccount(accountName);
    }


    // Select To Account
    public void selectToAccount(String accountName) {

        wait.until(ExpectedConditions.elementToBeClickable(toAccountDropdown))
                .click();

        selectAccount(accountName);
    }


    // Select account from dropdown
    private void selectAccount(String accountName) {

        By account = By.xpath(
                "//div[@role='option' and normalize-space()='" + accountName + "']"
        );

        wait.until(ExpectedConditions.elementToBeClickable(account))
                .click();
    }


    // Enter amount
    public void enterAmount(String amount) {

        wait.until(ExpectedConditions.visibilityOf(amountInput));

        amountInput.clear();
        amountInput.sendKeys(amount);
    }


    // Select transfer date
    public void selectTransferDate(String dateOption) {

        wait.until(ExpectedConditions.elementToBeClickable(transferDate))
                .click();

        By date = By.xpath(
                "//div[@data-testid='transfer-date-type']" +
                        "//*[normalize-space()='" + dateOption + "']"
        );

        wait.until(ExpectedConditions.elementToBeClickable(date))
                .click();
    }


    // Click Review Transfer
    public void clickReviewTransfer() {

        wait.until(ExpectedConditions.elementToBeClickable(reviewTransferButton))
                .click();
    }


    // Click Confirm Transfer
    public void clickConfirmTransferButton() {

        wait.until(ExpectedConditions.elementToBeClickable(confirmTransferButton))
                .click();
    }
}

