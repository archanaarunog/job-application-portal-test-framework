# CI/CD Plans (LoginPage Scope)

**Purpose:** Discuss and document CI/CD for the LoginPage tests only, free-of-cost, until ready to implement.

---

## Scope & Baseline
- **Scope:** `LoginTest` and related smoke scenarios.
- **Test status:** 1 skipped, 2 failing, rest passing consistently (baseline acceptable for CI discussion; aim to stabilize before build).

## CI vs CD (Plain Definitions)
- **Continuous Integration (CI):** Automate build + tests on every change. Shows status (pass/fail), artifacts (logs/results), and enforces quality via PR checks.
- **Continuous Delivery/Deployment (CD):** Automate releasing the application after CI. Delivery prepares a deployable artifact; Deployment actually pushes to an environment.
- **Start With:** CI only. CD requires a deployable app + environment strategy; not needed for a test framework repo.

## Environment: “Spin-up service” vs “Staging URL”
- **Spin-up service:** Start the web app inside the CI job (e.g., `docker-compose up` or `npm run start`) and run UI tests against `http://localhost:PORT`. Ephemeral, reproducible.
- **Staging URL:** Point tests to a persistent, internet-accessible environment managed outside CI (e.g., `https://staging.example.com`). No boot step; depends on external availability.
- **For Free:** Prefer staging if available; otherwise spin-up locally in the runner using free GitHub Actions compute (public repo).

## Secrets (Credentials)
- Store credentials in **GitHub Actions Secrets** (e.g., `TEST_USER_EMAIL`, `TEST_USER_PASSWORD`). Pass via env vars; never commit.
  
  - Where to add: GitHub → repo → Settings → "Secrets and variables" → Actions → "New repository secret".
  - Naming: Use clear names like `TEST_USER_EMAIL`, `TEST_USER_PASSWORD`.
  - Use in workflow:
    
    ```yaml
    env:
      TEST_USER_EMAIL: ${{ secrets.TEST_USER_EMAIL }}
      TEST_USER_PASSWORD: ${{ secrets.TEST_USER_PASSWORD }}
    ```
  - Pass to tests (example):
    
    ```bash
    mvn clean test -Denv=dev -Dtest.user.email="$TEST_USER_EMAIL" -Dtest.user.password="$TEST_USER_PASSWORD"
    ```
  - Read in Java via `System.getProperty("test.user.email")` or `System.getenv("TEST_USER_EMAIL")`. Avoid printing secrets in logs.

### Demo vs Best Practice
- **Demo-only (dummy accounts):** You could commit placeholder values if they’re non-sensitive and purely illustrative.
- **FAANG/MAANG best practice:** Always use Secrets for any credentials, even demos. It signals maturity and avoids bad habits. Document placeholders in README, but inject real values via Secrets.

## Visibility of Results (Free Options)
- **GitHub Actions Artifacts:** Upload `target/allure-results` and logs for download.
- **GitHub Pages (Static Hosting):** Publish generated Allure report as a static site; add README link. Free for public repos.
- **Actions Summary:** Add brief test summary to the job output (matrix, counts, links).

## Free-of-Cost Plan
- **Runner:** GitHub Actions (public repos: free minutes). 
- **Publishing:** GitHub Pages or artifacts only (no paid third-party).
- **Dependencies:** Use Maven cache to speed builds (free). Avoid paid SaaS.

## CI Event Triggers (Recommended)
- **pull_request → main:** Run smoke suite for incoming changes. Gate merges with required checks.
- **push → main:** Run smoke and optionally publish artifacts/Pages. (Ideally, pushes to `main` are via PR merges.)
- **workflow_dispatch:** Manual trigger for on-demand runs (e.g., regression).
- **schedule:** Nightly regression (e.g., cron `0 3 * * *`).

> For “every push to main triggers build”: Yes, include `push` to `main`. FAANG-level also emphasizes PR checks (pull_request) so problems are caught before merge.

