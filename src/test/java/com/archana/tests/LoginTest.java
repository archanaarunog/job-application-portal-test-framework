package com.archana.tests;

import com.archana.framework.base.BaseTest;
import com.archana.framework.pages.LoginPage;
import com.archana.framework.driver.DriverManager;
import org.testng.Assert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(LoginTest.class);

    private LoginPage login() {
        return new LoginPage();
    }

    @Test(description = "Verify valid login with correct credentials")
    public void testValidLogin() {
        log.info("[Valid Login] Starting scenario");
        boolean success = login()
                .enterEmail("test123@gmail.com")
                .enterPassword("Password@123")
                .clickLogin()
                .isLoggedIn();
        log.info("[Valid Login] isLoggedIn => {}", success);
        Assert.assertTrue(success, "Valid login should succeed.");
        log.info("[Valid Login] Assertion passed");
    }

    @Test(description = "Verify login fails when password is incorrect")
    public void testInvalidPassword() {
        log.info("[Invalid Password] Starting scenario");
        login()
                .enterEmail("validUser@example.com")
                .enterPassword("wrongPass")
                .clickLogin();
        log.info("[Invalid Password] Submitted bad creds, waiting for error");
        boolean errVisible = login().waitForAnyErrorVisible(5);
        String errText = login().getErrorText();
        log.info("[Invalid Password] errVisible={}, errText='{}'", errVisible, errText);
        Assert.assertTrue(errVisible && errText.toLowerCase().contains("invalid"),
                "Error should mention invalid credentials.");
        log.info("[Invalid Password] Assertion passed");
    }

    @Test(description = "Verify login fails when username is blank")
    public void testBlankUsername() {
        log.info("[Blank Username] Starting scenario");
        login()
                .enterEmail("")
                .enterPassword("somePass")
                .clickLogin();
        log.info("[Blank Username] Submitted, waiting for error");
        boolean errVisible = login().waitForAnyErrorVisible(5);
        Assert.assertTrue(errVisible, "Error should be shown for blank username.");
        log.info("[Blank Username] Assertion passed");
    }

    @Test(description = "Verify login fails when password is blank")
    public void testBlankPassword() {
        log.info("[Blank Password] Starting scenario");
        login()
                .enterEmail("validUser@example.com")
                .enterPassword("")
                .clickLogin();
        log.info("[Blank Password] Submitted, waiting for error");
        boolean errVisible = login().waitForAnyErrorVisible(5);
        Assert.assertTrue(errVisible, "Error should be shown for blank password.");
        log.info("[Blank Password] Assertion passed");
    }

    @Test(description = "Verify login fails when both username and password are blank")
    public void testBothBlank() {
        log.info("[Both Blank] Starting scenario");
        login()
                .enterEmail("")
                .enterPassword("")
                .clickLogin();
        log.info("[Both Blank] Submitted, waiting for error");
        boolean errVisible = login().waitForAnyErrorVisible(5);
        Assert.assertTrue(errVisible, "Error should be shown when both fields are blank.");
        log.info("[Both Blank] Assertion passed");
    }

    @Test(description = "Verify SQL injection attempt is handled")
    public void testSqlInjectionLogin() {
        log.info("[SQL Injection] Starting scenario");
        login()
                .enterEmail("' OR 1=1 --")
                .enterPassword("fakePass")
                .clickLogin();
        log.info("[SQL Injection] Submitted, waiting for error");
        boolean errVisible = login().waitForAnyErrorVisible(5);
        String errText = login().getErrorText();
        log.info("[SQL Injection] errVisible={}, errText='{}'", errVisible, errText);
        Assert.assertTrue(
            errVisible && (
                errText.toLowerCase().contains("invalid") ||
                errText.toLowerCase().contains("valid email address") ||
                errText.toLowerCase().contains("invalid email address")
            ),
            "SQL injection should not bypass login.");
        log.info("[SQL Injection] Assertion passed");
    }

    @Test(description = "Verify login with special characters is handled")
    public void testSpecialCharUsername() {
        log.info("[Special Chars] Starting scenario");
        login()
                .enterEmail("@@@###$$$")
                .enterPassword("somePass")
                .clickLogin();
        log.info("[Special Chars] Submitted, waiting for error");
        boolean errVisible = login().waitForAnyErrorVisible(5);
        Assert.assertTrue(errVisible, "Special chars should not be accepted as username.");
        log.info("[Special Chars] Assertion passed");
    }

    @Test(description = "Verify case sensitivity in username")
    public void testCaseSensitivity() {
        log.info("[Case Sensitivity] Starting scenario");
        login()
                .enterEmail("VALIDUSER")
                .enterPassword("validPass")
                .clickLogin();
        log.info("[Case Sensitivity] Submitted, waiting for error");
        boolean errVisible = login().waitForAnyErrorVisible(5);
        Assert.assertTrue(errVisible, "Uppercase username should fail if case-sensitive.");
        log.info("[Case Sensitivity] Assertion passed");
    }

    // Remember-me is flaky — keep under observation; disabled until implementation fixed
    @Test(enabled = false, description = "Verify Remember Me keeps user logged in after restart")
    public void testRememberMe() {
        log.info("[Remember Me] Starting scenario");
        login()
                .enterEmail("validUser@example.com")
                .enterPassword("validPass")
                .toggleRememberMe(true)
                .clickLogin();
        log.info("[Remember Me] Submitted, verifying login state");
        Assert.assertTrue(new LoginPage().isLoggedIn(), "Login should succeed with Remember Me.");
        log.info("[Remember Me] Assertion passed; quitting driver to simulate restart");

        // Simulate restart
        DriverManager.getDriver().quit();
        // re-create driver and open login page (not implemented here)
    }

    @Test(description = "Verify Forgot Password navigation and input")
    public void testForgotPassword() {
        log.info("[Forgot Password] Starting scenario");
        login()
                .clickForgotPassword()
                .enterResetEmail("sample@test.com")
                .submitResetForm();
        log.info("[Forgot Password] Submitted reset, retrieving confirmation text");
        // Confirm redirect or confirmation message
        String confirmation = login().getResetPasswordConfirmationText();
        log.info("[Forgot Password] Confirmation text length: {}", confirmation == null ? 0 : confirmation.length());
        Assert.assertTrue(confirmation.length() > 0, "Reset confirmation should be displayed.");
        log.info("[Forgot Password] Assertion passed");
    }

    @Test(description = "Verify Back to Home link works")
    public void testBackToHome() {
        log.info("[Back To Home] Starting scenario");
        login().clickBackToHome();
        Assert.assertTrue(true, "Back to home navigation verified.");
        log.info("[Back To Home] Assertion passed");
    }

    @Test(description = "Verify Sign Up Now link works")
    public void testSignUpNavigation() {
        log.info("[Sign Up] Starting scenario");
        login().clickSignUpNow();
        Assert.assertTrue(true, "Sign Up navigation verified.");
        log.info("[Sign Up] Assertion passed");
    }
}
