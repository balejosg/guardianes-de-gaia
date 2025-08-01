import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import '../../data/services/qr_scanner_service.dart';
import '../bloc/card_bloc.dart';
import '../bloc/card_event.dart';
import '../bloc/card_state.dart';
import '../widgets/scan_result_dialog.dart';

class QRScannerPage extends StatefulWidget {
  final int guardianId;

  const QRScannerPage({
    super.key,
    required this.guardianId,
  });

  @override
  State<QRScannerPage> createState() => _QRScannerPageState();
}

class _QRScannerPageState extends State<QRScannerPage> {
  final QRScannerService _scannerService = QRScannerService();
  StreamSubscription<String>? _scanSubscription;
  bool _isProcessing = false;
  bool _hasScanned = false;

  @override
  void initState() {
    super.initState();
    _initializeScanner();
  }

  Future<void> _initializeScanner() async {
    final success = await _scannerService.initialize();
    if (success && mounted) {
      _startScanning();
    } else {
      _showPermissionDialog();
    }
  }

  void _startScanning() {
    _scannerService.startScanning();
    _scanSubscription = _scannerService.scanResults.listen(_onQRCodeDetected);
  }

  void _onQRCodeDetected(String qrCode) {
    if (_isProcessing || _hasScanned) return;

    setState(() {
      _isProcessing = true;
      _hasScanned = true;
    });

    // Add vibration/sound feedback here if desired
    context.read<CardBloc>().add(ScanQRCodeEvent(
          guardianId: widget.guardianId,
          qrCode: qrCode,
        ));
  }

  void _showPermissionDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Permiso de Cámara'),
        content: const Text(
          'La aplicación necesita acceso a la cámara para escanear códigos QR.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () async {
              Navigator.of(context).pop();
              final granted = await QRScannerService.requestCameraPermission();
              if (granted && mounted) {
                _initializeScanner();
              } else {
                Navigator.of(context).pop();
              }
            },
            child: const Text('Permitir'),
          ),
        ],
      ),
    );
  }

  void _resetScanning() {
    setState(() {
      _isProcessing = false;
      _hasScanned = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Escanear Carta'),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      body: BlocListener<CardBloc, CardState>(
        listener: (context, state) {
          if (state is CardScanSuccess) {
            _showScanResult(state.result);
          } else if (state is CardScanFailure) {
            _showErrorResult(state.error);
          }
        },
        child: Stack(
          children: [
            if (_scannerService.isInitialized)
              MobileScanner(
                controller: _scannerService.controller,
                onDetect: _scannerService.onDetect,
              ),
            _buildOverlay(),
            if (_isProcessing) _buildLoadingIndicator(),
          ],
        ),
      ),
    );
  }

  Widget _buildOverlay() {
    return Container(
      decoration: ShapeDecoration(
        shape: QRScannerOverlayShape(
          borderColor: Theme.of(context).primaryColor,
          borderRadius: 10,
          borderLength: 30,
          borderWidth: 5,
          cutOutSize: 250,
        ),
      ),
    );
  }

  Widget _buildLoadingIndicator() {
    return Container(
      color: Colors.black54,
      child: const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            CircularProgressIndicator(),
            SizedBox(height: 16),
            Text(
              'Procesando código QR...',
              style: TextStyle(color: Colors.white),
            ),
          ],
        ),
      ),
    );
  }

  void _showScanResult(dynamic result) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => ScanResultDialog(
        result: result,
        guardianId: widget.guardianId,
        onContinue: () {
          Navigator.of(context).pop();
          _resetScanning();
        },
        onClose: () {
          Navigator.of(context).pop();
          Navigator.of(context).pop();
        },
      ),
    );
  }

  void _showErrorResult(String error) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Error'),
        content: Text(error),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              _resetScanning();
            },
            child: const Text('Intentar de nuevo'),
          ),
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              Navigator.of(context).pop();
            },
            child: const Text('Cerrar'),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _scanSubscription?.cancel();
    _scannerService.dispose();
    super.dispose();
  }
}