## Repo Topology: Single vs Separate Repos
- **Current:** App (site under test) is in a different repo; this repo holds the test framework.
- **CI location:**
  - Set up CI in this test repo to build and run tests.
  - The app repo should have its own CI for build/deploy.
- **Cross-repo options (later):** Trigger tests after app deploy via `workflow_run` or check out the app/artifacts inside the test workflow (e.g., use a container image or `actions/checkout` on the app repo with a token).
- **Recommendation (now):** Keep CI scoped to this repo. If a stable staging URL exists, point tests to it. Otherwise, consider spin-up inside the test workflow.

## Environment Recommendation (FAANG-minded, Free)
- **Start with staging URL** if available: simplest, no boot steps in CI, consistent endpoint.
- **If no staging:** **Spin-up in CI** (runner-local) using app start or Docker Compose; tests hit `http://localhost:PORT` on the runner.
- **Avoid self-hosted runners** initially: free but couples CI to your machine’s uptime; use only if necessary.

### Environment Decision Matrix (Options)
- **Staging URL**
  - Pros: Realistic environment; stable URL; minimal CI complexity.
  - Cons: Depends on external availability; configuration drift risks; needs credential/secrets management.
  - Prereqs: Hosted app with health endpoint; known `BASE_URL`; test accounts.
- **Spin-up in CI (Ephemeral)**
  - Pros: Reproducible; isolated; aligns with infra-as-code; no external dependency.
  - Cons: Boot time overhead; requires start script or containers; must implement health checks and teardown.
  - Prereqs: App start command or Docker Compose; ports; health check; test data seeding if needed.
- **Self-hosted runner**
  - Pros: Can access your local/staging behind firewalls; full control.
  - Cons: Ops overhead; ties CI to your machine; less portable; not ideal initially.
  - Prereqs: Runner installation; network routing; maintenance.
- **Mocked endpoints / API virtualization** (for unit/integration, not E2E login)
  - Pros: Fast; deterministic.
  - Cons: Not suitable for real login E2E.
  - Prereqs: Mocks; contract tests.

### “Spin-up in CI” Explained
1. **Checkout app code** (or use a container image of the app).
2. **Start the app** inside the CI job (e.g., `docker compose up -d` or `npm run start &`).
3. **Wait for health** (poll `http://localhost:PORT/health` until HTTP 200).
4. **Run tests** pointing `BASE_URL=http://localhost:PORT`.
5. **Teardown** (`docker compose down` or kill the process) to free resources.

Example (conceptual YAML):

```yaml
jobs:
  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Checkout app repo (optional if containerized)
        uses: actions/checkout@v4
        with:
          repository: owner/app-repo
          path: app
      - name: Start app (Docker Compose)
        run: |
          docker compose -f app/docker-compose.yml up -d
      - name: Wait for health
        run: |
          for i in {1..60}; do curl -sf http://localhost:8000/health && exit 0; sleep 5; done; exit 1
      - name: Run smoke tests
        env:
          BASE_URL: http://localhost:8000
          TEST_USER_EMAIL: ${{ secrets.TEST_USER_EMAIL }}
          TEST_USER_PASSWORD: ${{ secrets.TEST_USER_PASSWORD }}
        run: |
          mvn -DsuiteXmlFile=testng-smoke.xml clean test -Denv=dev -Dbase.url="$BASE_URL" -Dtest.user.email="$TEST_USER_EMAIL" -Dtest.user.password="$TEST_USER_PASSWORD"
      - name: Upload artifacts
        uses: actions/upload-artifact@v4
        with:
          name: smoke-artifacts
          path: |
            logs/*.log
            target/surefire-reports
            target/allure-results
      - name: Teardown app
        if: always()
        run: |
          docker compose -f app/docker-compose.yml down
```

### Recommendation (FAANG-minded, Free)
- **Preferred:** Use a **staging URL** if available; minimizes complexity and mirrors production-like conditions.
- **Otherwise:** Use **spin-up in CI** with Docker Compose and a health check loop; keep startup under a few minutes and ensure clean teardown.
- **Avoid:** Self-hosted runners until you need private network access; revisit later if constraints arise.

