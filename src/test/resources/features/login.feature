Feature: Login

  @smoke @bdd
    Scenario: Valid Login
        Given I open the login page
        When I login with "test123@gmail.com" and "Password@123"
        Then I should see the dashboard
