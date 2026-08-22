package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AccountsPage extends BasePage {
    private static final By ADD_ACCOUNT_BUTTON_BY = By.xpath("//button[@data-testid='add-account-btn']");
    private static final By ADD_ACCOUNT_BUTTON_ALT_BY = By.xpath("//button[normalize-space()='Add Account' or normalize-space()='Add New Account']");
    private static final By ADD_ACCOUNT_BUTTON_FUZZY_BY = By.xpath("//button[contains(normalize-space(),'Add') and contains(normalize-space(),'Account')]");
    private static final By ACCOUNTS_HEADER_BY = By.xpath("//span[normalize-space()='Accounts'] | //h1[normalize-space()='Accounts']");
    private static final By ACCOUNT_NAME_INPUT_BY = By.xpath("//input[@id='account-form-name']");
    private static final By ACCOUNT_NAME_INPUT_ALT_BY = By.cssSelector("input[data-testid='account-form-name-input']");
    private static final By ACCOUNT_TYPE_DROPDOWN_BY = By.xpath("//button[@id='account-form-type-trigger']");
    private static final By STARTING_BALANCE_INPUT_BY = By.xpath("//input[@name='account_balance_field']");
    private static final By ACCEPT_TERMS_CHECKBOX_BY = By.xpath("//span[@data-testid='account-form-accept-terms-checkbox']");
    private static final By ADD_ACCOUNT_SUBMIT_BY = By.xpath("(//button[text()='Add Account'])[2]");

    public AccountsPage() {
        super();
    }

    public boolean isAccountsPageDisplayed() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_HEADER_BY),
                    ExpectedConditions.visibilityOfElementLocated(ADD_ACCOUNT_BUTTON_BY),
                    ExpectedConditions.visibilityOfElementLocated(ADD_ACCOUNT_BUTTON_ALT_BY)
            ));
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public void clickAddNewAccountButton() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_HEADER_BY));

        if (isAccountFormVisible()) {
            return;
        }

        for (int attempt = 0; attempt < 4; attempt++) {
            if (clickAnyVisibleButton(ADD_ACCOUNT_BUTTON_BY) && waitForAccountFormVisible(Duration.ofSeconds(4))) {
                return;
            }
            if (clickAnyVisibleButton(ADD_ACCOUNT_BUTTON_ALT_BY) && waitForAccountFormVisible(Duration.ofSeconds(4))) {
                return;
            }
            if (clickAnyVisibleButton(ADD_ACCOUNT_BUTTON_FUZZY_BY) && waitForAccountFormVisible(Duration.ofSeconds(4))) {
                return;
            }
        }

        throw new IllegalStateException("Add Account form did not open after clicking Add New Account button.");
    }



    public void selectAccountType(String type) {
        if (!type.equals("Checking") && !type.equals("Savings") && !type.equals("Credit"))
            throw new IllegalArgumentException("Valid values: Checking, Savings, Credit");
        wait.until(ExpectedConditions.elementToBeClickable(ACCOUNT_TYPE_DROPDOWN_BY)).click();
        By optionBy = By.xpath("//div[@role='option' and (normalize-space()=\"" + type + "\" or .//span[normalize-space()=\"" + type + "\"]) ]");
        wait.until(ExpectedConditions.elementToBeClickable(optionBy)).click();
    }

    public void enterAccountName(String name) {
        WebElement accountName = waitForVisibleElement(ACCOUNT_NAME_INPUT_BY, ACCOUNT_NAME_INPUT_ALT_BY);
        accountName.clear();
        accountName.sendKeys(name);
    }

    public void enterStartingBalance(String balance) {
        WebElement startingBalance = wait.until(ExpectedConditions.visibilityOfElementLocated(STARTING_BALANCE_INPUT_BY));
        startingBalance.clear();
        startingBalance.sendKeys(balance);
    }

    public void clickAcceptTermsCheckbox() {
        wait.until(ExpectedConditions.elementToBeClickable(ACCEPT_TERMS_CHECKBOX_BY)).click();
    }

    public void clickAddAccountSubmitButton() {
        wait.until(ExpectedConditions.elementToBeClickable(ADD_ACCOUNT_SUBMIT_BY)).click();
    }

    private boolean isElementVisible(By locator) {
        try {
            return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean isAccountFormVisible() {
        return isElementVisible(ACCOUNT_NAME_INPUT_BY) || isElementVisible(ACCOUNT_NAME_INPUT_ALT_BY);
    }

    private boolean waitForAccountFormVisible(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(ACCOUNT_NAME_INPUT_BY),
                    ExpectedConditions.visibilityOfElementLocated(ACCOUNT_NAME_INPUT_ALT_BY)
            ));
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean clickAnyVisibleButton(By locator) {
        List<WebElement> buttons = driver.findElements(locator);
        for (WebElement button : buttons) {
            try {
                if (button.isDisplayed() && button.isEnabled()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", button);
                    clickElementWithFallback(button);
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Try the next matching element when one candidate is stale/not interactable.
            }
        }
        return false;
    }

    private WebElement waitForVisibleElement(By primary, By fallback) {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(primary),
                ExpectedConditions.visibilityOfElementLocated(fallback)
        ));

        if (isElementVisible(primary)) {
            return driver.findElement(primary);
        }
        return driver.findElement(fallback);
    }

    private void clickElementWithFallback(WebElement element) {
        try {
            element.click();
        } catch (RuntimeException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }




}
