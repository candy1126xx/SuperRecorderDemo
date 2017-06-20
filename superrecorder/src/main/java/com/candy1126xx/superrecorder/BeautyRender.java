package com.candy1126xx.superrecorder;

import android.graphics.Rect;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class BeautyRender {

    // Camera Shader Program
    private GlProgram cameraProgram;
    private GlFrameBuffer cameraFrameBuffer;
    private GlTexture cameraTexture;

    private float[] _TextureTransform;

    private int exceptWidth;
    private int exceptHeight;

    private int windowWidth;
    private int windowHeight;

    private int _InputTextureTarget;
    private int _InputTextureID;

    private RenderOutput _RenderOutput;

    public void realize() {
        cameraTexture = new GlTexture(3553, windowWidth, windowHeight);
        cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());
        cameraProgram = new GlProgram(GlFilter.getStandardVertShader(), GlFilter.getStandardFragShader(this._InputTextureTarget));
    }

    public void unrealize() {
        cameraTexture = null;
        cameraProgram = null;
    }

    public void setInputTexture(int textureTarget, int textureID) {
        this._InputTextureTarget = textureTarget;
        this._InputTextureID = textureID;
    }

    public void setInputTransform(float[] mat4) {
        this._TextureTransform = mat4;
    }

    public void setInputSize(int exceptWidth, int exceptHeight, int windowWidth, int windowHeight) {
        this.exceptWidth = exceptWidth;
        this.exceptHeight = exceptHeight;
        if (windowWidth * windowHeight == 0 || this.windowWidth != windowWidth || this.windowHeight != windowHeight) {
            this.windowWidth = windowWidth;
            this.windowHeight = windowHeight;

            if (cameraTexture != null) cameraTexture.release();
            cameraTexture = new GlTexture(3553, windowWidth, windowHeight);

            if (cameraFrameBuffer != null) cameraFrameBuffer.release();
            cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());
        }

    }

    public void setRenderOutput(RenderOutput output) {
        this._RenderOutput = output;
    }

    public void draw() {
        this._RenderOutput.beginFrame();
        BeautySkinning.drawFirstCameraTexture(
                cameraProgram,
                this._InputTextureTarget,
                this._InputTextureID,
                GlProgram.getVertexArray((float) exceptWidth, (float) exceptHeight),
                GlProgram.mTextureCoord,
                this._TextureTransform,
                GlProgram.getScaleTranslation(new Rect(0, 0, windowWidth, windowHeight)));
        this._RenderOutput.endFrame();
    }
}
