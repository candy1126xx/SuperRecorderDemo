package com.candy1126xx.superrecorderdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.candy1126xx.superrecorder.model.ProjectParameter;
import com.candy1126xx.superrecorder.model.RecordParameter;
import com.candy1126xx.superrecorder.view.SuperRecorderActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecordParameter recordParameter = new RecordParameter.Builder().build();
        ProjectParameter projectParameter = new ProjectParameter.Builder().build();

        SuperRecorderActivity.startRecord(this, 110, recordParameter, projectParameter);
    }
}
