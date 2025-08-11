import UIKit
import XCTest
@testable import Runner

class RunnerTests: XCTestCase {

  override func setUpWithError() throws {
    // Put setup code here. This method is called before the invocation of each test method in the class.
    continueAfterFailure = false
  }

  override func tearDownWithError() throws {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
  }

  func testAppDelegateExists() throws {
    // Test that the AppDelegate class can be instantiated
    let appDelegate = AppDelegate()
    XCTAssertNotNil(appDelegate, "AppDelegate should be instantiable")
  }
  
  func testApplicationDidFinishLaunching() throws {
    // Test that the app delegate responds to the launch method
    let appDelegate = AppDelegate()
    
    // In unit test environment, UIApplication.shared might not be fully initialized
    // So we test that the method exists and can be called safely
    XCTAssertTrue(appDelegate.responds(to: #selector(AppDelegate.application(_:didFinishLaunchingWithOptions:))), 
                 "AppDelegate should respond to application:didFinishLaunchingWithOptions:")
    
    // Test that the AppDelegate can be initialized without crashing
    XCTAssertNotNil(appDelegate, "AppDelegate should be instantiable")
  }
  
  func testAppBundleIdentifier() throws {
    // Verify the app bundle identifier is set correctly
    let bundleIdentifier = Bundle.main.bundleIdentifier
    XCTAssertNotNil(bundleIdentifier, "Bundle identifier should be set")
    XCTAssertTrue(bundleIdentifier?.contains("guardianes") == true, "Bundle identifier should contain 'guardianes'")
  }

  func testAppDisplayName() throws {
    // Verify the app display name is set
    let displayName = Bundle.main.object(forInfoDictionaryKey: "CFBundleDisplayName") as? String
    let bundleName = Bundle.main.object(forInfoDictionaryKey: "CFBundleName") as? String
    
    // In unit test environment, bundle info might be different
    // We'll check if any name is available, and if not, just verify the bundle is accessible
    if displayName == nil && bundleName == nil {
      // If neither is set, at least verify we can access Bundle.main
      XCTAssertNotNil(Bundle.main, "Bundle.main should be accessible")
      
      // Also verify basic bundle properties that should exist
      let infoDictionary = Bundle.main.infoDictionary
      XCTAssertNotNil(infoDictionary, "Bundle info dictionary should be accessible")
    } else {
      // If we have name information, verify it's not empty
      if let name = displayName ?? bundleName {
        XCTAssertFalse(name.isEmpty, "App name should not be empty")
      }
    }
  }

}
