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
    // CBAdvertisementDataServiceUUIDsKey: @[],
    // CBAdvertisementDataManufacturerDataKey: manufacturerData
    if (self.advertisementData) {
        NSData *manufacturerData = self.advertisementData[CBAdvertisementDataManufacturerDataKey];
        const uint8_t *bytes = (const uint8_t *)[manufacturerData bytes];
        NSUInteger length = [manufacturerData length];
        
        // ASCII转换表
        const uint8_t asciiTable[] = {
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 
            0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46
        };
        
        NSMutableString *localName = [NSMutableString string];
        for (NSUInteger i = 0; i < length; i++) {
            uint8_t byte = bytes[i];
            // 高4位
            uint8_t highNibble = (byte >> 4) & 0x0F;
            // 低4位
            uint8_t lowNibble = byte & 0x0F;
            
            [localName appendFormat:@"%c%c", asciiTable[highNibble], asciiTable[lowNibble]];
        }
        NSLog(@"localName: %@", localName);

        [self.peripheralManager startAdvertising:@{
            CBAdvertisementDataLocalNameKey: localName,
            CBAdvertisementDataServiceUUIDsKey: @[],
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
        // 修复：正确获取payload数据
        NSArray<NSNumber *> *payloadArray = [hexData subarrayWithRange:NSMakeRange(2, hexData.count-2)];
        NSMutableData *payloadData = [NSMutableData data];
        for (NSNumber *number in payloadArray) {
            uint8_t byte = [number unsignedCharValue];
            [payloadData appendBytes:&byte length:1];
        }
        NSMutableData *combinedData = [NSMutableData dataWithBytes:&manufacturerId length:2];
        [combinedData appendData:payloadData];
        self.advertisementData = @{CBAdvertisementDataManufacturerDataKey: combinedData};
        
        // 设置完数据后自动启动广播
        [self startAdvertising];
    }
}

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    if (@available(iOS 10.0, *)) {
        if (peripheral.state == CBManagerStatePoweredOn) {
            NSLog(@"Bluetooth is powered on and ready to advertise.");
        } else {
            NSLog(@"Bluetooth is not available.");
        }
    } else {
        // Fallback on earlier versions
        if (peripheral.state == CBPeripheralManagerStatePoweredOn) {
            NSLog(@"Bluetooth is powered on and ready to advertise.");
        } else {
            NSLog(@"Bluetooth is not available.");
        }
    }
}

@end
