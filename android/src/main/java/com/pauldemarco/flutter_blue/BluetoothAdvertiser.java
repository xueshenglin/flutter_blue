// 定义包名
package com.pauldemarco.flutter_blue;

// 导入必要的注解类
import android.annotation.TargetApi;
// 导入蓝牙适配器类
import android.bluetooth.BluetoothAdapter;
// 导入蓝牙管理器类
import android.bluetooth.BluetoothManager;
// 导入蓝牙广播回调类
import android.bluetooth.le.AdvertiseCallback;
// 导入蓝牙广播数据类
import android.bluetooth.le.AdvertiseData;
// 导入蓝牙广播设置类
import android.bluetooth.le.AdvertiseSettings;
// 导入蓝牙低功耗广播器类
import android.bluetooth.le.BluetoothLeAdvertiser;
// 导入上下文类
import android.content.Context;
// 导入系统版本类
import android.os.Build;
// 导入日志类
import android.util.Log;
// 导入字节数组类
import java.util.Arrays;

/**
 * 蓝牙广播器类，用于管理蓝牙低功耗广播功能。
 */
public class BluetoothAdvertiser {
    // 日志标签
    private static final String TAG = "BluetoothAdvertiser";
    // 蓝牙低功耗广播器实例
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    // 上下文对象
    private Context context;
    // 蓝牙广播回调实例
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
    // 蓝牙广播数据实例
    private AdvertiseData advertiseData;
    // 广告状态标志. 由于Android 没有提供直接获取广告状态的方法，
    // 我们需要自己维护一个标志来表示当前是否正在广播。
    // 这个标志可以在 startAdvertising 和 stopAdvertising 方法中进行同步。
    private boolean isAdvertising = false;
    private AdvertiseSettings advertiseSettings;

    /**
     * 构造函数，初始化蓝牙广播器。
     * 
     * @param context 上下文对象
     */
    public BluetoothAdvertiser(Context context) {
        this.context = context;
        // 获取蓝牙管理器实例
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        // 获取蓝牙适配器实例
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        // 检查系统版本是否支持蓝牙低功耗广播，并且蓝牙适配器是否可用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && bluetoothAdapter != null) {
            // 获取蓝牙低功耗广播器实例
            bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        }
        this.advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();
    }

    /**
     * 启动蓝牙低功耗广播。
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
     * 停止蓝牙低功耗广播。
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
     * 检查当前是否在广播状态
     * 
     * @return true表示正在广播，false表示未广播
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean isAdvertising() {
        return isAdvertising;
    }

    /**
     * 设置蓝牙低功耗广播数据并重新启动广播。
     * 
     * @param data 要设置的广播数据
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

        // 仅设置厂商数据，不包含其他信息
        this.advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(
                        ((data[0] & 0xFF) << 8) | (data[1] & 0xFF), // 前两个字节作为厂商ID
                        Arrays.copyOfRange(data, 2, data.length) // 剩余字节作为数据
                )
                .build();

        // 停止当前广播
        stopAdvertising();

        // 启动新广播
        if (this.advertiseData != null) {
            bluetoothLeAdvertiser.startAdvertising(advertiseSettings, this.advertiseData, advertiseCallback);
        }
    }
}