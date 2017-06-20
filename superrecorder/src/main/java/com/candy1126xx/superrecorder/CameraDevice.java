package com.candy1126xx.superrecorder;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class CameraDevice {

    private static CameraDevice cameraDevice;

    private int exceptWidth;
    private int exceptHeight;
    private int windowWidth;
    private int windowHeight;
    private int frameRate;
    private int bitRate;

    private SurfaceHolder displaySurface;
    private Surface codecSurface;

    private CameraManager cameraManager;

    private MediaCodecRecorder mediaCodec;

    private HandlerThread cameraThread;

    private Handler cameraHandler;

    private int currentID;

    private RecorderMission mission;

    private CameraDevice() {
        this.cameraManager = new CameraManager();
        this.cameraManager.setCallback(new CameraManager.CameraManagerCallback() {
            @Override
            public void openCameraSuccess(Camera camera, Camera.CameraInfo info, Camera.Parameters parameters) {
                Camera.Size previewSize = calculatePreviewSize(exceptWidth, exceptHeight);
                if (previewSize == null) {

                } else {
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    camera.setParameters(parameters);
                    camera.setDisplayOrientation(info.orientation);
                    mission = new RecorderMission(camera, displaySurface, codecSurface, exceptWidth, exceptHeight, new Rect(0, 0, windowWidth, windowHeight));
                }
            }

            @Override
            public void openCameraFail() {

            }
        });
    }

    public static CameraDevice getInstance() {
        if (cameraDevice == null) cameraDevice = new CameraDevice();
        return cameraDevice;
    }

    // 接收用户发来的视频参数
    public void init(int facing, int exceptWidth, int exceptHeight, int windowWidth, int windowHeight, int frameRate, int bitRate, SurfaceHolder displaySurface) {
        this.exceptWidth = exceptWidth;
        this.exceptHeight = exceptHeight;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.frameRate = frameRate;
        this.bitRate = bitRate;
        this.displaySurface = displaySurface;

        currentID = cameraManager.findCameraID(facing);
        if (currentID == -1) currentID = 0;
        cameraThread = new HandlerThread("Camera" + currentID);
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        cameraManager.openCameraByID(currentID);
                        break;
                    case 2:
                        cameraManager.closeCamera();
                        break;
                }
                return true;
            }
        });

        mediaCodec = new MediaCodecRecorder();
    }

    // 创建图像录制任务
    public void createMission() {
        cameraHandler.obtainMessage(1).sendToTarget();
    }

    // 是否输出到编码器
    public void writeToFile(boolean b) {

    }

    // 结束任务
    public void finishMission() {
        cameraHandler.obtainMessage(2).sendToTarget();
    }

    private Camera.Size calculatePreviewSize(int exceptWidth, int exceptHeight) {
        int width = exceptWidth;
        int height = exceptHeight;
        Camera.CameraInfo info = cameraManager.getCameraInfo(currentID);
        Camera.Parameters parameters = cameraManager.getCameraParameters();
        switch (info.orientation) {
            case 90:
            case 270:
                width = exceptHeight;
                height = exceptWidth;
            default:
                List<Camera.Size> supported_list = parameters.getSupportedPreviewSizes();
                float aspect_ratio = 0.0F;
                Camera.Size strict_list = parameters.getPreferredPreviewSizeForVideo();
                if (strict_list != null)
                    aspect_ratio = (float) strict_list.width / (float) strict_list.height;

                ArrayList<Camera.Size> var15 = new ArrayList<>();
                ArrayList<Camera.Size> loose_list = new ArrayList<>();

                int var12 = supported_list.size();
                int var13 = 0;

                for (; var13 < var12; ++var13) {
                    Camera.Size s = supported_list.get(var13);
                    if (s.width >= width && s.height >= height) {
                        loose_list.add(s);
                        if (aspect_ratio == 0.0F || (float) s.width / (float) s.height == aspect_ratio) {
                            var15.add(s);
                        }
                    }
                }

                if (var15.isEmpty()) {
                    if (loose_list.isEmpty()) {
                        return null;
                    }

                    var15 = loose_list;
                }

                Collections.sort(var15, new Comparator<Camera.Size>() {
                    @Override
                    public int compare(Camera.Size lhs, Camera.Size rhs) {
                        return lhs.width * lhs.height - rhs.width * rhs.height;
                    }
                });
                return var15.get(0);
        }
    }
}