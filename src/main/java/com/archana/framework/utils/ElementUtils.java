package com.archana.framework.utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.archana.framework.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ElementUtils {

    private ElementUtils(){}
    private static final Logger log = LogManager.getLogger(ElementUtils.class);

    private static WebDriver driver() {
        return DriverManager.getDriver();
    }

    public static void click(By locator){
        Allure.step("Click: " + locator, () -> {
            log.info("Clicking locator: {}", locator);
            WebElement el = WaitUtils.waitForClickable(locator);
            ScreenshotUtils.attachScreenshot("Before click: " + locator);
            boolean clicked = false;
            try {
                el.click();
                clicked = true;
                log.info("Clicked locator: {}", locator);
            } catch (Exception e) {
                log.warn("Native click failed on {}. Attempting JS click.", locator);
                try {
                    ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", el);
                    clicked = true;
                    log.info("JS clicked locator: {}", locator);
                } catch (Exception jsEx) {
                    ScreenshotUtils.attachScreenshot("click FAILED: " + locator);
                    log.error("Click failed on locator: {}", locator);
                    throw jsEx;
                }
            } finally {
                if (clicked) {
                    ScreenshotUtils.attachScreenshot("After click: " + locator);
                }
            }
        });
    }

    public static void type(By locator, String text){
        Allure.step("Type: " + locator + " => '" + (text == null ? "" : text) + "'", () -> {
            log.info("Typing into locator: {} => '{}'", locator, text);
            WebElement el = WaitUtils.waitForVisible(locator);
            ScreenshotUtils.attachScreenshot("Before type: " + locator);
            try {
                try { el.clear(); } catch (Exception ignored) {}
                el.sendKeys(text);
                log.info("Typed into locator: {}", locator);
            } finally {
                ScreenshotUtils.attachScreenshot("After type: " + locator);
            }
        });
    }

    public static boolean isDisplayed(By locator){
        try {
            WebElement el = driver().findElement(locator);
            return el != null && el.isDisplayed();
        } catch (Exception e){
            return false;
        }
    }

    public static String getText(By locator){
        final String[] out = {""};
        Allure.step("Get text: " + locator, () -> {
            log.info("Getting text from locator: {}", locator);
            String t = "";
            try {
                WebElement el = driver().findElement(locator);
                t = el.getText();
                if (t == null || t.trim().isEmpty()){
                    try { t = el.getAttribute("innerText"); } catch (Exception ignored) { }
                }
                String result = t == null ? "" : t.trim();
                log.info("Text from {} => '{}'", locator, result);
                out[0] = result;
            } catch (Exception e){
                log.error("Failed to get text from locator: {}", locator);
                out[0] = "";
            } finally {
                ScreenshotUtils.attachScreenshot("After getText: " + locator);
            }
        });
        return out[0];
    }

    public static boolean isSelected(By locator){
        try {
            WebElement el = driver().findElement(locator);
            return el.isSelected();
        } catch (Exception e){
            return false;
        }
    }
}
