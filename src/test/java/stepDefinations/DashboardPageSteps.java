package stepDefinations;

import context.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.DashboardPage;

public class DashboardPageSteps {

    private final TestContext testContext;

    public DashboardPageSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    private DashboardPage getDashboardPage() {
        if (testContext.getDashboardPage() == null) {
            testContext.setDashboardPage(new DashboardPage());
        }
        return testContext.getDashboardPage();
    }

    @When("Navigate to Dashboard")
    public void navigateToDashboard() {
        // Login step already lands on dashboard; ensure page object is available in context.
        getDashboardPage();
    }

    @Then("Locate the Recent Transactions widget")
    public void locateRecentTransactionsWidget() {
        Assert.assertTrue(
                getDashboardPage().isRecentTransactionsWidgetDisplayed(),
                "Recent Transactions widget is not visible on Dashboard."
        );
    }

    @And("Assert that maximum 5 transactions are shown")
    public void assertThatMaximum5TransactionsAreShown() {
        int count = getDashboardPage().getTransactionRowCount();
        Assert.assertTrue(
                count <= 5,
                "Expected maximum 5 recent transactions, but found: " + count
        );
    }

}
