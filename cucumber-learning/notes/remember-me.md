# Remember-me testing notes

Summary
- Implementation notes and test considerations for the remember-me flow (persisting sessions across browser restarts). Includes optional BDD references.

Retry analyzer (future option)
- A RetryAnalyzer can be used for the remember-me test to reduce noise from transient infra flakiness.
- Do NOT enable global retries. If added, scope to this flow only; MAX_RETRY=1.

Improved triage
- Capture screenshots, page-source, browser console logs, and element outerHTML on failure to speed debugging (cookies/localStorage issues are common here).

Attachments & expectations
- TestNG: `BaseTest` can attach screenshots and page-source on failure; files can also be saved under `target/screenshots/`.
- Cucumber (optional): if used, Hooks can attach screenshot/page source to Allure on failure.

Minimal additions
1) Capture browser console logs on failure and attach to Allure.
2) Export and attach cookies/localStorage for remember-me tests.
3) Optionally include outerHTML of the element under assertion.

Notes
- Keep RetryAnalyzer as an option after diagnostics are added and infra flakes are observed.
- Mark this test as slow/integration and run separately from fast smoke.
