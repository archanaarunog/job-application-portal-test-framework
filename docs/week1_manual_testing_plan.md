# Week 1: Manual Testing and Foundation Setup

## Final Output Target
- Comprehensive manual test suite (20-30 test cases) in `test-plan/`.
- Basic Selenium script.
- Documentation of app workflows and learnings.

## To-Do List
- [ ] Task 1: Set up local environment and explore portal.
- [ ] Task 2: Identify core features and UI elements.
- [ ] Task 3: Write initial test cases (10-15 basic).
- [ ] Task 4: Execute manual tests and document results.
- [ ] Task 5: Add edge/negative test cases (10-15).
- [ ] Task 6: Organize test-plan folder with subfolders.
- [ ] Task 7: Review and refine test suite.
- [ ] Task 8: Intro to Selenium (install and basic script).
- [ ] Task 9: Document learnings in README.
- [ ] Task 10: Prepare notes for Week 2 transition.

## Test Plan Architecture

### Folder Structure
```
test-plan/
├── README.md                 # Overview and guidelines
├── functional/               # Functional test cases (e.g., registration, login)
├── ui/                       # UI test cases (e.g., form validation)
├── integration/              # Integration test cases (e.g., apply flow)
└── templates/                # Test case templates
```

### Test Case Format
Each test case in Markdown:
- **ID**: TC001
- **Title**: User Registration
- **Description**: Verify user can register.
- **Preconditions**: Portal running.
- **Steps**: 1. Go to register page. 2. Fill form. 3. Submit.
- **Expected**: Account created, redirect to login.
- **Actual**: [Fill during execution]
- **Status**: Pass/Fail
- **Notes**: Screenshots, selectors for automation.

### Best Practices
- Use clear, actionable language.
- Note selectors (e.g., `button#register-btn`) for future automation.
- Cover CRUD: Create, Read, Update, Delete for entities.
- Categorize by priority (High, Medium, Low).

## Roadmap
- Week 1: Manual testing + selenium framework
- Week 2: API testing.
- Week 3: CI/CD.