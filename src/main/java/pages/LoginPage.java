package pages;

import org.apache.commons.collections4.SetUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utilities.SelUtils;
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
        SelUtils.clearAndSendKeys(username, value);
    }

    public void enterPassword(String value) {
        SelUtils.clearAndSendKeys(password, value);
    }

    public DashboardPage clickLogin() {
        SelUtils.clickElement(loginButton);
        return new DashboardPage();
    }

    public DashboardPage login(String username, String password) {
        SelUtils.enterText(this.username, username);
        SelUtils.enterText(this.password, password);
        return clickLogin();
        }
}