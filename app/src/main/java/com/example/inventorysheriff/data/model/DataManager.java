package com.example.inventorysheriff.data.model;


import android.util.Log;
import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Dani on 27/4/16.
 */
public class DataManager {
    public DataManager() {
    }

    public static void limpiarTablas(DatabaseHelper databaseHelper) {


        try {
            Dao<BluetoothSheriffDevice, String> sheriffDeviceDao= databaseHelper.getBluetoothSheriffDeviceDao();



            if (sheriffDeviceDao.isTableExists()) {
                List<BluetoothSheriffDevice> list = sheriffDeviceDao.queryForAll();
                for (int i = 0; i < list.size(); i++) {
                    sheriffDeviceDao.delete(list.get(i));
                }
            }

        } catch (SQLException e) {
            Log.e("ERROR",e.getMessage());
        }


    }




}
