package com.archana.framework.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DriverFactory {

    public static WebDriver createDriver() {
        String browser = System.getProperty("browser", "chrome").toLowerCase(Locale.ROOT);
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        WebDriver driver;
        switch (browser) {
            case "chrome":
                driver = createChrome(headless);
                break;
            case "firefox":
                driver = createFirefox(headless);
                break;
            case "safari":
            case "webkit":
                driver = createSafari();
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        } catch (Exception ignored) { }

        DriverManager.setDriver(driver);
        return driver;
    }

    /** kept for backward compatibility; delegates to createDriver() */
    public static void initDriver() {
        createDriver();
    }

    public static void quitDriver() {
        DriverManager.quitDriver();
    }

    private static WebDriver createChrome(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

        List<String> args = new ArrayList<>();
        args.add("--disable-notifications");
        args.add("--disable-gpu");
        args.add("--no-sandbox");
        args.add("--disable-dev-shm-usage");
        args.add("--remote-allow-origins=*");

        if (headless) {
            args.add("--headless=new");
            args.add("--window-size=1920,1080");
        } else {
            args.add("--start-maximized");
        }

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        options.addArguments(args);
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefox(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        if (headless) {
            options.addArguments("-headless");
        }
        WebDriver driver = new FirefoxDriver(options);
        if (!headless) {
            try { driver.manage().window().maximize(); } catch (Exception ignored) {}
        }
        return driver;
    }

    private static WebDriver createSafari() {
        SafariOptions options = new SafariOptions();
        WebDriver driver = new SafariDriver(options);
        try { driver.manage().window().maximize(); } catch (Exception ignored) {}
        return driver;
    }

    private DriverFactory() {}
}
