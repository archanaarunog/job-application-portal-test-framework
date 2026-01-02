# Day 1 Plan: Solid Base Framework + CI/CD Setup

**Date:** 28 December 2025  
**Goal:** Establish a production-grade (FAANG/MAANG-level) test automation framework for Login test cases with full CI/CD integration.

---

## Current Status

### Working Components
- **Framework Type:** POM (Page Object Model) + TestNG + Selenium 4.10.0
- **Tech Stack:** Java 17, WebDriverManager, Allure CLI, Log4j2
- **Utilities:** DriverFactory/DriverManager, ConfigReader/Manager, WaitUtils, ElementUtils (resilient click fallback), SessionUtils, TestLoggingListener
- **Allure Reports:** Generate via CLI; @Step annotations added for step-wise visibility
- **File Logging:** Log4j2 writes to `logs/test-run.log` and rotates `.log.gz` files

### Issues to Address

#### 1. **3 Test Cases Failing/Skipped**
- **Problem:** Earlier run showed `ERR_CONNECTION_REFUSED` (app not running at `http://127.0.0.1:8000`). Tests skip/fail at setup.
- **Action Items:**
  - Confirm app is running and reachable at `base.url`.
  - Run focused tests (`mvn -Denv=dev -Dtest=LoginTest#testValidLogin test`) and capture failure logs/screenshots.
  - Fix selectors/timeouts if login detection or error message checks fail.
  - Update `LoginPage.isLoggedIn()` and error detection if needed.
- **Owner:** Day 1
- **Priority:** P0

#### 2. **Allure Report Requires Server (Cannot Open `index.html` Directly)**
- **Problem:** Local browsers block AJAX/fetch for `file://` protocol. `allure open` works but user wants standalone HTML viewing.
- **Action Items:**
  - Document the quickest workaround: `python3 -m http.server 8088` in the report folder.
  - Optionally add a wrapper script (`view-report.sh`) that auto-starts a local server and opens the browser.
  - Long-term: host on GitHub Pages or S3 via CI/CD for always-available static hosting.
- **Owner:** Day 1
- **Priority:** P1

#### 3. **Logs: Single Compiled Log per Run + Readability**
- **Problem:** Log rotation produces `.log.gz` files mid-run; user wants one plain `.log` file per test execution without compression.
- **Action Items:**
  - Update `log4j2.xml` to use a timestamped filename per run (e.g., `test-run-20251228_143022.log`) without mid-run rolling or compression.
  - Ensure log level is INFO or DEBUG for all `com.archana.*` packages.
  - Add explicit log statements in tests for positive/negative flows (e.g., `log.info("Valid login flow started")`, `log.info("Expected error appeared: {}")`)
- **Owner:** Day 1
- **Priority:** P0

#### 4. **Detailed Positive & Negative Case Logs**
- **Problem:** Current logs show listener start/finish events but lack granular step logs for each action and assertion.
- **Action Items:**
  - Add `log.info()` in `LoginPage` methods (enterEmail, clickLogin, getErrorText, etc.) for each action.
  - Add `log.info()` in test methods for each assertion and data-driven row.
  - Attach step-level logs to Allure (already attached on failure; consider attaching full log on success too).
  - Ensure logs show: "Entering email: validUser@example.com", "Clicking login button", "Dashboard marker visible: true", etc.
- **Owner:** Day 1
- **Priority:** P1

---

## Framework Quality Audit (FAANG/MAANG Standards)

### Current Strengths
- ✅ Clean POM separation (pages in `src/main/java`, tests in `src/test/java`)
- ✅ Centralized utilities (Config, Wait, Element, Session)
- ✅ Custom TestNG listener for screenshots and log attachments
- ✅ Allure step annotations for report clarity
- ✅ Resilient click with fallback strategies
- ✅ Log4j2 file logging configured

### Gaps to Address for Enterprise Grade

#### 5. **Robust Waits & Retries**
- **Current:** 10s default wait; recently added 15s for dashboard. May still flake.
- **Improvement:** Add configurable wait timeouts, explicit waits for state changes (e.g., wait for URL change post-login), and optional retry logic for transient failures (TestNG `@Test(retryAnalyzer=...)`).
- **Priority:** P1

#### 6. **Config Management Consolidation**
- **Current:** Both `ConfigReader` and `ConfigManager` exist with identical logic.
- **Improvement:** Merge into one; delete duplicate. Add validation for required keys (throw clear error if `base.url` missing).
- **Priority:** P2