class QRScannerOverlayShape extends ShapeBorder {
  final Color borderColor;
  final double borderWidth;
  final Color overlayColor;
  final double borderRadius;
  final double borderLength;
  final double cutOutSize;

  const QRScannerOverlayShape({
    this.borderColor = Colors.red,
    this.borderWidth = 3.0,
    this.overlayColor = const Color.fromRGBO(0, 0, 0, 80),
    this.borderRadius = 0,
    this.borderLength = 40,
    this.cutOutSize = 250,
  });

  @override
  EdgeInsetsGeometry get dimensions => const EdgeInsets.all(10);

  @override
  Path getInnerPath(Rect rect, {TextDirection? textDirection}) {
    return Path()
      ..fillType = PathFillType.evenOdd
      ..addPath(getOuterPath(rect), Offset.zero);
  }

  @override
  Path getOuterPath(Rect rect, {TextDirection? textDirection}) {
    Path path = Path();
    path.addRect(rect);

    Path scanRect = Path();
    final scanSize = cutOutSize;
    final left = rect.center.dx - scanSize / 2;
    final top = rect.center.dy - scanSize / 2;
    final scanRectangle = Rect.fromLTWH(left, top, scanSize, scanSize);
    scanRect.addRRect(
        RRect.fromRectAndRadius(scanRectangle, Radius.circular(borderRadius)));

    return Path.combine(PathOperation.difference, path, scanRect);
  }

  @override
  void paint(Canvas canvas, Rect rect, {TextDirection? textDirection}) {
    final borderOffset = borderWidth / 2;
    final scanSize = cutOutSize;
    final scanRect = Rect.fromCenter(
      center: rect.center,
      width: scanSize,
      height: scanSize,
    );

    final borderPaint = Paint()
      ..color = borderColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = borderWidth;

    final path = Path()
      ..fillType = PathFillType.evenOdd
      ..addRect(rect)
      ..addRRect(
          RRect.fromRectAndRadius(scanRect, Radius.circular(borderRadius)));

    canvas.clipPath(path);
    canvas.drawRect(rect, Paint()..color = overlayColor);

    // Draw corner lines
    canvas.drawPath(
      Path()
        ..moveTo(scanRect.left - borderOffset,
            scanRect.top - borderOffset + borderLength)
        ..lineTo(scanRect.left - borderOffset, scanRect.top - borderOffset)
        ..lineTo(scanRect.left - borderOffset + borderLength,
            scanRect.top - borderOffset),
      borderPaint,
    );

    canvas.drawPath(
      Path()
        ..moveTo(scanRect.right + borderOffset,
            scanRect.top - borderOffset + borderLength)
        ..lineTo(scanRect.right + borderOffset, scanRect.top - borderOffset)
        ..lineTo(scanRect.right + borderOffset - borderLength,
            scanRect.top - borderOffset),
      borderPaint,
    );

    canvas.drawPath(
      Path()
        ..moveTo(scanRect.left - borderOffset,
            scanRect.bottom + borderOffset - borderLength)
        ..lineTo(scanRect.left - borderOffset, scanRect.bottom + borderOffset)
        ..lineTo(scanRect.left - borderOffset + borderLength,
            scanRect.bottom + borderOffset),
      borderPaint,
    );

    canvas.drawPath(
      Path()
        ..moveTo(scanRect.right + borderOffset,
            scanRect.bottom + borderOffset - borderLength)
        ..lineTo(scanRect.right + borderOffset, scanRect.bottom + borderOffset)
        ..lineTo(scanRect.right + borderOffset - borderLength,
            scanRect.bottom + borderOffset),
      borderPaint,
    );
  }

  @override
  ShapeBorder scale(double t) {
    return QRScannerOverlayShape(
      borderColor: borderColor,
      borderWidth: borderWidth,
      overlayColor: overlayColor,
    );
  }
}
