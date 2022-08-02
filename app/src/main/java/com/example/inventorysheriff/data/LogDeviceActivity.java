package com.example.inventorysheriff.data;

import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.example.inventorysheriff.R;
import com.example.inventorysheriff.data.model.BluetoothSheriffDevice;
import com.example.inventorysheriff.data.model.DatabaseHelper;

import java.sql.Date;
import java.sql.SQLException;


public class LogDeviceActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_device);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            //createLogDevices();
        }catch (Exception e){
            Log.e("ERROR",e.getMessage());
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_log_device);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    private void createLogDevices() throws SQLException {
        DatabaseHelper helper = new DatabaseHelper(this);
        for(int i = 0;i< 10;i++){
            BluetoothSheriffDevice device = new BluetoothSheriffDevice();
            device.setDate(new Date(new java.util.Date().getTime()));
            device.setAddress("30:AE:A4:02:33:A2");
            device.setName("sheriff");
            device.setItem(String.valueOf(i));
            device.setWeight(204.3+i);
            helper.getDao(BluetoothSheriffDevice.class).create(device);
        }
        for(int i = 0;i< 10;i++){
            BluetoothSheriffDevice device = new BluetoothSheriffDevice();
            device.setDate(new Date(new java.util.Date().getTime()));
            device.setAddress("30:AE:A4:02:33:A2");
            device.setName("sheriff");
            device.setItem(String.valueOf(i));
            device.setWeight(207.3+i);
            helper.getDao(BluetoothSheriffDevice.class).create(device);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_log_device);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.discover_devices:
                startActivity(new Intent(LogDeviceActivity.this, DiscoveryDevicesActivity.class));
                break;

            default:
                break;
        }

        return true;
    }


}