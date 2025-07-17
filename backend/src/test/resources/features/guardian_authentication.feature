Feature: Guardian Authentication
  As a child or parent user
  I want to register and authenticate as a Guardian
  So that I can access the Guardianes de Gaia application

  Background:
    Given the Guardian system is running
    And the database is clean

  Scenario: Successful Guardian registration
    Given I am a new user
    When I register with the following details:
      | username | testuser           |
      | email    | test@example.com   |
      | password | password123        |
      | name     | Test User          |
      | birthDate| 2010-01-15         |
    Then my registration should be successful
    And I should receive a JWT token
    And my Guardian profile should be created with:
      | username | testuser           |
      | email    | test@example.com   |
      | name     | Test User          |
      | level    | INITIATE           |
      | experience| 0                 |
      | active   | true               |

  Scenario: Registration with existing username
    Given a Guardian already exists with username "existinguser"
    When I try to register with username "existinguser"
    Then the registration should fail
    And I should get an error "Guardian with username 'existinguser' already exists"

  Scenario: Registration with existing email
    Given a Guardian already exists with email "existing@example.com"
    When I try to register with email "existing@example.com"
    Then the registration should fail
    And I should get an error "Guardian with email 'existing@example.com' already exists"

  Scenario Outline: Registration with invalid data
    When I register with invalid data:
      | field     | value   |
      | <field>   | <value> |
    Then the registration should fail
    And I should get a validation error

    Examples:
      | field     | value                    |
      | username  |                          |
      | username  | ab                       |
      | username  | a-very-long-username-that-exceeds-twenty-characters |
      | email     |                          |
      | email     | invalid-email            |
      | password  |                          |
      | password  | 123                      |
      | name      |                          |
      | birthDate | 2030-01-01               |

  Scenario: Successful Guardian authentication with username
    Given I am a registered Guardian with:
      | username | testuser        |
      | password | password123     |
    When I login with username "testuser" and password "password123"
    Then my authentication should be successful
    And I should receive a JWT token
    And my last active time should be updated

  Scenario: Successful Guardian authentication with email
    Given I am a registered Guardian with:
      | email    | test@example.com |
      | password | password123      |
    When I login with email "test@example.com" and password "password123"
    Then my authentication should be successful
    And I should receive a JWT token
    And my last active time should be updated

  Scenario: Authentication with wrong password
    Given I am a registered Guardian with:
      | username | testuser        |
      | password | password123     |
    When I login with username "testuser" and password "wrongpassword"
    Then my authentication should fail
    And I should get an error "Invalid credentials"

  Scenario: Authentication with non-existent user
    When I login with username "nonexistent" and password "password123"
    Then my authentication should fail
    And I should get an error "Invalid credentials"

  Scenario: Authentication with inactive Guardian
    Given I am a registered Guardian with:
      | username | testuser        |
      | password | password123     |
      | active   | false           |
    When I login with username "testuser" and password "password123"
    Then my authentication should fail
    And I should get an error "Account is inactive"

  Scenario: Guardian can change password
    Given I am an authenticated Guardian
    When I change my password from "oldpassword" to "newpassword123"
    Then the password change should be successful
    And I should be able to login with the new password

  Scenario: Password change with wrong current password
    Given I am an authenticated Guardian
    When I try to change my password from "wrongpassword" to "newpassword123"
    Then the password change should fail
    And I should get an error "Invalid current password"

  Scenario: Guardian profile access
    Given I am an authenticated Guardian with:
      | username   | testuser       |
      | name       | Test User      |
      | level      | APPRENTICE     |
      | experience | 150            |
      | steps      | 5000           |
      | energy     | 500            |
    When I access my profile
    Then I should see my profile information:
      | username   | testuser       |
      | name       | Test User      |
      | level      | APPRENTICE     |
      | experience | 150            |
      | steps      | 5000           |
      | energy     | 500            |

  Scenario: Unauthorized profile access
    Given I am not authenticated
    When I try to access my profile
    Then I should get an unauthorized error

  Scenario: Guardian can deactivate account
    Given I am an authenticated Guardian
    When I deactivate my account
    Then my account should be deactivated
    And I should not be able to login anymore

  Scenario: JWT token validation
    Given I am an authenticated Guardian
    When I make a request with a valid JWT token
    Then the request should be authorized
    And I should access protected resources

  Scenario: JWT token expiration
    Given I am an authenticated Guardian
    And my JWT token has expired
    When I make a request with the expired token
    Then the request should be unauthorized
    And I should get an error "Token expired"

  Scenario: Guardian experience and leveling
    Given I am a registered Guardian at INITIATE level
    When I gain 150 experience points
    Then my level should be updated to APPRENTICE
    And my total experience should be 150

  Scenario: Guardian step tracking
    Given I am a registered Guardian
    When I add 1000 steps to my profile
    Then my total steps should be 1000
    And my step history should be recorded

  Scenario: Guardian energy generation
    Given I am a registered Guardian
    When I generate 100 energy points
    Then my total energy generated should be 100
    And my energy history should be recorded