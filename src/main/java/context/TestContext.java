package context;

import pages.*;

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
    private SendMoneyPage sendMoneyPage;

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

    public SendMoneyPage getSendMoneyPage() {
        return sendMoneyPage;
    }

    public void setSendMoneyPage(SendMoneyPage sendMoneyPage) {
        this.sendMoneyPage = sendMoneyPage;
    }

    public SendMoneyPage getOrCreateSendMoneyPage() {
        if (sendMoneyPage == null) {
            sendMoneyPage = new SendMoneyPage();
        }
        return sendMoneyPage;
    }
}
