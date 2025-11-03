package com.archana.meta.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Paths;

public abstract class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setUp(){
        WebDriver driver = DriverFactory.getDriver();

    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result){
        if (!result.isSuccess()){
            try{
                WebDriver driver = DriverFactory.getDriver();
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String target = Paths.get("target", "screenshots", result.getName() + ".png").toString();
                FileUtils.copyFile(src, new File(target));
            } catch (Exception e) {
                // prefer a logging framework (SLF4J / Logback) in real projects, for example:
                // private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BaseTest.class);
                // logger.error("Failed to take screenshot", e);
                System.err.println("Failed to capture screenshot: " + e.getMessage());
            }
        }

        // Small, configurable delay before quitting the browser. Useful to allow
        // transient browser UI (like password-save prompts) to settle and not
        // interfere with test assertions right at the end.
        int delayMs = 0;
        try {
            delayMs = ConfigManager.getInt("teardown.delay.ms", 0);
        } catch (Exception ignored) { }
        if (delayMs > 0){
            try { Thread.sleep(delayMs); } catch (InterruptedException ignored) { }
        }
        DriverFactory.quitDriver();

    }

}

