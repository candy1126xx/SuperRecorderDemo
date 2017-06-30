package com.candy1126xx.superrecorder;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2017/6/30 0030.
 */

public class ProjectManager {

    private AVMuxer currentMuxer;

    private int index;

    private ProjectManagerCallback callback;

    public void createNewClip() {
        index++;
        currentMuxer = new AVMuxer(new File(Environment.getExternalStorageDirectory() +
                File.separator + "demo" + index + ".mp4"));
        if (callback != null) callback.onNewClipCreated();
    }

    public void stopCurrentClip() {
        currentMuxer.stop();
        if (callback != null) callback.onCurrentClipStop();
    }

    public AVMuxer getCurrentMuxer() {
        return currentMuxer;
    }

    public void setCallback(ProjectManagerCallback callback) {
        this.callback = callback;
    }

    public interface ProjectManagerCallback{
        void onNewClipCreated();
        void onCurrentClipStop();
    }
}
