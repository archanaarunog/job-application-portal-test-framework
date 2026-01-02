package com.archana.framework.base;

import com.archana.framework.driver.DriverManager;
import com.archana.framework.utils.ElementUtils;
import com.archana.framework.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BasePage {

    protected WebDriver driver;

    public BasePage() {
        this.driver = DriverManager.getDriver();
    }

    protected void click(By locator) {
        ElementUtils.click(locator);
    }

    protected void type(By locator, String text) {
        ElementUtils.type(locator, text);
    }

    protected String getText(By locator) {
        return ElementUtils.getText(locator);
    }

    protected WebElement waitForVisible(By locator) {
        return WaitUtils.waitForVisible(locator);
    }

    protected WebElement waitForClickable(By locator) {
        return WaitUtils.waitForClickable(locator);
    }
}
