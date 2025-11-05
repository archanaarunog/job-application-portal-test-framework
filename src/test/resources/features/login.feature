Feature: Login

  @smoke @bdd @ui
  Scenario: Valid Login
      Given I open the login page
      When I login with "alice5678@example.com" and "SecurePass@123"
      Then I should see the dashboard

  @negative
  Scenario Outline: Invalid login shows an error
    Given I open the login page
    When I login with "<email>" and "<password>"
    Then I should see an error containing "<error>"

    Examples:
      | email                | password   | error                                  |
      |                      | password   | Email address is required              |
      | invalidemail         | password   | Please enter a valid email address     |
      | user@example.com     | short      | Password must be at least 6 characters |


  
