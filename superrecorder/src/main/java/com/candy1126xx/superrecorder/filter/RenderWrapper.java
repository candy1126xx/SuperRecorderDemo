package com.candy1126xx.superrecorder.filter;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.candy1126xx.superrecorder.component.GLPreview;
import com.candy1126xx.superrecorder.openglwrapper.GlTexture;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 1个GLProgram 包含 1个VS1个FS
 * 1个Render 包含 多个GLProgram
 */

public class RenderWrapper implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private RenderImpl render;

    private GLPreview surfaceView;

    private GlTexture originTexture;

    private SurfaceTexture originSurfaceTexture;

    private boolean newFrameReady = true; // 新的Frame数据是否已经传递到SurfaceTexture。只有这时才更新texture，减轻GPU压力。

    private float[] mStMatrix = new float[16];

    private float[] surfaceCoord = new float[8];

    private Rect scaleRect = new Rect();

    private boolean GLContextCreated;

    public RenderWrapper(GLPreview surfaceView) {
        this.surfaceView = surfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLContextCreated = true;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        scaleRect.set(0, 0, width, height);
        surfaceCoord[1] = height;
        surfaceCoord[2] = width;
        surfaceCoord[3] = height;
        surfaceCoord[6] = width;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            if (newFrameReady && originSurfaceTexture != null) {
                originSurfaceTexture.updateTexImage();
                originSurfaceTexture.getTransformMatrix(mStMatrix);
                newFrameReady = false;
            }
            if (render != null && originTexture != null) {
                render.drawTexture(originTexture, surfaceCoord, scaleRect, mStMatrix);
            }
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        newFrameReady = true;
        surfaceView.requestRender();
    }

    public void init(SurfaceTexture originSurfaceTexture, GlTexture originTexture) {
        this.originTexture = originTexture;
        this.originSurfaceTexture = originSurfaceTexture;
        this.originSurfaceTexture.setOnFrameAvailableListener(this);
    }

    public void setRender(final RenderImpl Impl) {
        surfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                Impl.init();
                render = Impl;
            }
        });
    }

    public boolean isGLContextCreated() {
        return GLContextCreated;
    }
}
