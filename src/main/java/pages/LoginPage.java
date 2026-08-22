package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utilities.WaitUtils;

public class LoginPage extends BasePage {

    public LoginPage() {
        super();
    }

    @FindBy(id = "login-username")
    private WebElement username;

    @FindBy(id = "login-password")
    private WebElement password;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    public void enterUsername(String value) {
        WaitUtils.waitForElementVisible(username).clear();
        username.sendKeys(value);
    }

    public void enterPassword(String value) {
        WaitUtils.waitForElementVisible(password).clear();
        password.sendKeys(value);
    }

    public DashboardPage clickLogin() {
        WaitUtils.waitForElementClickable(loginButton).click();
        return new DashboardPage();
    }

    public DashboardPage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        return clickLogin();
    }
}