## Recommendation & Learning Path
- **Best overall (if available):** Staging URL — simplest, production-like, fewer CI moving parts.
- **Best learning experience:** Do both, sequentially:
  1) Pilot with staging (validate triggers, artifacts, reporting).
  2) Add a spin-up job (Docker Compose + health checks) to learn environment boot, readiness, teardown.
- **Outcome:** You’ll demonstrate FAANG-level maturity by supporting both deployment styles and showing deterministic, environment-agnostic tests.

### Dual-Mode Workflow Design (Conceptual)
- **Approach:** Parameterize environment with a matrix: `mode: [staging, spinup]`.
- **Conditional steps:**
  - If `mode=staging`: skip boot; set `BASE_URL` from Secrets; run smoke; upload artifacts.
  - If `mode=spinup`: checkout/start app; health loop; set `BASE_URL=http://localhost:PORT`; run smoke; teardown; upload artifacts.
- **Benefits:** Same test command; selectable environment; easy comparison; free on GitHub Actions.

## Local App vs CI Runners
- **Local-only app:** Fine for local dev. In cloud CI, `http://localhost:PORT` refers to the CI runner machine, not your laptop.
- **To run UI tests in CI:**
  - Spin up the app inside the CI job (e.g., `docker-compose up` or `npm run start`) and use that local URL.
  - Or use a publicly accessible staging URL.
  - Or use a self-hosted runner on your machine so CI can hit your local server (free but ties CI to your host availability).
- **If neither is available:** Start with CI that builds the framework and runs fast checks (lint/unit), and enable UI tests once an accessible environment exists.

## Artifacts vs Pages (Publishing)
- **Artifacts:** Files archived per CI run (e.g., `logs/*.log`, `target/allure-results`). Visible under the Actions run; downloadable; retention limited by repo settings. Great for raw evidence.
- **Pages:** Static website hosting of generated content (e.g., Allure HTML). Public link in README for easy viewing. Best for polished, always-available reports.
- **Order:** Start with artifacts; add Pages once smoke runs are stable and Allure generation is deterministic.

## Decisions So Far
- **Suite selection:** Smoke on each PR/push; regression via manual trigger (and later nightly schedule).
- **Artifacts/Publishing:** Begin with Actions artifacts; add GitHub Pages for Allure once stable.
- **Stability:** Add RetryAnalyzer with capped attempts; refine waits.
- **Caching:** Use Actions Maven cache.
- **Triggers:** Use `pull_request` + `push` to `main`; include `workflow_dispatch`, and consider `schedule` for nightly.

## Proposed CI (Outline, Not Implemented)
- **Triggers:** `push`, `pull_request` to `main`; `workflow_dispatch`.
- **Jobs:**
  - `build-and-test` (Java 17 + Maven cache).
  - Run smoke: `mvn -DsuiteXmlFile=testng-smoke.xml clean test -Denv=dev`.
  - Upload artifacts: `target/surefire-reports`, `target/allure-results`, `logs/*.log`.
  - Optional: Generate Allure and deploy to Pages.
- **Env:** Either spin-up app (local) or use staging `base.url` via secrets.
- **Flake control:** RetryAnalyzer + robust waits.

## FAANG/MAANG-Level CI Signals
- **Badges:** CI status badge in README.
- **PR Checks:** Required checks gating merges.
- **Rich Reports:** Allure link (Pages) with screenshots/logs.
- **Artifacts:** Downloadable logs and results per run.
- **Traceability:** Clear suite selection, deterministic runs, cache-enabled speed.

## CI vs CD: What We Will Do Here
- **CI (this repo):** Build the test framework and run tests automatically. Publish logs/results so anyone can see quality.
- **CD (app repo):** Build and deploy the application. Not applicable to this test-only repo. Our closest equivalent is “continuous publishing” of reports (Pages).
- **Can we have both?** Yes, but across repos: CI here; CD in the app’s repo. If desired later, we can wire app deploy events to trigger this repo’s tests.

