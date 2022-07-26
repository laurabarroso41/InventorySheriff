package com.example.inventorysheriff.data;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.example.inventorysheriff.R;
import com.example.inventorysheriff.data.adapter.DeviceLogListAdapter;
import com.example.inventorysheriff.data.model.BluetoothSheriffDevice;
import com.example.inventorysheriff.data.model.DatabaseHelper;
import com.j256.ormlite.stmt.query.OrderBy;

import java.util.List;

import javax.xml.validation.Validator;

public class DeviceLogListFragment extends Fragment implements AbsListView.OnScrollListener{


    ListView devicesLogList;
    DeviceLogListAdapter adapter;
    private int visibleLastIndex = 0;   //Final Visual Item Index
    private int visibleItemCount=10;       //Total number of visible items in the current window
    private Handler handler = new Handler();
    private ProgressBar progressBar;
    private TextView nodevicesTxt;
    private ScrollView scrollView;
    private long genericCount;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        devicesLogList = view.findViewById(R.id.log_devices_list);
        devicesLogList.setOnScrollListener(DeviceLogListFragment.this);
        progressBar = view.findViewById(R.id.progress);
        nodevicesTxt = view.findViewById(R.id.no_device);

        initAdapter();
        devicesLogList.setAdapter(adapter);
        return view;
    }

    private void initAdapter() {
        try {
            List<BluetoothSheriffDevice> data = new DatabaseHelper(getActivity()).
                    getDao(BluetoothSheriffDevice.class).queryBuilder().
                    limit(Long.parseLong(String.valueOf(10)))
                    .offset(Long.parseLong("0")).orderBy("date", false)
                    .query();
            if(data.size()>0) {
                adapter = new DeviceLogListAdapter(data, getActivity());
                genericCount = new DatabaseHelper(getActivity()).getBluetoothSheriffDeviceDao().countOf();

            }
            else
               nodevicesTxt.setVisibility(View.VISIBLE);
        }catch (Exception e){
            Log.e("ERROR",e.getMessage());
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = adapter.getCount() - 1;    //Index of the Last Item of Data Set
        int lastIndex = itemsLastIndex + 1;             //Add the loadMoreView item at the bottom

        Log.e("","visibleLastIndex = "+visibleLastIndex+" lastIndex = "+lastIndex);
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == itemsLastIndex) {
            //If it's automatic loading, you can put asynchronous loading data code here.
            Log.i("LOADMORE", "loading...");
            loadMore(view);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }
    /**
     * Click on the button event
     * @param view
     */
    public void loadMore(View view) {
        if (genericCount>adapter.getCount()) {
            if (progressBar != null)
                progressBar.setVisibility(View.VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadData();
                    adapter.notifyDataSetChanged();
                    //devicesLogList.setSelection(visibleLastIndex - visibleItemCount + 1);
                    progressBar.setVisibility(View.GONE);
                }
            }, 2000);
        }
    }

    private void loadData() {
        try {
            List<BluetoothSheriffDevice> list =adapter.getDataSet();
            List<BluetoothSheriffDevice> aux =
            new DatabaseHelper(getActivity()).
                    getDao(BluetoothSheriffDevice.class).queryBuilder().
                    limit(Long.parseLong(String.valueOf(visibleLastIndex+10)))
                    .offset(Long.parseLong(String.valueOf(visibleLastIndex+1
                    ))).
                    orderBy("date", false).query();
            if (aux!=null && !aux.isEmpty()){
                list.addAll(aux);
            }
            adapter.setDataSet(list);
            visibleItemCount +=10;
            visibleLastIndex+=10;
        }catch (Exception e){
            Log.e("ERROR",e.getMessage());
        }

    }




}