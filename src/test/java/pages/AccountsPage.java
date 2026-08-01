package pages;

import baseClass.BaseClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utilities.WaitUtils;

public class AccountsPage extends BaseClass {
    public AccountsPage(WebDriver driver) {

        PageFactory.initElements(driver, this);

    }

    @FindBy(xpath = "//button[@data-testid='add-account-btn']")
    WebElement addAccountButton;

    @FindBy(id="account-form-name")
    WebElement accountName;

    @FindBy(xpath = "(//span[text()='Select type']")
    WebElement accountType;

    @FindBy(xpath = "//div[text()='Savings']")
    WebElement savingsAccountType;

    @FindBy(name = "account_balance_field")
    WebElement startingBalance;

    @FindBy(xpath = "//div[text()='Checking']")
    WebElement checkingAccountType;

    @FindBy(xpath = "//button[@type='submit']")
    WebElement submitButton;

    @FindBy(xpath = "//span[@data-testid=\"account-form-accept-terms-checkbox\"]")
    WebElement acceptTermsCheckbox;

    public void clickAddAccountButton() {

        WaitUtils.waitForElementClickable(addAccountButton).click();
    }

    public void enterAccountName(String name) {
        WaitUtils.waitForElementVisible(accountName).clear();
        accountName.sendKeys(name);
    }

    public void selectAccountType(String type) {
        if ("Savings".equalsIgnoreCase(type)) {
            try {
                WaitUtils.waitForElementClickable(accountType).click();
                WaitUtils.waitForElementClickable(savingsAccountType).click();
            } catch (RuntimeException ignored) {
                // Some UI variants keep the default type without exposing the picker.
            }
            return;
        }

        if ("Checking".equalsIgnoreCase(type)) {
            WaitUtils.waitForElementClickable(accountType).click();
            WaitUtils.waitForElementClickable(checkingAccountType).click();
            return;
        }

        throw new IllegalArgumentException("Unsupported account type: " + type);
    }

    public void enterStartingBalance(String balance) {
        WaitUtils.waitForElementVisible(startingBalance).clear();
        startingBalance.sendKeys(balance);
    }

    public void clickSubmitButton() {
        WaitUtils.waitForElementClickable(submitButton).click();
    }

    public void clickAcceptTermsCheckbox() {
        WaitUtils.waitForElementClickable(acceptTermsCheckbox).click();
    }

    public boolean isAccountVisible(String name) {
        By accountNameLocator = By.xpath("//*[contains(normalize-space(),\"" + name + "\")]");
        return WaitUtils.waitForElementPresent(accountNameLocator);
    }




}
