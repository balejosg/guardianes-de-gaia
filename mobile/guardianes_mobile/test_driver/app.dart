import 'package:flutter_driver/driver_extension.dart';
import 'package:guardianes_mobile/main.dart' as app;

// Flutter Driver test app entry point
// This file enables the app to be controllable by Flutter Driver for performance testing
void main() {
  // Enable integration testing with Flutter Driver
  enableFlutterDriverExtension();
  
  // Run the main app
  app.main();
}