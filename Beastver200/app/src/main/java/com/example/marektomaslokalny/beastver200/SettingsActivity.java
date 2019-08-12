package com.example.marektomaslokalny.beastver200;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {
    @BindView(R.id.BtnBatterySendInterval)
    Button BtnBatterySendInterval;
    @BindView(R.id.BtnTransmittDataInterval)
    Button BtnTransmittDataInterval;
    @BindView(R.id.TVBatterySendInterval)
    TextView TVBatterySendInterval;
    @BindView(R.id.TVTransmittDataInterval)
    TextView TVTransmittDataInterval;
    @BindView(R.id.SbBatterySendInterval)
    SeekBar SbBatterySendInterval;
    @BindView(R.id.SbTransmittDataInterval)
    SeekBar SbTransmittDataInterval;


    private OwnBluetoothDevice Car = OwnBluetoothDevice.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        updateView();

        BtnBatterySendInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Car.setBatterySendIntervalInSec((int) SbBatterySendInterval.getProgress());
                    Car.getCommunicationController().sendBatteryIntervalFrame((byte) Car.getBatterySendIntervalInSec());
                    updateView();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        BtnTransmittDataInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = convertValueFromSeekbarToTransmittionInterval(SbTransmittDataInterval);
                Car.setTransmittionDataInterval(value);
                updateView();
            }
        });

        SbBatterySendInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TVBatterySendInterval.setText("");
                TVBatterySendInterval.setText("Battery send interval: " + Integer.toString(SbBatterySendInterval.getProgress()) + " s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                TVBatterySendInterval.setText("");
                TVBatterySendInterval.setText("Battery send interval: " + Integer.toString(SbBatterySendInterval.getProgress()) + " s");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SbTransmittDataInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = convertValueFromSeekbarToTransmittionInterval(SbTransmittDataInterval);
                TVTransmittDataInterval.setText("");
                TVTransmittDataInterval.setText("Transmission send interval: " + (value) + " ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                int value = convertValueFromSeekbarToTransmittionInterval(SbTransmittDataInterval);
                TVTransmittDataInterval.setText("");
                TVTransmittDataInterval.setText("Transmission send interval: " + (value) + " ms");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateView();
    }

    private int convertValueFromSeekbarToTransmittionInterval(SeekBar seekBar){
        int value = (seekBar.getProgress() + 1)*50;
        return value;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private int convertFromTransmittionItervalToSeekbarProgress(int value){
        return ((value/50) - 1);
    }

    private String setBatterySendIntervalText(int value){
        String text = "";
            if (value == 0){
                text = "Battery state send continuously";
            }
            else{
                text = "Battery send interval: " + Integer.toString(SbBatterySendInterval.getProgress()) + " s";
            }
        return text;
    }

    private void updateView(){
        if (Car != null) {
            TVTransmittDataInterval.setText("");
            TVBatterySendInterval.setText("");
            SbTransmittDataInterval.setProgress(convertFromTransmittionItervalToSeekbarProgress(Car.getTransmittionDataInterval()));
            SbBatterySendInterval.setProgress((Car.getBatterySendIntervalInSec()));
            TVTransmittDataInterval.setText("Transmission send interval: " + Car.getTransmittionDataInterval() + " ms");
            TVBatterySendInterval.setText("Battery send interval: " + (Car.getBatterySendIntervalInSec()) + " s");
        }
    }
}
