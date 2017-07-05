package com.candy1126xx.superrecorder.openglwrapper;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class GlUtil {

    private static final int SIZEOF_FLOAT = 4;
    public static final float[] IDENTITY_MATRIX = new float[16];

    private GlUtil() {
    }

    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if(error != 0) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e("GlUtil", msg);
        }

    }

    public static void checkLocation(int location, String label) {
        if(location < 0) {
            Log.e("GlUtil", "Unable to locate \'" + label + "\' in program");
        }

    }

    public static FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    static {
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }

}
