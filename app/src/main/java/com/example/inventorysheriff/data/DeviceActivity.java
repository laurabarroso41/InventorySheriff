package com.example.inventorysheriff.data;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventorysheriff.R;
import com.google.android.material.snackbar.Snackbar;
import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.BondState;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;
import com.welie.blessed.ScanFailure;
import com.welie.blessed.WriteType;

import java.util.Calendar;
import java.util.UUID;

public class DeviceActivity extends AppCompatActivity {

    // UUIDs for the Device Information service (DIS)
    private static final UUID DIS_SERVICE_UUID = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
    private static final UUID MANUFACTURER_NAME_CHARACTERISTIC_UUID = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb");
    private static final UUID MODEL_NUMBER_CHARACTERISTIC_UUID = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Current Time service (CTS)
    private static final UUID CTS_SERVICE_UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    private static final UUID CURRENT_TIME_CHARACTERISTIC_UUID = UUID.fromString("00002A2B-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Battery Service (BAS)
    private static final UUID BTS_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    private BluetoothCentralManager manager;
    private TextView deviceContent;
    private LinearLayout contentHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        deviceContent = findViewById(R.id.device_content);
        contentHolder = findViewById(R.id.content_holder);

        String address = savedInstanceState.getString("peripheral");

        manager = new BluetoothCentralManager(DeviceActivity.this,
                new BluetoothCentralManagerCallback() {

                    @Override
                    public void onDiscoveredPeripheral(BluetoothPeripheral peripheral,
                                                       ScanResult scanResult) {
                        if (peripheral.getBondState() == BondState.NONE &&
                                !address.equals(peripheral.getAddress())) {
                            peripheral.createBond();
                        }
                    }


                    @Override
                    public void onConnectedPeripheral(@NonNull BluetoothPeripheral peripheral) {
                        super.onConnectedPeripheral(peripheral);
                    }

                    @Override
                    public void onConnectionFailed(@NonNull BluetoothPeripheral peripheral,
                                                   @NonNull HciStatus status) {
                        super.onConnectionFailed(peripheral, status);
                    }

                    @Override
                    public void onDisconnectedPeripheral(@NonNull BluetoothPeripheral peripheral,
                                                         @NonNull HciStatus status) {
                        super.onDisconnectedPeripheral(peripheral, status);

                    }

                    @Override
                    public void onScanFailed(@NonNull ScanFailure scanFailure) {
                        super.onScanFailed(scanFailure);
                        Toast.makeText(DeviceActivity.this,
                                R.string.scan_failed,Toast.LENGTH_LONG).show();
                        Log.e("scan failed", "scan failed");
                    }

                }, new Handler());
       BluetoothPeripheral peripheral =  manager.getPeripheral(address);
       connectToDevice(peripheral);

    }


    private boolean isOmronBPM(final String name) {
        return name.contains("BLESmart_") || name.contains("BLEsmart_");
    }


