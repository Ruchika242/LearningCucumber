package stepDefinations;

import context.TestContext;
import drivermanager.DriverManagerClass;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.DashboardPage;
import pages.LoginPage;
import utilities.ConfigReader;

import java.time.Duration;

public class LoginPageSteps {

    private final TestContext testContext;

    public LoginPageSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    private LoginPage getLoginPage() {
        if (testContext.getLoginPage() == null) {
            testContext.setLoginPage(new LoginPage());
        }
        return testContext.getLoginPage();
    }

    @When("User opens the application URL")
    public void openApplicationUrl() {
        DriverManagerClass.DriverManager.getDriver().get(ConfigReader.getBaseUrl());
    }

    @And("User logs in with configured credentials")
    public void loginWithConfiguredCredentials() {
        DashboardPage dashboardPage =
                getLoginPage().login(ConfigReader.getUsername(), ConfigReader.getPassword());

        // Save for reuse in next steps/classes within the same scenario.
        testContext.setDashboardPage(dashboardPage);
    }

    @And("User logs in with invalid credentials")
    public void loginWithInvalidCredentials() {
        getLoginPage().login(ConfigReader.getInvalidUsername(), ConfigReader.getInvalidPassword());
        // Invalid login should remain on login page; clear dashboard from context.
        testContext.setDashboardPage(null);
    }

    @Then("DashboardPage URL should be {string}")
    public void verifyDashboardUrl(String expectedUrl) {
        WebDriverWait wait = new WebDriverWait(
                DriverManagerClass.DriverManager.getDriver(),
                Duration.ofSeconds(ConfigReader.getExplicitWaitTimeout())
        );
        wait.until(ExpectedConditions.urlContains("dashboard"));
        Assertions.assertEquals(expectedUrl, DriverManagerClass.DriverManager.getDriver().getCurrentUrl());
    }

    @Then("LoginPage URL should be {string}")
    public void verifyLoginPageUrl(String expectedUrl) {
        Assertions.assertEquals(expectedUrl, DriverManagerClass.DriverManager.getDriver().getCurrentUrl());
    }
}