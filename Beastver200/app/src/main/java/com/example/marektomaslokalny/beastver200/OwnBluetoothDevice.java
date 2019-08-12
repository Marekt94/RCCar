package com.example.marektomaslokalny.beastver200;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;


class OwnBluetoothDevice {
    private int batterySendIntervalInSec = 2;
    private double batteryLevelInVolt = 0.0;
    private int batteryLevelInPercent = 0;
    private static int transmittDataIntervalInMillis = 100;
    static final int RESISTANCE1 = 2120;
    static final int RESISTANCE2 = 4550;
    private static CommunicationController communicationController = null;
    private DataSenderThread dataSender;
    private DataReaderThread dataReader;

    private static final OwnBluetoothDevice ourInstance = new OwnBluetoothDevice();

    public DataSenderThread getDataSender(Context context){
        if (dataSender == null){
            dataSender = new DataSenderThread(context);
        }
        return dataSender;
    }

    public DataReaderThread getDataReader(Context context){
        if (dataReader == null){
            dataReader = new DataReaderThread(context);
        }
        return dataReader;
    }

    public int getBatterySendIntervalInSec() {
        return batterySendIntervalInSec;
    }

    public void setBatterySendIntervalInSec(int batterySendIntervalInSec) {
        this.batterySendIntervalInSec = batterySendIntervalInSec;
    }

    public int getBatteryLevelInPercent() {
        return batteryLevelInPercent;
    }

    public void setBatteryLevelInPercent(int batteryLevelInPercent) {

        this.batteryLevelInPercent = batteryLevelInPercent;
    }

    static OwnBluetoothDevice getInstance(){
        return ourInstance;
    }

    private OwnBluetoothDevice() {
        communicationController = new CommunicationController();
    }

    public static void setTransmittionDataInterval(int sleepTimeInMillis) {
        OwnBluetoothDevice.transmittDataIntervalInMillis = sleepTimeInMillis;
    }

    public static int getTransmittionDataInterval(){
        return OwnBluetoothDevice.transmittDataIntervalInMillis;
    }

    public CommunicationController getCommunicationController() {
        return communicationController;
    }

    public void setCommunicationController(CommunicationController dataController) {
        communicationController = dataController;
    }

    public double getBatteryLevelInVolt() {
        return batteryLevelInVolt;
    }

    public void setBatteryLevelInVolt(double batteryLevelInVolt) {
        this.batteryLevelInVolt = batteryLevelInVolt;
    }

    private int convertVoltToPercentage(double batteryStateInVolt){
        return (int) ((batteryStateInVolt - 3.6*3)/1.8 * 100);
    }

    public void setBatteryLevel(short batteryLevel){
        batteryLevelInVolt = evaluateBatteryInVolt(batteryLevel);
        batteryLevelInPercent = convertVoltToPercentage(batteryLevelInVolt);
    }

    private double evaluateBatteryInVolt(short voltageDividerRead){
        double outputVoltage = 0;
        double powerSupplyVoltage = 0;
        double analogNumberOfValues = 1024.0;
        double analogMaxVoltage = 5.0;

        outputVoltage = (float)(analogMaxVoltage * voltageDividerRead / analogNumberOfValues);
        powerSupplyVoltage = (RESISTANCE1 + RESISTANCE2) * outputVoltage / RESISTANCE1 * 10;
        powerSupplyVoltage = Math.floor(powerSupplyVoltage) / 10;
        return powerSupplyVoltage;
    }

    public boolean closeCommunicationThreads(){
        if (dataReader != null) {
            dataReader.cancel(true);
            while (dataReader.getStatus() == AsyncTask.Status.RUNNING) {}
            dataReader = null;
        }
        if (dataSender != null) {
            dataSender.cancel(true);
            while (dataSender.getStatus() == AsyncTask.Status.RUNNING) {}
            dataSender = null;
        }
        return true;
    }

    public class DataSenderThread extends AsyncTask<View,Void,Void> {
        private SeekBar SbVelocity;
        private SeekBar SbSteering;
        private ToggleButton BtnReverse;
        private ToggleButton BtnStop;
        private Context context;

        public DataSenderThread(Context context){
            this.context = context;
        }

        @Override
        protected Void doInBackground(View... views) {
            SbVelocity = (SeekBar) views[0];
            SbSteering = (SeekBar) views[1];
            BtnReverse = (ToggleButton) views[2];
            BtnStop = (ToggleButton) views[3];
            try {
                communicationController.sendStartStopTransmittionFrame(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!isCancelled()){
                try {
                    communicationController.sendControllFrame((byte)SbVelocity.getProgress(),(byte)SbSteering.getProgress(),BtnReverse.isChecked(),BtnStop.isChecked());
                    Thread.sleep(transmittDataIntervalInMillis);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public class DataReaderThread extends AsyncTask<Void, Void, Void> {
        byte[] frame;
        InputStream inputStream;
        Context context;

        public DataReaderThread(Context context){
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            frame = new byte[8];
            try {
                inputStream = communicationController.getSocket().getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!isCancelled()){
                if (communicationController.getSocket() != null) {
                    if (inputStream != null) {
                        try {
                            if (inputStream.available() > 8) {
                                inputStream.read(frame);
                                communicationController.decodeFrame(frame);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }
    }
}