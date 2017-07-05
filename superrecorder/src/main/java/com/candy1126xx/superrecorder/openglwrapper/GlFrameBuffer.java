package com.candy1126xx.superrecorder.openglwrapper;

import android.opengl.GLES20;

import java.nio.IntBuffer;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class GlFrameBuffer {

    private IntBuffer mFrameBuffer = IntBuffer.allocate(1);

    public GlFrameBuffer(int textureID) {
        GLES20.glGenFramebuffers(1, this.mFrameBuffer);
        GlUtil.checkGlError("glGenFramebuffers");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.mFrameBuffer.get(0));
        GlUtil.checkGlError("glBindFramebuffer");
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureID, 0);
        GlUtil.checkGlError("glFramebufferTexture2D");
    }

    public void release() {
        GLES20.glDeleteFramebuffers(1, this.mFrameBuffer);
    }

    public int getID() {
        return this.mFrameBuffer.get(0);
    }

}
