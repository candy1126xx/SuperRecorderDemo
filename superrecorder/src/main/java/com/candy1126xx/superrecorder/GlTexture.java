package com.candy1126xx.superrecorder;

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
        if(type == 3553) {
            GLES20.glTexImage2D(type, 0, 6408, width, height, 0, 6408, 5121, (Buffer)null);
            GLES20.glTexParameteri(type, 10241, 9729);
            GLES20.glTexParameteri(type, 10240, 9729);
            GLES20.glTexParameteri(type, 10242, '脯');
            GLES20.glTexParameteri(type, 10243, '脯');
        }

    }

    public GlTexture(FloatBuffer texture, int width, int height) {
        GLES20.glActiveTexture('蓀');
        GLES20.glPixelStorei(3317, 4);
        GLES20.glGenTextures(1, this.mTextureBuffer);
        GLES20.glBindTexture(3553, this.mTextureBuffer.get(0));
        GLES20.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5121, texture);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10242, '脯');
        GLES20.glTexParameteri(3553, 10243, '脯');
    }

    public GlTexture(Bitmap bitmap) {
        GLES20.glGenTextures(1, this.mTextureBuffer);
        GlUtil.checkGlError("glGenTextures");
        GLES20.glBindTexture(3553, this.mTextureBuffer.get(0));
        GlUtil.checkGlError("glBindTexture");
        GLUtils.texImage2D(3553, 0, 6408, bitmap, 5121, 0);
        GlUtil.checkGlError("texImage2D");
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10242, '脯');
        GLES20.glTexParameteri(3553, 10243, '脯');
    }

    public int getID() {
        return this.mTextureBuffer.get(0);
    }

    public void release() {
        GLES20.glDeleteTextures(1, this.mTextureBuffer);
    }

}
