package com.example.inventorysheriff.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.query.In;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Date;

@DatabaseTable(tableName = "bluetooth_sherif_device")
public class BluetoothSheriffDevice {
    @DatabaseField(columnName = "name")
    private String name;
    @DatabaseField(columnName = "address")
    private String address;
    @DatabaseField(generatedId = true, columnName = "device_id")
    public int deviceId;
    @DatabaseField(columnName = "item")
    private Integer item;
    @DatabaseField(columnName = "weight")
    private double weight;
    @DatabaseField(columnName = "date")
    private Date date;

    public int getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setItem(Integer item) {
        this.item = item;
    }

    public Integer getItem() {
        return item;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
