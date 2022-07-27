package com.example.inventorysheriff.data.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.example.inventorysheriff.R;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;



/**
 * Created by Dani on 22/10/15.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "dokuflex.db";
    private static final int DATABASE_VERSION = 21;

    /**
     * The data access object used to interact with the Sqlite database to do C.R.U.D operations.
     */
    //        nombre de la tabla, clave primaria
    private Dao<BluetoothSheriffDevice, String> sheriffDao;




    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION,
                 R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
        try {

            /**
             * creates the Todo database table
             */
            TableUtils.createTableIfNotExists(connectionSource, BluetoothSheriffDevice.class);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
        try {
            /**
             * Recreates the database when onUpgrade is called by the framework
             */
            if(oldVer<newVer) {
                TableUtils.dropTable(connectionSource, BluetoothSheriffDevice.class, true);
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e){
           Log.e("ERROR",e.getMessage());

        }
        finally {
            onCreate(sqliteDatabase, connectionSource);
        }
    }



    /**
     * Returns an instance of the data access object
     *
     * @return
     * @throws SQLException
     */
    public Dao<BluetoothSheriffDevice, String> getBluetoothSheriffDeviceDao() throws SQLException {
        if (sheriffDao == null) {
            sheriffDao = getDao(BluetoothSheriffDevice.class);
        }
        return sheriffDao;
    }


}
