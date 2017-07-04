package com.candy1126xx.superrecorder;

import android.app.Application;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.SurfaceHolder;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class RecordDevice implements
        CameraManager.CameraManagerCallback,
        AudioManager.AudioManagerCallback,
        ProjectManager.ProjectManagerCallback {

    private static RecordDevice cameraDevice;

    private int exceptWidth;
    private int exceptHeight;

    private SurfaceHolder displaySurface;

    private CameraManager cameraManager;
    private AudioManager audioManager;
    private ProjectManager projectManager;
    private AudioCodecRecorder audioCodec;
    private MediaCodecRecorder mediaCodec;

    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private HandlerThread audioThread;
    private Handler audioHandler;
    private HandlerThread projectThread;
    private Handler projectHandler;

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

        projectManager = new ProjectManager();
        projectManager.setCallback(this);

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

        projectThread = new HandlerThread("Project");
        projectThread.start();
        projectHandler = new Handler(projectThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        projectManager.createNewClip();
                        break;
                    case 2:
                        projectManager.stopCurrentClip();
                        break;
                    case 3:
                        projectManager.mergeAllClips();
                        break;
                }
                return true;
            }
        });

        // 创建编码器
        mediaCodec = new MediaCodecRecorder();
        mediaCodec.init(exceptWidth, exceptHeight);

        audioCodec = new AudioCodecRecorder();
        audioCodec.init();
    }

    // 创建图像录制任务，实际是打开摄像头
    public void createMission() {
        cameraHandler.obtainMessage(1).sendToTarget();
        audioHandler.obtainMessage(1).sendToTarget();
    }

    // 结束任务
    public void finishMission() {
        cameraHandler.obtainMessage(2).sendToTarget();
        audioHandler.obtainMessage(2).sendToTarget();
    }

    // 开始写入文件
    public void startWriteToFile() {
        projectHandler.obtainMessage(1).sendToTarget();
    }

    // 结束写入文件
    public void stopWriteToFile() {
        projectHandler.obtainMessage(2).sendToTarget();
    }

    // 开始合成文件
    public void startMerge() {
        projectHandler.obtainMessage(3).sendToTarget();
    }

    //--------------------------------------Camera线程
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
    //--------------------------------------Camera线程

    //--------------------------------------Mic线程
    @Override
    public void onOpenMicSuccess() {
        audioMission = new AudioMission(audioManager, audioCodec);
    }

    @Override
    public void onOpenMicFail() {

    }
    //--------------------------------------Mic线程

    //--------------------------------------Project线程
    @Override
    public void onNewClipCreated() {
        mediaCodec.installMuxer(projectManager.getCurrentMuxer());
        audioCodec.installMuxer(projectManager.getCurrentMuxer());
    }

    @Override
    public void onCurrentClipStop() {
        mediaCodec.uninstallMuxer();
        audioCodec.uninstallMuxer();
    }

    @Override
    public void onAllClipsMerged() {
        System.out.println("合并完成");
    }
    //--------------------------------------Project线程
}