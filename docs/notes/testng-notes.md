## TestNG quick notes for this project

Goal: short, copy-paste examples you can use and share. Focus: why exclude `flaky` from regression, how TestNG suite files work here, and exact mvn commands to run different scenarios.

### 1) Why exclude `flaky` from regression
- Flaky tests are unstable and create noise in the full regression run. Excluding them keeps regression results meaningful.
- Workflow: mark unstable tests with `@Test(groups = {"flaky"})`, exclude `flaky` in the large/slow regression suite, and track/repair those tests separately.

When to exclude:
- Nightly/full-regression: exclude flaky tests to reduce false alarms.
- Dedicated run: run flaky tests separately and collect diagnostics.

### 2) Suite files in this repo (how they behave)
- `testng.xml` at repo root: canonical full-suite file. When `mvn test` is run and Surefire points to a suite file, it will execute classes listed there.
- `testng-smoke.xml`: small, fast suite for PRs (only critical tests).
- `testng-regression.xml`: full run (commonly excludes `flaky`).

You can use a single `testng.xml` with multiple `<test>` entries or multiple suite files (recommended for CI clarity). Both are supported.

### 3) How groups work (minimal)
- Annotate tests:
	- Method level: `@Test(groups = {"smoke","ui"})`
	- Class level: `@Test(groups = {"ui"}) public class LoginTest { ... }`
- Include/exclude in suite files using `<groups>` or use CLI with `-Dgroups` / `-DexcludedGroups`.

### 4) Exact commands (copy/paste) and what they do

- Run the default suite (uses `pom.xml`/Surefire configuration or `testng.xml` if configured):
	mvn test

- Run a single test class:
	mvn -Dtest=com.archana.meta.tests.LoginTest test

- Run a single test method:
	mvn -Dtest=LoginTest#validLogin test

- Run tests by TestNG group (no suite file needed):
	mvn test -Dgroups=smoke

- Exclude groups (e.g., flaky):
	mvn test -DexcludedGroups=flaky

- Run a specific suite file (explicit):
	mvn -Dsurefire.suiteXmlFiles=testng-smoke.xml test

- Run multiple suite files together:
	mvn -Dsurefire.suiteXmlFiles="testng-smoke.xml,testng-regression.xml" test

- Pass custom runtime properties (useful for env/browser):
	mvn -Denv=dev -Dbrowser=chrome -Dtest=com.archana.meta.tests.LoginTest#validLogin test

Notes on behavior:
- If Surefire is configured with `<suiteXmlFiles>` in `pom.xml`, `mvn test` will run those files. If not, Surefire will scan for test classes (pattern `**/*Test.java`) and run them; `-Dtest=` overrides the scan.
- `-Dgroups` and `-DexcludedGroups` are processed by TestNG; they can be used with or without suite files.

### 5) Quick examples you can try locally

1) Run the smoke suite file (PR-like quick run):
	 mvn -Dsurefire.suiteXmlFiles=testng-smoke.xml test

2) Run the full canonical suite (if `testng.xml` is set in pom):
	 mvn test

3) Run only smoke-tagged tests via CLI (no suite file):
	 mvn test -Dgroups=smoke

4) Run a single method and pass environment property:
	 mvn -Denv=dev -Dbrowser=chrome -Dtest=LoginTest#validLogin test

### 6) Short checklist to integrate into this project
- Add `@Test(groups={"smoke"})` to the LoginTest happy-path tests.
- Create `testng-smoke.xml` listing the smoke classes (I can create it for you).
- Wire CI PR job to run `mvn -Dsurefire.suiteXmlFiles=testng-smoke.xml test`.
- Keep `testng.xml` as the canonical full suite and `testng-regression.xml` for nightly runs (exclude `flaky`).

If you want, I can now create the `testng-smoke.xml` and `testng-regression.xml` files on `feature/login-pom`, or I can annotate one real test in `src/test/java` to demonstrate.

----
File authored: concise notes + runnable commands. Ask me to apply the changes to the repo (I will commit to `feature/login-pom` if you want).

