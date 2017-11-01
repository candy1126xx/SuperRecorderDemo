package com.candy1126xx.superrecorder.record;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.candy1126xx.superrecorder.openglwrapper.EGLWrapper;
import com.candy1126xx.superrecorder.openglwrapper.GlTexture;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class RecorderMission implements SurfaceTexture.OnFrameAvailableListener {

    private EGLSurface tempEGLSurface;
    private EGLSurface displayEGLSurface;
    private EGLSurface codecEGLSurface;

    private GlTexture originTexture;
    private SurfaceTexture surfaceTexture;

    private EGLWrapper eglWrapper;

    private float[] mStMatrix = new float[16];

    private BeautyRender previewRender;
    private BeautyRender recordRender;

    private boolean isRunning; // 关闭硬件资源全是异步，为了防止报错，加上这个标志

    private MediaCodecRecorder mediaCodec;

    //------------------------------------以下代码在Camera线程

    public RecorderMission(Camera camera, SurfaceHolder displaySurface, MediaCodecRecorder mediaCodec,
                           int cameraWidth, int cameraHeight,
                           int surfaceWidth, int surfaceHeight,
                           int expectWidth, int expectHeight) {
        this.isRunning = true;
        this.mediaCodec = mediaCodec;
        eglWrapper = new EGLWrapper(null, 1);
        tempEGLSurface = eglWrapper.createPbufferSurface(1, 1);
        displayEGLSurface = eglWrapper.createWindowSurface(displaySurface);
        codecEGLSurface = eglWrapper.createWindowSurface(mediaCodec.getInputSurface());
        eglWrapper.makeCurrent(tempEGLSurface);

        originTexture = new GlTexture();
        surfaceTexture = new SurfaceTexture(originTexture.getID());
        surfaceTexture.setOnFrameAvailableListener(this);

        previewRender = new BeautyRender();
        previewRender.setInputTexture(originTexture);
        previewRender.setInputSize(cameraWidth, cameraHeight, surfaceWidth, surfaceHeight);
        previewRender.realize();

        recordRender = new BeautyRender();
        recordRender.setInputTexture(originTexture);
        recordRender.setInputSize(cameraWidth, cameraHeight, expectWidth, expectHeight);
        recordRender.realize();

        try {
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (!isRunning) return;

        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mStMatrix);
        previewRender.setInputTransform(mStMatrix);
        recordRender.setInputTransform(mStMatrix);

        eglWrapper.makeCurrent(displayEGLSurface);
        previewRender.draw();
        eglWrapper.swapBuffers(displayEGLSurface);

        if (codecEGLSurface != null) {
            eglWrapper.makeCurrent(codecEGLSurface);
            recordRender.draw();
            eglWrapper.swapBuffers(codecEGLSurface);
            mediaCodec.onSurfaceRender();
        }
    }

    public void finish() {
        isRunning = false;

        surfaceTexture.setOnFrameAvailableListener(null);
        surfaceTexture.release();
        surfaceTexture = null;

        previewRender.unrealize();
        previewRender = null;
        recordRender.unrealize();
        recordRender = null;

        eglWrapper.releaseSurface(tempEGLSurface);
        eglWrapper.releaseSurface(displayEGLSurface);
        eglWrapper.releaseSurface(codecEGLSurface);
        eglWrapper.release();
        eglWrapper = null;
    }
}
