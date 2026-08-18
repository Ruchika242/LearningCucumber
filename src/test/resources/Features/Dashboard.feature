@Dashboard @Regression
Feature: Dashboard
  As a logged-in user
  I want to view my banking dashboard
  So that I can monitor my account activity and recent transactions

  @Smoke
  Scenario: User can see the Dashboard page after successful login
    When User opens the application URL
    And User logs in with configured credentials
    Then DashboardPage URL should be "https://qaplayground.com/bank/dashboard"

  @Transactions
  Scenario: Verify Recent Transactions Widget on Dashboard Page
    When User opens the application URL
    And User logs in with configured credentials
    When Navigate to Dashboard
    Then Locate the Recent Transactions widget
    And Assert that maximum 5 transactions are shown
