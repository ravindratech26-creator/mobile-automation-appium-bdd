@login
Feature: User authentication
  As a Swag Labs customer
  I want to log in and log out of the mobile app
  So that only I can access my shopping session

  Background:
    Given the user is on the login screen

  @smoke @regression
  Scenario: Successful login with valid credentials
    When the user logs in as a "standard" user
    Then the products screen is displayed

  @smoke @regression
  Scenario: Unsuccessful login with invalid credentials
    When the user logs in with username "invalid_user" and password "wrong_password"
    Then the login error "Username and password do not match any user in this service." is displayed
    And the user remains on the login screen

  @regression
  Scenario Outline: Login validation - <case>
    When the user logs in with username "<username>" and password "<password>"
    Then the login error "<message>" is displayed

    Examples:
      | case                | username        | password     | message                                                       |
      | locked out user     | locked_out_user | secret_sauce | Sorry, this user has been locked out.                         |
      | empty username      |                 | secret_sauce | Username is required                                          |
      | empty password      | standard_user   |              | Password is required                                          |
      | wrong password      | standard_user   | wrong        | Username and password do not match any user in this service. |

  @smoke @regression @logout
  Scenario: Logout returns the user to the login screen
    Given the user logs in as a "standard" user
    And the products screen is displayed
    When the user logs out
    Then the login screen is displayed
