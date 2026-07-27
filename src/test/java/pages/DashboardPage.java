package pages;


import baseClass.BaseClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


public class DashboardPage extends BaseClass {


    private WebDriver driver;


    public DashboardPage(WebDriver driver) {

        this.driver = driver;

        PageFactory.initElements(driver, this);

    }



    public String getCurrentURL(){

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(10)
                );


        wait.until(
                ExpectedConditions.urlContains("dashboard")
        );


        return driver.getCurrentUrl();

    }

}


