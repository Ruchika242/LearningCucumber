Feature: Accounts


  Scenario: Verify Accounts Page
    When User opens the application URL
    And User logs in with configured credentials
    When Navigate to Accounts
    Then Locate the Accounts page
    And Assert that the Accounts page is displayed correctly


    Scenario: Add New Account
    When User opens the application URL
    And User logs in with configured credentials
    When Navigate to Accounts
    And Click on Add New Account button
    Then Fill Account Name, Account Type and Starting Balance fields
    And select the check box for "I accept the terms and conditions"
    And Click on "Add Account" button
