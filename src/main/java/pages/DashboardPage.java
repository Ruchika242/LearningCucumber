package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utilities.SelUtils;
import utilities.WaitUtils;

import java.util.List;

public class DashboardPage extends BasePage {

    public DashboardPage() {
        super();
    }

    @FindBy(xpath = "//h2[text()=\"Recent Transactions\"]")
    private WebElement recentTransactionsHeader;

    @FindBy(xpath = "//table[@data-testid='recent-transactions-table']//tbody/tr")
    private List<WebElement> recentTransactionsTable;

    @FindBy(xpath = "//span[text()='Transfer']")
    private WebElement transferLink;

    @FindBy(xpath = "//span[text()='Bill Pay']")
    private WebElement billPayLink;


    public boolean isRecentTransactionsWidgetDisplayed() {
       return WaitUtils.waitForElementVisible(recentTransactionsHeader);
    }

    public int getTransactionRowCount() {
        WaitUtils.waitForElementsVisible(recentTransactionsTable);
        return recentTransactionsTable.size();
    }

    public void clickTransferLink() {

        SelUtils.clickElement(transferLink);
    }

    public void clickBillPayLink() {
        SelUtils.clickElement(billPayLink);
    }
}

