# SDET Roadmap Discussions

## Tech Stack Decision
- **Chosen**: Java - For FAANG alignment, DSA practice, and enterprise SDET standards.
- **Full Stack**:
  - Language: Java 17 (LTS, modern features).
  - UI Automation: Selenium WebDriver with ChromeDriver.
  - API Testing: REST Assured.
  - Testing Framework: TestNG (better for data-driven and parallel tests).
  - Build Tool: Maven (standard for Java projects).
  - Reporting: Allure Framework.
  - CI/CD: GitHub Actions.
  - IDE: IntelliJ IDEA (Community Edition, free).
  - Version Control: Git.

## Brief Understanding of Each Tech Stack Component
- **Java 17**: Programming language for writing tests. Key: OOP (classes, inheritance), collections (List, Map for DSA), lambdas. Learn basics via tutorials.
- **Selenium WebDriver**: Automates browser interactions. Key: findElement, click, sendKeys. Use with ChromeDriver for Chrome.
- **ChromeDriver**: Browser driver for Selenium. Key: Download matching version, set system property.
- **REST Assured**: Library for API testing. Key: given().when().then() syntax for HTTP requests/responses.
- **TestNG**: Testing framework. Key: @Test annotations, data providers, assertions, parallel execution.
- **Maven**: Build tool. Key: pom.xml for dependencies, mvn test to run tests.
- **Allure**: Reporting tool. Key: @Step annotations, generates HTML reports with screenshots.
- **GitHub Actions**: CI/CD. Key: YAML workflows for build, test, deploy on push/PR.
- **IntelliJ IDEA**: IDE. Key: Code completion, debugging, Maven integration.

## IDE Choice
- VS Code: Possible with Java extensions (e.g., Extension Pack for Java), but basic for SDET. Good if you prefer it.
- IntelliJ: Recommended for Java/SDET—better debugging, TestNG support, Maven integration. Use Community Edition (free).

## Is Tech Stack Sufficient for FAANG/MAANG?
Yes—Java + Selenium + TestNG + REST Assured + Maven + Allure + GitHub Actions is standard for SDET roles at top companies. Shows enterprise skills.

## Keywords Documented
- Java: OOP, Collections, DSA (Arrays, LinkedList, HashMap)
- Selenium: WebDriver, Page Object Model, Locators (ID, XPath, CSS)
- API: REST, HTTP methods, JSON, Assertions
- Testing: Data-driven, Parallel, Assertions
- Build: Maven, Dependencies, Lifecycle
- CI/CD: Workflows, Triggers, Secrets
- Reporting: Allure, Screenshots, History

## High-Level Plan (Polished)
- **Overall Goal**: Build a robust, generic SDET framework in Java for UI/API testing, CI/CD, and portfolio showcase, with manual test cases as foundation.
- **Phases** (Adjusted for pacing):
  - Phase 1 (Week 1 - Extended focus): Environment setup, Java basics, manual test cases, Selenium/POM foundation.
  - Phase 2 (Week 2): API testing integration, data-driven tests, reporting.
  - Phase 3 (Week 3): CI/CD pipeline, advanced features, documentation.
- **Key Deliverables**: Functional framework with 10+ tests (manual + automated), Allure reports, GitHub Actions deployment, applied to Workday app.
- **Success Metrics**: All tests pass locally/CI, framework reusable, manual test cases documented.
- **Timeline**: 3 weeks, 2h weekdays + 4h weekends (Week 1 heavier on learning).
- **Risks**: Java learning curve; mitigate with daily practice and manual testing prep.

## Anything Missed?
- Manual test cases: Added to Week 1.
- Application to real project: Include testing on Workday portal.
- DSA integration: Practice collections in Java during Week 1.
- Portfolio prep: Screenshots, README updates.

## Low-Level Week 1 Plan
- **Focus**: Environment setup, Java basics, manual test cases (your primary writing), Selenium/POM foundation.
- **Daily Breakdown** (2h weekdays, 4h weekends):
  - Day 1-2: Install JDK 17, Maven, IntelliJ. Set up project structure (Maven archetype). Learn Java basics (OOP, variables).
  - Day 3-4: Write manual test cases for Workday app (10-15 cases: login, job search, application submit). Document in Excel/Google Sheets.
  - Day 5-7: Selenium setup (ChromeDriver), basic WebDriver scripts. Start POM (BasePage class).
- **Milestones**:
  - End Day 2: Project created, basic Java program runs.
  - End Day 4: Manual test cases completed and reviewed.
  - End Week 1: 2-3 POM classes, 1 basic Selenium test passing.
- **Tasks**:
  - Manual: Plan and write test cases (functional, edge cases).
  - Automated: Setup dependencies in pom.xml, practice DSA (arrays in Java).
- **Success Criteria**: Manual cases cover main flows, basic automation runs without errors.

## Next: Discuss Week 1 details or proceed to Week 2.