    public void connectToDevice(BluetoothPeripheral peripheral){
        Intent t = new Intent(this, DeviceActivity.class);
        t.putExtra("peripheral",peripheral.getAddress());
        startActivity(t);
        Snackbar.make(deviceContent,R.string.establishing_connection,Snackbar.LENGTH_LONG).show();

        manager.autoConnectPeripheral(peripheral, new BluetoothPeripheralCallback() {
            @Override
            public void onCharacteristicUpdate(@NonNull BluetoothPeripheral peripheral,
                                               @NonNull byte[] value,
                                               @NonNull BluetoothGattCharacteristic characteristic,
                                               @NonNull GattStatus status) {
                super.onCharacteristicUpdate(peripheral, value, characteristic, status);
                Log.e("blessed","on characteristic update");
            }

            @Override
            public void onCharacteristicWrite(@NonNull BluetoothPeripheral peripheral,
                                              @NonNull byte[] value,
                                              @NonNull BluetoothGattCharacteristic characteristic,
                                              @NonNull GattStatus status) {
                super.onCharacteristicWrite(peripheral, value, characteristic, status);
                Log.e("blessed","on characteristic write");
            }

            @Override
            public void onDescriptorRead(@NonNull BluetoothPeripheral peripheral,
                                         @NonNull byte[] value,
                                         @NonNull BluetoothGattDescriptor descriptor,
                                         @NonNull GattStatus status) {
                super.onDescriptorRead(peripheral, value, descriptor, status);
                Log.e("blessed","descriptor read");
            }

            @Override
            public void onDescriptorWrite(@NonNull BluetoothPeripheral peripheral,
                                          @NonNull byte[] value,
                                          @NonNull BluetoothGattDescriptor descriptor,
                                          @NonNull GattStatus status) {
                super.onDescriptorWrite(peripheral, value, descriptor, status);
                Log.e("blessed","descriptor write");
            }

            @Override
            public void onBondingStarted(@NonNull BluetoothPeripheral peripheral) {
                super.onBondingStarted(peripheral);
                Log.e("bonding started","bonding started");
                playSound();
            }

            @Override
            public void onBondingSucceeded(@NonNull BluetoothPeripheral peripheral) {
                super.onBondingSucceeded(peripheral);
                Log.e("bonding succeeded","bonding succeeded");
                Snackbar.make(deviceContent,getString(R.string.bonding_succeeded) +
                        peripheral.getName(),Snackbar.LENGTH_LONG).show();
                playSound();
            }

            @Override
            public void onBondingFailed(@NonNull BluetoothPeripheral peripheral) {
                super.onBondingFailed(peripheral);
                Snackbar.make(deviceContent,getString(R.string.bonding_failed) +
                        peripheral.getName(),Snackbar.LENGTH_LONG).show();
                playSound();
                Log.e("blessed","bonding failed");
            }

            @Override
            public void onBondLost(@NonNull BluetoothPeripheral peripheral) {
                super.onBondLost(peripheral);
                playSound();
                Snackbar.make(deviceContent,getString(R.string.bonding_lost) +
                        peripheral.getName(),Snackbar.LENGTH_LONG).show();
                Log.e("blessed","on bond lost");
            }

            @Override
            public void onConnectionUpdated(@NonNull BluetoothPeripheral peripheral, int interval,
                                            int latency, int timeout, @NonNull GattStatus status) {
                super.onConnectionUpdated(peripheral, interval, latency, timeout, status);
                playSound();
                if(status.equals(GattStatus.SUCCESS)){
                    deviceContent.setText(getString(R.string.connection_success));
                }else if (status.equals(GattStatus.CONNECTION_CONGESTED))
                {
                    deviceContent.setText(getString(R.string.connection_congested));
                }else if (status.equals(GattStatus.CONNECTION_CANCELLED))
                {
                    deviceContent.setText(getString(R.string.connection_canceled));
                }
                Log.e("blessed","on connection updated");
            }

            @Override
            public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral) {
                super.onServicesDiscovered(peripheral);
                deviceContent.setText(getString(R.string.services_discovered));
                peripheral.requestMtu(BluetoothPeripheral.MAX_MTU);
                // Request a new connection priority
                peripheral.requestConnectionPriority(ConnectionPriority.HIGH);
                peripheral.readCharacteristic(DIS_SERVICE_UUID, MANUFACTURER_NAME_CHARACTERISTIC_UUID);
                peripheral.readCharacteristic(DIS_SERVICE_UUID, MODEL_NUMBER_CHARACTERISTIC_UUID);
                // Turn on notifications for Current Time Service and write it if possible
                BluetoothGattCharacteristic currentTimeCharacteristic =
                        peripheral.getCharacteristic(CTS_SERVICE_UUID, CURRENT_TIME_CHARACTERISTIC_UUID);
                if (currentTimeCharacteristic != null) {
                    peripheral.setNotify(currentTimeCharacteristic, true);

                    // If it has the write property we write the current time
                    if ((currentTimeCharacteristic.getProperties() & PROPERTY_WRITE) > 0) {
                        // Write the current time unless it is an Omron device
                        if (!isOmronBPM(peripheral.getName())) {
                            BluetoothBytesParser parser = new BluetoothBytesParser();
                            parser.setCurrentTime(Calendar.getInstance());
                            peripheral.writeCharacteristic(currentTimeCharacteristic, parser.getValue(), WriteType.WITH_RESPONSE);
                        }
                    }
                }
                Toast.makeText(DeviceActivity.this,"servicio descubierto!",
                        Toast.LENGTH_LONG).show();
                playSound();
                Log.e("blessed","on services discovered");
                Log.e("blessed","on services discovered");
                Log.e("blessed","on services discovered");
                Log.e("blessed","on services discovered");
                Log.e("blessed","on services discovered");
                Log.e("blessed","on services discovered");
            }
        });

        Log.e("bonding state ","bonding state "+peripheral.getBondState().value);

    }

    private void playSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            Log.e("ERROR",e.getMessage());
        }
    }
}