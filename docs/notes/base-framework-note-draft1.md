# Automation Framework Learning Guide

## Stack
- Java 17, Selenium, TestNG, REST Assured, Maven, Allure, GitHub Actions, IntelliJ.

## Key Concepts
- **POM (Page Object Model)**: Encapsulate page elements and actions in classes (e.g., `LoginPage.java` with locators and methods like `enterEmail()`).
- **BDD (Behavior-Driven Development)**: Write human-readable scenarios in Gherkin (Given/When/Then) in `.feature` files. Use Cucumber to map to code.
- **DDT (Data-Driven Testing)**: Parameterize tests with data (e.g., CSV, TestNG data providers) for reusable scenarios.
- **Hybrid**: Combine BDD for structure, DDT for data, POM for maintenance.

## Project Directory Structure
```
JobApplicationPortalTestFramework/
├── pom.xml (Maven dependencies)
├── src/
│   ├── main/java/
│   │   └── pages/ (POM classes, e.g., LoginPage.java)
│   └── test/
│       ├── java/
│       │   ├── stepdefinitions/ (Cucumber steps, e.g., LoginSteps.java)
│       │   └── runners/ (TestRunner.java)
│       └── resources/
│           └── features/ (BDD .feature files, e.g., login.feature)
├── testng.xml (TestNG suite)
└── test-plan/ (Manual test cases, bugs, etc.)
```

## Step-by-Step Setup Guide
1. **Set Up Project in IntelliJ**:
   - Create a new Maven project.
   - Add `pom.xml` with dependencies: Selenium (4.x), TestNG (7.x), Cucumber-Java/TestNG (7.x), Allure-TestNG (2.x), REST Assured (5.x), WebDriverManager (5.x).
   - Structure as above.

2. **Learn POM for Login**:
   - Create `pages/LoginPage.java`: Use `@FindBy` for locators (e.g., `@FindBy(id="email") WebElement emailField;`).
   - Methods: `enterEmail(String email)`, `clickLogin()`, etc.
   - Initialize with `PageFactory.initElements(driver, this);`.

3. **Learn BDD for Login Scenarios**:
   - Create `src/test/resources/features/login.feature` with scenarios like:
     ```
     Feature: Candidate Login
     Scenario: Valid login
       Given on login page
       When enter "user@email.com" and "pass"
       Then redirected to jobs
     ```
   - Use Cucumber annotations in step definitions (`@Given`, `@When`, `@Then`).

4. **Learn DDT Integration**:
   - In steps, use parameters from scenarios.
   - For tables, use Cucumber's `DataTable` or TestNG `@DataProvider`.

5. **Run Tests**:
   - Create `TestRunner.java` extending `AbstractTestNGCucumberTests` with `@CucumberOptions`.
   - Run via TestNG or Maven (`mvn test`).
   - View reports with Allure (`mvn allure:serve`).

6. **Expand**:
   - Add more pages/features.
   - Use GitHub Actions for CI (add `.github/workflows/ci.yml` with Maven commands).

## Resources
- Selenium: Official docs.
- Cucumber: Cucumber.io.
- TestNG: TestNG docs.
- Allure: Allure docs.
- POM: "Page Object Model Selenium Java".
- Tutorials: YouTube "Automation Step by Step".