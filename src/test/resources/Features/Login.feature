Feature: QAPlayground Login
  Scenario: User can login to QA Playground successfully
    When User opens URL "https://qaplayground.com/bank/login"
    And User enters Username "standard_user" and Password "bank_sauce"
    And User clicks on Login button
    Then DashboardPage URL should be "https://qaplayground.com/bank/dashboard"
