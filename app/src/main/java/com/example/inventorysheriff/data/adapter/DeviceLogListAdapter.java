package com.example.inventorysheriff.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.inventorysheriff.R;
import com.example.inventorysheriff.data.model.BluetoothSheriffDevice;

import java.util.List;

public class DeviceLogListAdapter extends ArrayAdapter<BluetoothSheriffDevice> {

    private List<BluetoothSheriffDevice> dataSet;
    Context mContext;

    public DeviceLogListAdapter(List<BluetoothSheriffDevice> data, Context context) {
        super(context, R.layout.device_list_adapter, data);
        this.dataSet = data;
        this.mContext=context;
    }

    public List<BluetoothSheriffDevice> getDataSet() {
        return dataSet;
    }

    public void setDataSet(List<BluetoothSheriffDevice> dataSet) {
        this.dataSet = dataSet;
    }



    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothSheriffDevice dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        DeviceLogListAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {
            viewHolder = new DeviceLogListAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.device_log_list_adapter, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.deviceName);
            viewHolder.txtAddress = (TextView) convertView.findViewById(R.id.address);
            viewHolder.txtDate = (TextView)  convertView.findViewById(R.id.deviceDate);
            viewHolder.txtWeigth = (TextView)  convertView.findViewById(R.id.weight);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DeviceLogListAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ?
                R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;
        String name = dataModel.getName().isEmpty()? "Unnamed":dataModel.getName();
        viewHolder.txtName.setText(name);
        viewHolder.txtAddress.setText(dataModel.getAddress());
        viewHolder.txtDate.setText(dataModel.getDate().toString());
        viewHolder.txtWeigth.setText(String.valueOf(dataModel.getWeight()));
        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtAddress;
        TextView txtDate;
        TextView txtWeigth;
    }


}
