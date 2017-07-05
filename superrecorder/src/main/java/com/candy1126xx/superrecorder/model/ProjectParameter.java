package com.candy1126xx.superrecorder.model;

import android.os.Environment;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class ProjectParameter implements Serializable {

    private String outputPath;

    private String title;

    public String getOutputPath() {
        return outputPath;
    }

    public String getTitle() {
        return title;
    }

    private ProjectParameter() {
    }

    public static class Builder {

        private ProjectParameter parameter;

        public Builder() {
            parameter = new ProjectParameter();
            parameter.outputPath = Environment.getExternalStorageDirectory() +
                    File.separator + "SuperRecorder";
            parameter.title = "demo";
        }

        public Builder setOutputPath(String outputPath) {
            parameter.outputPath = outputPath;
            return this;
        }

        public Builder setTitle(String title) {
            parameter.title = title;
            return this;
        }

        public ProjectParameter build() {
            return parameter;
        }
    }
}
