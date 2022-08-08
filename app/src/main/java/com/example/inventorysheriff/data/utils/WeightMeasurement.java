package com.example.inventorysheriff.data.utils;

import com.welie.blessed.BluetoothBytesParser;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT8;

public class WeightMeasurement implements Serializable {
    public final double weight;
    public final WeightUnit unit;
    public Integer itemId;

    public WeightMeasurement(byte[] byteArray) {
        BluetoothBytesParser parser = new BluetoothBytesParser(byteArray);

        // Parse flag byte
        final int flags = parser.getIntValue(BluetoothBytesParser.FORMAT_UINT8);
        unit = ((flags & 0x01) > 0) ? WeightUnit.Pounds : WeightUnit.Kilograms;
        final boolean itemIdPresent = (flags & 0x04) > 0;

        // Get weight value
        double weightMultiplier = (unit == WeightUnit.Kilograms) ? 0.005 : 0.01;
        weight = parser.getIntValue(FORMAT_UINT16) * weightMultiplier;

        // Get item ID if present
        if (itemIdPresent) {
            itemId = parser.getIntValue(FORMAT_UINT8);
        }

    }

    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return String.format("%.1f %s, user %d", weight, unit == WeightUnit.Kilograms ? "kg" : "lb", itemId);
    }
}
