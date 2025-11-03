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

Explain each line (short):
- `package ...` — places file in `com.archana.meta.utils` package.
- imports — load Java `Properties` from classpath.
- static block — reads `config/{env}.properties` from `src/test/resources/config` (env defaults to `dev`), so running with `-Denv=qa` will load `qa.properties` instead.
- `get(...)` methods — return a property, allowing System property override (so `-Dbrowser=firefox` wins over file value).

How it connects: `DriverFactory` and tests call `ConfigManager.get("base.url")` & `getInt("timeout.seconds")` to configure browsers and timeouts.

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
            opts.addArguments("--start-maximized"); // visible chrome, not headless
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

Explain each line (short):
- Uses WebDriverManager to manage ChromeDriver binary.
- ThreadLocal ensures parallel tests have isolated drivers.
- `--start-maximized` makes Chrome visible (no `--headless`).
- Call `DriverFactory.getDriver()` from tests/page objects to retrieve the WebDriver instance.

How it connects: `BaseTest` will call `DriverFactory.getDriver()` in `@BeforeMethod` and `DriverFactory.quitDriver()` in `@AfterMethod`.

---

## 3) BaseTest (create file)
Path: `src/test/java/com/archana/meta/utils/BaseTest.java`

```java
package com.archana.meta.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Paths;

public abstract class BaseTest {

    @BeforeMethod
    public void setUp() {
        WebDriver driver = DriverFactory.getDriver();
        // optionally configure timeouts using ConfigManager
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (!result.isSuccess()) {
            try {
                WebDriver driver = DriverFactory.getDriver();
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String target = Paths.get("target", "screenshots", result.getName() + ".png").toString();
                FileUtils.copyFile(src, new File(target));
            } catch (Exception e) {
                // prefer a logging framework (SLF4J / Logback) in real projects, for example:
                // private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BaseTest.class);
                // logger.error("Failed to take screenshot", e);
                System.err.println("Failed to capture screenshot: " + e.getMessage());
            }
        }
        DriverFactory.quitDriver();
    }
}
```

Explain:
- `@BeforeMethod` prepares driver before each test method.
- `@AfterMethod` captures screenshot on failure and quits the driver; screenshots go to `target/screenshots`.
- Tests extend `BaseTest` to inherit setup/teardown.

---

## 4) LoginPage (POM) (create file)
Path: `src/test/java/com/archana/meta/pages/LoginPage.java`

```java
package com.archana.meta.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final By email = By.id("email");
    private final By password = By.id("password");
    private final By loginBtn = By.cssSelector("button[type='submit']");
    private final By dashboardSelector = By.id("dashboard");
    private final By errorMsg = By.cssSelector(".error-message");
    private final By alertMessage = By.id("alertMessage");
    private final By successMsg = By.xpath("//*[contains(normalize-space(.), 'Login successful! Redirecting')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    public boolean waitForTransientSuccessMessage() {
        try {
            WebDriverWait waitVisible = new WebDriverWait(driver, Duration.ofSeconds(3));
            waitVisible.until(ExpectedConditions.visibilityOfElementLocated(successMsg));
            WebDriverWait waitInvisible = new WebDriverWait(driver, Duration.ofSeconds(5));
            waitInvisible.until(ExpectedConditions.invisibilityOfElementLocated(successMsg));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getErrorText() {
        // 1) Field-level errors (e.g. .error-message)
        if (!driver.findElements(errorMsg).isEmpty()) {
            String text = driver.findElement(errorMsg).getText();
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }
        }
        // 2) Top-level alert panel (e.g. id="alertMessage")
        if (!driver.findElements(alertMessage).isEmpty()) {
            String text = driver.findElement(alertMessage).getText();
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }
        }
        // 3) Fallback: return empty string
        return "";
    }

    public boolean isLoggedIn() {
        return !driver.findElements(dashboardSelector).isEmpty();
    }
}
```

Short note: Replace the selectors (`By` locators) above with the actual selectors from your application (prefer `data-*`, `id`, or `name` where available). The `waitForTransientSuccessMessage()` method detects a short-lived "Login successful! Redirecting..." text by waiting for it to appear and then disappear.

