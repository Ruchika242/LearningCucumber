package stepDefinations;

import factory.DriverFactory;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import pages.DashboardPage;

public class DashboardPageSteps {

    private DashboardPage dashboardPage;

    private void initializeDashboardPage() {
        if (dashboardPage == null) {
            dashboardPage = new DashboardPage(DriverFactory.getDriver());
        }
    }

    @Then("DashboardPage URL should be {string}")
    public void dashboardURL(String expectedURL) {
        initializeDashboardPage();

        Assertions.assertEquals(
                expectedURL,
                dashboardPage.getCurrentURL()
        );
    }

}
