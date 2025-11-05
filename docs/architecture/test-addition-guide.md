# Test Addition Guide — Architecture & Best Practices

## Overview
This document explains how to add new test cases to the framework, follow established patterns, and maintain consistency. By the end, you'll understand how to create Page Objects, write TestNG tests, add Cucumber scenarios, and run/report on them.

---

## Project Structure

```
JobApplicationPortalTestFramework/
├── src/test/java/com/archana/meta/
│   ├── pages/           # Page Object Model (POM) classes
│   │   └── LoginPage.java
│   ├── tests/           # TestNG test classes (DDT)
│   │   └── LoginTest.java
│   ├── stepdefinitions/ # Cucumber step definitions (BDD)
│   │   └── LoginSteps.java
│   ├── hooks/           # Cucumber hooks (setup/teardown, screenshots)
│   │   └── Hooks.java
│   ├── runners/         # Cucumber TestNG runner
│   │   └── CucumberTestRunner.java
│   └── utils/           # Utilities (DriverFactory, ConfigManager, BaseTest)
│       ├── DriverFactory.java
│       ├── ConfigManager.java
│       └── BaseTest.java
├── src/test/resources/
│   ├── features/        # Cucumber .feature files
│   │   └── login.feature
│   └── config/          # Environment configs (dev.properties, qa.properties)
│       └── dev.properties
├── testng.xml           # Default TestNG suite
├── testng-smoke.xml     # Smoke test suite
├── testng-regression.xml # Regression test suite
└── pom.xml              # Maven dependencies & plugins
```

---

## Adding a New Page

### 1. Create a Page Object (POM)

**Location**: `src/test/java/com/archana/meta/pages/`

**Conventions**:
- One class per page (e.g., `LoginPage`, `DashboardPage`, `JobApplicationPage`)
- Private By locators
- Public methods for user actions (return `this` for chaining)
- No assertions in POMs (assertions belong in tests/step definitions)
- Use explicit waits (WebDriverWait) for dynamic elements

**Template**:
```java
package com.archana.meta.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class DashboardPage {
    private final WebDriver driver;
    
    // Locators
    private final By dashboardHeader = By.id("dashboard-header");
    private final By logoutBtn = By.id("logout-btn");
    
    public DashboardPage(WebDriver driver) {
        this.driver = driver;
    }
    
    public DashboardPage open(String baseUrl) {
        driver.get(baseUrl + "/dashboard.html");
        return this;
    }
    
    public String getHeaderText() {
        return driver.findElement(dashboardHeader).getText();
    }
    
    public void clickLogout() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(logoutBtn)).click();
    }
    
    public boolean isLoggedIn() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(6));
            wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardHeader));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## Adding TestNG Tests (DDT)

### 2. Create a TestNG Test Class

**Location**: `src/test/java/com/archana/meta/tests/`

**Conventions**:
- Extend `BaseTest` (provides setup/teardown)
- Use `@Test` with groups (`smoke`, `regression`, `ui`, `negative`)
- Use `@DataProvider` for data-driven tests
- Keep tests focused (one logical scenario per method)
- Use Allure annotations for better reporting (`@Description`, `@Severity`)

**Template**:
```java
package com.archana.meta.tests;

import com.archana.meta.pages.DashboardPage;
import com.archana.meta.utils.BaseTest;
import com.archana.meta.utils.ConfigManager;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DashboardTest extends BaseTest {
    
    @Test(groups = {"smoke", "ui"})
    @Description("Verify dashboard page loads successfully")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyDashboardLoads() {
        DashboardPage dashboardPage = new DashboardPage(driver);
        String baseUrl = ConfigManager.get("base.url");
        dashboardPage.open(baseUrl);
        
        assertTrue(dashboardPage.isLoggedIn(), "Dashboard should be visible");
        assertNotNull(dashboardPage.getHeaderText(), "Header text should be present");
    }
    
    @DataProvider(name = "dashboardSections")
    public Object[][] dashboardSections() {
        return new Object[][] {
            {"Jobs", true},
            {"Applications", true},
            {"Profile", true}
        };
    }
    
    @Test(dataProvider = "dashboardSections", groups = {"regression"})
    @Description("Verify dashboard sections are visible")
    public void verifySectionsVisible(String sectionName, boolean expectedVisible) {
        // Test implementation
    }
}
```

### 3. Add Test to Suite XML

**Location**: `testng-smoke.xml` or `testng-regression.xml`

```xml
<test name="Dashboard Tests">
    <classes>
        <class name="com.archana.meta.tests.DashboardTest">
            <methods>
                <include name="verifyDashboardLoads"/>
            </methods>
        </class>
    </classes>
