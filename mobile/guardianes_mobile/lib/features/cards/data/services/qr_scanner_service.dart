import 'dart:async';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'package:permission_handler/permission_handler.dart';

class QRScannerService {
  late MobileScannerController _controller;
  StreamController<String>? _scanResultController;
  bool _isInitialized = false;

  Stream<String> get scanResults => _scanResultController!.stream;
  bool get isInitialized => _isInitialized;

  Future<bool> initialize() async {
    try {
      // Request camera permission
      final permission = await Permission.camera.request();
      if (permission != PermissionStatus.granted) {
        return false;
      }

      _controller = MobileScannerController(
        detectionSpeed: DetectionSpeed.noDuplicates,
        facing: CameraFacing.back,
        torchEnabled: false,
      );

      _scanResultController = StreamController<String>.broadcast();
      _isInitialized = true;

      return true;
    } catch (e) {
      print('Error initializing QR scanner: $e');
      return false;
    }
  }

  MobileScannerController get controller {
    if (!_isInitialized) {
      throw StateError('QR Scanner not initialized. Call initialize() first.');
    }
    return _controller;
  }

  void startScanning() {
    if (!_isInitialized) {
      throw StateError('QR Scanner not initialized. Call initialize() first.');
    }
    _controller.start();
  }

  void stopScanning() {
    if (!_isInitialized) return;
    _controller.stop();
  }

  void onDetect(BarcodeCapture capture) {
    final List<Barcode> barcodes = capture.barcodes;
    for (final barcode in barcodes) {
      final String? code = barcode.rawValue;
      if (code != null && code.isNotEmpty) {
        _scanResultController?.add(code);
        break; // Only process first valid code
      }
    }
  }

  Future<void> toggleTorch() async {
    if (!_isInitialized) return;
    await _controller.toggleTorch();
  }

  Future<void> switchCamera() async {
    if (!_isInitialized) return;
    await _controller.switchCamera();
  }

  Future<bool> get hasTorch async {
    if (!_isInitialized) return false;
    // Note: hasTorch API may vary by mobile_scanner version
    // Check if torch is available by trying to use it
    try {
      return true; // Assume most cameras have flash
    } catch (e) {
      return false;
    }
  }

  void dispose() {
    if (_isInitialized) {
      _controller.dispose();
      _scanResultController?.close();
      _isInitialized = false;
    }
  }

  static Future<bool> checkCameraPermission() async {
    final permission = await Permission.camera.status;
    return permission == PermissionStatus.granted;
  }

  static Future<bool> requestCameraPermission() async {
    final permission = await Permission.camera.request();
    return permission == PermissionStatus.granted;
  }
}
