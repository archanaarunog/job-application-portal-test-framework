@TC001
Feature: Login

  @smoke @bdd @ui
  @TC001-01
  Scenario: Valid Login
      Given I open the login page
      When I login with "alice5678@example.com" and "SecurePass@123"
      Then I should see the dashboard

  @negative
  @TC001-02
  Scenario Outline: Invalid login shows an error
    Given I open the login page
    When I login with "<email>" and "<password>"
    Then I should see an error containing "<error>"

    Examples:
      | email                | password   | error                                  |
      |                      | password   | Email address is required              |
      | invalidemail         | password   | Please enter a valid email address     |
      | user@example.com     | short      | Password must be at least 6 characters |
      | nopassword@examp.com |            | Password is required                   |
      | wronguser@example.com| wrongpass  | Invalid email or password              |
      |                      |            | Email address is required and Password is required  |

  Scenario: Sign up now navigates to create account
    @TC001-06
    Given I open the login page
    When I click the "Sign up now" link
    Then I should be navigated to "register.html"

  Scenario: Back to home navigation
    @TC001-05
    Given I open the login page
    When I click the "Back to Home" link
    Then I should be navigated to "index.html"

  Scenario: Remember me persists session accross browser restart
    @TC001-04
    Given I open the login page
    When I login with "alice5678@example.com" and "SecurePass@123"
    And I set remember me to "true"
    And I export current session
    And I restart browser and restore session
    Then I should see the dashboard

  Scenario: Forgot password sends a reset email
    @TC001-03
    Given I open the login page
    When I click the "Forgot Password" link
    And I enter reset email "alice5678@example.com"
    And I submit the reset password form
    Then the forgot password modal should be closed
    And I should see a confirmation message "Password reset link has been sent to your email!"

    