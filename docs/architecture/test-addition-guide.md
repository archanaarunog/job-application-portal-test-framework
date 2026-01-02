# Test Addition Guide — TestNG + POM

Overview
- How to add new UI tests using TestNG with the Page Object Model (POM).
- No Cucumber/BDD in this guide. All examples use TestNG only.

---

Project Structure

```
JobApplicationPortalTestFramework/
├── src/main/java/pages/           # Page Object Model (POM) classes
│   └── LoginPage.java
├── src/main/java/utils/           # Utilities (DriverManager, ConfigReader, WaitUtils, etc.)
│   ├── DriverManager.java
│   ├── ConfigReader.java
│   └── WaitUtils.java
├── src/test/java/tests/           # TestNG test classes
│   └── LoginTest.java
├── src/test/resources/config/     # Environment configs (dev.properties, qa.properties)
│   └── dev.properties
├── testng.xml                     # Default TestNG suite
├── testng-smoke.xml               # Smoke test suite
├── testng-regression.xml          # Regression test suite
└── pom.xml                        # Maven dependencies & plugins
```

---

1) Create a Page Object

- Location: `src/main/java/pages/`
- Conventions:
  - One class per page (e.g., `DashboardPage`)
  - Keep locators private
  - Return `this` or a next page object for chaining
  - Do not put assertions in Page Objects

Template
```java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class DashboardPage {
    private final WebDriver driver;

    private final By dashboardHeader = By.id("dashboard-header");
    private final By logoutBtn = By.id("logout-btn");

    public DashboardPage(WebDriver driver) { this.driver = driver; }

    public DashboardPage open(String baseUrl) {
        driver.get(baseUrl + "/dashboard");
        return this;
    }

    public String getHeaderText() {
        return driver.findElement(dashboardHeader).getText();
    }

    public void clickLogout() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(logoutBtn))
                .click();
    }

    public boolean isVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6))
                    .until(ExpectedConditions.visibilityOfElementLocated(dashboardHeader));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
```

---

2) Write a TestNG Test

- Location: `src/test/java/tests/`
- Conventions:
  - Tests extend `BaseTest` for setup/teardown
  - Group tests using `@Test(groups = {...})` (e.g., `smoke`, `regression`, `ui`, `negative`)
  - Use `@DataProvider` for data-driven tests

Template
```java
package tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import pages.DashboardPage;
import utils.ConfigReader;
import utils.DriverManager;

public class DashboardTest extends BaseTest {

    @Test(groups = {"smoke", "ui"})
    public void verifyDashboardLoads() {
        String baseUrl = ConfigReader.get("base.url");
        DashboardPage dashboard = new DashboardPage(DriverManager.getDriver())
                .open(baseUrl);
        assertTrue(dashboard.isVisible(), "Dashboard should be visible");
        assertNotNull(dashboard.getHeaderText(), "Header text should be present");
    }

    @DataProvider(name = "sections")
    public Object[][] sections() {
        return new Object[][]{
                {"Jobs"},
                {"Applications"},
                {"Profile"}
        };
    }

    @Test(dataProvider = "sections", groups = {"regression", "ui"})
    public void verifySectionVisible(String sectionName) {
        // Example extension point:
        // new DashboardPage(DriverManager.getDriver()).isSectionVisible(sectionName)
        // Add assertions accordingly once the page supports this check
        assertTrue(true);
    }
}
```

Note
- Ensure your `BaseTest` initialises and quits the driver per test method.
- Utilities referenced above (`DriverManager`, `ConfigReader`) should already exist under `src/main/java/utils`.

---

3) Add Tests to a Suite XML

- `testng-smoke.xml` example
```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd" >
<suite name="Smoke Suite" parallel="false">
  <test name="UI Smoke">
    <classes>
      <class name="tests.DashboardTest"/>
    </classes>
  </test>
  <!-- Add more classes as needed -->
  <listeners>
    <!-- Add TestNG listeners here if used (e.g., Allure) -->
  </listeners>
  <groups>
    <run>
      <include name="smoke"/>
    </run>
  </groups>
  <suite-files/>
  <packages/>
  <method-selectors/>
  <parameters/>
  <method-selectors/>
  <listeners/>
  <packages/>
  <suite-files/>
  <test/>
</suite>
```

Run Commands
```bash
# Run a single test class
mvn -Dtest=tests.DashboardTest test

# Run the smoke suite
mvn -Dsurefire.suiteXmlFiles=testng-smoke.xml test

# Run regression suite and generate site reports
mvn -Dsurefire.suiteXmlFiles=testng-regression.xml verify
```

---

4) Reporting

- Surefire reports: `target/surefire-reports/`
- Allure (if enabled):
  - Generate: `mvn allure:report`
  - Serve: `mvn allure:serve`
  - Results: `target/allure-results/`

---

5) Best Practices

- Page Objects
  - Keep locators private and stable; prefer `id`, `name`, or `data-*` attributes
  - Use explicit waits; avoid `Thread.sleep`
  - Do not include assertions in POMs

- Tests
  - One logical scenario per method
  - Use `@DataProvider` for data-driven cases
  - Use TestNG groups for suite selection

- Utilities
  - Centralise driver init in `DriverManager` and per-test lifecycle in `BaseTest`
  - Read configs via `ConfigReader` with `System.getProperty` overrides

---

6) Checklist for Adding a New Test

- Create/extend a Page Object under `src/main/java/pages`
- Add a TestNG class under `src/test/java/tests`
- Add the class to `testng-smoke.xml` or `testng-regression.xml` if applicable
- Run locally with Maven and verify Surefire/Allure outputs

---

Questions?
- See `docs/notes/maven-notes.md` for Maven/TestNG commands
- See `docs/notes/REPORTING.md` for reporting setup
