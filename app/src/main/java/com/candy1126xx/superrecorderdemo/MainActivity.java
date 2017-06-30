package com.candy1126xx.superrecorderdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.candy1126xx.superrecorder.RecordDevice;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private SurfaceView surfaceView;

    private Button btn;

    private RecordDevice device;

    private int exceptWidth = 480;
    private int exceptHeight = 480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        btn = (Button) findViewById(R.id.btn);

        surfaceView.getHolder().setFixedSize(exceptWidth, exceptHeight);
        surfaceView.getHolder().addCallback(this);

        btn.setOnClickListener(this);

        device = RecordDevice.getInstance(getApplication());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        device.init(CAMERA_FACING_BACK, exceptWidth, exceptHeight, holder);
        device.createMission();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        device.finishMission();
    }

    @Override
    public void onClick(View v) {
        if (btn.getText().toString().equals("开始")){
            btn.setText("停止");
//            device.startWriteToFile();
        }else {
            btn.setText("完成");
            btn.setEnabled(false);
//            device.stopWriteToFile();
        }
    }
}
