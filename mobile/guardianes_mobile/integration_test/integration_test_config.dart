// Integration Test Configuration
class IntegrationTestConfig {
  // Backend Configuration
  static const String backendUrl = String.fromEnvironment(
    'BACKEND_URL',
    defaultValue: 'http://dev-guardianes.duckdns.org',
  );

  static const String apiVersion = 'v1';
  static const String baseApiUrl = '$backendUrl/api/$apiVersion';

  // Test Guardian Credentials
  static const String testEmail = 'integration.test@guardianes.com';
  static const String testPassword = 'TestPassword123!';
  static const String testGuardianName = 'Integration Test Guardian';

  // Test Timeouts
  static const Duration defaultTimeout = Duration(seconds: 30);
  static const Duration networkTimeout = Duration(seconds: 10);
  static const Duration animationTimeout = Duration(milliseconds: 300);

  // Test Step Counts
  static const int normalStepCount = 1000;
  static const int highStepCount = 10000;
  static const int extremeStepCount = 100000;

  // Energy Conversion Rate
  static const int stepsPerEnergy = 10;

  // Test Retry Configuration
  static const int maxRetries = 3;
  static const Duration retryDelay = Duration(seconds: 2);

  // CI Environment Detection
  static bool get isCI =>
      const String.fromEnvironment('CI', defaultValue: 'false') == 'true';

  // Test Mode Configuration
  static bool get verboseLogging =>
      const String.fromEnvironment('VERBOSE_TESTS', defaultValue: 'false') ==
      'true';

  static bool get skipSlowTests =>
      const String.fromEnvironment('SKIP_SLOW_TESTS', defaultValue: 'false') ==
      'true';
}

// Widget Key Constants for Testing
class TestKeys {
  // Authentication
  static const String guardianNameField = 'guardian_name_field';
  static const String guardianEmailField = 'guardian_email_field';
  static const String guardianPasswordField = 'guardian_password_field';
  static const String guardianConfirmPasswordField =
      'guardian_confirm_password_field';
  static const String registerSubmitButton = 'register_submit_button';
  static const String loginEmailField = 'login_email_field';
  static const String loginPasswordField = 'login_password_field';
  static const String loginSubmitButton = 'login_submit_button';

  // Navigation
  static const String stepTrackingNav = 'step_tracking_nav';
  static const String stepHistoryTab = 'step_history_tab';

  // Step Tracking
  static const String currentStepCount = 'current_step_count';
  static const String energyBalance = 'energy_balance';
  static const String energyDisplay = 'energy_display';
  static const String submitStepsButton = 'submit_steps_button';
  static const String stepHistoryList = 'step_history_list';
}

// Test Helpers
class TestHelpers {
  static String generateTestEmail() {
    final timestamp = DateTime.now().millisecondsSinceEpoch;
    return 'test.guardian.$timestamp@guardianes.com';
  }

  static String generateTestGuardianName() {
    final timestamp = DateTime.now().millisecondsSinceEpoch;
    return 'Test Guardian $timestamp';
  }

  static int calculateExpectedEnergy(int stepCount) {
    return stepCount ~/ IntegrationTestConfig.stepsPerEnergy;
  }
}
