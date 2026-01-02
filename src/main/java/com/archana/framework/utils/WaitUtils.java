package com.archana.framework.utils;

import com.archana.framework.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtils {

    private static final int DEFAULT_TIMEOUT = 10;

    // optional stored driver for compatibility with older calls
    private static WebDriver storedDriver;

    public static void setDriver(WebDriver driver, int pageLoadTimeoutSeconds) {
        storedDriver = driver;
        try {
            if (driver != null) {
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeoutSeconds));
            }
        } catch (Exception ignored) {}
    }

    private static WebDriver driver() {
        WebDriver d = DriverManager.getDriver();
        return d == null ? storedDriver : d;
    }

    public static WebElement waitForVisible(By locator) {
        return new WebDriverWait(driver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForVisible(By locator, int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(By locator) {
        return new WebDriverWait(driver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForClickable(By locator, int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean waitForInvisibility(By locator) {
        return new WebDriverWait(driver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static boolean waitForInvisibility(By locator, int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds))
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
}
