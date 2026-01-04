
## Stepwise Plan (CI-first)
1. **Prereqs:**
  - Decide environment: staging URL preferred; else spin-up app in CI.
  - Create Secrets: `TEST_USER_EMAIL`, `TEST_USER_PASSWORD`, and optionally `BASE_URL`.
  - Ensure `.gitignore` excludes `target/`, `logs/`, `allure-*`, `TestReports/`.
  - Verify config overrides: `ConfigManager` now accepts `-Dbase.url`, `BASE_URL`, or `base.url` in properties.

  Status:
  - Decide environment: Pending — not finalized yet. Recommendation remains staging first; spin-up in CI as fallback. Next: confirm choice and set `BASE_URL` accordingly.
  - Create Secrets: Completed — added in repo Secrets. Next: wire into workflow env as `BASE_URL`, `LOGIN_EMAIL`, `LOGIN_PASSWORD` (or map to `-Dlogin.email/password`).
  - .gitignore hygiene: Completed — confirmed entries exist for `/target/`, `logs/`, `/allure-results/`, `/allure-report/`, `TestReports/` in `.gitignore`.
  - Config overrides: Completed — `ConfigManager` supports precedence (system property → env var including `BASE_URL` → properties). Verified in `src/main/java/com/archana/framework/utils/ConfigManager.java`.

## Branch Strategy (CI Setup)
- **Recommended:** Populate `main` with a working baseline, protect it, then do CI in a feature branch via PR.
- **Steps (zsh):**
  ```zsh
  # If repo not initialized locally
  git init
  git remote add origin <your-repo-url>

  # Push baseline to main
  git checkout -b main
  git add .
  git commit -m "Initial baseline: test framework"
  git push -u origin main

  # Create CI setup branch
  git checkout -b ci-setup
  # add .github/workflows/ci.yml and related changes
  git add .
  git commit -m "Add CI workflow and configs"
  git push -u origin ci-setup
  ```
- **Protect `main`:** GitHub → Settings → Branches → Add rule for `main` → require PRs, require status checks.
- **Alternative:** If you prefer to keep `main` empty until CI is ready, set default branch to `develop`, work there, then PR into `main` and switch default back.
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