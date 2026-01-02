package com.archana.framework.base;

import com.archana.framework.driver.DriverFactory;
import com.archana.framework.driver.DriverManager;
import com.archana.framework.pages.LoginPage;
import com.archana.framework.utils.ConfigManager;
import com.archana.framework.utils.WaitUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

public class BaseTest {

    protected WebDriver driver;

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("=== Test Suite Started ===");
    }

    @BeforeMethod
    public void setUp() {

        System.out.println("=========== Starting Test ===========");

        String baseUrl = ConfigManager.getRequired("base.url");
        String browser = ConfigManager.get("browser", "chrome");
        int implicitWait = ConfigManager.getInt("implicitWait", 10);
        int pageLoadTimeout = ConfigManager.getInt("pageLoadTimeout", 20);

        // Initialize driver (DriverFactory will set DriverManager internally)
        // allow overriding browser via -Dbrowser on command line
        System.setProperty("browser", browser);
        DriverFactory.createDriver();
        driver = DriverManager.getDriver();

        // set implicit wait
        try {
            driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(implicitWait));
        } catch (Exception ignored){}

        // provide driver to WaitUtils (backwards compatibility)
        WaitUtils.setDriver(driver, pageLoadTimeout);

        // open login page
        new LoginPage().open();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        System.out.println("=========== Test Finished ===========");
        DriverFactory.quitDriver();
    }
}
