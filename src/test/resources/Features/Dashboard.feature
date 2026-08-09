Feature: Dashboard Page should be displayed after successful login and able to click on Accounts Page
  Scenario: User can login to QA Playground and see the Dashboard page
    When User opens URL "https://qaplayground.com/bank/login"
    And User enters Username "standard_user" and Password "bank_sauce"
    And User clicks on Login button
    Then DashboardPage URL should be "https://qaplayground.com/bank/dashboard"