---

## 5) TestNG LoginTest (create file)
Path: `src/test/java/com/archana/meta/tests/LoginTest.java`

```java
package com.archana.meta.tests;

import com.archana.meta.pages.LoginPage;
import com.archana.meta.utils.BaseTest;
import com.archana.meta.utils.ConfigManager;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(groups = {"smoke","ui"})
    public void validLogin() {
        String base = ConfigManager.get("base.url");
        LoginPage lp = new LoginPage(DriverFactory.getDriver());
        lp.open(base)
          .enterEmail("user@example.com")
          .enterPassword("Password1")
          .clickLogin();
        Assert.assertTrue(lp.isLoggedIn(), "User should be logged in");
    }

    @DataProvider(name = "badLogins")
    public Object[][] badLogins() {
        return new Object[][] {
            {"", "Password1", "Email is required"},
            {"bad@x", "Password1", "Invalid email"},
            {"user@example.com", "wrong", "Invalid credentials"}
        };
    }

    @Test(dataProvider = "badLogins", groups = {"negative","ui"})
    public void invalidLogin(String email, String pass, String expectedMsg) {
        String base = ConfigManager.get("base.url");
        LoginPage lp = new LoginPage(DriverFactory.getDriver());
        lp.open(base)
          .enterEmail(email)
          .enterPassword(pass)
          .clickLogin();
        Assert.assertTrue(lp.getErrorText().contains(expectedMsg));
    }
}
```

Explain:
- `validLogin` is the smoke test; it uses `ConfigManager` for base URL and the page object for actions.
- `badLogins` is an inline TestNG `DataProvider` (no Excel required) with rows for negative tests.
- Assertions check login success or expected error text.

---

## Validation & test reports (after step 5)

If you've implemented items 1–5 (ConfigManager, DriverFactory, BaseTest, LoginPage, LoginTest) you already have everything needed to run the UI tests and collect a basic set of test reports and artifacts — you don't need to finish the Cucumber work to validate the POM/TestNG tests.

Follow these steps to run and inspect results:

1) Build & run the TestNG tests (run the whole class)

```bash
mvn -Denv=dev -Dbrowser=chrome -Dtest=com.archana.meta.tests.LoginTest test
```

2) Run a single test method (quick iteration)

```bash
mvn -Denv=dev -Dbrowser=chrome -Dtest=LoginTest#validLogin test
```

3) Where to look for results and artifacts
- `target/surefire-reports/` : TestNG / Surefire reports and `testng-results.xml` (plain-text and XML). Open the `*.txt` or the XML to see test outcomes and stack traces.
- `target/screenshots/` : screenshots saved by `BaseTest.tearDown()` for failed tests (one per failed test method by default).
- Maven console output : contains driver / WebDriverManager logs and full exception traces.
- (Optional) If you add reporting tools (Surefire Report plugin, Allure) you'll get richer HTML output under `target/site/` or `target/allure-results`.

4) Quick pass/fail checklist (verify after a run)
- Maven finishes and exits normally: check the console for failures (non-zero exit means test failures or build errors).
- `target/surefire-reports/testng-results.xml` shows the number of tests run/passed/failed.
- For each failed test, a screenshot should exist under `target/screenshots/` with the test method name.
- If an assertion on `lp.getErrorText()` fails (negative tests), open the corresponding `*.txt` report and screenshot to inspect the page state and exact failure trace.

5) Common troubleshooting hints
- Chromedriver / browser mismatch: WebDriverManager downloads a matching driver by default but requires network access; check console logs for WebDriverManager messages. If CI is offline, pin a driver binary or use a CI-provided driver.
- Locator mismatch: use the browser devtools to verify selectors in `LoginPage` (e.g. `alertMessage`, `.error-message`). Update the `By` locators if the app markup differs.
- Timing / transient elements: if assertions fail due to timing, increase explicit wait durations in `LoginPage` or tune `timeout.seconds` in `dev.properties`.
- Tests not starting (compile errors): run `mvn -q -Denv=dev -Dbrowser=chrome test` locally or Build → Rebuild Project in IntelliJ and fix compilation errors first.
- Headless CI agents: current `DriverFactory` uses visible Chrome (`--start-maximized`). For headless CI add `--headless=new`, disable GPU, and set appropriate window size.

