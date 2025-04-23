#import <Flutter/Flutter.h>
#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

NS_ASSUME_NONNULL_BEGIN

@interface BluetoothAdvertiser : NSObject <CBPeripheralManagerDelegate>

- (instancetype)init;
- (void)startAdvertising;
- (void)stopAdvertising;
- (void)setAdvertisingData:(NSArray<NSNumber *> *)hexData;
- (BOOL)isAdvertising;

@end

NS_ASSUME_NONNULL_END
