package com.candy1126xx.superrecorder.record;

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

    private Camera.CameraInfo[] infos;

    private int currentID;

    private Camera currentCamera;

    private Camera.CameraInfo currentInfo;

    private Camera.Parameters currentParameters;

    private CameraManagerCallback callback;

    private int exceptWidth, exceptHeight;

    private Camera.Size previewSize;

    public CameraManager(int facing, int exceptWidth, int exceptHeight) {
        cameraCount = Camera.getNumberOfCameras();
        infos = new Camera.CameraInfo[cameraCount];
        for (int i = 0; i < cameraCount; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            infos[i] = info;
        }
        this.exceptWidth = exceptWidth;
        this.exceptHeight = exceptHeight;
        this.currentID = findCameraID(facing);
    }

    // 打开摄像头
    public synchronized void openCamera() {
        if (currentID < 0) {
            if (callback != null) callback.cannotFindCamera();
            return;
        }

        try {
            // case：切换摄像头时，要等待另一个摄像头完全关闭
            while(currentCamera != null) {
                wait();
            }
            currentCamera = Camera.open(currentID);
            currentInfo = infos[currentID];
            currentParameters = currentCamera.getParameters();
            previewSize = calculatePreviewSize();
            if (previewSize == null) {

            }else {
                initCamera();
                if (callback != null) callback.openCameraSuccess(currentCamera);
            }
        }catch (RuntimeException | InterruptedException e) {
            if (callback != null) callback.openCameraFail();
        }
    }

    // 根据方向找到摄像头ID
    private int findCameraID(int facing) {
        int id;
        for (int i = 0; i < cameraCount; i++) {
            if (infos[i].facing == facing) {
                id = i;
                return id;
            }
        }
        return cameraCount > 0 ? 0 : -1;
    }

    // 关闭摄像头
    public synchronized void closeCamera() {
        currentCamera.release();
        currentCamera = null;
        notifyAll();
    }

    private void initCamera() {
        currentParameters.setPreviewSize(previewSize.width, previewSize.height);
        currentCamera.setParameters(currentParameters);
        currentCamera.setDisplayOrientation(currentInfo.orientation);
    }

    private Camera.Size calculatePreviewSize() {
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
        void cannotFindCamera();
    }

}
