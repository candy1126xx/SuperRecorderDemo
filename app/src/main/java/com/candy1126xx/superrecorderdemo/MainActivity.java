package com.candy1126xx.superrecorderdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.candy1126xx.superrecorder.CameraDevice;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;

    private CameraDevice device;

    private int exceptWidth = 480;
    private int exceptHeight = 480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().setFixedSize(exceptWidth, exceptHeight);
        surfaceView.getHolder().addCallback(this);

        device = CameraDevice.getInstance(getApplication());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        device.init(CAMERA_FACING_BACK, exceptWidth, exceptHeight, 3, 1500000, holder);
        device.createMission();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        device.finishMission();
    }
}
