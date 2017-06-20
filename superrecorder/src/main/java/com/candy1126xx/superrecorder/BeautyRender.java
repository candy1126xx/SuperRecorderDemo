package com.candy1126xx.superrecorder;

import android.graphics.Rect;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class BeautyRender {

    private GlProgram _CameraProgram;
    private GlFrameBuffer _CameraFrameBuffer;
    private GlTexture _CameraTexture;
    private float[] _TextureTransform;
    private int _PreviewWidth = 640;
    private int _PreviewHeight = 360;
    private Rect _Crop = new Rect(0, 0, 640, 360);
    private int _InputTextureTarget;
    private int _InputTextureID;
    RenderOutput _RenderOutput;

    public void realize() {
        this._CameraTexture = new GlTexture(3553, this._Crop.width(), this._Crop.height());
        this._CameraFrameBuffer = new GlFrameBuffer(this._CameraTexture.getID());
        this._CameraProgram = new GlProgram(GlFilter.getStandardVertShader(), GlFilter.getStandardFragShader(this._InputTextureTarget));
    }

    public void unrealize() {
        this._CameraTexture = null;
        this._CameraProgram = null;
    }

    public void setInputTexture(int textureTarget, int textureID) {
        this._InputTextureTarget = textureTarget;
        this._InputTextureID = textureID;
    }

    public void setInputTransform(float[] mat4) {
        this._TextureTransform = mat4;
    }

    public void setInputSize(int width, int height, Rect crop) {
        this._PreviewWidth = width;
        this._PreviewHeight = height;
        if (this._Crop == null || this._Crop.width() != crop.width() || this._Crop.height() != crop.height()) {
            this._Crop = crop;
            if (this._CameraTexture != null) {
                this._CameraTexture.release();
            }

            this._CameraTexture = new GlTexture(3553, this._Crop.width(), this._Crop.height());
            if (this._CameraFrameBuffer != null) this._CameraFrameBuffer.release();
            this._CameraFrameBuffer = new GlFrameBuffer(this._CameraTexture.getID());
        }

    }

    public void setRenderOutput(RenderOutput output) {
        this._RenderOutput = output;
    }

    public void draw() {
        this._RenderOutput.beginFrame();
        BeautySkinning.drawFirstCameraTexture(this._CameraProgram, this._InputTextureTarget, this._InputTextureID, GlProgram.getVertexArray((float) this._PreviewWidth, (float) this._PreviewHeight), GlProgram.mTextureCoord, this._TextureTransform, GlProgram.getScaleTranslation(this._Crop));
        this._RenderOutput.endFrame();
    }
}
