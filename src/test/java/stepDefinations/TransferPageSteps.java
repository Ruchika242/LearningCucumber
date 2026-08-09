package stepDefinations;

import factory.DriverFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.DashboardPage;
import pages.TransferPage;

import java.time.Duration;

public class TransferPageSteps {

    private DashboardPage dashboardPage;
    private TransferPage transferPage;


    private void initializeDashboardPage() {

        if (dashboardPage == null) {
            dashboardPage = new DashboardPage(
                    DriverFactory.getDriver()
            );
        }
    }


    private void initializeTransferPage() {

        if (transferPage == null) {
            transferPage = new TransferPage(
                    DriverFactory.getDriver()
            );
        }
    }


    @Then("User clicks on Transfer button and should be redirected to Transfer page with URL {string}")
    public void userClicksOnTransferButtonAndShouldBeRedirectedToTransferPageWithURL(
            String expectedURL) {

        initializeDashboardPage();

        transferPage = dashboardPage.clickOnTransferButton();

        WebDriverWait wait = new WebDriverWait(
                DriverFactory.getDriver(),
                Duration.ofSeconds(10)
        );

        wait.until(
                ExpectedConditions.urlContains("transfer")
        );

        Assertions.assertEquals(
                expectedURL,
                DriverFactory.getDriver().getCurrentUrl()
        );
    }


    @When("User clicks on From Account dropdown and selects account {string}")
    public void userClicksOnFromAccountDropdownAndSelectsAccount(
            String fromAccount) {

        initializeTransferPage();

        transferPage.selectFromAccount(fromAccount);
    }


    @And("User clicks on To Account and select account {string}")
    public void userClicksOnToAccountAndSelectAccount(
            String toAccount) {

        initializeTransferPage();

        transferPage.selectToAccount(toAccount);
    }


    @And("User enters Transfer Amount {string}")
    public void userEntersTransferAmount(String amount) {

        initializeTransferPage();

        transferPage.enterAmount(amount);
    }


    @And("User clicks on Review Transfer button")
    public void userClicksOnReviewTransferButton() {

        initializeTransferPage();

        transferPage.clickOnReviewTransferButton();
    }


    @And("User clicks on Confirm Transfer button")
    public void userClicksOnConfirmTransferButton() {

        initializeTransferPage();

        transferPage.clickOnConfirmTransferButton();
    }


    @Then("User should see the success message {string}")
    public void userShouldSeeTheSuccessMessage(
            String expectedMessage) {

        initializeTransferPage();

        String actualMessage =
                transferPage.getTransferSuccessfulMessageText();

        String normalizedExpected =
                expectedMessage
                        .replace("!", "")
                        .trim()
                        .toLowerCase();

        String normalizedActual =
                actualMessage
                        .replace("!", "")
                        .trim()
                        .toLowerCase();

        Assertions.assertEquals(
                normalizedExpected,
                normalizedActual
        );
    }
}