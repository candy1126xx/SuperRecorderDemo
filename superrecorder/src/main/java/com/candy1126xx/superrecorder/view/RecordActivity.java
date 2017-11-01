package com.candy1126xx.superrecorder.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import com.candy1126xx.superrecorder.R;
import com.candy1126xx.superrecorder.SuperRecorder;
import com.candy1126xx.superrecorder.component.Preview;
import com.candy1126xx.superrecorder.component.RecordProgress;
import com.candy1126xx.superrecorder.model.Clip;
import com.candy1126xx.superrecorder.model.Video;
import com.candy1126xx.superrecorder.record.RecordDevice;

import java.util.LinkedList;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class RecordActivity extends BaseActivity {

    private Preview preview;
    private Button btnRecord, btnDelete, btnComplete;
    private RecordProgress progress;

    private RecordDevice device;

    public static void navigator(Activity fromAct) {
        Intent intent = new Intent(fromAct, RecordActivity.class);
        fromAct.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frg_record);
        preview = (Preview) findViewById(R.id.preview);
        progress = (RecordProgress) findViewById(R.id.progress);
        btnRecord = (Button) findViewById(R.id.btn_record);
        btnDelete = (Button) findViewById(R.id.btn_delete);
        btnComplete = (Button) findViewById(R.id.btn_complete);

        initRecord();
    }

    private void initRecord() {
        device = RecordDevice.getInstance();
        device.setCallback(new RecordDevice.RecordDeviceCallback() {
            @Override
            public void onDeviceReady() {
                bindClick();
            }

            @Override
            public void onRecordProgress(LinkedList<Clip> clips, int type) {
                if (type == 2 || type == 3) progress.setProgress(clips);
            }

            @Override
            public void onStartMerge() {
                showLoadingDialog("正在合并……");
            }

            @Override
            public void onRecordComplete(Video video) {
                device.finishMission();
                hideLoadingDialog();
                FilterActivity.navigator(RecordActivity.this, video);
//                mParent.finish();
//                if (video != null && SuperRecorder.result != null)
//                    SuperRecorder.result.onSuccess(video);
//                SuperRecorder.result = null;
            }
        });

        preview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                device.init(SuperRecorder.recordParameter, SuperRecorder.projectParameter, holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                device.createMission(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                device.finishMission();
            }
        });

        progress.init(SuperRecorder.recordParameter.getMaxDuration());
    }

    private void bindClick() {
        btnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    device.startWriteToFile();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    device.stopWriteToFile();
                }
                return false;
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.deleteCurrentClip();
            }
        });

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.startMerge();
            }
        });
    }
}