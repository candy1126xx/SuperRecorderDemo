package com.candy1126xx.superrecorder.record;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLES20;

import com.candy1126xx.superrecorder.openglwrapper.GlFilter;
import com.candy1126xx.superrecorder.openglwrapper.GlFrameBuffer;
import com.candy1126xx.superrecorder.openglwrapper.GlProgram;
import com.candy1126xx.superrecorder.openglwrapper.GlTexture;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class BeautyRender {

    // Camera Shader Program
    private GlProgram cameraProgram;

    private GlProgram gaussProgram;
    private GlProgram[] gaussProgramList = new GlProgram[4];

    private GlProgram smoothProgram;

    private GlTexture cameraTexture;
    private GlFrameBuffer cameraFrameBuffer;

    private GlTexture gaussTexture1;
    private GlFrameBuffer gaussFrameBuffer1;

    private GlTexture gaussTexture2;
    private GlFrameBuffer gaussFrameBuffer2;

    private GlTexture smoothTexture;
    private GlTexture[] smoothList = new GlTexture[7];

    private float[] _TextureTransform;

    private int exceptWidth;
    private int exceptHeight;

    private int _InputTextureTarget;
    private int _InputTextureID;

    private AssetManager assetManager;

    private RenderOutput _RenderOutput;

    private static final int[] mBlurRadius2Level = new int[]{1, 1, 2, 3, 3, 3, 4};

    public BeautyRender(AssetManager manager) {
        assetManager = manager;
    }

    public void realize() {
        cameraProgram = new GlProgram(GlFilter.getStandardVertShader(), GlFilter.getStandardFragShader(this._InputTextureTarget));

        gaussProgram = null;
        releaseGaussProgram();

        smoothProgram = new GlProgram(GlFilter.getBeautySkinningVertShader(), GlFilter.getBeautySkinningFragShader());

        cameraTexture = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());

        gaussTexture1 = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        gaussFrameBuffer1 = new GlFrameBuffer(gaussTexture1.getID());

        gaussTexture2 = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        gaussFrameBuffer2 = new GlFrameBuffer(gaussTexture2.getID());

        smoothTexture = null;
        releaseSmoothTexture();
    }

    public void unrealize() {
        cameraProgram = null;

        releaseGaussProgram();
        gaussProgram = null;

        smoothProgram = null;

        cameraTexture = null;

        gaussTexture1 = null;
        gaussFrameBuffer1 = null;

        gaussTexture2 = null;
        gaussFrameBuffer2 = null;

        releaseSmoothTexture();
        smoothTexture = null;
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

        if (cameraTexture != null) cameraTexture.release();
        cameraTexture = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        if (cameraFrameBuffer != null) cameraFrameBuffer.release();
        cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());

        if(gaussTexture1 != null) gaussTexture1.release();
        gaussTexture1 = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        if(gaussFrameBuffer1 != null) gaussFrameBuffer1.release();
        gaussFrameBuffer1 = new GlFrameBuffer(gaussTexture1.getID());
        if(gaussTexture2 != null) gaussTexture2.release();
        gaussTexture2 = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        if(gaussFrameBuffer2 != null) gaussFrameBuffer2.release();
        gaussFrameBuffer2 = new GlFrameBuffer(gaussTexture2.getID());
    }

    public void setRenderOutput(RenderOutput output) {
        this._RenderOutput = output;
    }

    public void draw() {
        updateTextureByProgress(80);
        beginFrame(cameraFrameBuffer.getID(), exceptWidth, exceptHeight);
        BeautySkinning.drawFirstCameraTexture(
                cameraProgram,
                _InputTextureTarget,
                _InputTextureID,
                GlProgram.getVertexArray((float)exceptWidth, (float)exceptHeight),
                GlProgram.mTextureCoord,
                _TextureTransform,
                GlProgram.getScaleTranslation(new Rect(0, 0, exceptWidth, exceptHeight)));
        BeautySkinning.drawSecondGaussTexture(
                gaussProgram,
                gaussFrameBuffer1.getID(),
                cameraTexture.getID(),
                GlProgram.mVertexLocation,
                GlProgram.mTextureCoord,
                (float)Math.sqrt(2.0D) / (float)exceptWidth,
                (float)Math.sqrt(2.0D) / (float)exceptHeight,
                exceptWidth, exceptHeight);
        BeautySkinning.drawSecondGaussTexture(
                gaussProgram,
                gaussFrameBuffer2.getID(),
                gaussTexture1.getID(),
                GlProgram.mVertexLocation,
                GlProgram.mTextureCoord,
                (float)Math.sqrt(2.0D) / (float)exceptWidth,
                (float)Math.sqrt(2.0D) / (float)exceptHeight,
                exceptWidth, exceptHeight);
        _RenderOutput.beginFrame();
        BeautySkinning.drawThirdSmoothLevelTexture(
                smoothProgram,
                cameraTexture.getID(),
                gaussTexture2.getID(),
                smoothTexture.getID(),
                GlProgram.mVertexLocation,
                GlProgram.mTextureCoord);
        _RenderOutput.endFrame();
    }

    private void updateTextureByProgress(int progress) {
        int pro = progress / 16 > 6 ? 6 : progress / 16;
        pro = pro > 6 ? mBlurRadius2Level[6]:(pro < 0 ? mBlurRadius2Level[0]:mBlurRadius2Level[pro]);
        gaussProgram = findGaussProgram(pro);

        pro = 1 + progress / 16;
        pro = pro < 1?1:(pro > 7?7:pro);
        smoothTexture = findSmoothTexture(pro);
    }

    private GlTexture findSmoothTexture(int level) {
        if(smoothList[level - 1] == null) {
            String path = String.format(Locale.SIMPLIFIED_CHINESE, "SkinBeautifier/beauty_%d.png", level);

            try {
                InputStream e = assetManager.open(path);
                Bitmap bitmap = BitmapFactory.decodeStream(e);
                smoothList[level - 1] = new GlTexture(bitmap);
                e.close();
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

        return smoothList[level - 1];
    }

    private GlProgram findGaussProgram(int blurRadius) {
        if(gaussProgramList[blurRadius - 1] == null) {
            gaussProgramList[blurRadius - 1] = new GlProgram(GlFilter.getVertexShaderForOptimizedBlurOfRadius(blurRadius, 2.0F), GlFilter.getFragmentShaderForOptimizedBlurOfRadius(blurRadius, 2.0F));
        }

        return gaussProgramList[blurRadius - 1];
    }

    private void beginFrame(int framebuffer, int width, int height) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, width, height);
    }

    private void releaseGaussProgram() {
        for(int i = 0; i < 4; ++i) {
            if(gaussProgramList[i] != null) {
                gaussProgramList[i].release();
                gaussProgramList[i] = null;
            }
        }
    }

    private void releaseSmoothTexture() {
        for(int i = 0; i < 7; ++i) {
            if(smoothList[i] != null) {
                smoothList[i].release();
                smoothList[i] = null;
            }
        }
    }
}
