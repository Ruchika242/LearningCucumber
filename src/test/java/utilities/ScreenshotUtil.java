package utilities;

import factory.DriverFactory;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ScreenshotUtil {


    public static byte[] getScreenshot() {

        TakesScreenshot ts =
                (TakesScreenshot) DriverFactory.getDriver();

        return ts.getScreenshotAs(OutputType.BYTES);

    }



    public static void captureScreenshot(String scenarioName) {


        TakesScreenshot ts =
                (TakesScreenshot) DriverFactory.getDriver();


        File source =
                ts.getScreenshotAs(OutputType.FILE);


        String timestamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());


        String path =
                "Screenshots/"
                        + scenarioName.replace(" ", "_")
                        + "_"
                        + timestamp
                        + ".png";


        try {

            FileUtils.copyFile(
                    source,
                    new File(path)
            );


            System.out.println(
                    "Screenshot saved at : " + path
            );


        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}