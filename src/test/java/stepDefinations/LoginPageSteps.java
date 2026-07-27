package stepDefinations;

import factory.DriverFactory;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import pages.DashboardPage;
import pages.LoginPage;



public class LoginPageSteps {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;


    private void initializePage() {

        if (loginPage == null) {

            loginPage = new LoginPage(
                    DriverFactory.getDriver()
            );

        }

    }


    @When("User opens URL {string}")
    public void openURL(String url) {



        DriverFactory
                .getDriver()
                .get(url);

    }



    @When("User enters Username {string} and Password {string}")
    public void enterCredentials(String username, String password) {


        initializePage();


        dashboardPage =
                loginPage.login(username, password);


    }

    @When("User clicks on Login button")
    public void clickLoginButton(){

        loginPage.clickLoginButton();

    }



    @Then("DashboardPage URL should be {string}")
    public void dashboardURL(String expectedURL) {


        Assertions.assertEquals(
                expectedURL,
                dashboardPage.getCurrentURL()
        );


    }

}