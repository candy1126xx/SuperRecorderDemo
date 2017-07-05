package com.candy1126xx.superrecorderdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import com.candy1126xx.superrecorder.OnRecordResult;
import com.candy1126xx.superrecorder.SuperRecorder;
import com.candy1126xx.superrecorder.model.ProjectParameter;
import com.candy1126xx.superrecorder.model.RecordParameter;
import com.candy1126xx.superrecorder.model.Video;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = (VideoView) findViewById(R.id.video_view);

        RecordParameter recordParameter = new RecordParameter.Builder().build();
        ProjectParameter projectParameter = new ProjectParameter.Builder().build();

        SuperRecorder.startRecord(this, recordParameter, projectParameter, new OnRecordResult() {
            @Override
            public void onSuccess(Video video) {
                videoView.setVideoPath(video.outputPath);
                videoView.start();
            }
        });
    }
}
