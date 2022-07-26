package com.example.inventorysheriff.data;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.inventorysheriff.R;
import com.example.inventorysheriff.data.adapter.DeviceListAdapter;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
public class DiscoveryDevicesActivity extends AppCompatActivity {

    private BluetoothCentralManager manager;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_LOCATION_REQUEST = 2;
    private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
    ListView devicesListView;
    List<BluetoothPeripheral> devices = new ArrayList<>();
    DeviceListAdapter adapter ;

    private List<String> adresses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_devices);
        devicesListView = findViewById(R.id.devices);
        adapter = new DeviceListAdapter(devices,DiscoveryDevicesActivity.this);
        devicesListView.setAdapter(adapter);
        ProgressBar progressBar = findViewById(R.id.progress);
        manager = new BluetoothCentralManager(DiscoveryDevicesActivity.this,
                new BluetoothCentralManagerCallback() {

                    @Override
                    public void onDiscoveredPeripheral(BluetoothPeripheral peripheral,
                                                       ScanResult scanResult) {
                        if (peripheral.getBondState() == BondState.NONE &&
                                !adresses.contains(peripheral.getAddress())) {
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
                                        R.string.scan_failed,Toast.LENGTH_LONG).show();
                        Log.e("scan failed", "scan failed");
                    }

                }, new Handler());

        manager.scanForPeripherals();
        Log.e("blueetooth enabled", "blueetooth enabled? " + manager.isBluetoothEnabled());

        if (!manager.isBluetoothEnabled()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.warning);
            dialogBuilder.setMessage(R.string.no_bluetooth_activated);
            dialogBuilder.setPositiveButton(R.string.enable_blueetoth, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (!mBluetoothAdapter.isEnabled()) {
                        if (ActivityCompat.checkSelfPermission(DiscoveryDevicesActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(DiscoveryDevicesActivity.this,
                                    new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.INTERNET, Manifest.permission.BLUETOOTH,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                    }, 43);
                        }
                        mBluetoothAdapter.enable();
                    }
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dialogBuilder.create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getBluetoothManager().getAdapter() != null) {
            if (!isBluetoothEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                checkPermissions();
            }
        } else {
            Toast.makeText(this,R.string.no_bluetooth_device,Toast.LENGTH_LONG).show();
        }
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }


     private BluetoothManager getBluetoothManager() {
        return Objects.requireNonNull((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE),"cannot get BluetoothManager");
    }



    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            if (missingPermissions.length > 0) {
                requestPermissions(missingPermissions, ACCESS_LOCATION_REQUEST);
            }
        }
    }

    private String[] getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String requiredPermission : requiredPermissions) {
                if (getApplicationContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
        }
        return missingPermissions.toArray(new String[0]);
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        } else return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    private boolean areLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Toast.makeText(this,"could not get location manager",Toast.LENGTH_LONG).show();
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
                            // if this button is clicked, just close
                            // the dialog box and do nothing
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


    public void connectToDevice(BluetoothPeripheral peripheral){
        Intent t = new Intent(this, DeviceActivity.class);
        t.putExtra("peripheral",peripheral.getAddress());
        startActivity(t);
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

        if (!allGranted) {
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
}