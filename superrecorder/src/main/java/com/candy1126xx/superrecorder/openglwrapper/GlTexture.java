package com.candy1126xx.superrecorder.openglwrapper;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class GlTexture {

    private IntBuffer mTextureBuffer = IntBuffer.allocate(1);

    public GlTexture(int type, int width, int height) {
        GLES20.glGenTextures(1, this.mTextureBuffer);
        GlUtil.checkGlError("glGenTextures");
        GLES20.glBindTexture(type, this.mTextureBuffer.get(0));
        GlUtil.checkGlError("glBindTexture");
        if(type == GLES20.GL_TEXTURE_2D) {
            GLES20.glTexImage2D(type, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer)null);
            GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }

    }

    public GlTexture(FloatBuffer texture, int width, int height) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glPixelStorei(3317, 4);
        GLES20.glGenTextures(1, this.mTextureBuffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.mTextureBuffer.get(0));
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, texture);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    public GlTexture(Bitmap bitmap) {
        GLES20.glGenTextures(1, this.mTextureBuffer);
        GlUtil.checkGlError("glGenTextures");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.mTextureBuffer.get(0));
        GlUtil.checkGlError("glBindTexture");
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, GLES20.GL_UNSIGNED_BYTE, 0);
        GlUtil.checkGlError("texImage2D");
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    public int getID() {
        return this.mTextureBuffer.get(0);
    }

    public void release() {
        GLES20.glDeleteTextures(1, this.mTextureBuffer);
    }

}
