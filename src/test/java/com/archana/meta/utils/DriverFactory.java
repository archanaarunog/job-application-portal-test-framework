package com.archana.meta.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        if (driver.get() == null){
            initDriver();
        }
        return driver.get();
    }

    private static void initDriver() {
        String browser = ConfigManager.get("browser", "chrome");
        if("chrome".equalsIgnoreCase(browser)){
            // If a chromedriver is present on PATH it can interfere; log its presence
            try {
                Process p = new ProcessBuilder("which", "chromedriver").start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String path = br.readLine();
                if (path != null && !path.isBlank()) {
                    System.err.println("[DriverFactory] Warning: chromedriver found on PATH: " + path + " — this can cause version conflicts. Consider removing it or ensuring it's up-to-date.");
                }
            } catch (Exception ignored) {
                // ignore diagnostics failures
            }

            // By default, do not force-download a driver. You can enable it with -Dwebdriver.forceDownload=true
            String force = System.getProperty("webdriver.forceDownload", "false");
            if ("true".equalsIgnoreCase(force)) {
                System.out.println("[DriverFactory] Forcing download of a matching ChromeDriver (webdriver.forceDownload=true)");
                WebDriverManager.chromedriver().forceDownload().setup();
            } else {
                System.out.println("[DriverFactory] Using WebDriverManager to setup ChromeDriver (no forced download)");
                WebDriverManager.chromedriver().setup();
            }

            ChromeOptions opts = new ChromeOptions();
            opts.addArguments("--remote-allow-origins=*");
            opts.addArguments("--start-maximized");
            // Optional: uncomment to run in incognito mode (prevents saved passwords/cookies)
            // opts.addArguments("--incognito");
            
            // Disable password save popup
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            opts.setExperimentalOption("prefs", prefs);

            try {
                driver.set(new ChromeDriver(opts));
                System.out.println("[DriverFactory] ChromeDriver started");
            } catch (WebDriverException e) {
                System.err.println("[DriverFactory] Failed to start ChromeDriver: " + (e.getMessage() == null ? "" : e.getMessage()));
                // No automatic retry — rethrow so caller sees the original problem. If you want an automatic retry, enable it explicitly.
                throw e;
            }
        } else {
            throw new RuntimeException("Unsuported browser: " + browser);
        }
    }

    public static void quitDriver() {
        WebDriver drv = driver.get();
        if (drv != null){
            drv.quit();
            driver.remove();
        }
    }

    private DriverFactory() {}
}
