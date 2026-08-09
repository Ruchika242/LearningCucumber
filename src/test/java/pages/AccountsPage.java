package pages;

import baseClass.BaseClass;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.WaitUtils;

import java.time.Duration;
import java.util.List;

public class AccountsPage extends BaseClass {
    private final WebDriver driver;

    public AccountsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);

    }

    @FindBy(xpath = "//button[@data-testid='add-account-btn']")
    WebElement addAccountButton;

    @FindBy(id="account-form-name")
    WebElement accountName;

    @FindBy(xpath = "//span[text()='Select type']")
    WebElement accountType;

    @FindBy(xpath = "//div[text()='Savings']")
    WebElement savingsAccountType;

    @FindBy(xpath = "//div[text()='Checking']")
    WebElement checkingAccountType;

    @FindBy(name = "account_balance_field")
    WebElement startingBalance;

    @FindBy(xpath = "//span[@data-testid=\"account-form-accept-terms-checkbox\"]")
    WebElement acceptTermsCheckbox;

    @FindBy(xpath = "(//button[text()='Add Account'])[2]")
    WebElement AddAccountButton;



    public void clickAddAccountButton() {

        WaitUtils.waitForElementClickable(addAccountButton).click();
    }

    public void enterAccountName(String name) {
        WaitUtils.waitForElementVisible(accountName).clear();
        accountName.sendKeys(name);
    }

    public void selectAccountType(String type) {

        WaitUtils.waitForElementClickable(accountType).click();

        By optionLocator = By.xpath(
                "//div[normalize-space()='" + type + "']"
        );

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement option = wait.until(
                ExpectedConditions.elementToBeClickable(optionLocator)
        );

        option.click();
    }

    public void enterStartingBalance(String balance) {
        try {
            // Re-find the element to avoid stale element reference
            WebElement balanceField = new WebDriverWait(
                    driver, Duration.ofSeconds(10)
            ).until(ExpectedConditions.visibilityOfElementLocated(By.name("account_balance_field")));
            balanceField.clear();
            balanceField.sendKeys(balance);
        } catch (StaleElementReferenceException e) {
            // If element is stale, use JavaScript to set value
            WebElement balanceField = driver.findElement(By.name("account_balance_field"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].value='';", balanceField);
            ((JavascriptExecutor) driver).executeScript("arguments[0].value=arguments[1];", balanceField, balance);
        }
    }

    public void clickAcceptTermsCheckbox() {

        WaitUtils.waitForElementClickable(acceptTermsCheckbox).click();
    }

    public void clickOnAddAccountButton() {
        WaitUtils.waitForElementClickable(AddAccountButton).click();
    }



    public boolean isAccountVisible(String name) {
        By accountNameLocator = By.xpath("//*[contains(normalize-space(),\"" + name + "\")]");
        return WaitUtils.waitForElementPresent(accountNameLocator);
    }




}