## Stepwise Plan (CI-first)
1. **Prereqs:**
  - Decide environment: staging URL preferred; else spin-up app in CI.
  - Create Secrets: `TEST_USER_EMAIL`, `TEST_USER_PASSWORD`, and optionally `BASE_URL`.
  - Ensure `.gitignore` excludes `target/`, `logs/`, `allure-*`, `TestReports/`.
  - Verify config overrides: `ConfigManager` now accepts `-Dbase.url`, `BASE_URL`, or `base.url` in properties.
2. **Triggers:**
  - Enable `pull_request` and `push` to `main`.
  - Add `workflow_dispatch` for manual runs; consider `schedule` nightlies later.
3. **Smoke Run Command:**
  - `mvn -DsuiteXmlFile=testng-smoke.xml clean test -Denv=dev` with secrets injected via env/`-D`.
4. **Artifacts:**
  - Upload `logs/*.log`, `target/surefire-reports`, `target/allure-results`.
5. **Pages (Optional, after stable):**
  - Generate Allure HTML; deploy to GitHub Pages; add README link.
6. **PR Checks:**
  - Mark CI as a required check for merges to `main`.
7. **Stability:**
  - Add RetryAnalyzer with capped attempts; refine waits/timeouts.

## What’s Next (Discussion → Implementation)
- **Confirm environment** (staging vs spin-up).
- **Approve trigger policy** (PR + push + manual; add nightly later).
- **Accept artifact set** (logs + allure-results; Pages later).
- **Greenlight secrets usage** even for demos (FAANG-level practice).
- Once confirmed, we draft the workflow YAML (still in notes), then implement.

## Step 1: Base URL & Credentials Setup (How-To)
- **Define `BASE_URL`:** Use the repo’s configured value: `http://127.0.0.1:8000` (ensure the app runs here).
- **Create Secrets (for CI):** In repo Settings → Actions → Secrets:
  - `TEST_USER_EMAIL`, `TEST_USER_PASSWORD`, `BASE_URL`.
- **Local verification (zsh):**

  ```zsh
  export BASE_URL="http://127.0.0.1:8000"
  export TEST_USER_EMAIL="test123@gmail.com"
  export TEST_USER_PASSWORD="Password@123"
  mvn -DsuiteXmlFile=testng-smoke.xml clean test \
    -Denv=dev \
    -Dtest.user.email="$TEST_USER_EMAIL" \
    -Dtest.user.password="$TEST_USER_PASSWORD"
  ```

  Notes:
  - With `ConfigManager` overrides, `BASE_URL` env var is picked up as `base.url` automatically; or pass `-Dbase.url="$BASE_URL"` explicitly.
  - Alternatively, rely on `config/dev.properties` which already contains `base.url`, `login.email`, and `login.password`.
  - Check `logs/test-run-*.log` and `target/allure-results` for outcomes.

- **.gitignore hygiene:** Ensure `target/`, `logs/`, `allure-results/`, `allure-report/`, `TestReports/` are ignored (already configured).

- **Troubleshooting:**
  - If you see `Required config key 'base.url' is missing ...`, set `BASE_URL` or `-Dbase.url`, or add to `config/dev.properties`.
  - Verify staging is reachable: `curl -I "$BASE_URL"` returns 200/302.

## Open Questions
- **Triggers:** Confirm exact branches and event types.
- **Environment choice:** Staging vs spin-up; source of the app if spin-up.
- **Credentials:** Finalize secrets names + masking strategy.
- **Publishing:** Pages vs artifacts-only (start with artifacts if Pages feels heavy).

## Next Discussion Topics
1. Finalize triggers + branch strategy.
2. Choose environment approach and define `base.url` provisioning in CI.
3. Decide initial publishing target (Artifacts vs Pages).
4. Confirm retry policy and max attempts.
5. Document caching impact and invalidation.

## References & Notes
- GitHub Actions (free for public repos): CI compute + artifact storage.
- GitHub Pages: Free static hosting for public repos — suitable for Allure HTML.
- Allure: Generate report from `target/allure-results`; serve via Pages or `python -m http.server` locally.