#### 7. **Evidence Collection on Failure**
- **Current:** Screenshot attached; log snapshot attached on failure/skipped.
- **Improvement:** Also attach cookies + localStorage JSON on failure (use `SessionUtils`). This helps debug session/auth issues (e.g., remember-me).
- **Priority:** P1

#### 8. **Parallel Execution Readiness**
- **Current:** ThreadLocal driver in `DriverManager` — ready for parallel.
- **Improvement:** Test with `parallel="methods"` in `testng.xml`. Ensure logs and screenshots are thread-safe (timestamped filenames). Add unique identifiers to Allure attachments.
- **Priority:** P2

#### 9. **Documentation & Onboarding**
- **Current:** No README with setup/run instructions.
- **Improvement:** Write comprehensive `README.md` with:
  - Tech stack overview
  - Folder structure and design patterns
  - Setup (Java 17, Maven, Allure CLI install)
  - Run commands (smoke, regression, single test)
  - Allure report generation and viewing
  - Log locations and how to read them
  - CI/CD badge and integration details
  - Contribution guide
- **Priority:** P0

#### 10. **Clear Test Data & Assertions**
- **Current:** Tests use hardcoded credentials (`validUser@example.com`).
- **Improvement:** Externalize test data (CSV/JSON or properties). Add clear assertion messages for every check. Group tests by suite (smoke, regression).
- **Priority:** P2

---

## CI/CD Pipeline Setup (GitHub Actions)

### Requirements
- Trigger on push/PR to `main` and manual workflow dispatch
- Run smoke suite on every commit; full regression nightly or on-demand
- Generate and publish Allure report to GitHub Pages or as workflow artifact
- Add build status badge to README

### Action Items
- **Step 1:** Create `.github/workflows/ci.yml`
  - Checkout code
  - Setup Java 17
  - Cache Maven dependencies
  - Run `mvn clean test -Denv=dev` (or smoke suite via `-DsuiteXmlFile=testng-smoke.xml`)
  - Generate Allure report: `allure generate target/allure-results --clean`
  - Publish report to GitHub Pages or upload as artifact
  - Post status badge to PR/README
- **Step 2:** Configure GitHub Pages for report hosting (or use Actions artifact download link)
- **Step 3:** Add CI badge to README: `![CI](https://github.com/archanaarunog/WorkdayJobApplicationAutomation/actions/workflows/ci.yml/badge.svg)`
- **Step 4:** Test pipeline end-to-end with a dummy commit
- **Priority:** P0 (after base framework is solid)

---

## Is This Framework FAANG/MAANG Worthy?

### Checklist (Current Score: ~60%)

| Criteria | Status | Notes |
|----------|--------|-------|
| Clean architecture (POM, separation of concerns) | ✅ | Yes — pages, tests, utils well separated |
| Reliable waits and retries | ⚠️ | Custom waits added; needs retry analyzer and flake tolerance |
| Comprehensive logging (positive + negative flows) | ❌ | Listener logs start/finish; need granular step logs |
| Evidence collection (screenshots, logs, session artifacts) | ⚠️ | Screenshots + log snapshot on failure; missing cookie/localStorage |
| Config management (DRY, validated, environment-aware) | ⚠️ | Duplicate ConfigReader/Manager; needs merge and validation |
| Parallel execution support | ⚠️ | ThreadLocal driver ready; not tested with parallel suite |
| CI/CD integration | ❌ | Not set up yet |
| Clear documentation (README, onboarding guide) | ❌ | Missing |
| Test data externalization | ❌ | Hardcoded credentials |
| Allure report generation + hosting | ⚠️ | CLI works; needs hosting automation |
| Single consolidated log per run | ❌ | Currently produces .gz rolled logs mid-run |
| Code quality (DRY, readable, maintainable) | ✅ | Clean, idiomatic Java |

### Verdict
- **Current State:** Solid foundation with good design patterns. ~60% FAANG-ready.
- **Gaps:** Logging granularity, evidence collection, CI/CD, documentation, test data management.
- **After Day 1 Plan Completion:** Will be 85–90% FAANG-ready. Adding data-driven external configs and parallel suite testing will push it to 95%+.

---

## Execution Plan (Prioritized)

### Phase 1: Fix Core Issues (Today)
1. **Fix 3 failing/skipped tests** (P0) — 1 hour
   - Start app at base.url
   - Run focused test and debug failures
   - Adjust selectors/timeouts as needed
2. **Consolidate logs per run** (P0) — 30 min
   - Update log4j2.xml to use timestamped file per run without mid-run rolling
