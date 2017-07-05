package com.candy1126xx.superrecorder;

import android.app.Activity;
import android.content.Intent;

import com.candy1126xx.superrecorder.model.ProjectParameter;
import com.candy1126xx.superrecorder.model.RecordParameter;
import com.candy1126xx.superrecorder.view.SuperRecorderActivity;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class SuperRecorder {

    public static OnRecordResult result;

    public static void startRecord(Activity activity,
                                   RecordParameter recordParameter,
                                   ProjectParameter projectParameter,
                                   OnRecordResult result) {
        SuperRecorder.result = result;
        Intent intent = new Intent(activity, SuperRecorderActivity.class);
        intent.putExtra("recordParameter", recordParameter);
        intent.putExtra("projectParameter", projectParameter);
        activity.startActivity(intent);
    }

}
