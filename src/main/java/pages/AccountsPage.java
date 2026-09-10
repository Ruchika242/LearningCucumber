package pages;

import org.apache.commons.collections4.SetUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.BasePage;
import utilities.SelUtils;
import utilities.WaitUtils;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AccountsPage extends BasePage {

    private static final Pattern MASKED_ACCOUNT_PATTERN =
            Pattern.compile("(?s).*[\\*\\u2022]{4,}\\s*\\d{3,4}.*");

    public AccountsPage() {
        super();
    }

    // Add New Account button
    @FindBy(xpath = "//button[@data-testid='add-account-btn']")
    private WebElement addAccountButton;

    // Account Name
    @FindBy(xpath = "//input[@id='account-form-name']")
    private WebElement accountNameInput;

    // Account Type dropdown
    @FindBy(xpath = "//button[@id='account-form-type-trigger']")
    private WebElement accountTypeDropdown;

    // Account Type options
    @FindBy(xpath = "//div[@role='option']")
    private List<WebElement> accountTypeOptions;

    // Starting Balance
    @FindBy(xpath = "//input[@name='account_balance_field']")
    private WebElement startingBalanceInput;

    // Accept Terms checkbox
    @FindBy(xpath = "//span[@data-testid='account-form-accept-terms-checkbox']")
    private WebElement acceptTermsCheckbox;

    // Add Account button
    @FindBy(xpath = "(//button[text()='Add Account'])[2]")
    private WebElement addAccountButtonSubmit;

    // Account table rows
    @FindBy(xpath = "//table[@data-testid='accounts-table']//tbody/tr")
    private List<WebElement> accountRows;


    // Verify Accounts page is displayed
    public boolean isAccountsPageDisplayed() {
        return WaitUtils.waitForElementVisible(addAccountButton);
    }


    // Click Add New Account
    public void clickAddNewAccountButton() {
        SelUtils.clickElement(addAccountButton);
    }

    // Verify Account Number field is displayed
    public boolean isAccountNumberFieldDisplayed() {
        waitForAccountRowsToRender();
        for (WebElement row : accountRows) {
            String rowText = row.getText();
            if (isMaskedAccountText(rowText)) {
                return true;
            }
        }
        return false;
    }


    // Enter Account Name
    public void enterAccountName(String name) {
        SelUtils.sendKeys(accountNameInput, name);

    }


    // Select Account Type
    public void selectAccountType(String type) {

        SelUtils.clickElement(accountTypeDropdown);

        for (WebElement option : accountTypeOptions) {

            if (option.getText().trim().equals(type)) {
                option.click();
                break;
            }
        }
    }


    // Enter Starting Balance
    public void enterStartingBalance(String balance) {
        SelUtils.sendKeys(startingBalanceInput, balance);

    }


    // Accept Terms
    public void clickAcceptTermsCheckbox() {
        SelUtils.clickElement(acceptTermsCheckbox);
    }

    //Add Account
    public void clickAddAccountSubmitButton() {
        SelUtils.clickElement(addAccountButtonSubmit);
    }


    // Verify account number is masked
    public boolean isAccountNumberDisplayedAsMasked(String accountName) {

        waitForAccountRowsToRender();

        for (WebElement row : accountRows) {

            String rowText = row.getText();

            if (rowText.contains(accountName)) {

                if (isMaskedAccountText(rowText)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void waitForAccountRowsToRender() {

        WaitUtils.waitForElementsVisible(accountRows);
    }

    private boolean isMaskedAccountText(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return MASKED_ACCOUNT_PATTERN.matcher(value).matches();
    }


}


