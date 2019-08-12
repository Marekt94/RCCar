package com.example.marektomaslokalny.beastver200;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class ConnectionActivity extends AppCompatActivity implements ConnectionInterfaces.View {
    @BindView(R.id.LVBluetoothDevicesList)
    ListView LVBluetoothDevicesList;
    @BindView(R.id.TVConnectedDevice)
    TextView TVConnectedDevice;
    @BindView(R.id.BtnDiscovery)
    Button BtnDiscovery;
    @BindView(R.id.EdtUUID)
    EditText EdtUUID;

    private ConnectionController controller;
    private List<String> devicesNameList;
    private ArrayAdapter<String> devicesAdapter;
    private Drawable selector;
    private AdapterView.OnItemClickListener deviceListItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        ButterKnife.bind(this);

        controller = new ConnectionController(this,getApplicationContext());
        devicesNameList = new ArrayList<String>();
        devicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devicesNameList);

        deviceListItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (controller.getCar().getCommunicationController().getSocket() != null) {
                    if (controller.getCar().getCommunicationController().getSocket().isConnected()) {
                        controller.disconnect();
                    } else {
                        controller.connect(position);
                    }
                }
                else{
                    controller.connect(position);
                }

            }
        };

        LVBluetoothDevicesList.setAdapter(devicesAdapter);
        LVBluetoothDevicesList.setOnItemClickListener(deviceListItemClickListener);

        controller.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void updateView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (controller.getConnectionState()) {
                    case _cs_standby:
                        break;
                    case _cs_connecting_started:
                        updateViewBeforeConnectionDisconnectionStarts();
                        Toast.makeText(getApplicationContext(),"Connecting started",Toast.LENGTH_SHORT).show();
                        break;
                    case _cs_connecting_in_progress:
                        Toast.makeText(getApplicationContext(),"Connecting...",Toast.LENGTH_SHORT).show();
                        break;
                    case _cs_connecting_finished:
                        updateViewAfterConnectionDisconnectionEnds();
                        if (controller.getCar().getCommunicationController().getSocket() != null) {
                            if (controller.getCar().getCommunicationController().getSocket().isConnected()) {
                                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getApplicationContext(), "Connecting failed", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Connecting failed",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case _cs_disconnecting_started:
                        updateViewBeforeConnectionDisconnectionStarts();
                        Toast.makeText(getApplicationContext(),"Disconnecting started",Toast.LENGTH_SHORT).show();
                        break;
                    case _cs_disconnecting_in_progress:
                        break;
                    case _cs_disconnecting_finished:
                        updateViewAfterConnectionDisconnectionEnds();
                        if (controller.getCar().getCommunicationController().getSocket() != null) {
                            if (controller.getCar().getCommunicationController().getSocket().isConnected()) {
                                Toast.makeText(getApplicationContext(), "Disconnecting failed", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Disconnected",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case _cs_searching_started:
                        Toast.makeText(getApplicationContext(),"Searching started",Toast.LENGTH_SHORT).show();
                        break;
                    case _cs_searching_in_progress:
                        break;
                    case _cs_searching_finished:
                        Toast.makeText(getApplicationContext(),"Searching finished",Toast.LENGTH_SHORT).show();
                        break;
                }
                updateDevicesList();
                updateConnectedDeviceTextView();
            }
        });
    }

    public void updateConnectedDeviceTextView() {
        if (controller.getCar().getCommunicationController().getSocket() != null) {
            if (controller.getCar().getCommunicationController().getSocket().isConnected()) {
                TVConnectedDevice.setText(controller.getCar().getCommunicationController().getSocket().getRemoteDevice().getName());
                TVConnectedDevice.setTextColor(Color.GREEN);
            } else {
                TVConnectedDevice.setText("No connected devices");
                TVConnectedDevice.setTextColor(Color.RED);
            }
        }
        else{
            TVConnectedDevice.setText("No connected devices");
            TVConnectedDevice.setTextColor(Color.RED);
        }
    }

    public void updateDevicesList() {
        devicesNameList.clear();
            for (BluetoothDevice device : controller.getDeviceList()) {
                devicesNameList.add(device.getName() + "\n  " + device.getAddress());
            }
        if (devicesNameList.isEmpty()){
            devicesNameList.add("No devices");
        }
        devicesAdapter.notifyDataSetChanged();
    }

    @Override
    public String getUUID() {
        return EdtUUID.getText().toString().toString();
    }

    public void updateViewBeforeConnectionDisconnectionStarts() {
            selector = LVBluetoothDevicesList.getSelector();
            LVBluetoothDevicesList.setOnItemClickListener(null);
            LVBluetoothDevicesList.setSelector(android.R.color.transparent);
    }

    public void updateViewAfterConnectionDisconnectionEnds() {
            LVBluetoothDevicesList.setSelector(selector);
            LVBluetoothDevicesList.setOnItemClickListener(deviceListItemClickListener);
    }

    @OnClick(R.id.BtnDiscovery)
    public void onViewClicked() {
        if (!controller.searchForDevices()){
            controller.stopDiscovery();
        }
    }

    @OnItemClick(R.id.LVBluetoothDevicesList)
    public void onItemClick(int position) {
    }
}
