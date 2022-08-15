package com.example.inventorysheriff.data;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.inventorysheriff.R;
import com.example.inventorysheriff.data.adapter.DeviceListAdapter;
import com.example.inventorysheriff.data.model.BluetoothSheriffDevice;
import com.example.inventorysheriff.data.model.DatabaseHelper;
import com.example.inventorysheriff.data.utils.BluetoothHandler;
import com.example.inventorysheriff.data.utils.WeightMeasurement;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BondState;

import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DiscoveryDevicesActivity extends AppCompatActivity {


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_LOCATION_REQUEST = 2;
    ListView devicesListView;
    List<BluetoothPeripheral> devices = new ArrayList<>();
    DeviceListAdapter adapter;
    private List<String> adresses = new ArrayList<>();
    ProgressBar progressBar;
    private TextView blackBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_devices);
        devicesListView = findViewById(R.id.devices);
        adapter = new DeviceListAdapter(devices, DiscoveryDevicesActivity.this);
        devicesListView.setAdapter(adapter);
        progressBar = findViewById(R.id.progress);
        blackBoard = findViewById(R.id.blackboard);
        registerReceiver(locationServiceStateReceiver,
                new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));
        registerReceiver(weightDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_WEIGHT));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getBluetoothManager().getAdapter() != null) {
            if (!isBluetoothEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
               checkPermissions();
            }
        } else {
            Log.e("","This device has no Bluetooth hardware");
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            Log.e("","missing permissions length "+missingPermissions.length);
            for(String permission:missingPermissions)
                Log.e("","perm "+permission);
            if (missingPermissions.length > 0) {
                requestPermissions(missingPermissions, ACCESS_LOCATION_REQUEST);
            } else {
                permissionsGranted();
            }
        }
    }
    private String[] getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String requiredPermission : requiredPermissions) {
                if (getApplicationContext().checkSelfPermission(requiredPermission) !=
                    PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
        }
        return missingPermissions.toArray(new String[0]);
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S)
        {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q)
        {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        } else return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    private void permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && targetSdkVersion < Build.VERSION_CODES.S)
        {
            if (checkLocationServices()) {
                initBluetoothHandler();
            }
        } else {
            initBluetoothHandler();
        }
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }

    private void initBluetoothHandler()
    {
        BluetoothHandler.getInstance(DiscoveryDevicesActivity.this);
    }

    @NotNull
    private BluetoothManager getBluetoothManager() {
        return Objects.requireNonNull((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE),"cannot get BluetoothManager");
    }

    private BluetoothPeripheral getPeripheral(String peripheralAddress) {
        BluetoothCentralManager central = BluetoothHandler.getInstance(DiscoveryDevicesActivity.this).central;
        return central.getPeripheral(peripheralAddress);
    }

    private final BroadcastReceiver weightDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            WeightMeasurement measurement = (WeightMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_WEIGHT_EXTRA);
            if (measurement != null) {
            //    measurementValue.setText(String.format(Locale.ENGLISH, "%.1f %s\n%s\n\nfrom %s", measurement.weight,
            //    measurement.unit.toString(), dateFormat.format(measurement.timestamp), peripheral.getName()));
                Log.e("", "Found peripheral '%s'" + peripheral.getName());
                Log.e("FOUND!", "PERIPHERAL FOUND!!!!");
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
                    //saving data to bd
                    try {
                        BluetoothSheriffDevice sheriffDevice = new BluetoothSheriffDevice();
                        sheriffDevice.setName(peripheral.getName());
                        sheriffDevice.setAddress(peripheral.getAddress());
                        sheriffDevice.setDate(new Date(new java.util.Date().getTime()));
                        sheriffDevice.setWeight(measurement.weight);
                        sheriffDevice.setItem(measurement.itemId);
                        new DatabaseHelper(DiscoveryDevicesActivity.this).getBluetoothSheriffDeviceDao().createOrUpdate(sheriffDevice);
                    }catch (Exception e){
                        Log.e("ERROR",e.getMessage());
                    }
                }
            }
        }
    };


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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(locationServiceStateReceiver);
        unregisterReceiver(weightDataReceiver);
    }
    private final BroadcastReceiver locationServiceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(LocationManager.MODE_CHANGED_ACTION)) {
                boolean isEnabled = areLocationServicesEnabled();
                Log.e("",String.format("Location service state changed to: %s",
                             isEnabled ? "on" : "off"));
               checkPermissions();
            }
        }
    };

    private boolean areLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getApplicationContext().
                                          getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e("","could not get location manager");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            return isGpsEnabled || isNetworkEnabled;
        }
    }

    private boolean checkLocationServices() {
        if (!areLocationServicesEnabled()) {
            new AlertDialog.Builder(DiscoveryDevicesActivity.this)
                    .setTitle("Location services are not enabled")
                    .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                          dialog.cancel();
                        }
                    })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check if all permission were granted
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            permissionsGranted();
        } else {
            new AlertDialog.Builder(DiscoveryDevicesActivity.this)
                    .setTitle("Permission is required for scanning Bluetooth peripherals")
                    .setMessage("Please grant permissions")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            checkPermissions();
                        }
                    })
                    .create()
                    .show();
        }
    }

    public void publishDevice(BluetoothPeripheral peripheral){
        Log.e("", "Found peripheral '%s'" + peripheral.getName());
        Log.e("FOUND!", "PERIPHERAL FOUND!!!!");
        StringBuffer text = new StringBuffer(blackBoard.getText().toString());
        text.append("\n");
        if(peripheral.getName()!=null && !peripheral.getName().isEmpty()){
            text.append(String.format(getString(R.string.peripheral_found)+" %s",peripheral.getName()));
        }else{
            text.append(String.format(getString(R.string.peripheral_found)+" %s","unknow"));
        }
        text.append("\n");
        text.append(String.format("Address: %s",peripheral.getAddress()));
        text.append("\n");
        text.append(getString(R.string.establishing_connection));
        text.append("...");
        blackBoard.setText(text.toString());
    }
}