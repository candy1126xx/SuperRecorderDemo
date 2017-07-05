package com.candy1126xx.superrecorder.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.FrameLayout;

import com.candy1126xx.superrecorder.R;
import com.candy1126xx.superrecorder.model.ProjectParameter;
import com.candy1126xx.superrecorder.model.RecordParameter;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class SuperRecorderActivity extends FragmentActivity {

    private FrameLayout container;

    private RecordFragment recordFragment;

    private RecordParameter recordParameter;
    private ProjectParameter projectParameter;

    private FragmentManager fm;

    public static final void startRecord(Activity activity, int requestCode,
                                         RecordParameter recordParameter,
                                         ProjectParameter projectParameter) {
        Intent intent = new Intent(activity, SuperRecorderActivity.class);
        intent.putExtra("recordParameter", recordParameter);
        intent.putExtra("projectParameter", projectParameter);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_super_recorder);
        container = (FrameLayout) findViewById(R.id.container);
        fm = getSupportFragmentManager();

        initData();
        loadRecordFrg();
    }

    private void initData() {
        recordParameter = (RecordParameter) getIntent().getSerializableExtra("recordParameter");
        projectParameter = (ProjectParameter) getIntent().getSerializableExtra("projectParameter");
    }

    protected RecordParameter getRecordParameter() {
        return recordParameter;
    }

    public ProjectParameter getProjectParameter() {
        return projectParameter;
    }

    private void loadRecordFrg() {
        recordFragment = RecordFragment.newInstance();
        fm.beginTransaction().add(R.id.container, recordFragment, "record").commitAllowingStateLoss();
    }
}
