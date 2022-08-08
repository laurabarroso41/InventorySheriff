package com.example.inventorysheriff.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.inventorysheriff.R;
import com.example.inventorysheriff.data.DiscoveryDevicesActivity;
import com.welie.blessed.BluetoothPeripheral;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<BluetoothPeripheral> {

    private List<BluetoothPeripheral> dataSet;
    Context mContext;

    public DeviceListAdapter(List<BluetoothPeripheral> data, Context context) {
        super(context, R.layout.device_list_adapter, data);
        this.dataSet = data;
        this.mContext=context;
    }

    public List<BluetoothPeripheral> getDataSet() {
        return dataSet;
    }

    public void setDataSet(List<BluetoothPeripheral> dataSet) {
        this.dataSet = dataSet;
    }



    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothPeripheral dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.device_list_adapter, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.deviceName);
            viewHolder.txtAddress = (TextView) convertView.findViewById(R.id.address);
            viewHolder.btnConnect = (ImageButton)convertView.findViewById(R.id.connectBtn);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ?
                R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        String name = dataModel.getName().isEmpty()? "Unnamed":dataModel.getName();
        viewHolder.txtName.setText(name);
        viewHolder.txtAddress.setText(dataModel.getAddress());

        viewHolder.btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.btnConnect.setVisibility(View.GONE );
             //   ((DiscoveryDevicesActivity) mContext).connectToDevice(dataModel);
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtAddress;
        ImageButton btnConnect;
    }


}
