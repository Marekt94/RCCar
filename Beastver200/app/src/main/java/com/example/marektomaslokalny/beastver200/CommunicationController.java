package com.example.marektomaslokalny.beastver200;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Observable;


public class CommunicationController extends Observable {
    static final byte STARTING_FRAME = 35;
    static final byte ENDING_FRAME = 38;

    //COMMANDS
    static final byte STEERING_FRAME = 1;
    static final byte BATTERYSTATE_FRAME = 2;
    static final byte BATTERYINTERVAL_FRAME = 3;
    static final byte STARTSTOPTRANSMITTION_FRAME = 4;

    private OutputStream outputStream = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private InputStream inputstream = null;
    private OwnBluetoothDevice car = OwnBluetoothDevice.getInstance();

    public CommunicationController(){

    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public void setSocket(BluetoothSocket socket) throws IOException {
        this.socket = socket;
        if(this.socket != null){
            inputstream = this.socket.getInputStream();
            outputStream = this.socket.getOutputStream();
        }
        else{
            inputstream = null;
            outputStream = null;
        }
    }

    public java.io.InputStream getInputstream() {
        return inputstream;
    }

    public void setInputstream(java.io.InputStream inputstream) {
        this.inputstream = inputstream;
    }

    public java.io.OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(java.io.OutputStream outputStream) {

        this.outputStream = outputStream;
    }

    public void sendFrame(byte command, byte data1, byte data2, byte data3, byte data4) throws IOException {
        byte[] frame = new byte[8];

        frame[0] = 35;
        frame[1] = command;
        frame[2] = data1;
        frame[3] = data2;
        frame[4] = data3;
        frame[5] = data4;
        frame[6] = calculateCRC(command, data1, data2, data3, data4);
        frame[7] = 38;

        if (socket != null) {
            if (socket.getOutputStream() != null) {
                socket.getOutputStream().write(frame);
            } else {
                Log.d("OUTPUTSTREAM_ERROR", "Output stream is null");
            }
        }
        else{
            Log.d("SOCKET_ERROR","socket is null");
        }
    }

    private byte calculateCRC(byte command, byte data1, byte data2, byte data3, byte data4){
        byte crc;

        crc = 0;

        return crc;
    };

    public void decodeFrame(byte[] frame){
        byte startFrame;
        byte endFrame;
        byte command;
        byte data1;
        byte data2;
        byte data3;
        byte data4;
        byte crc;

        startFrame = frame[0];
        command = frame[1];
        data1 = frame[2];
        data2 = frame[3];
        data3 = frame[4];
        data4 = frame[5];
        crc = frame[6];
        endFrame = frame[7];

        if (!((startFrame == STARTING_FRAME) || (endFrame == ENDING_FRAME))){
            return;
        }

        switch (command) {
            case STEERING_FRAME:
                break;
            case BATTERYINTERVAL_FRAME:
                break;
            case BATTERYSTATE_FRAME:
                if (car == null){
                    car = OwnBluetoothDevice.getInstance();}
                byte[] byteArray = new byte[2];
                byteArray[0] = data1;
                byteArray[1] = data2;
                car.setBatteryLevel(bytesArrayToWord(byteArray));
                setChanged();
                notifyObservers();
                break;
            default:
                break;
        }
    }

    public void sendControllFrame(byte acceleration, byte steering, boolean isReverse, boolean isStop) throws IOException {
        byte reverseState;
        byte stopState;
        if (isReverse) { reverseState = (byte) 1;}
        else
            reverseState = (byte) 0;
        if (isStop){
            stopState = (byte) 1;
        }
        else
            stopState = (byte) 0;
        sendFrame(STEERING_FRAME,acceleration,steering,reverseState,stopState);
    }

    public void sendBatteryIntervalFrame(byte timeInSec) throws IOException {
        sendFrame(BATTERYINTERVAL_FRAME,timeInSec,(byte) 0,(byte) 0,(byte) 0);
    }

    public void sendStartStopTransmittionFrame(boolean stratTransmittion) throws IOException {
        if (stratTransmittion) {
            sendFrame(STARTSTOPTRANSMITTION_FRAME, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
        }
        else{
            sendFrame(STARTSTOPTRANSMITTION_FRAME, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
        }
    }

    public void sendStopMovingFrame() throws IOException {
        sendFrame(STEERING_FRAME,(byte) 0,(byte) 90,(byte) 0,(byte) 1);
    }


    private short bytesArrayToWord(byte []data)
    {
        return ByteBuffer.wrap(data).getShort();
    }
}
