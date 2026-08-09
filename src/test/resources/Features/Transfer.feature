Feature: User is able to transfer money from one account to another

  Scenario: User can login to QA Playground and able to transfer money from one account to another on same day
    When User opens URL "https://qaplayground.com/bank/login"
    And User enters Username "standard_user" and Password "bank_sauce"
    And User clicks on Login button
    Then DashboardPage URL should be "https://qaplayground.com/bank/dashboard"
    Then User clicks on Transfer button and should be redirected to Transfer page with URL "https://qaplayground.com/bank/transfer"

    When User clicks on From Account dropdown and selects account "Everyday Checking"
    And User clicks on To Account and select account "High-Yield Savings"
    And User enters Transfer Amount "100"
    And User clicks on Review Transfer button
    And User clicks on Confirm Transfer button

    Then User should see the success message "Transfer Successful"