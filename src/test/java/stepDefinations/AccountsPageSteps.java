package stepDefinations;

import context.TestContext;
import drivermanager.DriverManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import pages.AccountsPage;
import utilities.ConfigReader;

import java.net.URI;

public class AccountsPageSteps {

    private final TestContext testContext;

    SoftAssert softAssert = new SoftAssert();

    public AccountsPageSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @When("Navigate to Accounts")
    public void navigateToAccounts() {
        DriverManager.getDriver().get(getAccountsUrl());
        testContext.setAccountsPage(new AccountsPage());
    }

    @Then("Locate the Accounts page")
    public void locateTheAccountsPage() {
        softAssert.assertTrue(
                accountsPage().isAccountsPageDisplayed(),
                "Accounts page is not visible." );
                softAssert.assertAll();

    }

    @And("Assert that the Accounts page is displayed correctly")
    public void assertThatTheAccountsPageIsDisplayedCorrectly() {
        softAssert.assertTrue(accountsPage().isAccountsPageDisplayed(), "Accounts page validation failed.");
        softAssert.assertAll();
    }

    @And("Click on Add New Account button")
    public void clickOnAddNewAccountButton() {
        accountsPage().clickAddNewAccountButton();
    }

    @Then("Fill Account Name, Account Type and Starting Balance fields")
    public void fillAccountNameAccountTypeAndStartingBalanceFields() {
        AccountsPage page = accountsPage();
        page.enterAccountName("Automation Account");
        page.selectAccountType("Checking");
        page.enterStartingBalance("5000");
    }

    @And("select the check box for {string}")
    public void selectTheCheckBoxFor(String termsLabel) {
        // Keep this simple: the user intent is to tick the terms checkbox.
        accountsPage().clickAcceptTermsCheckbox();
    }

    @And("Click on \"Add Account\" button")
    public void clickOnAddAccountButton() {
        accountsPage().clickAddAccountSubmitButton();
    }

    private AccountsPage accountsPage() {
        return testContext.getOrCreateAccountsPage();
    }

    // Account number validation steps are implemented in TransferPageSteps as requested.

    private String getAccountsUrl() {
        URI baseUri = URI.create(ConfigReader.getBaseUrl());
        return baseUri.getScheme() + "://" + baseUri.getAuthority() + "/bank/accounts";
    }
}
