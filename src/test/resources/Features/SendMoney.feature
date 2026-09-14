Feature: Send Money Functionality
  As a registered user
  I want to send money to another user
  So that I can transfer funds securely

  @Smoke
  Scenario: User can send money successfully by adding new Payee instantly on the same form
    When User opens the application URL
    And User logs in with configured credentials
    And User navigates to the Send Money page
    And Select "From Account"
    And User click on "Add" button to add a new Payee instantly on the same form
    And User enter valid payee details like Payee Name, Bank Name, Routing Number, Account Number
    And Click on "Add Payee" button to add the payee
    And User enters amount
    And User click on "Review & Send" button to review the transaction
    Then User click on "Confirm & Send" button to complete the transaction


