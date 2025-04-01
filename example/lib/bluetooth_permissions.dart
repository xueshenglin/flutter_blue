import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

class BluetoothPermissions {
  static final BluetoothPermissions _instance = BluetoothPermissions._internal();
  factory BluetoothPermissions() => _instance;
  BluetoothPermissions._internal();

  Future<bool> checkAndRequestPermissions() async {
    return await _requestPermissions();
  }

  Future<bool> _requestPermissions() async {
    if (await Permission.bluetooth.status.isDenied) {
      await Permission.bluetooth.request();
    }
    if (await Permission.bluetoothScan.status.isDenied) {
      await Permission.bluetoothScan.request();
    }
    if (await Permission.bluetoothConnect.status.isDenied) {
      await Permission.bluetoothConnect.request();
    }
    if (await Permission.location.status.isDenied) {
      await Permission.location.request();
    }

    return await Permission.bluetooth.isGranted &&
        await Permission.bluetoothScan.isGranted &&
        await Permission.bluetoothConnect.isGranted &&
        await Permission.location.isGranted;
  }
}

class BluetoothPermissionWidget extends StatelessWidget {
  final VoidCallback onPermissionGranted;
  final bool permissionsGranted;
  final VoidCallback onRequestPermissions;

  const BluetoothPermissionWidget({
    Key? key,
    required this.onPermissionGranted,
    required this.permissionsGranted,
    required this.onRequestPermissions,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if (permissionsGranted) return const SizedBox.shrink();

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        ElevatedButton(
          child: const Text('授予蓝牙权限'),
          onPressed: onRequestPermissions,
        ),
        Padding(
          padding: const EdgeInsets.all(20.0),
          child: Text(
            '请授予必要的权限以使用蓝牙功能',
            style: Theme.of(context)
                .primaryTextTheme
                .bodyMedium
                ?.copyWith(color: Colors.white70),
            textAlign: TextAlign.center,
          ),
        ),
      ],
    );
  }
} 