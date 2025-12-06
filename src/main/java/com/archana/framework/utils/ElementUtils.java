package com.archana.framework.utils;

import com.archana.framework.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class ElementUtils {

    private static final Logger log = LogManager.getLogger(ElementUtils.class);

    private static WebDriver driver() {
        return DriverManager.getDriver();
    }

    // ---------------- BASIC ACTIONS ----------------

    public static void click(By locator) {
        try {
            WaitUtils.waitForClickable(locator);
            driver().findElement(locator).click();
            log.info("Clicked element: {}", locator);
        } catch (Exception e) {
            log.warn("Standard click failed for {}: {}. Attempting fallback clicks.", locator, e.toString());
            try {
                // Try a direct click without explicit wait
                driver().findElement(locator).click();
                log.info("Fallback direct click succeeded for {}", locator);
                return;
            } catch (Exception ex1) {
                try {
                    // Try JavaScript click as last resort
                    WebElement el = driver().findElement(locator);
                    ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", el);
                    log.info("Fallback JS click succeeded for {}", locator);
                    return;
                } catch (Exception ex2) {
                    log.error("All click attempts failed for element: {}", locator, ex2);
                    throw ex2;
                }
            }
        }
    }

    public static void type(By locator, String text) {
        try {
            WaitUtils.waitForVisible(locator);
            WebElement element = driver().findElement(locator);
            element.clear();
            element.sendKeys(text);
            log.info("Typed '{}' into element {}", text, locator);
        } catch (Exception e) {
            log.error("Failed to type '{}' into element: {}", text, locator, e);
            throw e;
        }
    }

    public static String getText(By locator) {
        try {
            WaitUtils.waitForVisible(locator);
            String text = driver().findElement(locator).getText();
            log.info("Fetched text '{}' from {}", text, locator);
            return text;
        } catch (Exception e) {
            log.error("Failed to fetch text from element: {}", locator, e);
            throw e;
        }
    }

    public static boolean isDisplayed(By locator) {
        try {
            return driver().findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static boolean isSelected(By locator) {
        try {
            return driver().findElement(locator).isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------- ADVANCED ----------------

    public static void scrollIntoView(By locator) {
        try {
            WebElement element = driver().findElement(locator);
            ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView(true);", element);
            log.info("Scrolled into view: {}", locator);
        } catch (Exception e) {
            log.error("Failed to scroll into view: {}", locator, e);
            throw e;
        }
    }

    public static void hover(By locator) {
        try {
            WebElement element = driver().findElement(locator);
            Actions actions = new Actions(driver());
            actions.moveToElement(element).perform();
            log.info("Hovered on element: {}", locator);
        } catch (Exception e) {
            log.error("Failed to hover on element: {}", locator, e);
            throw e;
        }
    }
}
