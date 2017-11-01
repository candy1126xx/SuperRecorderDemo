package com.candy1126xx.superrecorder;

import android.app.Activity;

import com.candy1126xx.superrecorder.model.ProjectParameter;
import com.candy1126xx.superrecorder.model.RecordParameter;
import com.candy1126xx.superrecorder.model.Video;
import com.candy1126xx.superrecorder.view.FilterActivity;
import com.candy1126xx.superrecorder.view.RecordActivity;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class SuperRecorder {

    public static OnRecordResult result;

    public static RecordParameter recordParameter;

    public static ProjectParameter projectParameter;

    public static void startRecord(Activity activity,
                                   RecordParameter recordParameter,
                                   ProjectParameter projectParameter,
                                   OnRecordResult result) {
        SuperRecorder.result = result;
        SuperRecorder.recordParameter = recordParameter;
        SuperRecorder.projectParameter = projectParameter;
        RecordActivity.navigator(activity);
    }

}
