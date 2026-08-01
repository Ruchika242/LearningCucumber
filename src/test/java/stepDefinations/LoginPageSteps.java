package stepDefinations;

import factory.DriverFactory;
import io.cucumber.java.en.*;
import pages.LoginPage;



public class LoginPageSteps {

    private LoginPage loginPage;


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
        loginPage.login(username, password);

    }

    @When("User clicks on Login button")
    public void clickLoginButton(){

        loginPage.clickLoginButton();

    }


}