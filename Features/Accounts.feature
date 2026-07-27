Feature: Accounts
  Scenario: Add the customer account and verify it is added successfully
    Given I Launch chrome browser
    When User opens URL "https://qaplayground.com/bank/login"
    And User enters Username "standard_user" and Password "secret_sauce"
    And User clicks on Login button
    Then User can view dashboard page
    When User clicks on Accounts button
    Then User can view Add Account button
    When User clicks on Add Account button
    Then User can view Account Name, Account Type, and Starting Balance fields
    When User enters Account Name as "Ruchi", Last Name as "Doe", and Post Code as "100"
    Then User click on check box to accept terms and conditions
    When User clicks on Add Account button
    Then User can view that created account in accounts page
    Then User clicks on Logout button
    Then User can view login page
    Then I close the browser
