## CI/CD Setup Guide: TestNG + POM Automation Framework

### Objective
Establish a reliable CI/CD pipeline for the TestNG + Page Object Model (POM) automation framework using Maven and GitHub Actions. No legacy/meta/Cucumber code is present in the main framework.

### Steps
1. **Maven Build**
   - Ensure `pom.xml` is configured for TestNG, Selenium, Allure, and required dependencies.
   - Use Maven commands for build and test execution:
     ```sh
     mvn clean test
     ```

2. **TestNG Suites**
   - Define test suites in XML files (e.g., `testng.xml`, `testng-smoke.xml`, `testng-regression.xml`).
   - Organize tests for targeted execution.

3. **Allure Reporting**
   - Configure Allure plugin in `pom.xml`.
   - Generate reports after test runs:
     ```sh
     mvn allure:serve
     ```

4. **GitHub Actions Workflow**
   - Create `.github/workflows/ci.yml` for automated build/test/reporting.
   - Example workflow:
     ```yaml
     name: CI
     on: [push, pull_request]
     jobs:
       test:
         runs-on: ubuntu-latest
         steps:
           - uses: actions/checkout@v2
           - name: Set up JDK 11
             uses: actions/setup-java@v2
             with:
               java-version: '11'
           - name: Build and Test
             run: mvn clean test
           - name: Generate Allure Report
             run: mvn allure:serve
     ```

5. **Maintenance**
   - Keep dependencies up to date.
   - Monitor CI runs and address failures promptly.

### Notes
- No legacy/meta/Cucumber code in main framework.
- For future AI/self-healing, see separate repo plans.

---