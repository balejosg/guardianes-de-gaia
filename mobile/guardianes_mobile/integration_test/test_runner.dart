import 'package:integration_test/integration_test.dart';
import 'package:flutter_test/flutter_test.dart';

// Import all integration test files
import 'guardian_auth_integration_test.dart' as auth_tests;
import 'step_tracking_integration_test.dart' as step_tests;
import 'app_flow_integration_test.dart' as flow_tests;

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('All Integration Tests', () {
    // Run authentication tests
    auth_tests.main();

    // Run step tracking tests
    step_tests.main();

    // Run complete app flow tests
    flow_tests.main();
  });
}
