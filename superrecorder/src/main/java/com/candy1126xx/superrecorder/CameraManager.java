package com.candy1126xx.superrecorder;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class CameraManager {

    private int cameraCount;

    /**
     * 包含信息：
     * facing:前置or后置
     * orientation:摄像头与屏幕角度
     * canDisableShutterSound:是否可以关闭拍照声音
     */
    private Camera.CameraInfo[] infos;

    private Camera currentCamera;

    private CameraManagerCallback callback;

    public CameraManager() {
        cameraCount = Camera.getNumberOfCameras();
        infos = new Camera.CameraInfo[cameraCount];
        for (int i = 0; i < cameraCount; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            infos[i] = info;
        }
    }

    // 获取摄像头数量
    public int getCamerasCount() {
        return cameraCount;
    }

    public Camera.CameraInfo getCameraInfo(int ID) {
        return infos[ID];
    }

    public Camera.Parameters getCameraParameters() {
        return currentCamera.getParameters();
    }

    // 根据ID打开相应的摄像头
    public void openCameraByID(int id) {
        try {
            // case：切换摄像头时，要等待另一个摄像头完全关闭
            while(currentCamera != null) {
                wait();
            }
            currentCamera = Camera.open(id);
            if (callback != null) callback.openCameraSuccess(currentCamera, getCameraInfo(id), getCameraParameters());
        }catch (RuntimeException | InterruptedException e) {
            if (callback != null) callback.openCameraFail();
        }
    }

    // 根据方向找到摄像头ID
    public int findCameraID(int facing) {
        int frontID = -1;
        for (int i = 0; i < cameraCount; i++) {
            if (infos[i].facing == facing) {
                frontID = i;
                return frontID;
            }
        }
        return frontID;
    }

    // 关闭摄像头
    public void closeCamera() {
        currentCamera.release();
        currentCamera = null;
        notifyAll();
    }

    public void setCallback(CameraManagerCallback callback) {
        this.callback = callback;
    }

    public interface CameraManagerCallback{
        void openCameraSuccess(Camera camera, Camera.CameraInfo info, Camera.Parameters parameters);
        void openCameraFail();
    }

}
