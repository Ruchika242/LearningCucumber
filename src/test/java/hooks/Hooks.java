package hooks;


import baseClass.BaseClass;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;


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


        if(scenario.isFailed()){


            takeScreenshot(
                    scenario.getName()
            );

        }



        closeBrowser();



        logger.info(
                "Scenario Completed"
        );

    }


}