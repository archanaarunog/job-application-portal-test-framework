package com.archana.meta.stepdefinitions;

import io.cucumber.java.en.*;
import static org.testng.Assert.*;

import com.archana.meta.pages.LoginPage;
import com.archana.meta.utils.DriverFactory;
import com.archana.meta.utils.ConfigManager;
import org.openqa.selenium.WebDriver;

public class LoginSteps{
    private WebDriver driver = DriverFactory.getDriver();
    private LoginPage loginPage = new LoginPage(driver);

    @Given("I open the login page")
    public void i_open_the_login_page() {
        String base = ConfigManager.get("base.url");
        loginPage.open(base);
    }

    @When("I login with {string} and {string}")
    public void i_login_with_and(String email, String password){
        loginPage.enterEmail(email).enterPassword(password).clickLogin();
    }

    @Then("I should see the dashboard")
    public void i_should_see_the_dahsboard() {
        boolean messageShown = loginPage.waitForTransientSuccessMessage();
        assertTrue(messageShown, "Expected transient success message to appear");
        boolean loggedIn = loginPage.isLoggedIn();
        assertTrue(loggedIn, "Expected dashboard to visible after login");
    }

   @Then("I should see an error containing {string}")
    public void i_should_see_an_error_containing(String expected) {
        // Validate test data: expected error should not be empty
        assertNotNull(expected, "Expected error message should not be null (check Examples table)");
        assertFalse(expected.trim().isEmpty(), "Expected error message should not be empty (check Examples table)");
        
        // wait up to 6s for any error to appear (we added this helper to LoginPage)
        boolean anyError = loginPage.waitForAnyErrorVisible(6);
        String err = loginPage.getErrorText();
        System.out.println("[LoginSteps] actual errorText='" + err + "'; expected contains='" + expected + "'");
        
        // optionally attach the error text to Allure (if available)
        try {
            io.qameta.allure.Allure.addAttachment("error-text", err == null ? "" : err);
        } catch (Exception ignore) { }

        // now assert
        assertTrue(anyError, "Expected some error to be visible on the page");
        assertNotNull(err, "Expected an error message from the UI");
        assertFalse(err.trim().isEmpty(), "Error message should not be empty");
        assertTrue(err.toLowerCase().contains(expected.toLowerCase()), 
            "Error should contain: '" + expected + "' but got: '" + err + "'");
    }


}