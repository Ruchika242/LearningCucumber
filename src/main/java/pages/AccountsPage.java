package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.BasePage;

import java.util.List;

public class AccountsPage extends BasePage {

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
        return wait.until(
                ExpectedConditions.visibilityOf(addAccountButton)
        ).isDisplayed();
    }


    // Click Add New Account
    public void clickAddNewAccountButton() {
        wait.until(
                ExpectedConditions.elementToBeClickable(addAccountButton)
        ).click();
    }

    // Verify Account Number field is displayed
    public boolean isAccountNumberFieldDisplayed() {
        for (WebElement row : accountRows) {
            String rowText = row.getText();
            if (rowText.matches("(?s).*\\*{4}\\d{4}.*")) {
                return true;
            }
        }
        return false;
    }


            // Enter Account Name
            public void enterAccountName(String name) {
                wait.until(
                        ExpectedConditions.visibilityOf(accountNameInput)
                ).sendKeys(name);
            }


            // Select Account Type
            public void selectAccountType(String type) {

                wait.until(
                        ExpectedConditions.elementToBeClickable(accountTypeDropdown)
                ).click();

                for (WebElement option : accountTypeOptions) {

                    if (option.getText().trim().equals(type)) {
                        option.click();
                        break;
                    }
                }
            }


            // Enter Starting Balance
            public void enterStartingBalance(String balance) {

                wait.until(
                        ExpectedConditions.visibilityOf(startingBalanceInput)
                ).sendKeys(balance);
            }


            // Accept Terms
            public void clickAcceptTermsCheckbox() {

                wait.until(
                        ExpectedConditions.elementToBeClickable(acceptTermsCheckbox)
                ).click();
            }


            // Click Add Account
            public void clickAddAccountSubmitButton() {

                wait.until(ExpectedConditions.elementToBeClickable(addAccountButtonSubmit)).click();
            }


            // Verify account number is masked
            public boolean isAccountNumberDisplayedAsMasked(String accountName) {

                for (WebElement row : accountRows) {

                    String rowText = row.getText();

                    if (rowText.contains(accountName)) {

                        // Example: ****4321
                        if (rowText.matches("(?s).*\\*{4}\\d{4}.*")) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }


