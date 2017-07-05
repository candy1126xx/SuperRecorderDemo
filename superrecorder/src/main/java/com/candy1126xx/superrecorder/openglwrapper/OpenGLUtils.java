package com.candy1126xx.superrecorder.openglwrapper;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class OpenGLUtils {

    // 创建Texture
    public static int createTextureObject(int texture_target) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        checkGlError("glGenTextures");
        int texId = textures[0];
        GLES20.glBindTexture(texture_target, texId);
        checkGlError("glBindTexture " + texId);
        GLES20.glTexParameterf(texture_target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(texture_target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(texture_target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(texture_target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameter");
        return texId;
    }

    public static void checkLocation(int location, String label) {
        if(location < 0) {
            throw new RuntimeException("Unable to locate \'" + label + "\' in program");
        }
    }

    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if(error != 0) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e("OpenGL", msg);
            throw new RuntimeException(msg);
        }
    }

}
