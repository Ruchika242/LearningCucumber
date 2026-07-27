Feature: QAPlayground Login
  Scenario: User can login to QA Playground successfully
    Given I Launch chrome browser
    When User opens URL "https://qaplayground.com/bank/login"
    And User enters Username "standard_user" and Password "secret_sauce"
    And User clicks on Login button
    Then Page title should be "QA Playground - Master Automation Testing"
    When User clicks on Logout button
    Then Page title should be "QA Playground - Master Automation Testing"
    Then I close the browser