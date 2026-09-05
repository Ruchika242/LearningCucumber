package stepDefinations;

import context.TestContext;
import drivermanager.DriverManagerClass;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.TransferPage;
import utilities.ConfigReader;

import java.net.URI;

public class TransferPageSteps {
    private final TestContext testContext;

    public TransferPageSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @When("Navigate to Transfer")
    public void navigateToTransferFunds() {
        URI baseUri = URI.create(ConfigReader.getBaseUrl());
        String transferUrl = baseUri.getScheme() + "://" + baseUri.getAuthority() + "/bank/transfer";
        DriverManagerClass.getDriver().get(transferUrl);
        testContext.setTransferPage(new TransferPage());
    }

    @Then("Locate the Transfer page")
    public void locateTheTransferFundsPage() {
        Assert.assertTrue(
                testContext.getOrCreateTransferPage().isTransferPageDisplayed(),
                "Transfer page is not displayed"
        );
    }

    @And("Assert that the Transfer page is displayed correctly")
    public void assertThatTheTransferPageIsDisplayedCorrectly() {
        Assert.assertTrue(
                testContext.getOrCreateTransferPage().isTransferPageDisplayed(),
                "Transfer page is not displayed correctly"
        );
    }

    @And("Fill in the transfer details including From Account, To Account, Amount and Schedule Date")
    public void fillInTheTransferDetailsWithAndScheduleDate() {
        testContext.getOrCreateTransferPage().selectFromAccount("Everyday Checking");
        testContext.getOrCreateTransferPage().selectToAccount("Pipeline");
        testContext.getOrCreateTransferPage().enterAmount("100");
    }

    @And("Fill in the transfer details including From Account, To Account, Amount, and Schedule Date")
    public void fillInTheTransferDetailsWithCommaAndScheduleDate() {
        testContext.getOrCreateTransferPage().selectFromAccount("Everyday Checking");
        testContext.getOrCreateTransferPage().selectToAccount("Pipeline");
        testContext.getOrCreateTransferPage().enterAmount("100");
    }

    @And("Fill in the transfer details including From Account, To Account, Amount(greater than available balance), and Schedule Date")
    public void fillInTheTransferDetailsWithInvalidAmount() {
        testContext.getOrCreateTransferPage().selectFromAccount("Everyday Checking");
        testContext.getOrCreateTransferPage().selectToAccount("test1");
        testContext.getOrCreateTransferPage().enterAmount("4500");
    }

    @And("Select the {string} option")
    public void selectTheOption(String option) {
        testContext.getOrCreateTransferPage().selectTransferDate(option);
    }

    @When("User selects transfer date {string}")
    public void userSelectsTransferDate(String dateOption) {
        testContext.getOrCreateTransferPage().selectTransferDate(dateOption);
    }

    @And("Click on \"Review Transfer\" button")
    public void clickOnReviewTransferButton() {
        testContext.getOrCreateTransferPage().clickReviewTransfer();
    }

    @Then("Click on \"Confirm Transfer\" button")
    public void clickOnConfirmTransferButton() {
        testContext.getOrCreateTransferPage().clickConfirmTransferButton();
    }

    @Then("Locate the Account Number field")
    public void locateTheAccountNumberField() {
        Assert.assertTrue(
                testContext.getOrCreateAccountsPage().isAccountNumberFieldDisplayed(),
                "Account Number field is not visible."
        );
    }

    @Then("^Assert that the Account Number is masked \\(e\\.g\\., displayed as \"([^\"]*)\"\\)$")
    public void assertThatTheAccountNumberIsMasked(String maskedFormatExample) {
        testContext.getOrCreateAccountsPage().isAccountNumberDisplayedAsMasked("Everyday Checking");
    }
}

