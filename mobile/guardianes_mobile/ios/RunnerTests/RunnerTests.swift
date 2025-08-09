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
    let application = UIApplication.shared
    
    // This should not crash
    let result = appDelegate.application(application, didFinishLaunchingWithOptions: nil)
    XCTAssertTrue(result, "Application should finish launching successfully")
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
    
    // Either display name or bundle name should be set
    XCTAssertTrue(displayName != nil || bundleName != nil, "App should have a display name or bundle name")
    
    if let name = displayName ?? bundleName {
      XCTAssertFalse(name.isEmpty, "App name should not be empty")
    }
  }

}
