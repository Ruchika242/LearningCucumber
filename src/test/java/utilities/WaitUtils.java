package utilities;

import factory.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


    public class WaitUtils {


        private static WebDriverWait getWait(){

            return new WebDriverWait(
                    DriverFactory.getDriver(),
                    Duration.ofSeconds(15)
            );
        }



        public static WebElement waitForElementVisible(WebElement element){

            return getWait()
                    .until(ExpectedConditions.visibilityOf(element));

        }



        public static WebElement waitForElementClickable(WebElement element){

            return getWait()
                    .until(ExpectedConditions
                            .elementToBeClickable(element));
        }



        public static boolean waitForElementPresent(By element){

            return getWait()
                    .until(ExpectedConditions
                            .presenceOfElementLocated(element))
                    .isDisplayed();
        }


    }

