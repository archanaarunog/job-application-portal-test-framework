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
This document moved.

The main framework uses TestNG + POM only. For Cucumber/BDD materials, see:
- `cucumber-learning/notes/cucumber.md`

If you landed here from an old link, update your bookmarks to the new location. 
BDD content is intentionally kept outside `docs/` to avoid confusion.
	System.err.println("Window maximize not supported: " + e.getMessage());
}
```

Place the `manage().window().maximize()` call immediately after the driver is created but inside try/catch so remote or headless setups that don't support resize won't fail the init.

Option B — Call from `BaseTest.setUp()` (if you prefer):

```java
@BeforeMethod(alwaysRun = true)
public void setUp() {
	WebDriver drv = DriverFactory.getDriver();
	try {
		drv.manage().window().maximize();
	} catch (Exception e) {
		// remote or headless may throw; log and continue
	}
}
```

Notes:
- For headless runs use `opts.addArguments("--headless=new", "--window-size=1920,1080")` (old `--headless` may be deprecated depending on Chrome version).
- `driver.manage().window().maximize()` is WebDriver standard; `--start-maximized` is a browser start arg — using both is the most robust approach across local, CI, and remote providers.

10) Clarify `loginPage.open(...)` vs `driver.get(...)`
- `loginPage.open(base)` is a Page Object wrapper you should implement in `LoginPage`. Inside it, call `driver.get(base + "/login")` and perform any wait/checks. Keep step defs calling the POM wrapper so low-level driver calls remain inside Page Objects.

11) Troubleshooting & tips
- Glue not found: ensure step defs package is in `glue`.
- Steps not matched: exact text match required for literal steps, else use regex/expressions.
- Allure results empty: check `target/allure-results` is populated and adapter plugin is configured.
- Browser not closing: ensure `DriverFactory.quitDriver()` is called in `@After` hook and not swallowed by exceptions.

12) Final notes & CI
- Use tags (`@smoke`, `@negative`) to map BDD scenarios to TestNG groups in CI job definitions.
- Prefer `--start-maximized` + `manage().window().maximize()` for consistent viewport sizing locally and in many CI providers. For headless use `--window-size=1920,1080`.

Paste this entire file into `docs/notes/cucumber.md` in your repo. It includes code snippets you can copy to the real Java files (Step defs, Hooks, Runner) or use as documentation.

