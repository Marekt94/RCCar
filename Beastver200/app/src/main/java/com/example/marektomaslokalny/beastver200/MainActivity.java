package com.example.marektomaslokalny.beastver200;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Observer {
    @BindView(R.id.BtnConnect)
    ToggleButton BtnConnect;
    @BindView(R.id.ConstrLayout)
    ConstraintLayout ConstrLayout;
    @BindView(R.id.SbSteering)
    SeekBar SbSteering;
    @BindView(R.id.SbVelocity)
    SeekBar SbVelocity;
    @BindView(R.id.BtnReverse)
    ToggleButton BtnReverse;
    @BindView(R.id.TVBatteryCapacity)
    TextView TVBatteryCapacity;
    @BindView(R.id.BtnSettings)
    Button BtnSettings;
    @BindView(R.id.BtnStop)
    ToggleButton BtnStop;

    private static OwnBluetoothDevice Car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Car = OwnBluetoothDevice.getInstance();
        CommunicationController CommunicationController = Car.getCommunicationController();
        CommunicationController.addObserver(this);

        BtnConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnConnect.setChecked(!BtnConnect.isChecked());
                Intent intent = new Intent(MainActivity.this, ConnectionActivity.class);
                startActivity(intent);
            }
        });

        BtnReverse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SbSteering.setProgress(90);
                SbVelocity.setProgress(0);
            }
        });
        BtnStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SbVelocity.setProgress(0);
                SbSteering.setProgress(90);
                try {
                    Car.getCommunicationController().sendStopMovingFrame();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        BtnSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        try {
            Car.getCommunicationController().sendStartStopTransmittionFrame(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
        System.exit(0);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Car.getCommunicationController().getSocket() != null){
            if (Car.getCommunicationController().getSocket().isConnected()){
                if(Car.getDataReader(this).getStatus() != AsyncTask.Status.RUNNING){
                    Car.getDataReader(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if(Car.getDataSender(this).getStatus() != AsyncTask.Status.RUNNING){
                    Car.getDataSender(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,SbVelocity,SbSteering, BtnReverse, BtnStop);
                }
                BtnConnect.setChecked(true);
            }
            else{
                BtnConnect.setChecked(false);
            }
        }
        else
            BtnConnect.setChecked(false);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof CommunicationController){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TVBatteryCapacity.setText(Double.toString(Car.getBatteryLevelInPercent()) + " %");
                }
            });
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        ViewGroup.LayoutParams params = SbVelocity.getLayoutParams();
        params.width = ConstrLayout.getHeight();
        SbVelocity.setLayoutParams(params);
        SbSteering.setLayoutParams(params);
    }
}