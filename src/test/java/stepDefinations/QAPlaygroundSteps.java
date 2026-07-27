package stepDefinations;

import io.cucumber.java.en.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.LoginPage;


public class QAPlaygroundSteps {

    WebDriver driver;
    LoginPage loginPage;


    @Given("I Launch chrome browser")
    public void launch_browser(){

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        // Initialize LoginPage after browser launch
        loginPage = new LoginPage(driver);

    }


    @When("User opens URL {string}")
    public void user_opens_url(String url){

        driver.get(url);

    }


    @And("verify that logo is displayed on login page")
    public void verify_logo(){

        boolean logoStatus = loginPage.isLogoDisplayed();

        Assertions.assertTrue(logoStatus, "Logo is not displayed on the page");

    }


    @And("User enters Username {string} and Password {string}")
    public void user_enters_credentials(String username, String password){

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

    }


    @And("User clicks on Login button")
    public void user_clicks_login_button(){

        loginPage.clickLoginButton();

    }


    @Then("User can view dashboard page")
    public void verify_dashboard_page(){

        // Wait for dashboard to load and verify URL or page element
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertNotNull(currentUrl, "Current URL is null");
        Assertions.assertTrue(currentUrl.contains("dashboard") || currentUrl.contains("home"),
                "User is not on dashboard page");

        // Close the browser after test
        driver.quit();

    }

}
