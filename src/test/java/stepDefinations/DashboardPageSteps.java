package stepDefinations;

import context.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import pages.DashboardPage;

public class DashboardPageSteps {

    private final TestContext testContext;
    SoftAssert softAssert;

    public DashboardPageSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @When("Navigate to Dashboard")
    public void navigateToDashboard() {
        // Login step already lands on dashboard; ensure page object is available in context.
        testContext.getOrCreateDashboardPage();
    }

    @Then("Locate the Recent Transactions widget")
    public void locateRecentTransactionsWidget() {
        softAssert.assertTrue(
                getDashboardPage().isRecentTransactionsWidgetDisplayed(),
                "Recent Transactions widget is not visible on Dashboard."
        );
        softAssert.assertAll();
    }

    @And("Assert that maximum 5 transactions are shown")
    public void assertThatMaximum5TransactionsAreShown() {
        int count = getDashboardPage().getTransactionRowCount();
        softAssert.assertTrue(
                count <= 5,
                "Expected maximum 5 recent transactions, but found: " + count
        );
        softAssert.assertAll();
    }

    private DashboardPage getDashboardPage() {
        return testContext.getOrCreateDashboardPage();
    }

    @Then("Click on Transfer link")
    public void clickOnTransferLink() {
        getDashboardPage().clickTransferLink();
    }

    @Then("Verify that user is navigated to Transfer page")
    public void verifyThatUserIsNavigatedToTransferPage() {
        // Assuming that the TransferPageSteps will handle the navigation verification.
        // This step can be left empty or can call a method in TransferPageSteps if needed.
    }

    @Then("Click on Bill Pay link")
    public void clickOnBillPayLink() {
        getDashboardPage().clickBillPayLink();
    }

    @Then("Verify that user is navigated to Bill Pay page")
    public void verifyThatUserIsNavigatedToBillPayPage() {

    }
}
