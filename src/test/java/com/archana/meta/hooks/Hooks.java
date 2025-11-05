package com.archana.meta.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import com.archana.meta.utils.DriverFactory;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;

public class Hooks {

    @Before 
    public void beforeScenario(Scenario scenario){
        DriverFactory.getDriver();
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        // Attach immediately when a step fails so Allure captures within step lifecycle
        if (scenario.isFailed()) {
            System.out.println("[Hooks] Step failed in scenario: " + scenario.getName());
            try {
                byte[] screenshot = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "screenshot-" + scenario.getName());
                Allure.addAttachment("Screenshot on Failure: " + scenario.getName(), "image/png",
                        new ByteArrayInputStream(screenshot), "png");
                System.out.println("[Hooks] Screenshot attached (" + screenshot.length + " bytes)");
            } catch (Exception e) {
                System.out.println("[Hooks] Failed to capture screenshot: " + e.getMessage());
            }

            try {
                String pageSource = DriverFactory.getDriver().getPageSource();
                byte[] pageSourceBytes = pageSource.getBytes();
                scenario.attach(pageSourceBytes, "text/html", "page-source-" + scenario.getName());
                Allure.addAttachment("Page Source on Failure: " + scenario.getName(), "text/html",
                        new ByteArrayInputStream(pageSourceBytes), "html");
                System.out.println("[Hooks] Page source attached (" + pageSourceBytes.length + " bytes)");
            } catch (Exception e) {
                System.out.println("[Hooks] Failed to capture page source: " + e.getMessage());
            }
        }
    }

    @After
    public void teardown(Scenario scenario) {
        // Always quit the driver at the end of scenario
        DriverFactory.quitDriver();
    }
}