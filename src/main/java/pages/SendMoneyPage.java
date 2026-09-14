package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utilities.SelUtils;

public class SendMoneyPage extends BasePage {
    private static final String accountName = "High-Yield Savings";

    public SendMoneyPage() {
        super();
    }

    @FindBy(xpath = "//span[text()='Send Money']")
    private WebElement sendMoneyHeader;

    @FindBy(id = "send-from-trigger")
    private WebElement fromAccountDropdown;

    @FindBy(xpath = "//div[contains(normalize-space(), '" + accountName + "')]")
    private WebElement fromAccountOptions;

    @FindBy(xpath = "//button[@data-testid='add-payee-btn']")
    private WebElement addButton;

    @FindBy(id="add-payee-name")
    private WebElement payeeNameInput;

    @FindBy(id="add-payee-bank")
    private WebElement payeeBankInput;

    @FindBy(xpath = "//input[@placeholder='9-digit routing number']")
    private WebElement routingNumberInput;

    @FindBy(xpath = "//input[@placeholder='8–17 digits']")
    private WebElement accountNumberInput;

    @FindBy(xpath = "//button[text()='Add Payee']")
    private WebElement addPayeeButton;

    @FindBy(id="send-amount")
    private WebElement amountInput;

    @FindBy(xpath = "//button[text()='Review & Send']")
    private WebElement reviewAndSendButton;

    @FindBy(xpath = "//button[text()='Confirm & Send']")
    private WebElement confirmAndSendButton;

    public void clickSendMoneyHeader() {
        SelUtils.clickElement(sendMoneyHeader);
    }

    public void clickFromAccountDropdown() {
        SelUtils.clickElement(fromAccountDropdown);
    }

    public void clickFromAccountOptions(String accountName) {
        SelUtils.clickElement(fromAccountOptions);

    }

    public void clickAddButton() {
        SelUtils.clickElement(addButton);
    }

    public void fillPayeeDetails(String payeeName, String payeeBank, String routingNumber, String accountNumber) {
        SelUtils.sendKeys(payeeNameInput, payeeName);
        SelUtils.sendKeys(payeeBankInput, payeeBank);
        SelUtils.sendKeys(routingNumberInput, routingNumber);
        SelUtils.sendKeys(accountNumberInput, accountNumber);
    }

    public void clickAddPayeeButton() {
        SelUtils.clickElement(addPayeeButton);
    }

    public void enterAmount(String amount) {
        SelUtils.sendKeys(amountInput, amount);
    }

    public  void clickReviewAndSendButton() {
        SelUtils.clickElement(reviewAndSendButton);
    }

    public  void clickConfirmAndSendButton() {
        SelUtils.clickElement(confirmAndSendButton);
    }





}
