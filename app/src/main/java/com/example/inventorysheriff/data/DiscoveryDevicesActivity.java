package com.example.inventorysheriff.data;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.inventorysheriff.R;
import com.example.inventorysheriff.data.adapter.DeviceListAdapter;
import com.example.inventorysheriff.ui.login.LoginActivity;
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

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;

public class DiscoveryDevicesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 113;
    private static final int REQUEST_BLUETOOTH_CONNECT = 114;
    private BluetoothCentralManager manager;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_LOCATION_REQUEST = 2;
    private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
    ListView devicesListView;
    List<BluetoothPeripheral> devices = new ArrayList<>();
    DeviceListAdapter adapter;
    private List<String> adresses = new ArrayList<>();
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_devices);
        devicesListView = findViewById(R.id.devices);
        adapter = new DeviceListAdapter(devices, DiscoveryDevicesActivity.this);
        devicesListView.setAdapter(adapter);
        progressBar = findViewById(R.id.progress);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH_SCAN}, 1);
        else
        initBluetoothHandler();
    }


    private void initBluetoothHandler() {
        initializeManager();
        manager.scanForPeripherals();
    }







    private void initializeManager() {
        manager = new BluetoothCentralManager(DiscoveryDevicesActivity.this,
                new BluetoothCentralManagerCallback() {

                    @Override
                    public void onDiscoveredPeripheral(BluetoothPeripheral peripheral,
                                                       ScanResult scanResult) {
                        Log.e("FOUND!","PERIPHERAL FOUND!!!!");
                        if (peripheral.getBondState() == BondState.NONE &&
                                !adresses.contains(peripheral.getAddress()) &&
                                (peripheral.getName().contains("sheriff") || true)) {
                            progressBar.setVisibility(View.GONE);
                            peripheral.createBond();
                            devices.add(peripheral);
                            adresses.add(peripheral.getAddress());
                            DiscoveryDevicesActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.setDataSet(devices);
                                    adapter.notifyDataSetChanged();
                                }
                            });
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
                        devices.remove(peripheral);
                        DiscoveryDevicesActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.setDataSet(devices);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onScanFailed(@NonNull ScanFailure scanFailure) {
                        super.onScanFailed(scanFailure);
                        Toast.makeText(DiscoveryDevicesActivity.this,
                                R.string.scan_failed, Toast.LENGTH_LONG).show();
                        Log.e("scan failed", "scan failed");
                    }

                }, new Handler(Looper.getMainLooper()));

        Log.e("blueetooth enabled", "blueetooth enabled? " + manager.isBluetoothEnabled());
    }



    public void connectToDevice(BluetoothPeripheral peripheral) {
        Intent t = new Intent(this, DeviceActivity.class);
        t.putExtra("peripheral", peripheral.getAddress());
        startActivity(t);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.devices_log_menu:
                startActivity(new Intent(DiscoveryDevicesActivity.this, LogDeviceActivity.class));
                break;

            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                initBluetoothHandler();
            }
            else
                finish();
        }
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                initBluetoothHandler();
            }else finish();
        }

    }
}