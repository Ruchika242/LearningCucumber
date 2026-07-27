package pages;

import baseClass.BaseClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utilities.WaitUtils;


public class LoginPage {

    private WebDriver driver;


    public LoginPage(WebDriver driver) {

        this.driver = driver;

        PageFactory.initElements(driver, this);

    }


    @FindBy(xpath = "//input[@id='login-username']")
    private WebElement usernameField;


    @FindBy(xpath = "//input[@id='login-password']")
    private WebElement passwordField;


    @FindBy(xpath = "//button[@type='submit']")
    private WebElement loginButton;


    public void enterUsername(String username) {

        WaitUtils
                .waitForElementVisible(usernameField)
                .clear();

        usernameField.sendKeys(username);

    }



    public void enterPassword(String password) {

        WaitUtils
                .waitForElementVisible(passwordField)
                .clear();

        passwordField.sendKeys(password);

    }



    public void clickLoginButton() {

        WaitUtils
                .waitForElementClickable(loginButton)
                .click();

    }


    public DashboardPage login(String username, String password) {

        enterUsername(username);

        enterPassword(password);

        clickLoginButton();

        return new DashboardPage(driver);

    }

}