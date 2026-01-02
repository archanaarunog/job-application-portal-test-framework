Allure test report storage & naming conventions

Goal
- Produce one permanent, static Allure HTML report per test run that can be archived, shared, and attached to your resume or portfolio.
- Keep repository clean: avoid committing large binary artifacts by default.

Recommended naming format for a run folder
- <YYYYMMDD>_<HHMMSS>__<label>__<branch>__<short-sha>
  - Example: 20251105_153339__bdd-local__main__a1b2c3d
  - label: optional short string (e.g., "bdd-local", "ci-nightly")
  - short-sha: first 7 characters of the commit SHA for traceability

Folder layout (one run)
- TestReports/
  - 20251105_153339__bdd-local__main__a1b2c3d/
    - allure-report/       <-- generated static HTML ready to open
    - allure-results/      <-- raw Allure JSON + attachments (optional archive)
    - run-info.txt         <-- small metadata (timestamp, branch, commit, label, test summary)
  - latest/                <-- symlink to the most recent run folder (convenience)

Why this naming
- Human-readable timestamp + label gives immediate context when you list folders.
- Branch + short commit makes it resume/FAANG-friendly: you can show the exact code state that produced the report.

How to generate a permanent report (local CLI)
- Ensure tests have been run and `target/allure-results` exists.
- Using the repo-local Allure CLI (no Homebrew required):

  ./tools/allure-cli/allure-2.20.1/bin/allure generate target/allure-results --clean -o "TestReports/<RUN_FOLDER>/allure-report"

- Create a small run-info.txt next to the report (example):

  echo "Generated: $(date)" > TestReports/<RUN_FOLDER>/run-info.txt
  echo "Label: <label>" >> TestReports/<RUN_FOLDER>/run-info.txt
  echo "Branch: $(git rev-parse --abbrev-ref HEAD)" >> TestReports/<RUN_FOLDER>/run-info.txt
  echo "Commit: $(git rev-parse --short HEAD)" >> TestReports/<RUN_FOLDER>/run-info.txt

- Optional: update `TestReports/latest` symlink

  rm -f TestReports/latest
  ln -s "TestReports/<RUN_FOLDER>" TestReports/latest

Viewing notes
- Opening `index.html` via file:// in most browsers results in "Loading..." because browsers block file:// XHR.
- Options:
  - Host the folder with a static server (recommended for sharing): S3, GitHub Pages, or a CI artifact viewer.
  - For local one-off viewing: either launch Chrome with `--allow-file-access-from-files` (temporary), or run a local http server in the folder: `python3 -m http.server`.

What to commit / not commit to repo
- Do NOT commit heavy binary attachments or the entire `TestReports` directory. Add `TestReports/` to `.gitignore` (done).
- Commit small `run-info.txt` if you want to keep a human-readable pointer; better: keep these archives outside the git repo and upload to a portfolio host or artifact store.

FAANG-ready checklist
- Each report folder contains `run-info.txt` with branch and short SHA.
- Screenshots attached and visible in the report (verify attachments added by listeners on failure).
- Provide a short README or link in your resume pointing to the static report URL (hosted on a static web host or as CI artifacts).

Commands you can copy-paste
- Run tests (example):
  mvn -Dtest=tests.LoginTest test

- Generate report into a run folder:
  RUN="$(date +'%Y%m%d_%H%M%S')__bdd-local__$(git rev-parse --abbrev-ref HEAD)__$(git rev-parse --short HEAD)"
  mkdir -p "TestReports/$RUN"
  ./tools/allure-cli/allure-2.20.1/bin/allure generate target/allure-results --clean -o "TestReports/$RUN/allure-report"
  cp -R target/allure-results "TestReports/$RUN/allure-results"
  echo "Generated: $(date)" > "TestReports/$RUN/run-info.txt"
  echo "Branch: $(git rev-parse --abbrev-ref HEAD)" >> "TestReports/$RUN/run-info.txt"
  echo "Commit: $(git rev-parse --short HEAD)" >> "TestReports/$RUN/run-info.txt"
  rm -f TestReports/latest && ln -s "TestReports/$RUN" TestReports/latest

Questions / next options
- I can delete the `tools/allure-cli` folder and switch to a script that downloads the CLI on-demand (keeps repo small).
- I can add a small `Makefile` or `scripts/generate-report.sh` that runs the above commands (but you previously asked to avoid persistent scripts).

If you want, I can now:
- 1) Remove all remaining unwanted folders from the repo (confirm which: `.idea`, `tools/allure-cli`, `allure-results`?),
- 2) Create the symlink `TestReports/latest` for convenience,
- 3) Add the `docs/notes/REPORTING.md` (done), and
- 4) Create a small `scripts/generate-report.sh` (optional) to automate the process.

Tell me which of (1)-(4) to perform next.
