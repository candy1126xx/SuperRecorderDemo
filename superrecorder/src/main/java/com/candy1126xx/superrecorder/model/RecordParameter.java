package com.candy1126xx.superrecorder.model;

import java.io.Serializable;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class RecordParameter implements Serializable {

    private int exceptWidth;
    private int exceptHeight;
    private int facing;
    private long maxDuration;
    private long minDuration;

    //--------------------------------------------

    public int getExceptWidth() {
        return exceptWidth;
    }

    public int getExceptHeight() {
        return exceptHeight;
    }

    public int getFacing() {
        return facing;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public long getMinDuration() {
        return minDuration;
    }

    private RecordParameter() {}

    //--------------------------------------------

    public static class Builder {

        private RecordParameter parameter;

        public Builder() {
            parameter = new RecordParameter();
            parameter.exceptWidth = 480;
            parameter.exceptHeight = 480;
            parameter.facing = CAMERA_FACING_BACK;
            parameter.maxDuration = 15000000;
            parameter.minDuration = 2000000;
        }

        public Builder setResolution(int resolution) {
            switch (resolution) {
                case RESOLUTION_480P:
                    parameter.exceptWidth = 480;
                    parameter.exceptHeight = 480;
                    break;
            }
            return this;
        }

        public Builder setFacing(int facing) {
            switch (facing) {
                case FACING_BACK:
                    parameter.facing = CAMERA_FACING_BACK;
                    break;
                case FACING_FRONT:
                    parameter.facing = CAMERA_FACING_FRONT;
                    break;
            }
            return this;
        }

        public Builder setMaxDuration(int max) {
            parameter.maxDuration = max * 1000000;
            return this;
        }

        public Builder setMinDuration(int min) {
            parameter.minDuration = min * 1000000;
            return this;
        }

        public RecordParameter build() {
            return parameter;
        }

    }

    //--------------------------------------------

    public static final int RESOLUTION_480P = 1;

    public static final int FACING_BACK = 1;
    public static final int FACING_FRONT = 2;

}
