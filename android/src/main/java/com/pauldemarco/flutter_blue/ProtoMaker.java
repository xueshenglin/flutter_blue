// Copyright 2017, Paul DeMarco.
// All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.pauldemarco.flutter_blue;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

/**
 * Created by paul on 8/31/17.
 */

public class ProtoMaker {

    private  final UUID CCCD_UUID = UUID.fromString("000002902-0000-1000-8000-00805f9b34fb");

    static Protos.ScanResult from(BluetoothDevice device, byte[] advertisementData, int rssi) {
        Protos.ScanResult.Builder p = Protos.ScanResult.newBuilder();
        p.setDevice(from(device));
        if(advertisementData != null && advertisementData.length > 0)
            p.setAdvertisementData(AdvertisementParser.parse(advertisementData));
        p.setRssi(rssi);
        return p.build();
    }

    @TargetApi(21)
    static Protos.ScanResult from(BluetoothDevice device, ScanResult scanResult) {
        Protos.ScanResult.Builder p = Protos.ScanResult.newBuilder();
        p.setDevice(from(device));
        Protos.AdvertisementData.Builder a = Protos.AdvertisementData.newBuilder();
        ScanRecord scanRecord = scanResult.getScanRecord();
        if(Build.VERSION.SDK_INT >= 26) {
            a.setConnectable(scanResult.isConnectable());
        } else {
            if(scanRecord != null) {
                int flags = scanRecord.getAdvertiseFlags();
                a.setConnectable((flags & 0x2) > 0);
            }
        }
        if(scanRecord != null) {
            String deviceName = scanRecord.getDeviceName();
            if(deviceName != null) {
                // Log.d("FlutterBlue", "Device name: " + deviceName);
                a.setLocalName(deviceName);
            }
            int txPower = scanRecord.getTxPowerLevel();
            if(txPower != Integer.MIN_VALUE) {
                // Log.d("FlutterBlue", "Tx power: " + txPower);
                a.setTxPowerLevel(Protos.Int32Value.newBuilder().setValue(txPower));
            }
            // Manufacturer Specific Data
            SparseArray<byte[]> msd = scanRecord.getManufacturerSpecificData();
            if(msd != null) {
                for (int i = 0; i < msd.size(); i++) {
                    try {
                        int key = msd.keyAt(i);
                        byte[] value = msd.valueAt(i);
                       // Log.d("FlutterBlue", "Adding manufacturer data: " + key + ", length: " + (value != null ? value.length : "null"));
                        if (value != null) {
                            a.putManufacturerData(key, ByteString.copyFrom(value));
                        }
                    } catch (Exception e) {
                        // Log.e("FlutterBlue", "Error adding manufacturer data", e);
                    }
                }
            }
            
            // Service Data
            Map<ParcelUuid, byte[]> serviceData = scanRecord.getServiceData();
            if(serviceData != null) {
                // Log.d("FlutterBlue", "Service data size: " + serviceData.size());
                for (Map.Entry<ParcelUuid, byte[]> entry : serviceData.entrySet()) {
                    try {
                        ParcelUuid key = entry.getKey();
                        byte[] value = entry.getValue();
                        if(key != null && value != null) {
                            String uuidString = key.getUuid().toString();
                            // Log.d("FlutterBlue", "Adding service data for UUID: " + uuidString + ", length: " + value.length);
                            a.putServiceData(uuidString, ByteString.copyFrom(value));
                        } else {
                            // Log.w("FlutterBlue", "Service data entry has null key or value");
                        }
                    } catch (Exception e) {
                        Log.e("FlutterBlue", "Error adding service data", e);
                    }
                }
            }
            
            // Service UUIDs
            List<ParcelUuid> serviceUuids = scanRecord.getServiceUuids();
            if(serviceUuids != null) {
                // Log.d("FlutterBlue", "Service UUIDs size: " + serviceUuids.size());
                for (ParcelUuid s : serviceUuids) {
                    try {
                        if(s != null) {
                            String uuidString = s.getUuid().toString();
                            // Log.d("FlutterBlue", "Adding service UUID: " + uuidString);
                            a.addServiceUuids(uuidString);
                        } else {
                            // Log.w("FlutterBlue", "Service UUID is null");
                        }
                    } catch (Exception e) {
                        // Log.e("FlutterBlue", "Error adding service UUID", e);
                    }
                }
            }
        } else {
            // Log.d("FlutterBlue", "Scan record is null");
        }
        
        // Log.d("FlutterBlue", "Building advertisement data...");
        // // 打印当前 AdvertisementData.Builder 的状态
        // Log.d("FlutterBlue", "Advertisement data state before build:");
        // Log.d("FlutterBlue", "  - Manufacturer data size: " + a.getManufacturerDataCount());
        // Log.d("FlutterBlue", "  - Service data size: " + a.getServiceDataCount());
        // Log.d("FlutterBlue", "  - Service UUIDs size: " + a.getServiceUuidsCount());
        
        try {
            // Log.d("FlutterBlue", "Building advertisement data...");
            
            // 创建一个新的 builder
            Protos.AdvertisementData.Builder newBuilder = Protos.AdvertisementData.newBuilder();
            
            // 复制基本字段
            String localName = a.getLocalName();
            if (localName != null && !localName.isEmpty()) {
                newBuilder.setLocalName(localName);
            }
            
            Protos.Int32Value txPowerLevel = a.getTxPowerLevel();
            if (txPowerLevel != null) {
                newBuilder.setTxPowerLevel(txPowerLevel);
            }
            
            newBuilder.setConnectable(a.getConnectable());
            
            // 清理并复制 manufacturer data
            Map<Integer, ByteString> manufacturerData = new HashMap<>();
            for (Map.Entry<Integer, ByteString> entry : a.getManufacturerDataMap().entrySet()) {
                try {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        manufacturerData.put(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    // Log.e("FlutterBlue", "Error processing manufacturer data entry", e);
                }
            }
            
            // 清理并复制 service data
            Map<String, ByteString> serviceData = new HashMap<>();
            for (Map.Entry<String, ByteString> entry : a.getServiceDataMap().entrySet()) {
                try {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        serviceData.put(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    // Log.e("FlutterBlue", "Error processing service data entry", e);
                }
            }
            
            // 清理并复制 service UUIDs
            List<String> serviceUuids = new ArrayList<>();
            for (String uuid : a.getServiceUuidsList()) {
                try {
                    if (uuid != null && !uuid.isEmpty()) {
                        serviceUuids.add(uuid);
                    }
                } catch (Exception e) {
                    // Log.e("FlutterBlue", "Error processing service UUID", e);
                }
            }
            
            // 添加清理后的数据
            for (Map.Entry<Integer, ByteString> entry : manufacturerData.entrySet()) {
                newBuilder.putManufacturerData(entry.getKey(), entry.getValue());
            }
            
            for (Map.Entry<String, ByteString> entry : serviceData.entrySet()) {
                newBuilder.putServiceData(entry.getKey(), entry.getValue());
            }
            
            for (String uuid : serviceUuids) {
                newBuilder.addServiceUuids(uuid);
            }
            
            Protos.AdvertisementData advertisementData = newBuilder.build();
            // Log.d("FlutterBlue", "Built advertisement data");
            
            // Log.d("FlutterBlue", "Setting advertisement data...");
            p.setAdvertisementData(advertisementData);
            // Log.d("FlutterBlue", "Set advertisement data");
            
            // Log.d("FlutterBlue", "Building final result...");
            Protos.ScanResult result = p.build();
            // Log.d("FlutterBlue", "Built final result");
            return result;
        } catch (Exception e) {
            // Log.e("FlutterBlue", "Error building advertisement data", e);
            e.printStackTrace();
            // 返回一个基本的扫描结果
            return Protos.ScanResult.newBuilder()
                    .setDevice(from(device))
                    .setRssi(scanResult.getRssi())
                    .setAdvertisementData(Protos.AdvertisementData.newBuilder().build())
                    .build();
        }
    }

    static Protos.BluetoothDevice from(BluetoothDevice device) {
        Protos.BluetoothDevice.Builder p = Protos.BluetoothDevice.newBuilder();
        p.setRemoteId(device.getAddress());
        String name = device.getName();
        if(name != null) {
            p.setName(name);
        }
        switch(device.getType()){
            case BluetoothDevice.DEVICE_TYPE_LE:
                p.setType(Protos.BluetoothDevice.Type.LE);
                break;
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                p.setType(Protos.BluetoothDevice.Type.CLASSIC);
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                p.setType(Protos.BluetoothDevice.Type.DUAL);
                break;
            default:
                p.setType(Protos.BluetoothDevice.Type.UNKNOWN);
                break;
        }
        return p.build();
    }

    static Protos.BluetoothService from(BluetoothDevice device, BluetoothGattService service, BluetoothGatt gatt) {
        Protos.BluetoothService.Builder p = Protos.BluetoothService.newBuilder();
        p.setRemoteId(device.getAddress());
        p.setUuid(service.getUuid().toString());
        p.setIsPrimary(service.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY);
        for(BluetoothGattCharacteristic c : service.getCharacteristics()) {
            p.addCharacteristics(from(device, c, gatt));
        }
        for(BluetoothGattService s : service.getIncludedServices()) {
            p.addIncludedServices(from(device, s, gatt));
        }
        return p.build();
    }

    static Protos.BluetoothCharacteristic from(BluetoothDevice device, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        Protos.BluetoothCharacteristic.Builder p = Protos.BluetoothCharacteristic.newBuilder();
        p.setRemoteId(device.getAddress());
        p.setUuid(characteristic.getUuid().toString());
        p.setProperties(from(characteristic.getProperties()));
        if(characteristic.getValue() != null)
            p.setValue(ByteString.copyFrom(characteristic.getValue()));
        for(BluetoothGattDescriptor d : characteristic.getDescriptors()) {
            p.addDescriptors(from(device, d));
        }
        if(characteristic.getService().getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY) {
            p.setServiceUuid(characteristic.getService().getUuid().toString());
        } else {
            // Reverse search to find service
            for(BluetoothGattService s : gatt.getServices()) {
                for(BluetoothGattService ss : s.getIncludedServices()) {
                    if(ss.getUuid().equals(characteristic.getService().getUuid())){
                        p.setServiceUuid(s.getUuid().toString());
                        p.setSecondaryServiceUuid(ss.getUuid().toString());
                        break;
                    }
                }
            }
        }
        return p.build();
    }

    static Protos.BluetoothDescriptor from(BluetoothDevice device, BluetoothGattDescriptor descriptor) {
        Protos.BluetoothDescriptor.Builder p = Protos.BluetoothDescriptor.newBuilder();
        p.setRemoteId(device.getAddress());
        p.setUuid(descriptor.getUuid().toString());
        p.setCharacteristicUuid(descriptor.getCharacteristic().getUuid().toString());
        p.setServiceUuid(descriptor.getCharacteristic().getService().getUuid().toString());
        if(descriptor.getValue() != null)
            p.setValue(ByteString.copyFrom(descriptor.getValue()));
        return p.build();
    }

    static Protos.CharacteristicProperties from(int properties) {
        return Protos.CharacteristicProperties.newBuilder()
                .setBroadcast((properties & 1) != 0)
                .setRead((properties & 2) != 0)
                .setWriteWithoutResponse((properties & 4) != 0)
                .setWrite((properties & 8) != 0)
                .setNotify((properties & 16) != 0)
                .setIndicate((properties & 32) != 0)
                .setAuthenticatedSignedWrites((properties & 64) != 0)
                .setExtendedProperties((properties & 128) != 0)
                .setNotifyEncryptionRequired((properties & 256) != 0)
                .setIndicateEncryptionRequired((properties & 512) != 0)
                .build();
    }

    static Protos.DeviceStateResponse from(BluetoothDevice device, int state) {
        Protos.DeviceStateResponse.Builder p = Protos.DeviceStateResponse.newBuilder();
        switch(state) {
            case BluetoothProfile.STATE_DISCONNECTING:
                p.setState(Protos.DeviceStateResponse.BluetoothDeviceState.DISCONNECTING);
                break;
            case BluetoothProfile.STATE_CONNECTED:
                p.setState(Protos.DeviceStateResponse.BluetoothDeviceState.CONNECTED);
                break;
            case BluetoothProfile.STATE_CONNECTING:
                p.setState(Protos.DeviceStateResponse.BluetoothDeviceState.CONNECTING);
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                p.setState(Protos.DeviceStateResponse.BluetoothDeviceState.DISCONNECTED);
                break;
            default:
                break;
        }
        p.setRemoteId(device.getAddress());
        return p.build();
    }
}