package stepDefinations;

import context.TestContext;
import drivermanager.DriverManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import pages.AccountsPage;
import pages.TransferPage;
import utilities.ConfigReader;

import java.net.URI;

public class SendMoneyPageSteps {

    private final TestContext testContext;

    public SendMoneyPageSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @When("User navigates to the Send Money page")
    public void userNavigatesToTheSendMoneyPage() {
        URI baseUri = URI.create(ConfigReader.getBaseUrl());
        String SendMoneyUrl = baseUri.getScheme() + "://" + baseUri.getAuthority() + "/bank/send-money";
        DriverManager.getDriver().get(SendMoneyUrl);
        testContext.setTransferPage(new TransferPage());
    }

    @And("Select \"From Account\"")
    public void selectFromAccount() {
        testContext.getOrCreateSendMoneyPage().clickFromAccountDropdown();
        testContext.getOrCreateSendMoneyPage().clickFromAccountOptions("High-Yield Savings");


    }

    @And("User click on \"Add\" button to add a new Payee instantly on the same form")
    public void userClickOnAddButton() {
        testContext.getOrCreateSendMoneyPage().clickAddButton();
    }

    @And("User enter valid payee details like Payee Name, Bank Name, Routing Number, Account Number")
    public void userEnterValidPayeeDetails() {
        testContext.getOrCreateSendMoneyPage().fillPayeeDetails("John Doe", "Bank of America", "123456789", "987654321");
    }

    @And("Click on \"Add Payee\" button to add the payee")
    public void clickOnAddPayeeButton() {
        testContext.getOrCreateSendMoneyPage().clickAddPayeeButton();
    }

    @And("User enters amount")
    public void userEntersAmount() {
        testContext.getOrCreateSendMoneyPage().enterAmount("100");
    }

    @And("User click on \"Review & Send\" button to review the transaction")
    public void userClickOnReviewAndSendButton() {
        testContext.getOrCreateSendMoneyPage().clickReviewAndSendButton();
    }

    @And("User click on \"Confirm & Send\" button to complete the transaction")
    public void userClickOnConfirmAndSendButton() {
        testContext.getOrCreateSendMoneyPage().clickConfirmAndSendButton();
    }

}
