package com.candy1126xx.superrecorder;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.IntBuffer;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class GlFrameBuffer {

    private IntBuffer mFrameBuffer = IntBuffer.allocate(1);

    public GlFrameBuffer(int textureID) {
        GLES20.glGenFramebuffers(1, this.mFrameBuffer);
        GlUtil.checkGlError("glGenFramebuffers");
        GLES20.glBindFramebuffer('赀', this.mFrameBuffer.get(0));
        GlUtil.checkGlError("glBindFramebuffer");
        GLES20.glFramebufferTexture2D('赀', '賠', 3553, textureID, 0);
        GlUtil.checkGlError("glFramebufferTexture2D");
        int status = GLES20.glCheckFramebufferStatus('赀');
    }

    public void release() {
        GLES20.glDeleteFramebuffers(1, this.mFrameBuffer);
    }

    public int getID() {
        return this.mFrameBuffer.get(0);
    }

}
