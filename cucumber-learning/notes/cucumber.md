# Cucumber BDD Guide — Step-by-step

Overview
- Purpose: Add a Cucumber BDD layer that reuses existing Page Objects (`LoginPage`) and the `DriverFactory`.
- Location conventions:
  - Feature files: `src/test/resources/features/`
  - Step definitions: `src/test/java/com/archana/meta/stepdefinitions/`
  - Hooks: `src/test/java/com/archana/meta/hooks/`
  - Runners: `src/test/java/com/archana/meta/runners/`

1) Add dependencies (pom.xml)
- Why: Cucumber + Allure need test-scoped dependencies. If your `pom.xml` already includes these, verify versions match the project's other test libs.
- Add inside `<dependencies>` (adjust versions if your project uses other majors):

```xml
<!-- Cucumber -->
<dependency>
  <groupId>io.cucumber</groupId>
  <artifactId>cucumber-java</artifactId>
  <version>7.11.0</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>io.cucumber</groupId>
  <artifactId>cucumber-testng</artifactId>
  <version>7.11.0</version>
  <scope>test</scope>
</dependency>

<!-- Allure Cucumber Adapter -->
<dependency>
  <groupId>io.qameta.allure</groupId>
  <artifactId>allure-cucumber7-jvm</artifactId>
  <version>2.20.0</version>
  <scope>test</scope>
</dependency>
```

Run a quick validate to fetch deps:

```bash
mvn -q -DskipTests validate
```

2) Feature file example
- Create `src/test/resources/features/login.feature` with this content:

```
@bdd
Feature: Login

  @smoke @ui
  Scenario: Valid login shows dashboard
	Given I open the login page
	When I login with "alice5678@example.com" and "SecurePass@123"
	Then I should see the dashboard

  @negative
  Scenario Outline: Invalid login shows an error
	Given I open the login page
	When I login with "<email>" and "<password>"
	Then I should see an error containing "<error>"

	Examples:
	  | email                | password   | error                       |
	  | ""                   | ""         | Email required              |
	  | "invalidemail"       | "password" | Invalid email               |
	  | "user@example.com"   | "short"    | Password must be at least   |
```

3) Step definitions (reuse `LoginPage`)
- Create `src/test/java/com/archana/meta/stepdefinitions/LoginSteps.java` (adjust package if needed):

```java
package com.archana.meta.stepdefinitions;

import io.cucumber.java.en.*;
import static org.testng.Assert.*;

import com.archana.meta.pages.LoginPage;
import com.archana.meta.utils.DriverFactory;
import com.archana.meta.utils.ConfigManager;
import org.openqa.selenium.WebDriver;

public class LoginSteps {
	private WebDriver driver = DriverFactory.getDriver();
	private LoginPage loginPage = new LoginPage(driver);

	@Given("I open the login page")
	public void i_open_the_login_page() {
		String base = ConfigManager.get("base.url");
		loginPage.open(base); // LoginPage.open should call driver.get(base + "/login") internally
	}

	@When("I login with {string} and {string}")
	public void i_login_with_and(String email, String password) {
		loginPage.enterEmail(email).enterPassword(password).clickLogin();
	}

	@Then("I should see the dashboard")
	public void i_should_see_the_dashboard() {
		boolean ok = loginPage.waitForTransientSuccessMessage();
		assertTrue(ok, "Expected dashboard or success message");
	}

	@Then("I should see an error containing {string}")
	public void i_should_see_an_error_containing(String text) {
		String err = loginPage.getErrorText();
		assertNotNull(err, "Expected an error message");
		assertTrue(err.toLowerCase().contains(text.toLowerCase()),
				   "Error should contain: " + text);
	}
}
```

4) Hooks (attach artifacts + driver lifecycle)
- Create `src/test/java/com/archana/meta/hooks/Hooks.java`:

```java
package com.archana.meta.hooks;

import io.cucumber.java.*;
import com.archana.meta.utils.DriverFactory;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;

public class Hooks {

	@Before
	public void beforeScenario(Scenario scenario) {
		DriverFactory.getDriver(); // ensure driver is initialised
	}

	@After
	public void afterScenario(Scenario scenario) {
		try {
			if (scenario.isFailed()) {
				byte[] screenshot = ((TakesScreenshot) DriverFactory.getDriver())
						.getScreenshotAs(OutputType.BYTES);
				Allure.addAttachment("screenshot-" + scenario.getName(),
						new ByteArrayInputStream(screenshot));
				// attach page source
				String pageSource = DriverFactory.getDriver().getPageSource();
				Allure.addAttachment("page-source-" + scenario.getName(),
						"text/html", pageSource, ".html");
			}
		} catch (Exception e) {
			System.err.println("Failed to attach artifacts: " + e.getMessage());
		} finally {
			DriverFactory.quitDriver();
		}
	}
}
```

5) TestNG-compatible Cucumber runner
- Create `src/test/java/com/archana/meta/runners/CucumberTestRunner.java`:

```java
package com.archana.meta.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
	features = "src/test/resources/features",
	glue = {"com.archana.meta.stepdefinitions", "com.archana.meta.hooks"},
	plugin = {
		"pretty",
		"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
	}
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {
	@Override
	@DataProvider(parallel = false) // set true to try parallel scenarios
	public Object[][] scenarios() {
		return super.scenarios();
	}
}
```

6) Run commands
- Run only the Cucumber runner:
```bash
mvn -Dtest=com.archana.meta.runners.CucumberTestRunner test
```
- Run with tag filter (smoke only):
```bash
mvn -Dcucumber.filter.tags="@smoke" -Dtest=com.archana.meta.runners.CucumberTestRunner test
```

7) Generate Allure report
```bash
mvn allure:serve
```

8) Parallel scenarios
- To enable parallel per-scenario set `@DataProvider(parallel = true)` in `CucumberTestRunner` and ensure `DriverFactory` uses a ThreadLocal driver.

9) Window sizing (maximize)
- Use both `--start-maximized` Chrome arg and `driver.manage().window().maximize()` in your driver setup for best cross-env consistency.

Notes
- This file is intentionally placed in `cucumber-learning/notes/` because the main framework is TestNG + POM only.
