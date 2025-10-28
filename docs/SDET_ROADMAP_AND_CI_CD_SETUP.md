# SDET Roadmap and CI/CD Setup - Brainstormed Ideas

## Overview
- Build a 3-week automation framework foundation focusing on Selenium/POM, API testing, and CI/CD.
- Framework must be advanced, resume-worthy for Amazon/FAANG interviews, generic and reusable (not coupled to Workday project).
- Timeline: 3 weeks (2 hours weekdays + 4 hours weekends ≈ 30-35 hours total).
- Future: Clone framework for AI self-healing (6-12 weeks separate phase).

## Key Priorities
- Selenium with POM (Page Object Model) - master basics to advanced.
- API testing - build from scratch (0/10 current skill).
- CI/CD setup - free tools (GitHub Actions), integrate with Railway deployment.
- Full-stack SDET: API + UI automation + CI/CD pipelines.

## AI Self-Healing Concept (High-Level Path)
- Not implemented in 3 weeks - just design with extensibility in mind.
- Future: AI-assisted element locators and failure recovery (e.g., dynamic selectors, fallback strategies).
- Trigger: Manually clone base framework at end of Week 3 to new repo for AI development.
- Benefits: Modular, avoids importing complications, showcases innovation separately.

## Making It Stand Apart
- Modular architecture: Plugin-based for easy extensions (e.g., AI plugins later).
- Cross-browser testing, data-driven frameworks, comprehensive logging for failure analysis.
- Extensibility: Locator strategies that can be swapped, recovery mechanisms pluggable.
- Generic design: Reusable for any project, not Workday-specific.
- Advanced features: Parallel execution, reporting, integration with Railway for end-to-end CI/CD.

## Tools and Options
- CI/CD: GitHub Actions (free, doable).
- Deployment: Railway.app (confirmed).
- Repo Structure: New repo for automation framework (separate from Workday), use Copilot for bug fixes in Workday to focus on SDET.
- Learning Path: Solid foundation one by one - Selenium/POM first, then API, then CI/CD.

## Shortlisted Points for 3-Week Plan
1. Data-Driven Test Framework (Excel/CSV for scalability).
2. Custom Reporting (Allure/ExtentReports with screenshots/logs).
3. Parallel Test Execution (pytest-xdist or Selenium Grid).
4. API-UI Integration Tests (full-stack coverage).
5. Modular POM Structure (base classes, utilities, config).
6. CI/CD Pipeline with Deployment (GitHub Actions to Railway).
7. Cross-Browser Testing (Chrome first, Safari later - not priority).
8. Error Handling and Logging (for failures, AI groundwork).
9. Test Coverage on Workday App (real scenarios validation).
10. Documentation and Portfolio Prep (README, demos).

## Week Breakdown
- Week 1: Selenium/POM basics + data-driven + modular structure.
- Week 2: API testing + API-UI integration + error handling.
- Week 3: CI/CD setup + reporting + parallel execution + documentation.

## Additional Value-Adding Points
- Unit Tests for Framework: Test the framework's own utilities (e.g., POM classes) with pytest.
- Environment Management: Config for local/dev/staging environments.
- API Security Basics: Include auth/token handling in API tests.
- Performance Metrics: Add basic timing/logs in reporting.
- Demo and Open-Source: Host on GitHub with a README, badges, and maybe a simple CI badge.

## Tech Stack Discussion
- **Core Language**: Python 3.11 (matches Workday project, strong for testing).
- **UI Automation**: Selenium WebDriver with ChromeDriver (focus on Chrome, extensible to Safari).
- **API Testing**: requests library + pytest for REST API validation.
- **Test Framework**: pytest (fixtures, parametrization, plugins like pytest-html).
- **Data-Driven**: openpyxl or pandas for Excel/CSV handling.
- **Reporting**: Allure Framework (detailed reports with screenshots, history).
- **Parallel Execution**: pytest-xdist for concurrent tests.
- **CI/CD**: GitHub Actions (workflows for build, test, deploy).
- **Deployment**: Railway.app (staging environment for end-to-end).
- **Version Control**: Git (new repo for framework).
- **IDE**: VS Code with extensions for Python/Selenium.
- **Other Libraries**: webdriver-manager (auto driver updates), logging (built-in), configparser (.ini files).

## Open Questions
- Specific Selenium/POM aspects to master (e.g., cross-browser, data-driven).
- API testing depth (REST basics or deeper).
- How advanced for interviews (e.g., include performance/security later).
- Cloning trigger: Manual at Week 3 end.
- AI sophistication: Start simple (rule-based fallbacks) to advanced ML.

## Next Steps
- Continue brainstorming 3-week plan details after this update.
