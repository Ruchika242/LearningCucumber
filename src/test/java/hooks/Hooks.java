package hooks;


import baseClass.BaseClass;
import factory.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;


public class Hooks extends BaseClass {


    @Before
    public void beforeScenario(){


        launchBrowser();


        logger.info(
                "Scenario Started"
        );

    }



    @After
    public void afterScenario(Scenario scenario){


        if(scenario.getStatus() != Status.PASSED){

            takeScreenshot(
                    scenario.getName()
            );

            try {
                byte[] screenshot =
                        ((TakesScreenshot) DriverFactory.getDriver())
                                .getScreenshotAs(OutputType.BYTES);
                scenario.attach(
                        screenshot,
                        "image/png",
                        "Failure Screenshot"
                );
            }
            catch (WebDriverException | ClassCastException | NullPointerException e) {
                logger.warn("Unable to attach screenshot to report", e);
            }

        }



        closeBrowser();



        logger.info(
                "Scenario Completed"
        );

    }


}