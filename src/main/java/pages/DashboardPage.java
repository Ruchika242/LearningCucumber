package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

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
        return wait.until(ExpectedConditions.visibilityOf(recentTransactionsHeader)).isDisplayed();
    }

    public int getTransactionRowCount() {
        wait.until(ExpectedConditions.visibilityOfAllElements(recentTransactionsTable));
        return recentTransactionsTable.size();
    }

    public void clickTransferLink() {
        wait.until(ExpectedConditions.elementToBeClickable(transferLink)).click();
    }

    public void clickBillPayLink() {
        wait.until(ExpectedConditions.elementToBeClickable(billPayLink)).click();
    }
}