</test>
```

### 4. Run TestNG Tests

```bash
# Run specific test class
mvn -Dtest=com.archana.meta.tests.DashboardTest test

# Run smoke suite
mvn -Dsurefire.suiteXmlFiles=testng-smoke.xml test

# Run + generate Allure report
mvn -Dsurefire.suiteXmlFiles=testng-regression.xml verify
```

---

## Adding Cucumber Scenarios (BDD)

### 5. Create a Feature File

**Location**: `src/test/resources/features/`

**Conventions**:
- Use Gherkin syntax (Given/When/Then)
- Use Scenario Outline for data-driven cases
- Tag scenarios (`@smoke`, `@regression`, `@negative`, `@bdd`, `@ui`)
- Keep scenarios business-readable

**Template**: `dashboard.feature`
```gherkin
@bdd @ui
Feature: Dashboard
  As a logged-in user
  I want to view my dashboard
  So that I can access job applications

  @smoke
  Scenario: View dashboard after login
    Given I am logged in as "alice5678@example.com"
    When I navigate to the dashboard
    Then I should see the dashboard header

  @regression
  Scenario Outline: Verify dashboard sections
    Given I am on the dashboard
    When I look for section "<section>"
    Then the section should be <visible>

    Examples:
      | section      | visible |
      | Jobs         | visible |
      | Applications | visible |
      | Profile      | visible |
```

### 6. Create Step Definitions

**Location**: `src/test/java/com/archana/meta/stepdefinitions/`

**Conventions**:
- Reuse Page Objects (no duplication)
- Keep step definitions thin (delegate to POMs)
- Add assertions in step definitions
- Use Allure attachments for debugging (`Allure.addAttachment`)

**Template**: `DashboardSteps.java`
```java
package com.archana.meta.stepdefinitions;

import com.archana.meta.pages.DashboardPage;
import com.archana.meta.pages.LoginPage;
import com.archana.meta.utils.ConfigManager;
import com.archana.meta.utils.DriverFactory;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import static org.testng.Assert.*;

public class DashboardSteps {
    private WebDriver driver = DriverFactory.getDriver();
    private DashboardPage dashboardPage = new DashboardPage(driver);
    private LoginPage loginPage = new LoginPage(driver);
    
    @Given("I am logged in as {string}")
    public void i_am_logged_in_as(String email) {
        String baseUrl = ConfigManager.get("base.url");
        loginPage.open(baseUrl);
        loginPage.enterEmail(email).enterPassword("SecurePass@123").clickLogin();
        assertTrue(loginPage.waitForTransientSuccessMessage(), "Login should succeed");
    }
    
    @When("I navigate to the dashboard")
    public void i_navigate_to_the_dashboard() {
        String baseUrl = ConfigManager.get("base.url");
        dashboardPage.open(baseUrl);
    }
    
    @Then("I should see the dashboard header")
    public void i_should_see_the_dashboard_header() {
        assertTrue(dashboardPage.isLoggedIn(), "Dashboard should be visible");
        assertNotNull(dashboardPage.getHeaderText(), "Header should have text");
    }
}
```

### 7. Run Cucumber Tests

```bash
# Run Cucumber scenarios
mvn -Dtest=com.archana.meta.runners.CucumberTestRunner test

