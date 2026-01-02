package com.archana.framework.utils;

import com.archana.framework.driver.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

public final class ScreenshotUtils {

    public static void attachScreenshot(String name) {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver instanceof TakesScreenshot) {
                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), ".png");
            } else {
                // no-op if driver cannot take screenshots
            }
        } catch (Exception ignored) {
        }
    }

    private ScreenshotUtils() {}
}
