# Test Case: TC001

**ID**: TC001  
**Title**: Candidate Login  
**Priority**: High  
**Type**: Functional  
**Description**: Verify login functionality for candidates, including valid/invalid credentials and navigation.  

**Preconditions**:  
- Portal running locally (http://127.0.0.1:8000).  
- User not logged in.  

**BDD Scenario** (Gherkin format):  
```
Scenario: Successful login with valid credentials
  Given the candidate is on the login page
  When the candidate enters valid <username> and <password>
  Then the candidate is redirected to the job portal listing page

Scenario: Invalid login with wrong credentials
  Given the candidate is on the login page
  When the candidate enters invalid <username> and <password>
  Then an error message is displayed and page reloads (bug noted)

Scenario: Forgot password link
  Given the candidate is on the login page
  When the candidate clicks "Forgot Password"
  Then a reset email is sent and the login page displays "Password reset link has been sent to your email!"

Scenario: Remember me functionality
  Given the candidate is on the login page
  When the candidate checks "Remember me" and logs in
  Then the session persists for 30 days

Scenario: Back to home navigation
  Given the candidate is on the login page
  When the candidate clicks "Back to home page"
  Then the candidate is redirected to the main page

Scenario: Sign up now
  Given the candidate is on the login page
  When the candidate clicks "Sign up now"
  Then the candidate is redirected to the create account page

Scenario: Login with empty fields
  Given the candidate is on the login page
  When the candidate leaves username and password fields empty and clicks login
  Then error messages "Email address is required" and "Password is required" are displayed

Scenario: Login with invalid email format
  Given the candidate is on the login page
  When the candidate enters an invalid email format (e.g., "invalidemail") and a password shorter than 6 characters
  Then error messages "please enter a valid email address" and "Password must be at least 6 characters" are displayed
```

**Data-Driven Table** (for parameters):  
| Scenario | Username | Password | Expected Outcome |  
|----------|----------|----------|------------------|  
| Valid Login | test123@gmail.com | Password@123 | Redirect to job portal |  
| Invalid Login | wrong@email.com | wrongpass | Error message, page reload |  
| Forgot Password | N/A | N/A | Email sent, message displayed |  
| Remember Me | test123@gmail.com | Password@123 | Session persists |  
| Back to Home | N/A | N/A | Redirect to main page |  
| Sign up now | N/A | N/A | Redirect to create account |  
| Empty Fields |  |  | Errors: Email address is required, Password is required |  
| Invalid Email | invalidemail | short | Errors: please enter a valid email address, Password must be at least 6 characters |  



**Expected Result**:  
- Valid login: Redirect to job listing.  
- Invalid: Error shown, page reloads (log as bug).  
- Forgot: Email sent, message displayed.  
- Remember: Persistent login.  
- Back: Home page.  
- Sign up: Create account page.  
- Empty fields: Error messages "Email address is required" and "Password is required".  
- Invalid email: Error messages "please enter a valid email address" and "Password must be at least 6 characters".  

**Actual Result**:  
- Valid login: Passed, redirected to job portal.  
- Invalid login: Passed, error displayed but page reloads (bug noted).  
- Forgot password: Failed, button shows but email not sent (implementation missing).  
- Remember me: Passed, session persists.  
- Back to home: Passed, redirected.  
- Sign up now: Passed, redirected to create account.  
- Empty fields: Passed, errors displayed.  
- Invalid email: Passed, errors displayed.  

**Status**: Pass (with notes on bugs)  
**Tested By**: Archana
**Date Tested**: 31 October 2025  
**Environment**: Local, Chrome  

**Notes/Bugs**:  
**Notes/Bugs**:  
- Forgot password: Button present but email sending not implemented (limitation).  
- Back button after login causes buffering (BUG-001).  
- Screenshots: `test-plan/screenshots/login_valid.png`, `test-plan/screenshots/login_invalid.png`, `test-plan/screenshots/login_empty_fields.png`, `test-plan/screenshots/login_invalid_email.png`.  

**Automation Notes**:  
- Selenium: `driver.findElement(By.id("username")).sendKeys(username);`

## Automation mapping (feature -> test-plan)

The following mapping links test-plan IDs to the automated scenarios in `src/test/resources/features/login.feature`.

| Test Case ID | Feature scenario (file) | Comment / tag |
|--------------:|:------------------------|:--------------|
| TC001-01 | Valid Login — `src/test/resources/features/login.feature` | @TC001-01 (Valid Login)
| TC001-02 | Invalid login shows an error (Scenario Outline) — `src/test/resources/features/login.feature` | @TC001-02 (covers empty/invalid email/short password examples)
| TC001-03 | Forgot password sends a reset email — `src/test/resources/features/login.feature` | @TC001-03
| TC001-04 | Remember me persists session across browser restart — `src/test/resources/features/login.feature` | @TC001-04
| TC001-05 | Back to home navigation — `src/test/resources/features/login.feature` | @TC001-05
| TC001-06 | Sign up now navigates to create account — `src/test/resources/features/login.feature` | @TC001-06

### How to run a single test-case (by tag)
Use the Cucumber tag filter to run a single TC ID. Example:

```bash
# run only TC001-01
mvn test -Dcucumber.filter.tags="@TC001-01"
```

If your runner uses a different property, use the runner configuration to filter by tag. The tags are visible in Allure reports as scenario tags.