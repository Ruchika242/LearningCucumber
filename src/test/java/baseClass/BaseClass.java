package baseClass;


import factory.DriverFactory;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BaseClass {


    protected Logger logger =
            LogManager.getLogger(this.getClass());



    private String browser="CHROME";


    private String url=
            "https://qaplayground.com/bank/login";



    public void launchBrowser(){


        WebDriver driver;


        switch(browser){


            case "CHROME":


                WebDriverManager.chromedriver().setup();


                ChromeOptions options =
                        new ChromeOptions();


                driver =
                        new ChromeDriver(options);


                break;



            case "EDGE":

                driver =
                        WebDriverManager.edgedriver().create();

                break;



            case "FIREFOX":

                driver =
                        WebDriverManager.firefoxdriver().create();

                break;



            default:

                throw new RuntimeException(
                        "Invalid Browser"
                );

        }



        DriverFactory.setDriver(driver);



        DriverFactory.getDriver()
                .manage()
                .window()
                .maximize();



        DriverFactory.getDriver()
                .manage()
                .deleteAllCookies();



        DriverFactory.getDriver()
                .get(url);



        logger.info(
                "Browser launched : "
                        + Thread.currentThread().getName()
        );

    }




    public void closeBrowser(){

        DriverFactory.quitDriver();


        logger.info(
                "Browser closed : "
                        + Thread.currentThread().getName()
        );

    }





    public void takeScreenshot(String scenarioName){
        WebDriver driver = DriverFactory.getDriver();

        if (driver == null) {
            logger.warn("Screenshot skipped: WebDriver is null");
            return;
        }

        String timestamp =
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss")
                        .format(new Date());

        String safeScenarioName =
                scenarioName.replaceAll("[^a-zA-Z0-9-_ ]", "")
                        .replace(" ", "_");

        File screenshotDir = new File("ScreenShots");
        if (!screenshotDir.exists() && !screenshotDir.mkdirs()) {
            logger.error("Screenshot failed: unable to create directory " + screenshotDir.getAbsolutePath());
            return;
        }

        File destination =
                new File(
                        screenshotDir,
                        safeScenarioName
                                + "_"
                                + timestamp
                                + ".png"
                );

        try {

            File screenshot =
                    ((TakesScreenshot) driver)
                            .getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(
                    screenshot,
                    destination
            );


            logger.info(
                    "Screenshot saved : "
                            + destination.getAbsolutePath()
            );


        }
        catch(IOException | WebDriverException e){

            logger.error(
                    "Screenshot failed",
                    e
            );

        }

    }

}