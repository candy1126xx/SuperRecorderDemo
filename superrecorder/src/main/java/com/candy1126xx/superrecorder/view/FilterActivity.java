package com.candy1126xx.superrecorder.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.candy1126xx.superrecorder.R;
import com.candy1126xx.superrecorder.component.GLPreview;
import com.candy1126xx.superrecorder.filter.FilterPreviewMission;
import com.candy1126xx.superrecorder.model.Video;

import java.io.File;

import static android.os.Environment.DIRECTORY_DCIM;

/**
 * Created by Administrator on 2017/11/1 0001.
 */

public class FilterActivity extends BaseActivity {

    private GLPreview preview;

    private Button btnDefault, btnGrayRender;

    private FilterPreviewMission mission;

    private Video originVideo, outputVideo;

    public static void navigator(Activity fromAct, Video video) {
        Intent intent = new Intent(fromAct, FilterActivity.class);
        intent.putExtra("originVideo", video);
        fromAct.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        originVideo = (Video) getIntent().getSerializableExtra("originVideo");
        setContentView(R.layout.frg_filter);
        preview = (GLPreview) findViewById(R.id.preview);
        btnDefault = (Button) findViewById(R.id.btn_default);
        btnGrayRender = (Button) findViewById(R.id.btn_gray_render);

        btnDefault.setOnClickListener(new ButtonHandler(0));
        btnGrayRender.setOnClickListener(new ButtonHandler(1));

        mission = new FilterPreviewMission(this, originVideo.outputPath, preview.getWrapper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        preview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.onPause();
    }

    private class ButtonHandler implements View.OnClickListener {

        private int index;

        ButtonHandler(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            mission.start(index);
        }
    }
}
