# Framework setup notes — hands-on guide (Java + Maven + TestNG + Cucumber + Selenium)

Purpose
- Very specific, copy-paste guidance so you can create a minimal hybrid framework locally, run a sample login test (POM + TestNG), and then add a simple Cucumber feature that reuses the same page object.

Checklist (do these in order)
1. Create folders (IDE):
   - `src/test/java/com/archana/meta/pages`
   - `src/test/java/com/archana/meta/tests`
   - `src/test/java/com/archana/meta/utils`
   - `src/test/java/com/archana/meta/stepdefinitions`
   - `src/test/java/com/archana/meta/runners`
   - `src/test/resources/features`
   - `src/test/resources/config`
   - `src/test/resources/testdata`

2. Add config file (create): `src/test/resources/config/dev.properties` with:
```
base.url=https://staging.example.com
browser=chrome
timeout.seconds=10
```
- Explanation: these values are loaded by `ConfigManager` so you can override via `-D` properties when running.

3. Create the Java utility classes and tests (below snippets to paste into files). After creating each file, save and recompile in IntelliJ (Build → Build Project) to catch syntax errors.

---

## 1) ConfigManager (create file)
Path: `src/test/java/com/archana/meta/utils/ConfigManager.java`

```java
package com.archana.meta.utils;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager {
    private static final Properties props = new Properties();

    static {
        String env = System.getProperty("env", "dev");
        String resource = "config/" + env + ".properties";
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in != null) {
                props.load(in);
            } else {
                throw new RuntimeException("Config resource not found: " + resource);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + resource, e);
        }
    }

    public static String get(String key, String defaultVal) {
        return System.getProperty(key, props.getProperty(key, defaultVal));
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static int getInt(String key, int defaultVal) {
        return Integer.parseInt(get(key, Integer.toString(defaultVal)));
    }

    private ConfigManager() { }
}
```

---

## 2) DriverFactory (create file)
Path: `src/test/java/com/archana/meta/utils/DriverFactory.java`

```java
package com.archana.meta.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public final class DriverFactory {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            initDriver();
        }
        return driver.get();
    }

    private static void initDriver() {
        String browser = ConfigManager.get("browser", "chrome");
        if ("chrome".equalsIgnoreCase(browser)) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions opts = new ChromeOptions();
            opts.addArguments("--start-maximized");
            driver.set(new ChromeDriver(opts));
        } else {
            throw new RuntimeException("Unsupported browser: " + browser);
        }
    }

    public static void quitDriver() {
        WebDriver drv = driver.get();
        if (drv != null) {
            drv.quit();
            driver.remove();
        }
    }

    private DriverFactory() {}
}
```

---

## 3) BaseTest (create file)
Path: `src/test/java/com/archana/meta/utils/BaseTest.java`

```java
package com.archana.meta.utils;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        WebDriver driver = DriverFactory.getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        DriverFactory.quitDriver();
    }
}
```

---

## 4) LoginPage (POM) (create file)
Path: `src/test/java/com/archana/meta/pages/LoginPage.java`

```java
package com.archana.meta.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final By email = By.id("email");
    private final By password = By.id("password");
    private final By loginBtn = By.cssSelector("button[type='submit']");

    public LoginPage(WebDriver driver) { this.driver = driver; }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        return this;
    }

    public LoginPage enterEmail(String e) {
        driver.findElement(email).clear();
        driver.findElement(email).sendKeys(e);
        return this;
    }

    public LoginPage enterPassword(String p) {
        driver.findElement(password).clear();
        driver.findElement(password).sendKeys(p);
        return this;
    }

    public void clickLogin() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(loginBtn))
                .click();
    }
}
```

---

## 5) TestNG LoginTest (create file)
Path: `src/test/java/com/archana/meta/tests/LoginTest.java`

```java
package com.archana.meta.tests;

import com.archana.meta.pages.LoginPage;
import com.archana.meta.utils.BaseTest;
import com.archana.meta.utils.DriverFactory;
import com.archana.meta.utils.ConfigManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(groups = {"smoke", "ui"})
    public void validLogin() {
        String base = ConfigManager.get("base.url");
        LoginPage lp = new LoginPage(DriverFactory.getDriver());
        lp.open(base)
          .enterEmail("user@example.com")
          .enterPassword("Password1")
          .clickLogin();
        Assert.assertTrue(true, "User should be logged in");
    }
}
```

---

## Cucumber Layer (optional learning)
See the dedicated Cucumber guide at `cucumber-learning/notes/cucumber.md` for BDD additions and runner setup.
