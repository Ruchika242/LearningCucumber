package pages;

import drivermanager.DriverManagerClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.ConfigReader;

import java.time.Duration;
import java.util.List;

public class DashboardPage {

    private final WebDriverWait wait;

    public DashboardPage() {
        WebDriver driver = DriverManagerClass.DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getExplicitWaitTimeout()));
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//h2[text()=\"Recent Transactions\"]")
    private WebElement recentTransactionsHeader;


    @FindBy(xpath = "//table[@data-testid='recent-transactions-table']//tbody/tr")
    private List<WebElement> recentTransactionsTable;


    public boolean isRecentTransactionsWidgetDisplayed() {
        return wait.until(ExpectedConditions.visibilityOf(recentTransactionsHeader)).isDisplayed();
    }


    public int getTransactionRowCount() {
        wait.until(ExpectedConditions.visibilityOfAllElements(recentTransactionsTable));
        return recentTransactionsTable.size();
    }
}
