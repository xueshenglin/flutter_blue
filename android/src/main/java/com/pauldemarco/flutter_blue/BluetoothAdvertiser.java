// Define package name
package com.pauldemarco.flutter_blue;

// Import necessary annotation classes
import android.annotation.TargetApi;
// Import Bluetooth adapter class
import android.bluetooth.BluetoothAdapter;
// Import Bluetooth manager class
import android.bluetooth.BluetoothManager;
// Import Bluetooth advertise callback class
import android.bluetooth.le.AdvertiseCallback;
// Import Bluetooth advertise data class
import android.bluetooth.le.AdvertiseData;
// Import Bluetooth advertise settings class
import android.bluetooth.le.AdvertiseSettings;
// Import Bluetooth Low Energy advertiser class
import android.bluetooth.le.BluetoothLeAdvertiser;
// Import context class
import android.content.Context;
// Import system version class
import android.os.Build;
// 导入日志类
import android.util.Log;
// 导入字节数组类
import java.util.Arrays;

/**
 * Bluetooth advertiser class for managing Bluetooth Low Energy advertising functionality.
 */
public class BluetoothAdvertiser {
    // Log tag
    private static final String TAG = "BluetoothAdvertiser";
    // Bluetooth Low Energy advertiser instance
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    // Context object
    private Context context;
    // Bluetooth advertise callback instance
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            isAdvertising = true; // 确保状态同步
            Log.i(TAG, "Advertising started successfully.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            isAdvertising = false; // 确保状态同步
            Log.e(TAG, "Advertising failed with error code: " + errorCode);
        }
    };
    // Bluetooth advertise data instance
    private AdvertiseData advertiseData;
    // Advertising status flag. Since Android does not provide a direct method to get advertising status,
    // we need to maintain our own flag to indicate whether advertising is currently active.
    // This flag is synchronized in the startAdvertising and stopAdvertising methods.
    private boolean isAdvertising = false;
    private AdvertiseSettings advertiseSettings;

    /**
     * Constructor to initialize the Bluetooth advertiser.
     * 
     * @param context Context object
     */
    public BluetoothAdvertiser(Context context) {
        this.context = context;
        // Get Bluetooth manager instance
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        // Get Bluetooth adapter instance
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        // Check if system version supports Bluetooth Low Energy advertising and if Bluetooth adapter is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && bluetoothAdapter != null) {
            // Get Bluetooth Low Energy advertiser instance
            bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        }
        this.advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();
    }

    /**
     * Starts Bluetooth Low Energy advertising.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startAdvertising() {
        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "Bluetooth LE advertiser is not available.");
            return;
        }

        // 启动蓝牙低功耗广播
        if (advertiseData != null) {
            bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
            isAdvertising = true;
        } else {
            Log.e(TAG, "Advertise data is not set.");
        }
    }

    /**
     * Stops Bluetooth Low Energy advertising.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopAdvertising() {
        // 检查蓝牙低功耗广播器和广播回调是否可用
        if (bluetoothLeAdvertiser != null && advertiseCallback != null) {
            // 停止蓝牙低功耗广播
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            isAdvertising = false; // 设置广告状态为false
            Log.i(TAG, "Advertising stopped.");
        }
    }

    /**
     * Checks if currently advertising
     * 
     * @return true if advertising, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean isAdvertising() {
        return isAdvertising;
    }

    /**
     * Sets Bluetooth Low Energy advertising data and restarts advertising.
     * 
     * @param data The advertising data to set
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setAdvertisingData(byte[] data) {
        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "Bluetooth LE advertiser is not available.");
            return;
        }

        if (advertiseCallback == null) {
            Log.e(TAG, "Advertise callback is not initialized.");
            return;
        }

        // Only set manufacturer data, no other information included
        this.advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(
                        ((data[0] & 0xFF) << 8) | (data[1] & 0xFF), // First two bytes as manufacturer ID
                        Arrays.copyOfRange(data, 2, data.length) // Remaining bytes as data
                )
                .build();

        // Stop current advertising
        stopAdvertising();

        // Restart advertising with new data
        if (this.advertiseData != null) {
            bluetoothLeAdvertiser.startAdvertising(advertiseSettings, this.advertiseData, advertiseCallback);
        }
    }
}