package context;

import pages.AccountsPage;
import pages.DashboardPage;
import pages.LoginPage;
import pages.TransferPage;

/**
 * Shared scenario context.
 * Stores objects that need to be reused across step-definition classes
 * during one Cucumber scenario.
 */
public class TestContext {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;
    private AccountsPage accountsPage;
    private TransferPage transferPage;

    public LoginPage getLoginPage() {

        return loginPage;
    }

    public void setLoginPage(LoginPage loginPage) {
        this.loginPage = loginPage;
    }

    public LoginPage getOrCreateLoginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage();
        }
        return loginPage;
    }

    public DashboardPage getDashboardPage() {
        return dashboardPage;
    }

    public void setDashboardPage(DashboardPage dashboardPage) {
        this.dashboardPage = dashboardPage;
    }

    public DashboardPage getOrCreateDashboardPage() {
        if (dashboardPage == null) {
            dashboardPage = new DashboardPage();
        }
        return dashboardPage;
    }

    public AccountsPage getAccountsPage() {
        return accountsPage;
    }

    public void setAccountsPage(AccountsPage accountsPage) {
        this.accountsPage = accountsPage;
    }

    public AccountsPage getOrCreateAccountsPage() {
        if (accountsPage == null) {
            accountsPage = new AccountsPage();
        }
        return accountsPage;
    }

    public TransferPage getTransferPage() {
        return transferPage;
    }

    public void setTransferPage(TransferPage transferPage) {
        this.transferPage = transferPage;
    }

    public TransferPage getOrCreateTransferPage() {
        if (transferPage == null) {
            transferPage = new TransferPage();
        }
        return transferPage;
    }
}
