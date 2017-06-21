package com.candy1126xx.superrecorder;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    private Camera.CameraInfo currentInfo;

    private Camera.Parameters currentParameters;

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

    public Camera.CameraInfo getCameraInfo() {
        return currentInfo;
    }

    public Camera.Parameters getCameraParameters() {
        return currentParameters;
    }

    // 根据ID打开相应的摄像头
    public void openCameraByID(int id, int exceptWidth, int exceptHeight) {
        try {
            // case：切换摄像头时，要等待另一个摄像头完全关闭
            while(currentCamera != null) {
                wait();
            }
            currentCamera = Camera.open(id);
            currentInfo = infos[id];
            currentParameters = currentCamera.getParameters();
            Camera.Size previewSize = calculatePreviewSize(exceptWidth, exceptHeight);
            if (previewSize == null) {

            }else {
                initCamera(previewSize);
                if (callback != null) callback.openCameraSuccess(currentCamera);
            }
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

    private void initCamera(Camera.Size previewSize) {
        currentParameters.setPreviewSize(previewSize.width, previewSize.height);
        currentCamera.setParameters(currentParameters);
        currentCamera.setDisplayOrientation(currentInfo.orientation);
    }

    private Camera.Size calculatePreviewSize(int exceptWidth, int exceptHeight) {
        int width = exceptWidth;
        int height = exceptHeight;
        switch (currentInfo.orientation) {
            case 90:
            case 270:
                width = exceptHeight;
                height = exceptWidth;
            default:
                List<Camera.Size> supported_list = currentParameters.getSupportedPreviewSizes();
                float aspect_ratio = 0.0F;
                Camera.Size strict_list = currentParameters.getPreferredPreviewSizeForVideo();
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

    public void setCallback(CameraManagerCallback callback) {
        this.callback = callback;
    }

    public interface CameraManagerCallback{
        void openCameraSuccess(Camera camera);
        void openCameraFail();
    }

}
