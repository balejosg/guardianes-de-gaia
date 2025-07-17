Feature: Basic Step Tracking (W1)
  As a Guardian (child player aged 6-12)
  I want to track my daily steps and convert them to energy
  So that I can use energy for card battles and challenges

  Background:
    Given I am a registered Guardian with ID 1
    And today is "2025-07-04"

  Scenario: Guardian tracks steps throughout the day
    Given I have taken 0 steps today
    When I walk and record 1000 steps at 09:00
    And I walk and record 2000 more steps at 12:00
    And I walk and record 1500 more steps at 15:00
    Then my total daily steps should be 4500
    And my energy balance should be 450 energy points

  Scenario: Guardian cannot record negative steps
    Given I have taken 2000 steps today
    When I try to record -500 steps
    Then the step recording should be rejected
    And I should see an error message "Step count cannot be negative"
    And my total daily steps should remain 2000

  Scenario: Guardian cannot exceed daily step limit
    Given I have taken 45000 steps today
    When I try to record 10000 more steps
    Then the step recording should be rejected
    And I should see an error message "Daily step count would exceed maximum allowed (50000)"
    And my total daily steps should remain 45000

  Scenario: Guardian earns energy from daily steps
    Given I have taken 0 steps today
    And my energy balance is 0
    When I complete my daily walking with 3500 steps
    Then my energy balance should increase by 350 energy points
    And there should be a transaction record showing "EARNED 350 energy from DAILY_STEPS"

  Scenario: Guardian can view step history
    Given I have the following step history:
      | date       | steps |
      | 2025-07-01 | 2000  |
      | 2025-07-02 | 3000  |
      | 2025-07-03 | 4000  |
    When I request my step history from "2025-07-01" to "2025-07-03"
    Then I should see 3 daily records
    And the records should show steps: 2000, 3000, 4000

  Scenario: Guardian can view current step count in real-time
    Given I have taken 1500 steps today
    When I check my current step count
    Then I should see 1500 steps
    And I should see 150 energy points equivalent

  Scenario: System prevents step count manipulation
    Given I have a normal step pattern averaging 2000 steps daily
    When I try to record 20000 steps in one submission
    Then the system should flag this as anomalous
    And the step recording should be rejected
    And I should see an error message "Step count appears anomalous and requires verification"

  Scenario: Guardian receives step validation feedback
    Given I am recording steps normally
    When I submit 5000 steps at 14:00
    Then the step count should be validated successfully
    And the steps should be added to my daily total
    And my energy should be updated accordingly

  Scenario: Energy calculation is accurate
    Given I understand that 10 steps = 1 energy point
    When I record exactly 12500 steps
    Then my energy should increase by exactly 1250 energy points
    And partial steps (remainder 5) should not generate energy

  Scenario: Guardian cannot go into negative energy
    Given my energy balance is 100 energy points
    When I try to spend 150 energy points on a battle
    Then the transaction should be rejected
    And I should see an error message about insufficient energy
    And my energy balance should remain 100

  Scenario: Daily step aggregation works correctly
    Given I record steps multiple times throughout the day:
      | time  | steps |
      | 08:00 | 800   |
      | 10:00 | 1200  |
      | 14:00 | 1500  |
      | 18:00 | 2000  |
    When I check my daily step summary
    Then my total daily steps should be 5500
    And my total daily energy earned should be 550

  Rule: Step tracking must be fraud-resistant
    
    Scenario: System detects impossible step increments
      Given I recorded 1000 steps at 10:00
      When I try to record 15000 additional steps at 10:05
      Then the system should detect an unreasonable increment
      And the step recording should be rejected

    Scenario: System limits submission frequency
      Given I have submitted steps 100 times in the last hour
      When I try to submit steps again
      Then the system should reject due to rate limiting
      And I should see a message about submission frequency

  Rule: Data integrity must be maintained

    Scenario: Step records require valid data
      When I try to submit a step record with missing guardian ID
      Then the system should reject the submission
      And data integrity validation should fail

    Scenario: Step timestamps must be reasonable
      When I try to submit steps with a future timestamp
      Then the system should reject the submission
      And temporal validation should fail

  # REST API Integration Scenarios
  Rule: REST API must provide secure and reliable step tracking interface

    Scenario: Guardian submits steps via REST API
      Given I am authenticated as Guardian with ID 1
      And I have taken 1000 steps today
      When I POST to "/api/v1/guardians/1/steps" with:
        | stepCount | 2500 |
        | timestamp | 2025-07-04T14:30:00Z |
      Then I should receive HTTP 201 Created
      And the response should contain the updated daily total of 3500
      And my energy balance should increase by 250

    Scenario: API validates step submission rate limiting
      Given I am authenticated as Guardian with ID 1
      And I have submitted steps 95 times in the last hour
      When I POST to "/api/v1/guardians/1/steps" with valid data
      Then I should receive HTTP 429 Too Many Requests
      And the response should contain rate limit information
      And my step count should remain unchanged

    Scenario: API returns current step count
      Given I am authenticated as Guardian with ID 1
      And I have taken 4200 steps today
      When I GET "/api/v1/guardians/1/steps/current"
      Then I should receive HTTP 200 OK
      And the response should show 4200 current steps
      And the response should show 420 available energy

    Scenario: API returns step history
      Given I am authenticated as Guardian with ID 1
      And I have the following step history:
        | date       | steps |
        | 2025-07-01 | 2000  |
        | 2025-07-02 | 3000  |
        | 2025-07-03 | 4000  |
      When I GET "/api/v1/guardians/1/steps/history?from=2025-07-01&to=2025-07-03"
      Then I should receive HTTP 200 OK
      And the response should contain 3 daily records
      And the records should match the expected step counts

    Scenario: API handles invalid step submissions
      Given I am authenticated as Guardian with ID 1
      When I POST to "/api/v1/guardians/1/steps" with:
        | stepCount | -100 |
        | timestamp | 2025-07-04T14:30:00Z |
      Then I should receive HTTP 400 Bad Request
      And the response should contain validation error details
      And my step count should remain unchanged

    Scenario: API requires authentication
      Given I am not authenticated
      When I POST to "/api/v1/guardians/1/steps" with valid data
      Then I should receive HTTP 401 Unauthorized
      And the response should contain authentication error

    Scenario: API returns energy balance
      Given I am authenticated as Guardian with ID 1
      And my energy balance is 750 energy points
      When I GET "/api/v1/guardians/1/energy/balance"
      Then I should receive HTTP 200 OK
      And the response should show 750 energy points
      And the response should include transaction history summary

    Scenario: API handles energy spending
      Given I am authenticated as Guardian with ID 1
      And my energy balance is 500 energy points
      When I POST to "/api/v1/guardians/1/energy/spend" with:
        | amount | 100 |
        | source | BATTLE |
      Then I should receive HTTP 200 OK
      And my energy balance should decrease to 400
      And there should be a spending transaction record

    Scenario: API prevents overspending energy
      Given I am authenticated as Guardian with ID 1
      And my energy balance is 50 energy points
      When I POST to "/api/v1/guardians/1/energy/spend" with:
        | amount | 100 |
        | source | BATTLE |
      Then I should receive HTTP 200 OK
      And the response should contain insufficient energy error
      And my energy balance should remain 50