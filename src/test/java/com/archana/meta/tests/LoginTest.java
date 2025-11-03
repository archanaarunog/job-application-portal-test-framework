package com.archana.meta.tests;

import com.archana.meta.pages.LoginPage;
import com.archana.meta.utils.BaseTest;
import com.archana.meta.utils.ConfigManager;
import com.archana.meta.utils.DriverFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(groups = {"smoke", "ui"})
    public void validLogin() {
        String base = ConfigManager.get("base.url");
        LoginPage lp = new LoginPage(DriverFactory.getDriver());
        lp.open(base)
            .enterEmail("alice5678@example.com")
                .enterPassword("SecurePass@123")
                .clickLogin();
        Assert.assertTrue(lp.waitForTransientSuccessMessage(), "Login success message should appear");
        Assert.assertTrue(lp.isLoggedIn(), "User should be logged in");
    }

    @DataProvider(name = "badLogins")
    public Object[][] badLogins() {
    return new Object[][]{
        {"", "Password1","Email address is required" },
        {"bad@x", "Password1", "Please enter a valid email address"},
        {"user@example.com", "wrong@123", "Invalid email or password"},
        {"user@example.com", "short", "Password must be at least 6 characters"}
    };
    }

    @Test(dataProvider = "badLogins", groups = {"negative", "ui"})
    public void invalidLogin(String email, String pass, String expectedMsg){
        String base = ConfigManager.get("base.url");
        LoginPage lp = new LoginPage(DriverFactory.getDriver());
        lp.open(base)
                .enterEmail(email)
                .enterPassword(pass)
                .clickLogin();
        Assert.assertTrue(lp.getErrorText().contains(expectedMsg));

    }

}
