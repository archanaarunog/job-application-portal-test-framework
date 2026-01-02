# Automation Framework Learning Guide

## Stack
- Java 17, Selenium, TestNG, REST Assured, Maven, Allure, GitHub Actions, IntelliJ.

## Key Concepts
- POM (Page Object Model): Encapsulate page elements and actions in classes (e.g., `LoginPage.java` with locators and methods like `enterEmail()`).
- BDD (Behavior-Driven Development): Write human-readable scenarios in Gherkin (Given/When/Then) in `.feature` files. Use Cucumber to map to code.
- DDT (Data-Driven Testing): Parameterize tests with data (e.g., CSV, TestNG data providers) for reusable scenarios.
- Hybrid: Combine BDD for structure, DDT for data, POM for maintenance.

## Project Directory Structure (canonical single-module Maven layout)
Place `pom.xml` at the repository root (same level as `docs/`, `bugs/`, etc.).

```
JobApplicationPortalTestFramework/
├── pom.xml                       # project POM (Maven coordinates & dependencies)
├── testng.xml                    # TestNG suite (optional)
├── src/
│   ├── main/
│   │   ├── java/                 # (optional) shared main/java code
│   │   └── resources/            # (optional) main resources
│   └── test/
│       ├── java/
│       │   ├── com/archana/meta/pages             # PageObjects (LoginPage, JobsPage)
│       │   ├── com/archana/meta/features          # Cucumber step defs / feature-related code
│       │   ├── com/archana/meta/tests             # TestNG tests or helper tests / runners
│       │   └── com/archana/meta/utils             # DriverFactory, BaseTest, listeners, utils
│       └── resources/
│           ├── features/           # .feature files (BDD)
│           ├── config/             # env properties (dev.properties, qa.properties)
│           └── testdata/           # csv/json test data
├── cucumber-learning/notes/       # learning-only notes (this file)
├── docs/                          # project docs (TestNG + POM only)
```

Notes:
- Keep `pom.xml` at the repo root. Do NOT create a nested module unless multi-module is intended.
- Package names shown are examples; adapt as needed.

## Minimal checklist
1. Import the Maven `pom.xml` in IntelliJ and set SDK to Java 17.
2. Create `src/main/java`, `src/test/java`, `src/test/resources`.
3. Install Cucumber and Gherkin plugins if working with BDD.

## Hybrid to-do (learning)
1. Implement infra: `DriverFactory`, `BaseTest`, `ConfigManager`.
2. Create POM: `LoginPage`.
3. TestNG smoke: `LoginTest`.
4. Add BDD: `login.feature`, `LoginSteps`, `CucumberTestRunner`.
5. Reporting: Allure adapter(s).
6. CI: Maven `test`/`verify` goals in workflow.

## Resources
- Selenium docs, Cucumber.io, TestNG docs, Allure docs, tutorials.
