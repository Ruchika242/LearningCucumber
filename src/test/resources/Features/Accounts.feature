Feature: User is able to add the new account and able to see the account in the Accounts page

  Scenario: User can login to QA Playground and see the Accounts page
    When User opens URL "https://qaplayground.com/bank/login"
    And User enters Username "standard_user" and Password "bank_sauce"
    And User clicks on Login button
    Then DashboardPage URL should be "https://qaplayground.com/bank/dashboard"
    Then User clicks on Accounts button and should be redirected to Accounts page with URL "https://qaplayground.com/bank/accounts"

  Scenario: User can add a new account and see the account in the Accounts page
        When User clicks on Add Account button
        And User enters Account Name "Test Account1" and Account Type "Savings" and Account Balance "1000"
        And User clicks on accept Terms and Conditions checkbox
        And User clicks on Submit button
        Then User should see the new account "Test Account1" in the Accounts page
