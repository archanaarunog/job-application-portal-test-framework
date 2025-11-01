# Test Case Template

Use this template for all test cases in the `test-plan/` folders. Supports hybrid BDD + data-driven approach.

## Test Case: [ID]

**ID**: [e.g., TC001]  
**Title**: [Brief title, e.g., User Registration]  
**Priority**: [High/Medium/Low]  
**Type**: [Functional/UI/Integration]  
**Description**: [Detailed description of what the test verifies]  

**Preconditions**:  
- [List prerequisites, e.g., Portal running, user not logged in]  

**BDD Scenario** (Gherkin format):  
```
Scenario: [Scenario name]
  Given [Setup condition]
  When [Action performed]
  Then [Expected outcome]
```

**Data-Driven Table** (for parameters):  
| Parameter | Value |  
|-----------|-------|  
| [e.g., Username] | [e.g., testuser] |  

**Test Steps** (if not using BDD):  
1. [Step 1]  
2. [Step 2]  
3. [Etc.]  

**Expected Result**:  
- [What should happen, e.g., Account created, redirect to login]  

**Actual Result**:  
- [Fill during execution]  

**Status**: [Pass/Fail/Pending]  
**Tested By**: [Your name]  
**Date Tested**: [Date]  
**Environment**: [Local/Prod, Browser, etc.]  

**Notes/Bugs**:  
- [Screenshots, selectors (e.g., `button#register-btn`), observations, or bug details. If Fail, create separate bug doc in `bugs/`]  

**Automation Notes**:  
- [Future: Selenium selectors, API endpoints, etc.]