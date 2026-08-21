package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.ITestContext;
import utilities.BrowserContext;

@CucumberOptions(
        features = "src/test/resources/Features",
        glue = {
                "stepDefinations",
                "hooks"
        },
        plugin = {
                "pretty",
                "summary",
                "reporting.ProfessionalCucumberHtmlPlugin"
        },
        monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {

    private String browser = "chrome";

    @BeforeMethod(alwaysRun = true)
    public void setBrowserForScenario(ITestContext testContext) {
        String configuredBrowser = testContext.getCurrentXmlTest().getParameter("browser");
        this.browser = configuredBrowser == null || configuredBrowser.isBlank()
                ? "chrome"
                : configuredBrowser;
        BrowserContext.setBrowser(this.browser);
    }

    @AfterClass(alwaysRun = true)
    public void clearBrowser() {
        BrowserContext.clear();
    }

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    @Override
    @Test(groups = "cucumber", description = "Runs Cucumber Scenario", dataProvider = "scenarios")
    public void runScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        BrowserContext.setBrowser(browser);
        super.runScenario(pickleWrapper, featureWrapper);
    }
}