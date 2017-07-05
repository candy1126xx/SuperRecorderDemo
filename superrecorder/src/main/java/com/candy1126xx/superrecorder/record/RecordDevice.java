package com.candy1126xx.superrecorder.record;

import android.app.Application;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

import com.candy1126xx.superrecorder.model.Clip;
import com.candy1126xx.superrecorder.model.ProjectParameter;
import com.candy1126xx.superrecorder.model.RecordParameter;

import java.util.LinkedList;

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

    private Handler mainHandler;

    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private HandlerThread audioThread;
    private Handler audioHandler;
    private HandlerThread projectThread;
    private Handler projectHandler;

    private RecorderMission recorderMission;
    private AudioMission audioMission;

    private AssetManager manager;

    private RecordDeviceCallback callback;

    private boolean cameraReady, audioReady;
    private LinkedList<Clip> clips = new LinkedList<>(); // 主线程专用

    private RecordDevice(Application app) {
        this.manager = app.getAssets();
    }

    public static RecordDevice getInstance(Application app) {
        if (cameraDevice == null) cameraDevice = new RecordDevice(app);
        return cameraDevice;
    }

    public void setCallback(RecordDeviceCallback callback) {
        this.callback = callback;
    }

    // 接收用户发来的视频参数
    public void init(RecordParameter recordParameter, ProjectParameter projectParameter, SurfaceHolder displaySurface) {
        this.exceptWidth = recordParameter.getExceptWidth();
        this.exceptHeight = recordParameter.getExceptHeight();
        this.displaySurface = displaySurface;

        mainHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1: // Camera准备好了
                        cameraReady = true;
                        if (cameraReady && audioReady) callback.onDeviceReady();
                        break;
                    case 2: // Audio准备好了
                        audioReady = true;
                        if (cameraReady && audioReady) callback.onDeviceReady();
                        break;
                    case 3: // 新增进度
                        clips.add((Clip) msg.getData().getSerializable("newClip"));
                        callback.onRecordProgress(clips, 1);
                        break;
                    case 4: // 更新进度
                        Clip last = clips.getLast();
                        last.duration = msg.getData().getLong("duration");
                        last.endTime = last.startTime + last.duration;
                        callback.onRecordProgress(clips ,2);
                        break;
                    case 5: // 删除进度
                        clips.removeLast();
                        callback.onRecordProgress(clips ,3);
                        break;
                }
                return true;
            }
        });

        // 创建Manager
        cameraManager = new CameraManager(recordParameter.getFacing(), exceptWidth, exceptHeight);
        cameraManager.setCallback(this);

        audioManager = new AudioManager();
        audioManager.setCallback(this);

        projectManager = new ProjectManager(projectParameter);
        projectManager.setCallback(this);

        // 创建子线程
        cameraThread = new HandlerThread("Camera" + recordParameter.getFacing());
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
                    case 4:
                        projectManager.deleteCurrentClip();
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

    // 删除当前片段
    public void deleteCurrentClip() {
        projectHandler.obtainMessage(4).sendToTarget();
    }

    //--------------------------------------Camera线程
    // 打开摄像头后创建任务
    @Override
    public void openCameraSuccess(Camera camera) {
        recorderMission = new RecorderMission(manager, camera, displaySurface, mediaCodec, exceptWidth, exceptHeight);
        mainHandler.obtainMessage(1).sendToTarget();
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
        mainHandler.obtainMessage(2).sendToTarget();
        audioMission = new AudioMission(audioManager, audioCodec);
    }

    @Override
    public void onOpenMicFail() {

    }
    //--------------------------------------Mic线程

    //--------------------------------------Project线程
    @Override
    public void onNewClipCreated(Clip newClip) {
        mediaCodec.installMuxer(projectManager.getCurrentMuxer());
        audioCodec.installMuxer(projectManager.getCurrentMuxer());
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putSerializable("newClip", newClip);
        message.setData(bundle);
        message.what = 3;
        mainHandler.dispatchMessage(message);
    }

    @Override
    public void onCurrentClipProgress(long currentClipDuration) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putLong("duration", currentClipDuration);
        message.setData(bundle);
        message.what = 4;
        mainHandler.dispatchMessage(message);
    }

    @Override
    public void onCurrentClipStop() {
        mediaCodec.uninstallMuxer();
        audioCodec.uninstallMuxer();
    }

    @Override
    public void onCurrentClipDelete() {
        mainHandler.obtainMessage(5).sendToTarget();
    }

    @Override
    public void onAllClipsMerged() {
        System.out.println("合并完成");
    }
    //--------------------------------------Project线程

    public interface RecordDeviceCallback {
        void onDeviceReady();
        void onRecordProgress(LinkedList<Clip> clips, int type);
    }
}