# Automation Framework Learning Guide

## Stack
- Java 17, Selenium, TestNG, REST Assured, Maven, Allure, GitHub Actions, IntelliJ.

## Key Concepts
- **POM (Page Object Model)**: Encapsulate page elements and actions in classes (e.g., `LoginPage.java` with locators and methods like `enterEmail()`).
- **BDD (Behavior-Driven Development)**: Write human-readable scenarios in Gherkin (Given/When/Then) in `.feature` files. Use Cucumber to map to code.
- **DDT (Data-Driven Testing)**: Parameterize tests with data (e.g., CSV, TestNG data providers) for reusable scenarios.
- **Hybrid**: Combine BDD for structure, DDT for data, POM for maintenance.

## Project Directory Structure (canonical single-module Maven layout)
Place `pom.xml` at the repository root (same level as `docs/`, `bugs/`, etc.). This is the layout to document and create inside IntelliJ.

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
├── docs/                          # your existing docs and notes
├── bugs/
├── test-plan/
└── .github/                       # CI workflows (optional)
```

Notes:
- Keep `pom.xml` at the repo root. Do NOT create a nested module folder unless you want a multi-module project.
- Package names shown are examples; adapt to your org naming but keep the src/test/java -> package structure.

----

FINAL CHECKLIST (minimal, follow these in order)
1. Confirm `pom.xml` is at the project root (next to `docs/`, `bugs/`).
2. In IntelliJ: import the `pom.xml` (see GUI steps below). This creates the IntelliJ module that maps to your repo root.
3. Create the `src/main/java`, `src/main/resources`, `src/test/java`, and `src/test/resources/features` folders using IntelliJ New → Directory if they are missing.
4. In Project view, mark the directories appropriately (Sources / Test Sources / Resources / Test Resources) using right-click → Mark Directory As.
5. Set Project SDK to Java 17 (File → Project Structure → Project).
6. Enable Maven auto-import and refresh the Maven window (View → Tool Windows → Maven → Refresh / Enable Auto-Import).
7. Install helpful plugins: Cucumber for Java, Gherkin (Settings → Plugins). - done
8. Create run configurations (Run → Edit Configurations): Maven (goal `test -Denv=dev -Dbrowser=chrome`) or TestNG using `testng.xml`.

----

GUI-only steps to make IntelliJ recognize the project and folders (no terminal commands)
Follow these steps exactly inside IntelliJ on macOS (menu names are the same on Windows/Linux):

1) Open the Project tool window and select the "Project" view
- View → Tool Windows → Project (or press ⌥1).
- At the top of the Project tool window there's a small dropdown; choose "Project" so you see module nodes and the real folder tree.

2) Import the Maven `pom.xml` (this creates the module if needed)
- Option A (recommended): Open `pom.xml` in the editor. IntelliJ will usually show a banner: "Maven projects need to be imported" — click "Import Changes" or "Enable Auto-Import".
- Option B: View → Tool Windows → Maven → Click the "+" / Import icon and select the `pom.xml` at the repo root.
- After import, the Project root should be treated as a Maven module (you'll see Maven lifecycles in the Maven tool window).

3) If IntelliJ still shows no module node: add a module explicitly
- File → New → Module... → choose "Maven" → Next.
- For "Module name" use the project name or leave default. For "Module location" select the project root folder (so the module maps to root). Finish.
- This creates/links a module. If you already have `pom.xml`, IntelliJ will associate it.

4) Create missing source/test folders using the IDE (GUI)
- In the Project view, right-click the project root or the `src` folder → New → Directory.
- Create: `src/main/java`, `src/main/resources`, `src/test/java`, `src/test/resources`, and `src/test/resources/features` (create only those you need).

5) Mark the folders so IntelliJ uses them correctly (important)
- Right-click `src/main/java` → Mark Directory As → Sources Root
- Right-click `src/main/resources` → Mark Directory As → Resources Root
- Right-click `src/test/java` → Mark Directory As → Test Sources Root
- Right-click `src/test/resources` → Mark Directory As → Test Resources Root

6) Refresh Maven and enable auto-import
- View → Tool Windows → Maven → Click the Refresh icon (circular arrows).
- In the Maven tool window or Preferences → Build Tools → Maven → Importing, enable "Import Maven projects automatically" if desired.

7) Set Project SDK
- File → Project Structure → Project → Project SDK: choose your installed JDK 17.

8) Install Cucumber/Gherkin plugins (optional but recommended)
- Preferences/Settings → Plugins → Marketplace → search "Cucumber for Java" and "Gherkin" → Install → Restart IDE if prompted.

9) Create run/debug configurations (GUI)
- Run → Edit Configurations → + → Maven (or TestNG)
  - For Maven: Working directory = project root, Command line = `test -Denv=dev -Dbrowser=chrome`.
  - For TestNG: pick `testng.xml` as the suite file.

10) Verify everything
- Open View → Tool Windows → Maven: you should see lifecycle nodes and dependencies.
- Project Structure → Modules should list your module mapped to the project root.
- The `src/test/resources/features` folder should be visible and marked as Test Resources Root so Cucumber can find `.feature` files.


## Login test — Hybrid to-do checklist (concise & scalable)

Follow these exact tasks to implement the Login testing story using a hybrid POM + BDD + DDT approach; do them in order and keep each item small.

1) Project skeleton (confirm)
- Ensure these packages exist under `src/test/java`: `com.archana.meta.pages`, `com.archana.meta.tests`, `com.archana.meta.utils`, `com.archana.meta.stepdefinitions`, `com.archana.meta.runners`.
- Ensure resources: `src/test/resources/features`, `src/test/resources/config`, `src/test/resources/testdata`.

========================
2) Config and conventions (one-shot)
- Create `src/test/resources/config/dev.properties` with `base.url`, `browser=chrome`, `timeout.seconds`.
- Naming convention: `*Page` for pages, `*Test` for TestNG tests, `*Steps` for step defs, `*Runner` for runners.

3) Core infra (implement before tests)
- `DriverFactory` (ThreadLocal<WebDriver>, WebDriverManager, read `-Dbrowser` fallback to properties).
- `BaseTest` (TestNG @BeforeMethod/@AfterMethod lifecycle, screenshot-on-failure attachment hook).
- `ConfigManager` (simple properties loader with System property overrides).

4) Page Object: `LoginPage` (POM)
- Minimal API: `open()`, `enterEmail(String)`, `enterPassword(String)`, `clickLogin()`, `getErrorText()`, `isLoggedIn()`.
- Keep locators private; methods return page objects or booleans for assertions.

5) TestNG (POM + DDT) tests
- Implement `LoginTest` with:
  - `@Test` valid-login smoke test (single fast case).
  - `@DataProvider` inline `badLogins` for invalid cases (email blank, password blank, invalid email format, wrong password).
  - Assertions: URL change or presence of dashboard element for success; error text for failures.
- Group tests: `@Test(groups={"smoke","ui"})` and negative tests `groups={"negative"}`.

6) Cucumber BDD (thin layer that reuses POM)
- `login.feature` with scenarios:
  - Valid login (tag `@smoke @bdd`) 
  - Invalid login examples (Scenario Outline + Examples, tag `@negative`)
  - Password reset flow (tag `@wip`/`@manual` if needed)
- `LoginSteps` delegate to `LoginPage` (no business logic in steps).
- `CucumberTestRunner` extends `AbstractTestNGCucumberTests` with `@CucumberOptions` (features path, glue, tags, plugin for Allure).

7) API-level login tests (optional but recommended)
- Add `com.archana.meta.tests.api.LoginApiTest` using REST Assured to validate login endpoints (happy & error paths). Keep them separate from UI tests and tag as `api`.

8) Orchestration and selection
- `testng.xml` contains separate `<test>` sections for POM/TestNG tests and for the Cucumber runner; use TestNG groups or Cucumber tags to select subsets.
- Add Maven profiles (e.g., `-Pui`, `-Papi`, `-Pfull`) to run targeted suites in CI.

9) Test data & DDT strategy
- Small datasets: inline TestNG `@DataProvider` for quick tests.
- For scalable data, place CSV/JSON in `src/test/resources/testdata/` and add a reusable `TestDataReader` util.
- For BDD parameterization use Scenario Outline + Examples in `.feature` files.

10) Reporting, artifacts & CI
- Use Allure TestNG adapter and attach screenshots/logs on failure.
- In CI, produce `target/allure-results` and upload as workflow artifacts; generate/publish report in CD pipeline if needed.

11) Scalable design notes (follow these rules)
- Keep step definitions thin; reuse the Page Objects.
- Put reusable UI components under `com.archana.meta.pages.components`.
- Use explicit waits (WaitUtils) and avoid Thread.sleep.
- Protect credentials — use CI secrets and environment variables (never commit secrets).
- Keep tests idempotent and stateless — create and clean test data where possible.

12) Minimal set of Login test cases to automate (pointwise)
- TC-01: Valid login (happy path) — smoke — verify dashboard loads.
- TC-02: Invalid email format — verify inline validation/error.
- TC-03: Empty email or password — verify error messages.
- TC-04: Incorrect password — verify authentication error.
- TC-05: Password reset request flow (open reset page, submit known email, verify confirmation message).
- TC-06: Remember-me/session persistence (optional) — login, close and re-open, verify session.
- TC-07: Security negative checks (simple SQL injection string, XSS input reflected) — assert safe handling.
- TC-08: API login success/failure parity (compare UI and API responses for basic credentials).

13) How to progress (one-line each)
- Implement infra (DriverFactory/BaseTest) → implement `LoginPage` → write TestNG smoke test → add DataProvider negative tests → create `login.feature` and stepdefs → run runner → add Allure and CI profile.

14) Checklist to add to README or notes (copy-ready)
- Add this short todo under `docs/` or README: "Login: POM + TestNG smoke + DDT negatives + Cucumber BDD + API parity + Allure + CI profile".


## (existing content continues)

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
