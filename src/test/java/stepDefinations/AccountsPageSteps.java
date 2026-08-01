package stepDefinations;

import factory.DriverFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.AccountsPage;
import pages.DashboardPage;
import pages.LoginPage;

import java.time.Duration;

public class AccountsPageSteps {

    private DashboardPage dashboardPage;
    private AccountsPage accountsPage;

    private void initializeDashboardPage() {
        if (dashboardPage == null) {
            dashboardPage = new DashboardPage(DriverFactory.getDriver());
        }
    }

    private void initializeAccountsPage() {
        if (accountsPage == null) {
            accountsPage = new AccountsPage(DriverFactory.getDriver());
        }
    }

    private void ensureOnAccountsPage() {
        String currentUrl = DriverFactory.getDriver().getCurrentUrl();

        if (!currentUrl.contains("accounts")) {
            DashboardPage currentDashboardPage;

            if (currentUrl.contains("dashboard")) {
                currentDashboardPage = new DashboardPage(DriverFactory.getDriver());
            } else {
                LoginPage loginPage = new LoginPage(DriverFactory.getDriver());
                currentDashboardPage = loginPage.login("standard_user", "bank_sauce");
            }

            currentDashboardPage.clickOnAccountsOption();

            WebDriverWait wait = new WebDriverWait(
                    DriverFactory.getDriver(),
                    Duration.ofSeconds(10)
            );
            wait.until(ExpectedConditions.urlContains("accounts"));
        }

        initializeAccountsPage();
    }

    @Then("User clicks on Accounts button and should be redirected to Accounts page with URL {string}")
    public void userClicksOnAccountsButtonAndShouldBeRedirectedToAccountsPageWithURL(String expectedURL) {
        initializeDashboardPage();

        dashboardPage.clickOnAccountsOption();

        WebDriverWait wait = new WebDriverWait(
                DriverFactory.getDriver(),
                Duration.ofSeconds(10)
        );
        wait.until(ExpectedConditions.urlContains("accounts"));

        Assertions.assertEquals(
                expectedURL,
                DriverFactory.getDriver().getCurrentUrl()
        );

        initializeAccountsPage();
    }

    @When("User clicks on Add Account button")
    public void userClicksOnAddAccountButton() {
        ensureOnAccountsPage();
        accountsPage.clickAddAccountButton();
    }

    @And("User enters Account Name {string} and Account Type {string} and Account Balance {string}")
    public void userEntersAccountDetails(String accountName, String accountType, String accountBalance) {
        initializeAccountsPage();

        accountsPage.enterAccountName(accountName);
        accountsPage.selectAccountType(accountType);
        accountsPage.enterStartingBalance(accountBalance);
    }

    @And("User clicks on Submit button")
    public void userClicksOnSubmitButton() {
        initializeAccountsPage();
        accountsPage.clickSubmitButton();
    }

    @Then("User should see the new account {string} in the Accounts page")
    public void userShouldSeeTheNewAccountInTheAccountsPage(String accountName) {
        initializeAccountsPage();
        Assertions.assertTrue(accountsPage.isAccountVisible(accountName));
    }
}

