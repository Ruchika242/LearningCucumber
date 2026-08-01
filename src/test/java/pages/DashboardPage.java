package pages;


import baseClass.BaseClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.WaitUtils;

import java.time.Duration;


public class DashboardPage extends BaseClass {


    private final WebDriver driver;


    public DashboardPage(WebDriver driver) {

        this.driver = driver;

        PageFactory.initElements(driver, this);

    }

    @FindBy(xpath = "//span[text()='Accounts']")
    WebElement accountsOption;


    public String getCurrentURL(){

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(10)
                );


        wait.until(
                ExpectedConditions.urlContains("dashboard")
        );


        return driver.getCurrentUrl();

    }

    public AccountsPage clickOnAccountsOption() {
        WaitUtils.waitForElementClickable(accountsOption).click();
        return new AccountsPage(driver);
    }

}