3. **Add detailed step logs** (P1) — 1 hour
   - Add log.info() in LoginPage methods and test assertions
   - Verify logs show positive and negative flows clearly
4. **Enable standalone Allure viewing** (P1) — 30 min
   - Add `view-report.sh` script with python3 http.server
   - Document in README

### Phase 2: Quality Enhancements (Today/Tomorrow)
5. **Attach cookies/localStorage on failure** (P1) — 30 min
6. **Merge ConfigReader/ConfigManager** (P2) — 20 min
7. **Add retry analyzer** (P1) — 30 min
8. **Write comprehensive README** (P0) — 1 hour

### Phase 3: CI/CD Setup (Tomorrow)
9. **Create GitHub Actions workflow** (P0) — 1.5 hours
   - ci.yml with build, test, Allure generation, Pages deployment
10. **Test and validate pipeline** (P0) — 30 min
11. **Add CI badge to README** (P0) — 5 min

### Phase 4: Final Polish (Optional, Post-CI/CD)
12. **Externalize test data** (P2)
13. **Parallel execution test** (P2)
14. **Performance benchmarks** (P3)

---

## Success Criteria
- ✅ All login tests pass (0 failures, 0 skipped)
- ✅ Single plain `.log` file per test run with readable step-by-step logs for positive and negative cases
- ✅ Allure report viewable standalone (via script or direct hosting)
- ✅ GitHub CI/CD pipeline runs on push, generates report, and publishes to Pages
- ✅ README is complete with setup, run, and troubleshooting instructions
- ✅ Framework scores 85%+ on FAANG quality checklist

---

## Next Immediate Steps (Right Now)
1. Start app at `http://127.0.0.1:8000` (or update `base.url` in `dev.properties`)
2. Run `mvn clean test -Denv=dev` and capture output
3. Fix log4j2.xml to produce single timestamped log per run
4. Add step-level log statements to LoginPage and tests
5. Re-run tests and verify logs are readable and detailed
6. Proceed to CI/CD setup once all tests are green

---

**Owner:** Archana  
**Reviewed By:** AI Pair Programmer  
**Status:** Active — Day 1 in progress

---

## Appendix: Pending Items & 15-Min Action Plan

### Pending Items (Concise)
- **Evidence & Reporting:** Attach cookies/localStorage, page source, console logs on failures; add Allure labels (epic/feature/story/severity), categories, and enable history/trends.
- **Logging & Steps:** Add `log.info()` inside `LoginPage` methods and test assertions for clear positive/negative flow messages.
- **Config & Environments:** Merge `ConfigReader` and `ConfigManager`; validate required keys (e.g., `base.url`) with explicit errors.
- **Stability & Flake Control:** Add RetryAnalyzer for transient failures; strengthen state waits (URL change, post-login markers) and centralize configurable timeouts.
- **Documentation & Scripts:** Add comprehensive README; create `view-report.sh` (local server + open); optional wrappers for `allure generate/open`.
- **Test Data & Suites:** Externalize credentials/test data; align `testng-smoke.xml` and `testng-regression.xml` with groups/tags.
- **Code Hygiene:** Remove placeholders/duplications; consider formatter/checkstyle; keep dependencies current.
- **Parallel & Cross-Browser:** Low priority (per guidance). Verify later with TestNG parallel and Chrome/Firefox matrix.

### 15-Min Action Plan (Priority Now)
1. **Step-level logs:** Add `log.info()` in `LoginPage` actions (enterEmail, enterPassword, clickLogin, getErrorText, isLoggedIn) and in test assertions.
2. **Failure evidence:** Wire `SessionUtils` to attach cookies/localStorage and browser console logs on failures (extend the TestNG listener).
3. **Config consolidation:** Merge `ConfigReader/ConfigManager`; validate `base.url` and fail fast with clear messages.
4. **Retry scaffolding:** Introduce a basic TestNG `RetryAnalyzer` and `IAnnotationTransformer` (disabled by default, ready to enable).
5. **Report viewing script:** Add a simple `view-report.sh` using `python3 -m http.server` to serve Allure reports locally.
6. **Allure labels:** Add `@Epic`, `@Feature`, `@Story`, and `@Severity` annotations to key tests for richer report navigation.
7. **README skeleton:** Add a starter README section outlining setup, run commands, log locations, and Allure viewing.

Note: Cross-browser and parallel execution are intentionally set to lower priority and will be scheduled after core stability and evidence improvements.
