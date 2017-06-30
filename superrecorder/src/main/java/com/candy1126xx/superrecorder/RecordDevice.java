package com.candy1126xx.superrecorder;

import android.app.Application;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class RecordDevice implements
        CameraManager.CameraManagerCallback,
        AudioManager.AudioManagerCallback {

    private static RecordDevice cameraDevice;

    private int exceptWidth;
    private int exceptHeight;

    private SurfaceHolder displaySurface;

    private CameraManager cameraManager;
    private AudioManager audioManager;
    private AudioCodecRecorder audioCodec;
    private MediaCodecRecorder mediaCodec;
    private AVMuxer muxer;

    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private HandlerThread audioThread;
    private Handler audioHandler;

    private RecorderMission recorderMission;
    private AudioMission audioMission;

    private AssetManager manager;

    private RecordDevice(Application app) {
        this.manager = app.getAssets();
    }

    public static RecordDevice getInstance(Application app) {
        if (cameraDevice == null) cameraDevice = new RecordDevice(app);
        return cameraDevice;
    }

    // 接收用户发来的视频参数
    public void init(int facing, int exceptWidth, int exceptHeight, SurfaceHolder displaySurface) {
        this.exceptWidth = exceptWidth;
        this.exceptHeight = exceptHeight;
        this.displaySurface = displaySurface;

        // 创建CameraManager
        cameraManager = new CameraManager(facing, exceptWidth, exceptHeight);
        cameraManager.setCallback(this);

        audioManager = new AudioManager();
        audioManager.setCallback(this);

        // 创建子线程
        cameraThread = new HandlerThread("Camera" + facing);
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        cameraManager.openCamera();
                        break;
                    case 2:
                        cameraManager.closeCamera();
                        recorderMission.finish();
                        mediaCodec.close();
                        break;
                }
                return true;
            }
        });

        audioThread = new HandlerThread("Mic");
        audioThread.start();
        audioHandler = new Handler(audioThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        audioManager.openMic();
                        break;
                    case 2:
                        audioManager.closeMic();
                        audioMission.finish();
                        audioCodec.close();
                        break;
                }
                return true;
            }
        });

        muxer = new AVMuxer();
        muxer.init();

        // 创建编码器
        mediaCodec = new MediaCodecRecorder();
        mediaCodec.init(muxer, exceptWidth, exceptHeight);

        audioCodec = new AudioCodecRecorder();
        audioCodec.init(muxer);
    }

    // 创建图像录制任务，实际是打开摄像头
    public void createMission() {
        cameraHandler.obtainMessage(1).sendToTarget();
        audioHandler.obtainMessage(1).sendToTarget();
    }

    // 打开摄像头后创建任务
    @Override
    public void openCameraSuccess(Camera camera) {
        recorderMission = new RecorderMission(manager, camera, displaySurface, mediaCodec, exceptWidth, exceptHeight);
    }

    @Override
    public void openCameraFail() {

    }

    @Override
    public void cannotFindCamera() {

    }

    // 是否输出到编码器
    public void writeToFile(boolean b) {

    }

    // 结束任务
    public void finishMission() {
        cameraHandler.obtainMessage(2).sendToTarget();
        audioHandler.obtainMessage(2).sendToTarget();
        muxer.stop();
    }

    @Override
    public void onOpenMicSuccess() {
        audioMission = new AudioMission(audioManager, audioCodec, muxer);
    }

    @Override
    public void onOpenMicFail() {

    }
}