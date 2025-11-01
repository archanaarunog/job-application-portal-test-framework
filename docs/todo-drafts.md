# Finalized Summary So Far (Pick Up Tomorrow)
- **Tech Stack**: Java 17, Selenium, TestNG, REST Assured, Maven, Allure, GitHub Actions, IntelliJ.
- **Repo**: job-application-portal-test-framework on GitHub.
- **DUT**: Meta portal (Workday Job Application Automation) - deploy locally for testing.
- **High-Level Plan**: 3 weeks - Week 1: Manual test cases + framework setup + basic Selenium/POM.
- **Week 1 Targets**: Today - High-level manual test plan (10-15 cases in testplans folder). By Sunday - Framework setup, 3-5 Selenium tests, Allure reports.
- **Next**: Create testplans folder, README with deployment instructions, start manual test cases.

1. deploy the site  in railway.app, update gitignore to ignore hte .sh files clean_cache etc
2. write test cases 
3. update the readme 



1. add more ci cases in dev project
2. 



t I changed (summary)

Created these folders:
test-plan/screenshots/login
test-plan/screenshots/mainpage
test-plan/screenshots/create-account
test-plan/screenshots/job-portal
test-plan/screenshots/dashboard
test-plan/screenshots/profile
test-plan/screenshots/my-applications
test-plan/screenshots/misc
Moved and renamed files from design-docs to the new folders using descriptive names (no timestamps). Example moves:
login_page.png -> login_page.png
login-page-invalid-email.png -> test-plan/screenshots/login/login_invalid_email.png
design-docs/Screenshot 2025-10-30 at 7.23.32 PM.png -> test-plan/screenshots/mainpage/mainpage_hero.png
(All other mapped screenshots were moved similarly.)
Deleted the JobApplicationPortalTestFramework/design-docs/ directory.
Updated todo list: screenshot tasks are now "completed".
If you want the exact file list I moved, I can print a tree of screenshots now.

Next recommended actions (choose one or more)

I can convert one of your drafts (e.g., the main page draft) into 2–3 template-compliant test cases and a BDD scenario as a sample. This will show you the final format and help refine the template.
I can create a test-plan/bugs/bug_template.md and add the first bug entries (using option A: plain BUG-001 + Severity field). I can also create an initial BUG-001 for the "double logout" issue you flagged.
Start assigning test IDs and move drafts into the three folders (functional/, ui/, integration/) — I can do a first pass and mark duplicates for review.
Produce a short script/py or guidance for referencing screenshots in your Markdown files (small snippet showing how to embed the relative links).
Tell me which of the above to run next. If you want the sample conversion of the main-page draft, I can do that immediately.


____________________________
1. reorganise to suits 
2. BDD and data driven approach - how to proceed now  ?
3. for now, focus on login test case 
4. perfect login test cases , prepare selenium framework , hybrid framework 

