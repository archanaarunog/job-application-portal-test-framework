## Implementation Plan: TestNG + Page Object Model (POM) Automation Framework

### Objective
Refactor and maintain a robust, scalable automation framework using Java, Selenium WebDriver, TestNG, and the Page Object Model (POM) design pattern. Remove all legacy, meta, and Cucumber/BDD code from the main framework. Future AI/self-healing features will be managed in a separate repository.

### Key Steps
1. **Project Structure**
   - Organize code into `src/main/java` (core framework, page objects, utils) and `src/test/java` (TestNG test classes).
   - All documentation files reside in `docs/`.
   - Legacy/meta/Cucumber code is isolated or removed.

2. **Core Components**
   - `BaseTest`: Abstracts test setup/teardown, driver lifecycle.
   - `BasePage`: Encapsulates common page actions.
   - `DriverManager` (ThreadLocal): Manages WebDriver instances per thread.
   - `ConfigReader`, `WaitUtils`, `ElementUtils`, `SessionUtils`, `LogUtils`: Utility classes for configuration, waits, element actions, session, and logging.

3. **TestNG Integration**
   - All tests use TestNG annotations and assertions.
   - Test suites defined in XML files (e.g., `testng.xml`).
   - Allure reporting integrated for test results.

4. **Page Object Model (POM)**
   - Each web page is represented by a dedicated Page class.
   - Page classes contain locators and methods for page interactions.
   - Tests interact only with Page classes, not directly with WebDriver.

5. **Build & CI/CD**
   - Maven for build/test lifecycle.
   - GitHub Actions for CI/CD (see CI_CD_SETUP.md).

6. **Future Enhancements**
   - AI/self-healing features will be developed in a separate repository.

### Migration Checklist
- [x] Refactor all tests to TestNG + POM.
- [x] Remove legacy/meta/Cucumber code from main framework.
- [x] Update documentation in `docs/`.
- [x] Validate build and test execution.
- [x] Plan for future AI/self-healing repo.

---