# Run + generate Allure report
mvn -Dtest=com.archana.meta.runners.CucumberTestRunner verify
```

---

## Running & Reporting

### Quick Commands

| Task | Command |
|------|---------|
| Run BDD (Cucumber) | `mvn -Dtest=com.archana.meta.runners.CucumberTestRunner test` |
| Run DDT (TestNG class) | `mvn -Dtest=com.archana.meta.tests.LoginTest test` |
| Run DDT (suite) | `mvn -Dsurefire.suiteXmlFiles=testng-smoke.xml test` |
| Run + auto-generate HTML | `mvn -Dtest=... verify` |
| Generate Allure HTML | `mvn allure:report` |
| Serve Allure report | `mvn allure:serve` |
| Serve static HTML | `cd target/site/allure-maven-plugin && python3 -m http.server 8000` |

### Report Locations

- **Allure JSON results**: `target/allure-results/` (raw test data)
- **Allure HTML report**: `target/site/allure-maven-plugin/index.html`
- **Surefire reports**: `target/surefire-reports/` (XML/TXT)
- **Screenshots (on failure)**: Attached in `target/allure-results/*-attachment.png`

---

## Best Practices & Tips

### 1. Page Object Patterns
- ✅ Return `this` for method chaining: `loginPage.enterEmail("test").enterPassword("pass").clickLogin()`
- ✅ Use explicit waits for dynamic elements
- ❌ Don't put assertions in POMs
- ❌ Don't use `Thread.sleep()` (use WebDriverWait)

### 2. Test Data Management
- Use `@DataProvider` for TestNG DDT
- Use Scenario Outline Examples for Cucumber DDT
- Store test data in external files (CSV/JSON) for large datasets
- Use `ConfigManager.get()` for environment-specific configs

### 3. Waits & Synchronization
- Prefer explicit waits over implicit waits
- Use `WebDriverWait` with `ExpectedConditions`
- Add helper methods like `waitForElementVisible()` in POMs
- Avoid hardcoded sleeps

### 4. Test Organization
- **Smoke tests**: Critical happy-path scenarios (login, dashboard load)
- **Regression tests**: Comprehensive coverage (positive + negative)
- **Negative tests**: Validation/error handling
- Use TestNG groups and Cucumber tags consistently

### 5. Reporting & Debugging
- Add `@Description` and `@Severity` annotations (Allure)
- Use `Allure.addAttachment()` for debug info (error messages, screenshots)
- Screenshots are auto-captured on failure (via Hooks)
- Check `target/allure-results/` for raw JSON if reports look wrong

### 6. CI/CD Integration
- Use `mvn verify` in CI pipelines (runs tests + generates HTML)
- Archive `target/site/allure-maven-plugin/` as artifact
- Configure test parallelization carefully (ThreadLocal WebDriver is set up)

---

## Scaling to 50+ Test Cases

### Workflow
1. **Identify pages to test** (Dashboard, Job Listing, Job Application, Profile, Admin)
2. **Create Page Objects** for each page (`DashboardPage.java`, `JobListingPage.java`, etc.)
3. **Write test scenarios** (mix of TestNG + Cucumber)
   - Start with smoke tests (critical paths)
   - Add regression tests (edge cases, validation)
4. **Organize into suites**
   - `testng-smoke.xml` (5-10 tests, <5 min run time)
   - `testng-regression.xml` (full suite)
5. **Run in batches**
   - Run smoke after every code change
   - Run regression nightly or pre-release
6. **Review Allure reports** for failures and flaky tests

### Time Estimates (50 test cases)
- **Page Objects** (5 pages × 30 min): ~2.5 hours
- **TestNG tests** (25 tests × 10 min): ~4 hours
- **Cucumber scenarios** (25 scenarios × 10 min): ~4 hours
- **Debugging & cleanup**: ~2 hours
- **Total**: ~12-13 hours (achievable in 1 day with focus)

### Priority Order
1. **Critical flows** (login, dashboard, job application submission)
2. **Validation** (form field validation, error messages)
3. **Edge cases** (empty inputs, special characters, long text)
4. **Admin flows** (if applicable)

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Tests fail with "element not found" | Add explicit waits; check if element is in iframe |
| Chrome password popup | Already disabled in DriverFactory |
| CDP version warnings | Optional; add `selenium-devtools-vXX` if using CDP features |
| Allure report shows "Loading..." | Serve via HTTP (python3 -m http.server) not file:// |
| Screenshots not captured | Check Hooks.java; screenshots only captured on failure |
| Tests flaky | Add waits, check for timing issues, verify app is stable |

---

## Next Steps

1. ✅ Review this guide
2. Create Page Objects for next page (Dashboard)
3. Write 5-10 smoke tests (TestNG + Cucumber)
4. Run tests and verify Allure reports
5. Scale to 50+ tests following the patterns above
6. Set up CI/CD (GitHub Actions) next week

---

## Questions?
- Check `docs/notes/maven-notes.md` for command reference
- Check `docs/notes/cucumber.md` for Cucumber-specific tips
- Review existing POMs (`LoginPage.java`) for examples
- Run helper scripts in `scripts/` folder for automation