6) Next steps
- If TestNG runs produce expected results you can stop here and gather the reports/artifacts to share.
- To add BDD coverage later, implement items 6–7 (Cucumber feature files, step definitions, and runner). After adding step definitions you can run the Cucumber TestNG runner:

```bash
mvn -Denv=dev -Dbrowser=chrome -Dtest=com.archana.meta.runners.CucumberTestRunner test
```

- If you'd like, I can (A) update the actual `LoginPage.java` in the repo to include the `alertMessage` locator and updated `getErrorText()` (if not already present), then run `mvn -Denv=dev -Dbrowser=chrome -Dtest=com.archana.meta.tests.LoginTest test` and share the console output and report locations, or (B) leave the notes as-is and guide you step-by-step while you run locally. Reply with A or B.

---

## 6) Cucumber feature (create file)
Path: `src/test/resources/features/login.feature`

```
Feature: Login

  @smoke @bdd
  Scenario: Valid login
    Given I open the login page
    When I login with "user@example.com" and "Password1"
    Then I should see the dashboard

  @negative
  Scenario Outline: Invalid login
    Given I open the login page
    When I login with "<email>" and "<password>"
    Then I should see an error containing "<expected>"

    Examples:
      | email            | password   | expected           |
      |                  | Password1  | Email is required  |
      | bad@x            | Password1  | Invalid email      |
      | user@example.com | wrong      | Invalid credentials|
```

Explain:
- `Scenario Outline` + `Examples` provides BDD-style DDT.
- Tags (`@smoke`, `@negative`) allow selective runs.

---

## 7) Cucumber TestNG Runner (create file)
Path: `src/test/java/com/archana/meta/runners/CucumberTestRunner.java`

```java
package com.archana.meta.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.archana.meta.stepdefinitions",
    plugin = {"pretty", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests { }
```

Explain:
- `features` path points to your resource feature files.
- `glue` points to your step definition package.
- `plugin` integrates with Allure for BDD steps (optional setup of Allure required).
- Run this runner using TestNG or include it in `testng.xml`.

---

## 8) testng.xml (put at project root)
```
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd" >
<suite name="JobPortal Suite">
  <test name="POM Tests">
    <classes>
      <class name="com.archana.meta.tests.LoginTest"/>
    </classes>
  </test>
  <test name="Cucumber Tests">
    <classes>
      <class name="com.archana.meta.runners.CucumberTestRunner"/>
    </classes>
  </test>
</suite>
```

Explain:
- `testng.xml` can run both TestNG tests and the Cucumber TestNG runner in one Maven invocation; remove one `<test>` section to run only that subset.

---

## How to run locally (commands)
- Build & run TestNG (POM) tests only:
```bash
mvn -Denv=dev -Dbrowser=chrome -Dtest=com.archana.meta.tests.LoginTest test
```
- Run Cucumber TestNG runner only:
```bash
mvn -Denv=dev -Dbrowser=chrome -Dtest=com.archana.meta.runners.CucumberTestRunner test
```
- Run both via `testng.xml` (default):
```bash
mvn -Denv=dev -Dbrowser=chrome test
```

Explain:
- `-Denv=dev` picks `dev.properties` via `ConfigManager`.
- `-Dbrowser=chrome` overrides the browser setting if needed.
- `-Dtest=...` limits surefire to a specific test class for quick iteration.

---

## Extra quick tips (one-liners)
- Install Cucumber and Gherkin plugins in IntelliJ for `.feature` editing support.
- Mark `src/test/resources` as Test Resources Root (right-click → Mark Directory As).
- Check the Maven tool window (View → Tool Windows → Maven) — expand your project → Lifecycle → run `test`.
- If any compile errors appear, Build → Rebuild Project and fix highlighted issues in the editor.

---

If you want, I can now: (choose one)
- A: Create all these files in the repo for you (I will then run a project build to validate). 
- B: Leave these instructions so you create them manually and I provide small code fragments on demand.

Reply with A or B. I will not change files unless you pick A.
