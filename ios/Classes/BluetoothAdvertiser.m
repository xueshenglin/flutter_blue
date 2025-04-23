#import "BluetoothAdvertiser.h"

@interface BluetoothAdvertiser ()

@property (nonatomic, strong) CBPeripheralManager *peripheralManager;
@property (nonatomic, strong) NSDictionary<NSString *, id> *advertisementData;

@end

@implementation BluetoothAdvertiser

- (instancetype)init {
    self = [super init];
    if (self) {
        self.peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];
        self.advertisementData = nil;
    }
    return self;
}

- (void)startAdvertising {
    // 当advertisementData为nil时，相当于停止广播
    // 当advertisementData与当前蓝牙广播的数据不同时, 相当于更新广播的数据.
    // CoreBluetooth的广播机制会自动处理数据更新，不需要先停止广播再重新启动
    if (self.advertisementData) {
        // 确保使用不可连接模式
        NSDictionary *options = @{CBPeripheralManagerOptionShowPowerAlertKey: @NO,
                                 CBPeripheralManagerOptionRestoreIdentifierKey: @"myIdentifier"};
        [self.peripheralManager startAdvertising:@{
            CBAdvertisementDataServiceUUIDsKey: @[], // 空服务列表
            CBAdvertisementDataLocalNameKey: [NSNull null], // 不广播名称
            CBAdvertisementDataManufacturerDataKey: self.advertisementData[CBAdvertisementDataManufacturerDataKey]
        }];
    }
}

- (void)stopAdvertising {
    [self.peripheralManager stopAdvertising];
}

- (BOOL)isAdvertising {
    return self.peripheralManager.isAdvertising;
}

// data = {0x01,0x02,0x03,0x04,0x05,0x06}
// 0x0102 为厂商ID
// 0x03,0x04,0x05,0x06 为厂商数据
// @{CBAdvertisementDataManufacturerDataKey: <010203040506>}
- (void)setAdvertisingData:(NSArray<NSNumber *> *)hexData {
    if (hexData.count == 0) {
        // 当hexData为空数组时，相当于停止广播
        self.advertisementData = nil;
        [self stopAdvertising];  // 确保停止广播
        return;
    }
    NSMutableData *data = [NSMutableData data];
    for (NSNumber *number in hexData) {
        uint8_t byte = [number unsignedCharValue];
        [data appendBytes:&byte length:1];
    }
    
    if (hexData.count >= 2) {
        uint16_t manufacturerId = ([hexData[0] unsignedCharValue] << 8) | [hexData[1] unsignedCharValue];
        NSData *payload = [NSData dataWithBytes:hexData[2].unsignedCharValue length:hexData.count-2];
        NSMutableData *combinedData = [NSMutableData dataWithBytes:&manufacturerId length:2];
        [combinedData appendData:payload];
        self.advertisementData = @{CBAdvertisementDataManufacturerDataKey: combinedData};
        
        // 设置完数据后自动启动广播
        [self startAdvertising];
    }
}

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    if (peripheral.state == CBPeripheralManagerStatePoweredOn) {
        NSLog(@"Bluetooth is powered on and ready to advertise.");
    } else {
        NSLog(@"Bluetooth is not available.");
    }
}

@end