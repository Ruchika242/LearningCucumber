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

    Scenario: Validate weather account number is masked on the Accounts page
    When User opens the application URL
    And User logs in with configured credentials
    When Navigate to Accounts
    Then Locate the Account Number field
    And Assert that the Account Number is masked (e.g., displayed as "****1234")


