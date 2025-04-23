import CoreBluetooth

/// 蓝牙广播器类，用于管理BLE广播功能
@objcMembers public class BluetoothAdvertiser: NSObject {
    /// 外围设备管理器，用于控制蓝牙广播
    private var peripheralManager: CBPeripheralManager?
    /// 广播数据，包含厂商自定义数据
    private var advertisementData: [String: Any]?

    override public init() {
        super.init()
        self.peripheralManager = CBPeripheralManager(delegate: self, queue: nil)
        self.advertisementData = nil
    }

    /// 开始广播
    /// 如果已经设置了广播数据，则开始广播
    public func startAdvertising() {
        if let data = advertisementData {
            peripheralManager?.startAdvertising(data)
        }
    }

    /// 停止广播
    public func stopAdvertising() {
        peripheralManager?.stopAdvertising()
    }

    /// 设置广播数据
    /// - Parameter hexData: 十六进制数据列表，将作为厂商自定义数据广播
    public func setAdvertisingData(_ hexData: [NSNumber]) {
        // 将十六进制列表转换为NSData
        var data = Data()
        for number in hexData {
            data.append(UInt8(truncating: number))
        }
        
        // 只包含厂商定义数据
        advertisementData = [
            CBAdvertisementDataManufacturerDataKey: data
        ]
    }
}

/// 实现CBPeripheralManagerDelegate协议
extension BluetoothAdvertiser: CBPeripheralManagerDelegate {
    /// 蓝牙状态更新回调
    public func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        if peripheral.state == .poweredOn {
            print("Bluetooth is powered on and ready to advertise.")
        } else {
            print("Bluetooth is not available.")
        }
    }
}
