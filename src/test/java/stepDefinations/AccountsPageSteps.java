package stepDefinations;

import context.TestContext;
import drivermanager.DriverManagerClass;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.AccountsPage;
import utilities.ConfigReader;

import java.net.URI;

public class AccountsPageSteps {

    private final TestContext testContext;

    public AccountsPageSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @When("Navigate to Accounts")
    public void navigateToAccounts() {
        URI baseUri = URI.create(ConfigReader.getBaseUrl());
        String accountsUrl = baseUri.getScheme() + "://" + baseUri.getAuthority() + "/bank/accounts";
        DriverManagerClass.getDriver().get(accountsUrl);
        testContext.setAccountsPage(new AccountsPage());
    }

    @Then("Locate the Accounts page")
    public void locateTheAccountsPage() {
        Assert.assertTrue(getAccountsPage().isAccountsPageDisplayed(), "Accounts page is not visible.");
    }

    @And("Assert that the Accounts page is displayed correctly")
    public void assertThatTheAccountsPageIsDisplayedCorrectly() {
        Assert.assertTrue(getAccountsPage().isAccountsPageDisplayed(), "Accounts page validation failed.");
    }

    @And("Click on Add New Account button")
    public void clickOnAddNewAccountButton() {
        getAccountsPage().clickAddNewAccountButton();
    }

    @Then("Fill Account Name, Account Type and Starting Balance fields")
    public void fillAccountNameAccountTypeAndStartingBalanceFields() {
        AccountsPage accountsPage = getAccountsPage();
        accountsPage.enterAccountName("Automation Account");
        accountsPage.selectAccountType("Checking");
        accountsPage.enterStartingBalance("5000");
    }

    @And("select the check box for {string}")
    public void selectTheCheckBoxFor(String termsLabel) {
        Assert.assertEquals(termsLabel, "I accept the terms and conditions", "Unexpected checkbox label.");
        getAccountsPage().clickAcceptTermsCheckbox();
    }

    @And("Click on {string} button")
    public void clickOnButton(String buttonName) {
        Assert.assertEquals(buttonName, "Add Account", "Unsupported button for this step.");
        getAccountsPage().clickAddAccountSubmitButton();
    }

    private AccountsPage getAccountsPage() {
        return testContext.getOrCreateAccountsPage();
    }
}
