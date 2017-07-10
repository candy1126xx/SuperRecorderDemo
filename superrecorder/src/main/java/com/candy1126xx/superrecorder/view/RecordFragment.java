package com.candy1126xx.superrecorder.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.candy1126xx.superrecorder.R;
import com.candy1126xx.superrecorder.SuperRecorder;
import com.candy1126xx.superrecorder.component.Preview;
import com.candy1126xx.superrecorder.component.RecordProgress;
import com.candy1126xx.superrecorder.model.Clip;
import com.candy1126xx.superrecorder.model.ProjectParameter;
import com.candy1126xx.superrecorder.model.RecordParameter;
import com.candy1126xx.superrecorder.model.Video;
import com.candy1126xx.superrecorder.record.RecordDevice;

import java.util.LinkedList;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class RecordFragment extends BaseFragment {

    private Preview preview;
    private Button btnRecord, btnDelete, btnComplete;
    private RecordProgress progress;

    private RecordDevice device;

    private RecordParameter recordParameter;
    private ProjectParameter projectParameter;

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frg_record, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preview = (Preview) view.findViewById(R.id.preview);
        progress = (RecordProgress) view.findViewById(R.id.progress);
        btnRecord = (Button) view.findViewById(R.id.btn_record);
        btnDelete = (Button) view.findViewById(R.id.btn_delete);
        btnComplete = (Button) view.findViewById(R.id.btn_complete);

        initParameter();
        initRecord();
    }

    private void initParameter() {
        recordParameter = mParent.getRecordParameter();
        projectParameter = mParent.getProjectParameter();
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
                new AlertDialog.Builder(mParent).setTitle("正在合并……").create().show();
            }

            @Override
            public void onRecordComplete(Video video) {
                mParent.finish();
                if (video != null && SuperRecorder.result != null)
                    SuperRecorder.result.onSuccess(video);
                SuperRecorder.result = null;
            }
        });

        preview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                device.init(recordParameter, projectParameter, holder);
                device.createMission();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                holder.setFixedSize(recordParameter.getExceptWidth(), recordParameter.getExceptHeight());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                device.finishMission();
            }
        });

        progress.init(recordParameter.getMaxDuration());
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