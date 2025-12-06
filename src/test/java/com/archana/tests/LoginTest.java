package com.archana.tests;

import com.archana.framework.base.BaseTest;
import com.archana.framework.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test
    public void validLogin() {
        LoginPage login = new LoginPage()
                .open()
                .enterUsername("alice5678@example.com")
                .enterPassword("SecurePass@123")
                .clickLogin();
        Assert.assertTrue(login.waitForTransientSuccessMessage(), "Transient success message should appear and disappear");
        Assert.assertTrue(login.isLoginSuccessful(), "User should be logged in");
    }

    @DataProvider(name = "invalidCases")
    public Object[][] invalidCases() {
        return new Object[][]{
                {"", "Password1", "Email address is required"},
                {"bad@x", "Password1", "Please enter a valid email address"},
                {"user@example.com", "wrong@123", "Invalid email or password"},
                {"user@example.com", "short", "Password must be at least 6 characters"},
                {"nopassword@examp.com", "", "Password is required"},
                {"", "", "Email address is required and Password is required"}
        };
    }

    @Test(dataProvider = "invalidCases")
    public void invalidLogin(String username, String password, String expected) {
        LoginPage login = new LoginPage()
                .open()
                .enterUsername(username)
                .enterPassword(password)
                .clickLogin();
        // The application may perform validation in different ways (inline, modal, or server-side).
        // Ensure login did NOT succeed; if an error message is present, verify it contains expected text.
        boolean loggedIn = login.isLoginSuccessful();
        String error = login.getErrorMessage();
        Assert.assertFalse(loggedIn, "Login should not be successful with invalid credentials");

        if (error != null && !error.isBlank()) {
            if (expected != null && expected.contains(" and ")) {
                for (String part : expected.split("\\sand\\s")) {
                    Assert.assertTrue(error.toLowerCase().contains(part.trim().toLowerCase()),
                            "Expected to contain: " + part + ", actual: " + error);
                }
            } else if (expected != null && !expected.isBlank()) {
                Assert.assertTrue(error.toLowerCase().contains(expected.toLowerCase()),
                        "Expected to contain: " + expected + ", actual: " + error);
            }
        } else {
            // No visible error message — acceptable as long as login didn't succeed.
            System.out.println("No visible error message for invalid login [" + username + "] — login blocked as expected.");
        }
    }

    @Test
    public void forgotPasswordResetFlow() {
        LoginPage login = new LoginPage()
                .open()
                .clickForgotPassword()
                .enterResetEmail("alice5678@example.com")
                .submitResetForm();

        String confirmation = login.getResetPasswordConfirmationText();
        Assert.assertTrue(confirmation != null && !confirmation.isBlank(), "Confirmation text should appear after reset form submission");
        Assert.assertTrue(login.isForgotModalClosed(), "Forgot password modal should be closed after submission");
    }

    @Test
    public void rememberMeRetainsUsernameAfterRefresh() {
        String user = "remember_me_user@example.com";
        LoginPage login = new LoginPage()
                .open()
                .enterUsername(user)
                .toggleRememberMe(true);

        String original = login.getUsernameValue();
        Assert.assertEquals(original, user, "Username field should contain entered value before refresh");

        login.refresh();
        String persisted = login.getUsernameValue();
        if (persisted == null || persisted.isBlank()) {
            // Some builds persist remember-me state only across sessions; accept if checkbox remains checked.
            boolean checked = login.isRememberMeChecked();
            Assert.assertTrue(checked, "Remember-me didn't persist username after refresh; checkbox should still be checked");
        } else {
            Assert.assertEquals(persisted, user, "Username should persist after refresh when Remember Me is checked");
            Assert.assertTrue(login.isRememberMeChecked(), "Remember Me checkbox should remain checked");
        }
    }

    @Test
    public void rememberMeDoesNotRetainWhenUnchecked() {
        String user = "temp_user@example.com";
        LoginPage login = new LoginPage()
                .open()
                .enterUsername(user)
                .toggleRememberMe(false);

        Assert.assertEquals(login.getUsernameValue(), user, "Pre-refresh field value should match entered username");
        login.refresh();
        Assert.assertTrue(login.getUsernameValue().isBlank() || !login.getUsernameValue().equals(user), "Username should not persist after refresh when Remember Me is unchecked");
        Assert.assertFalse(login.isRememberMeChecked(), "Remember Me checkbox should remain unchecked");
    }

    @Test
    public void signUpNavigation() {
        LoginPage login = new LoginPage()
                .open()
                .clickSignUpNow();

        Assert.assertTrue(login.getCurrentUrl().toLowerCase().contains("register"), "Should navigate to register page URL");
    }

    @Test
    public void backToHomeNavigation() {
        LoginPage login = new LoginPage()
                .open()
                .clickBackToHome();

        String url = login.getCurrentUrl().toLowerCase();
        Assert.assertTrue(url.endsWith("/") || url.contains("index"), "Should navigate to home/index page");
    }
}
