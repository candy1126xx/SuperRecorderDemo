package com.candy1126xx.superrecorder.openglwrapper;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class GlTexture {

    private int textureTarget;
    private IntBuffer mTextureBuffer = IntBuffer.allocate(1);

    // GLES20.GL_TEXTURE_2D
    public GlTexture(int width, int height) {
        textureTarget = GLES20.GL_TEXTURE_2D;
        GLES20.glGenTextures(1, this.mTextureBuffer);
        GlUtil.checkGlError("glGenTextures");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.mTextureBuffer.get(0));
        GlUtil.checkGlError("glBindTexture");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer) null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    // GLES11Ext.GL_TEXTURE_EXTERNAL_OES
    public GlTexture() {
        textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        GLES20.glGenTextures(1, this.mTextureBuffer);
        GlUtil.checkGlError("glGenTextures");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, this.mTextureBuffer.get(0));
        GlUtil.checkGlError("glBindTexture");
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    public int getID() {
        return this.mTextureBuffer.get(0);
    }

    public int getTextureTarget() {
        return textureTarget;
    }

    public void release() {
        GLES20.glDeleteTextures(1, this.mTextureBuffer);
    }

}
