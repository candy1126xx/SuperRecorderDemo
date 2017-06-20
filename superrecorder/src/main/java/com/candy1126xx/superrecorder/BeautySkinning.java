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
        GLES20.glActiveTexture('蓀');
        GLES20.glBindTexture(target, texture);
        program.setSampler2D("camerTexture", 0);
        program.setUniformMatrix4fv("texMatrix", textransformMatrix);
        program.setUniformMatrix4fv("verMatrix", verscaleMatrix);
        GLES20.glDrawArrays(5, 0, 4);
    }

}
