package stepDefinations;

import context.TestContext;
import drivermanager.DriverManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.asserts.SoftAssert;
import pages.DashboardPage;
import utilities.ConfigReader;
import utilities.WaitUtils;
import org.testng.Assert;

public class LoginPageSteps {

    SoftAssert softAssert = new SoftAssert();

    private final TestContext testContext;

    public LoginPageSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @When("User opens the application URL")
    public void openApplicationUrl() {
        DriverManager.getDriver().get(ConfigReader.getBaseUrl());
    }

    @And("User logs in with configured credentials")
    public void loginWithConfiguredCredentials() {
        DashboardPage dashboardPage =
                testContext.getOrCreateLoginPage().login(ConfigReader.getUsername(), ConfigReader.getPassword());

        WaitUtils.waitForUrlContains("/bank/dashboard");

        // Save for reuse in next steps/classes within the same scenario.
        testContext.setDashboardPage(dashboardPage);
    }

    @And("User logs in with invalid credentials")
    public void loginWithInvalidCredentials() {
        testContext.getOrCreateLoginPage().login(ConfigReader.getInvalidUsername(), ConfigReader.getInvalidPassword());
        // Invalid login should remain on login page; clear dashboard from context.
        testContext.setDashboardPage(null);
    }

    @Then("DashboardPage URL should be {string}")
    public void verifyDashboardUrl(String expectedUrl) {
        WaitUtils.waitForUrlContains("dashboard");
        softAssert.assertEquals(expectedUrl, DriverManager.getDriver().getCurrentUrl());

        softAssert.assertAll();
    }

    @Then("LoginPage URL should be {string}")
    public void verifyLoginPageUrl(String expectedUrl) {
        softAssert.assertEquals(expectedUrl, DriverManager.getDriver().getCurrentUrl());
        softAssert.assertAll();
    }
}