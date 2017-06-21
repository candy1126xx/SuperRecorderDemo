package com.candy1126xx.superrecorder;

import android.graphics.Rect;
import android.opengl.GLES20;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class BeautyRender {

    // Camera Shader Program
    private GlProgram cameraProgram;

//    private GlFrameBuffer cameraFrameBuffer;
//    private GlTexture cameraTexture;

    //
//    private GlProgram gaussProgram;
//    private GlFrameBuffer gaussFrameBuffer1;
//    private GlFrameBuffer gaussFrameBuffer2;
//    private GlTexture gaussTexture1;
//    private GlTexture gaussTexture2;
//    private GlProgram[] gaussProgramList = new GlProgram[4];

    private float[] _TextureTransform;

    private int exceptWidth;
    private int exceptHeight;

    private int _InputTextureTarget;
    private int _InputTextureID;

    private RenderOutput _RenderOutput;

    private static final int[] mBlurRadius2Level = new int[]{1, 1, 2, 3, 3, 3, 4};

    public void realize() {
        cameraProgram = new GlProgram(GlFilter.getStandardVertShader(), GlFilter.getStandardFragShader(this._InputTextureTarget));

//        cameraTexture = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
//        cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());

//        gaussTexture1 = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
//        gaussFrameBuffer1 = new GlFrameBuffer(gaussTexture1.getID());
//        gaussTexture2 = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
//        gaussFrameBuffer2 = new GlFrameBuffer(gaussTexture2.getID());
//        gaussProgram = null;
//        releaseGaussProgram();
    }

    public void unrealize() {
        cameraProgram = null;

//        cameraTexture = null;

//        gaussTexture1 = null;
//        gaussFrameBuffer1 = null;
//        gaussTexture2 = null;
//        gaussFrameBuffer2 = null;
//        releaseGaussProgram();
//        gaussProgram = null;
    }

    public void setInputTexture(int textureTarget, int textureID) {
        this._InputTextureTarget = textureTarget;
        this._InputTextureID = textureID;
    }

    public void setInputTransform(float[] mat4) {
        this._TextureTransform = mat4;
    }

    public void setInputSize(int exceptWidth, int exceptHeight) {
        this.exceptWidth = exceptWidth;
        this.exceptHeight = exceptHeight;

//        if (cameraTexture != null) cameraTexture.release();
//        cameraTexture = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
//        if (cameraFrameBuffer != null) cameraFrameBuffer.release();
//        cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());
//
//        if(gaussTexture1 != null) gaussTexture1.release();
//        gaussTexture1 = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
//        if(gaussFrameBuffer1 != null) gaussFrameBuffer1.release();
//        gaussFrameBuffer1 = new GlFrameBuffer(gaussTexture1.getID());
//        if(gaussTexture2 != null) gaussTexture2.release();
//        gaussTexture2 = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
//        if(gaussFrameBuffer2 != null) gaussFrameBuffer2.release();
//        gaussFrameBuffer2 = new GlFrameBuffer(gaussTexture2.getID());
    }

    public void setRenderOutput(RenderOutput output) {
        this._RenderOutput = output;
    }

    public void draw() {
//        updateTextureByProgress(80);
        beginFrame(0, exceptWidth, exceptHeight);
        BeautySkinning.drawFirstCameraTexture(
                cameraProgram,
                _InputTextureTarget,
                _InputTextureID,
                GlProgram.getVertexArray((float)exceptWidth, (float)exceptHeight),
                GlProgram.mTextureCoord,
                _TextureTransform,
                GlProgram.getScaleTranslation(new Rect(0, 0, exceptWidth, exceptHeight)));
//        BeautySkinning.drawSecondGaussTexture(
//                gaussProgram,
//                gaussFrameBuffer1.getID(),
//                cameraTexture.getID(),
//                GlProgram.mVertexLocation,
//                GlProgram.mTextureCoord,
//                (float)Math.sqrt(2.0D) / (float)exceptWidth,
//                (float)Math.sqrt(2.0D) / (float)exceptHeight,
//                exceptWidth, exceptHeight);
//        BeautySkinning.drawSecondGaussTexture(
//                gaussProgram,
//                gaussFrameBuffer2.getID(),
//                gaussTexture1.getID(),
//                GlProgram.mVertexLocation,
//                GlProgram.mTextureCoord,
//                (float)Math.sqrt(2.0D) / (float)exceptWidth,
//                (float)Math.sqrt(2.0D) / (float)exceptHeight,
//                exceptWidth, exceptHeight);
//        _RenderOutput.beginFrame();
//        BeautySkinning.drawThirdSmoothLevelTexture(this._SmoothProgram, this._CameraTexture.getID(), this._GaussTexture2.getID(), this._SmoothTexture.getID(), GlProgram.mVertexLocation, GlProgram.mTextureCoord);
//        _RenderOutput.endFrame();
    }

//    private void updateTextureByProgress(int progress) {
//        int pro = 1 + progress / 16;
//        pro = pro > 6 ? mBlurRadius2Level[6]:(pro < 0 ? mBlurRadius2Level[0]:mBlurRadius2Level[pro]);
//        gaussProgram = findGaussProgram(pro);
//    }
//
//    private GlProgram findGaussProgram(int blurRadius) {
//        if(gaussProgramList[blurRadius - 1] == null) {
//            gaussProgramList[blurRadius - 1] = new GlProgram(GlFilter.getVertexShaderForOptimizedBlurOfRadius(blurRadius, 2.0F), GlFilter.getFragmentShaderForOptimizedBlurOfRadius(blurRadius, 2.0F));
//        }
//
//        return gaussProgramList[blurRadius - 1];
//    }

    private void beginFrame(int framebuffer, int width, int height) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, width, height);
    }

//    private void releaseGaussProgram() {
//        for(int i = 0; i < 4; ++i) {
//            if(gaussProgramList[i] != null) {
//                gaussProgramList[i].release();
//                gaussProgramList[i] = null;
//            }
//        }
//    }
}
