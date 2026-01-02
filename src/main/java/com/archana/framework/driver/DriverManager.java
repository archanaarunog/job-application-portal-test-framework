package com.archana.framework.driver;

import org.openqa.selenium.WebDriver;

public final class DriverManager {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager(){}

    public static void setDriver(WebDriver driver){
        DRIVER.set(driver);
    }

    public static WebDriver getDriver(){
        return DRIVER.get();
    }

    public static void quitDriver(){
        WebDriver d = DRIVER.get();
        if (d != null) {
            try { d.quit(); } catch (Exception ignored) {}
            DRIVER.remove();
        }
    }
}
