package com.candy1126xx.superrecorder;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class GlProgram {

    private static String TAG = "GlProgram";
    private int mProgram;
    public static final float[] mVertexLocation = new float[]{-1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F};
    public static final float[] mTextureCoord = new float[]{0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F};

    public GlProgram(String vert, String frag) {
        int mVertShader = this.loadShader('謱', vert);
        int mFragShader = this.loadShader('謰', frag);
        this.mProgram = GLES20.glCreateProgram();
        GlUtil.checkGlError("glCreateProgram");
        GLES20.glAttachShader(this.mProgram, mVertShader);
        GlUtil.checkGlError("glAttachShader");
        GLES20.glAttachShader(this.mProgram, mFragShader);
        GlUtil.checkGlError("glAttachShader");
        GLES20.glLinkProgram(this.mProgram);
        GlUtil.checkGlError("glLinkProgram");
        GLES20.glDeleteShader(mVertShader);
        GlUtil.checkGlError("glDeleteShader");
        GLES20.glDeleteShader(mFragShader);
        GlUtil.checkGlError("glDeleteShader");
    }

    public int getID() {
        return this.mProgram;
    }

    public void release() {
        GLES20.glDeleteProgram(this.mProgram);
    }

    public static float[] getVertexArray(float x, float y) {
        float[] array = new float[]{0.0F, y, x, y, 0.0F, 0.0F, x, 0.0F};
        return array;
    }

    public static float[] getScaleTranslation(Rect rect) {
        float[] array = new float[16];
        Matrix.orthoM(array, 0, (float)rect.left, (float)rect.right, (float)rect.bottom, (float)rect.top, 1.0F, -1.0F);
        return array;
    }

    public void setUniformMatrix4fv(String location, float[] matrix) {
        int id = GLES20.glGetUniformLocation(this.mProgram, location);
        GlUtil.checkLocation(id, location);
        GLES20.glUniformMatrix4fv(id, 1, false, matrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");
    }

    public void setVertexAttriArray(String location, int num, float[] array) {
        int id = GLES20.glGetAttribLocation(this.mProgram, location);
        GlUtil.checkGlError("glGetAttribLocation");
        GlUtil.checkLocation(id, location);
        GLES20.glEnableVertexAttribArray(id);
        GLES20.glBindBuffer('袒', 0);
        GLES20.glVertexAttribPointer(id, num, 5126, false, 0, GlUtil.createFloatBuffer(array));
    }

    public void setFloat(String location, float fnum) {
        int id = GLES20.glGetUniformLocation(this.mProgram, location);
        GlUtil.checkLocation(id, location);
        GLES20.glEnableVertexAttribArray(id);
        GLES20.glBindBuffer('袒', 0);
        GLES20.glUniform1f(id, fnum);
    }

    public void setSampler2D(String location, int activenum) {
        int id = GLES20.glGetUniformLocation(this.mProgram, location);
        GlUtil.checkLocation(id, location);
        GLES20.glUniform1i(id, activenum);
    }

    public int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        GlUtil.checkGlError("glCreateShader");
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        GlUtil.checkGlError("glCreateShader");
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, '讁', compiled, 0);
        if(compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + source);
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }

}
