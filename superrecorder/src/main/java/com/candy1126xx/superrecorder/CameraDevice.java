package com.candy1126xx.superrecorder;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;

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
            public void openCameraSuccess(Camera camera) {
                mission = new RecorderMission(camera, displaySurface, codecSurface, exceptWidth, exceptHeight, windowWidth, windowHeight);
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
                        cameraManager.openCameraByID(currentID, CameraDevice.this.exceptWidth, CameraDevice.this.exceptHeight);
                        break;
                    case 2:
                        cameraManager.closeCamera();
                        mission.finish();
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
}