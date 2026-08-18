@Login @Regression
Feature: QAPlayground Login
  As a registered user
  I want to log in to QA Playground
  So that I can access my banking dashboard

  @Smoke
  Scenario: User can login to QA Playground successfully
    When User opens the application URL
    And User logs in with configured credentials
    Then DashboardPage URL should be "https://qaplayground.com/bank/dashboard"

  @NegativeTest
  Scenario: User cannot login to QA Playground with invalid credentials
    When User opens the application URL
    And User logs in with invalid credentials
    Then LoginPage URL should be "https://qaplayground.com/bank/login"