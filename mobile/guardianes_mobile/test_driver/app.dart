import 'package:flutter_driver/flutter_driver.dart';
import '../lib/main.dart' as app;

// Flutter Driver test app entry point
// This file enables the app to be controllable by Flutter Driver for performance testing
void main() {
  // Enable integration testing with Flutter Driver
  enableFlutterDriverExtension();
  
  // Run the main app
  app.main();
}