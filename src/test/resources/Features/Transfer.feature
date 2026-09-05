Feature: Transfer

  Scenario: Verify Transfer Page
    When User opens the application URL
    And User logs in with configured credentials
    When Navigate to Transfer
    Then Locate the Transfer page
    And Assert that the Transfer page is displayed correctly

  Scenario: Perform Instant Fund Transfer
    When User opens the application URL
    And User logs in with configured credentials
    When Navigate to Transfer
    And Fill in the transfer details including From Account, To Account, Amount and Schedule Date
    And Select the "Today" option
    And Click on "Review Transfer" button
    Then Click on "Confirm Transfer" button

  Scenario: Perform Scheduled Fund Transfer
    When User opens the application URL
    And User logs in with configured credentials
    When Navigate to Transfer
    And Fill in the transfer details including From Account, To Account, Amount, and Schedule Date
    And Select the "Schedule for later" option
    And Click on "Review Transfer" button
    Then Click on "Confirm Transfer" button

    Scenario: Validate if user enters amount greater than available balance in From Account
    When User opens the application URL
    And User logs in with configured credentials
    When Navigate to Transfer
    And Fill in the transfer details including From Account, To Account, Amount (greater than available balance), and Schedule Date
    And Select the "Today" option
    And Click on "Review Transfer" button
      Then Assert that an error message is displayed indicating insufficient funds for the transfer like Insufficient funds. Available balance: $4,230.00.
