package com.candy1126xx.superrecorder;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class RecorderMission implements SurfaceTexture.OnFrameAvailableListener, RenderOutput {

    private EGLSurface tempEGLSurface;
    private EGLSurface displayEGLSurface;
    private EGLSurface codecEGLSurface;

    private int mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    private int mTextureID;
    private SurfaceTexture surfaceTexture;

    private EGLWrapper eglWrapper;

    private float[] mStMatrix = new float[16];

    private BeautyRender render;

    private Rect viewPort = new Rect();

    private int exceptWidth;
    private int exceptHeight;

    public RecorderMission(Camera camera, SurfaceHolder displaySurface, Surface codecSurface, int exceptWidth, int exceptHeight) {
        this.exceptWidth = exceptWidth;
        this.exceptHeight = exceptHeight;
        eglWrapper = new EGLWrapper(null, 1);
        tempEGLSurface = eglWrapper.createPbufferSurface(1, 1);
        displayEGLSurface = eglWrapper.createWindowSurface(displaySurface);
//        codecEGLSurface = eglWrapper.createWindowSurface(codecSurface);
        eglWrapper.makeCurrent(tempEGLSurface);
        mTextureID = OpenGLUtils.createTextureObject(mTextureTarget);

        surfaceTexture = new SurfaceTexture(mTextureID);
        surfaceTexture.setOnFrameAvailableListener(this);

        render = new BeautyRender();
        render.setRenderOutput(this);
        render.setInputTexture(mTextureTarget, mTextureID);
        render.setInputSize(exceptWidth, exceptHeight);
        render.realize();

        try {
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mStMatrix);

        eglWrapper.makeCurrent(displayEGLSurface);

        render.setInputTransform(mStMatrix);
        render.draw();

        eglWrapper.swapBuffers(displayEGLSurface);
    }

    @Override
    public void beginFrame() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        calculateViewPort(exceptWidth, exceptHeight, viewPort);
        GLES20.glViewport(viewPort.left, viewPort.top, viewPort.width(), viewPort.height());
    }

    @Override
    public void endFrame() {

    }

    public void finish() {
        eglWrapper.releaseSurface(tempEGLSurface);
        eglWrapper.releaseSurface(displayEGLSurface);
        eglWrapper.releaseSurface(codecEGLSurface);
        eglWrapper.release();
        eglWrapper = null;

        render.unrealize();
        render = null;

        surfaceTexture.release();
        surfaceTexture = null;
    }

    private void calculateViewPort(int sw, int sh, Rect rect) {
        if (rect == null) rect = new Rect();
        rect.set(0, 0, sw, sh);
    }
}
