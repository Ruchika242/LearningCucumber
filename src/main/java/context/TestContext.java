package context;

import pages.DashboardPage;
import pages.LoginPage;

/**
 * Shared scenario context.
 * Stores objects that need to be reused across step-definition classes
 * during one Cucumber scenario.
 */
public class TestContext {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    public LoginPage getLoginPage() {
        return loginPage;
    }

    public void setLoginPage(LoginPage loginPage) {
        this.loginPage = loginPage;
    }

    public DashboardPage getDashboardPage() {
        return dashboardPage;
    }

    public void setDashboardPage(DashboardPage dashboardPage) {
        this.dashboardPage = dashboardPage;
    }
}
