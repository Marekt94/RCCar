package com.example.marektomaslokalny.beastver200;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConnectionController implements ConnectionInterfaces.Controller {
    private ConnectionInterfaces.View view;
    private Context context;
    private OwnBluetoothDevice car;
    private BroadcastReceiver broadcastReceiver;
    private Set<BluetoothDevice> deviceSet;
    private IntentFilter filter;
    private ConnectThread deviceConnector;
    private DisconnectThread deviceDisconnector;
    private ConnectionState connectionState;

    public List<BluetoothDevice> getDeviceList() {
        return deviceList;
    }

    private List<BluetoothDevice> deviceList;
    private BluetoothAdapter bluetoothAdapter;

    public OwnBluetoothDevice getCar() {
        return car;
    }

    public ConnectionController(ConnectionInterfaces.View view, Context context){
        this.view = view;
        this.context = context;
        car = OwnBluetoothDevice.getInstance();

        deviceSet = new HashSet<BluetoothDevice>();
        deviceList = new ArrayList<BluetoothDevice>();
        filter = new IntentFilter();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive (Context context, Intent intent){
                onDiscoveryStateReceived(intent);
            }
        };

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(bluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(bluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        this.context.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void init() {
        connectionState = ConnectionState._cs_standby;
        requestPermissions();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        updateDeviceList();
        view.updateView();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        context.unregisterReceiver(broadcastReceiver);
    }

    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= 23){
            ActivityCompat.requestPermissions((Activity) view,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH
                    }, 1);
        }
    }

    public void connect(int index){
        deviceConnector = new ConnectThread();
        deviceConnector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,index);
    }

    public void disconnect(){
        deviceDisconnector = new DisconnectThread();
        deviceDisconnector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void onDiscoveryStateReceived(Intent intent){
        String action = intent.getAction();
        if (bluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            connectionState = ConnectionState._cs_searching_started;
            deviceSet.clear();
            updateDeviceList();
            view.updateView();
        }
        if (bluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            connectionState = ConnectionState._cs_searching_finished;
            view.updateView();
        }
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            connectionState = ConnectionState._cs_searching_in_progress;
            addDevice(device);
            updateDeviceList();
            view.updateView();
            connectionState = ConnectionState._cs_standby;
        }
    }

    private void addDevice(BluetoothDevice device){
        deviceSet.add(device);
    }

    private void updateDeviceList() {
        deviceList.clear();
        deviceSet.addAll(bluetoothAdapter.getBondedDevices());
        deviceList.addAll(deviceSet);
    }

    public boolean searchForDevices(){
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
            return true;
        }
        else{
            return false;
        }
    }

    public void stopDiscovery(){
        bluetoothAdapter.cancelDiscovery();
    };

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    private class ConnectThread extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectionState = ConnectionState._cs_connecting_started;
            view.updateView();
        }

        @Override
        protected Boolean doInBackground(Integer... index) {
            return connectToDevice(index[0]);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            super.onPostExecute(isConnected);
            connectionState = ConnectionState._cs_connecting_finished;
            view.updateView();
            connectionState = ConnectionState._cs_standby;
        }
    }


    private boolean connectToDevice(int listIndex){
        connectionState = ConnectionState._cs_connecting_in_progress;
        view.updateView();
        if (car.getCommunicationController().getSocket() != null) {
            try {
                car.getCommunicationController().getSocket().close();
                car.getCommunicationController().setSocket(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bluetoothAdapter.cancelDiscovery();
        car.getCommunicationController().setDevice(deviceList.get(listIndex));
        try {
            try {
                BluetoothSocket socket = car.getCommunicationController().getDevice().createRfcommSocketToServiceRecord(UUID.fromString(view.getUUID()));
                car.getCommunicationController().setSocket(socket);
            }
            catch(IOException e){
                e.printStackTrace();
            }
            car.getCommunicationController().getSocket().connect();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                car.getCommunicationController().getSocket().close();
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }
        }
        return car.getCommunicationController().getSocket().isConnected();
    }

    private class DisconnectThread extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectionState = ConnectionState._cs_disconnecting_started;
            view.updateView();
        }

        @Override
        protected Boolean doInBackground(Integer... index) {
            return disconnectFromDevice();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            connectionState = ConnectionState._cs_disconnecting_finished;
            view.updateView();
            connectionState = ConnectionState._cs_standby;
        }
    }

    private boolean disconnectFromDevice() {
        try {
            connectionState = ConnectionState._cs_disconnecting_in_progress;
            view.updateView();
            bluetoothAdapter.cancelDiscovery();
            car.getCommunicationController().sendStartStopTransmittionFrame(false);
            car.closeCommunicationThreads();
            car.getCommunicationController().getSocket().close();
            car.getCommunicationController().setSocket(null);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

