package com.candy1126xx.superrecorder;

import android.opengl.GLES20;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class BeautySkinning {

    public static void drawFirstCameraTexture(GlProgram program, int target, int texture, float[] vertexcoord, float[] texturecoord, float[] textransformMatrix, float[] verscaleMatrix) {
        GLES20.glUseProgram(program.getID());
        GlUtil.checkGlError("glUseProgram");
        program.setVertexAttriArray("position", 2, vertexcoord);
        program.setVertexAttriArray("textureCoord", 2, texturecoord);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(target, texture);
        program.setSampler2D("camerTexture", 0);
        program.setUniformMatrix4fv("texMatrix", textransformMatrix);
        program.setUniformMatrix4fv("verMatrix", verscaleMatrix);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public static void drawSecondGaussTexture(GlProgram program, int framebuffer, int texture, float[] vertexcoord, float[] texturecoord, float widthoffset, float heightoffset, int width, int height) {
        GLES20.glUseProgram(program.getID());
        GlUtil.checkGlError("glUseProgram");
        program.setVertexAttriArray("position", 2, vertexcoord);
        program.setVertexAttriArray("inputTextureCoordinate", 2, texturecoord);
        program.setFloat("texelWidthOffset", widthoffset);
        program.setFloat("texelHeightOffset", heightoffset);
        GLES20.glActiveTexture('蓁');
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        program.setSampler2D("inputImageTexture", 1);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glDrawArrays(5, 0, 4);
    }

}
