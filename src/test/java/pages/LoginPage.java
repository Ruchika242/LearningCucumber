package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Page Object Model (POM) for the QAPlayground Login Page.
 * Contains web elements and action methods for login functionality.
 */
public class LoginPage {

    private WebDriver driver;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        // Initialize web elements using PageFactory
        PageFactory.initElements(driver, this);
    }

    // ========== Web Elements ==========

    @FindBy(xpath = "//input[@id='login-username']")
    private WebElement usernameField;

    @FindBy(xpath = "//input[@id='login-password']")
    private WebElement passwordField;

    @FindBy(xpath = "//button[@type='submit' or contains(text(),'Login')]")
    private WebElement loginButton;

    @FindBy(xpath = "//div[contains(@class,'rounded-2xl')]")
    private WebElement logoElement;


    // ========== Action Methods ==========

    /**
     * Enter username in the username field
     */
    public void enterUsername(String username) {
        usernameField.clear();
        usernameField.sendKeys(username);
    }

    /**
     * Enter password in the password field
     */
    public void enterPassword(String password) {
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    /**
     * Click the login button
     */
    public void clickLoginButton() {
        loginButton.click();
    }

    /**
     * Verify if the logo is displayed on the page
     */
    public boolean isLogoDisplayed() {
        return logoElement.isDisplayed();
    }

    /**
     * Get the current page title
     */
    public String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Get the current page URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Combined method to login with username and password
     */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }

}

