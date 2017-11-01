package com.candy1126xx.superrecorder.filter;

import android.graphics.Rect;
import android.opengl.GLES20;

import com.candy1126xx.superrecorder.openglwrapper.GlProgram;
import com.candy1126xx.superrecorder.openglwrapper.GlTexture;
import com.candy1126xx.superrecorder.openglwrapper.GlUtil;
import com.candy1126xx.superrecorder.record.RuntimeFilter;

/**
 *
 */

public class SimpleRender implements RenderImpl {

    private final String vs = ""
            + "uniform mat4 texMatrix;\n"
            + "uniform mat4 verMatrix;\n"
            + "attribute vec2 position;\n"
            + "attribute vec2 textureCoord;\n"
            + "varying vec2 textureCoordinate;\n"
            + "\n"
            + "void main() {\n"
            + "    gl_Position = verMatrix * vec4(position,0,1);\n"
            + "    textureCoordinate = (texMatrix * vec4(textureCoord,0,1)).xy;\n"
            + "}";

    private final String fsOES = ""
            + "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "\n"
            + "varying mediump vec2 textureCoordinate;\n"
            + "uniform samplerExternalOES inputTexture;\n"
            + "\n"
            + "void main(void) {\n"
            + "    gl_FragColor =texture2D(inputTexture, textureCoordinate);\n"
            + "}";

    public SimpleRender() {

    }

    @Override
    public void init() {
        program = new GlProgram(vs, fsOES);
    }

    private GlProgram program;

    @Override
    public void drawTexture(GlTexture texture, float[] surfaceCoord, Rect scaleRect, float[] mStMatrix) {
        GLES20.glUseProgram(program.getID());
        GlUtil.checkGlError("glUseProgram");
        // 输入顶点在世界坐标系中的坐标
        program.setVertexAttriArray("position", 2, surfaceCoord);
        // 输入顶点从世界坐标系到视锥坐标系的矩阵
        program.setUniformMatrix4fv("verMatrix", GlProgram.getScaleTranslation(scaleRect));
        // 输入顶点纹理标准坐标
        program.setVertexAttriArray("textureCoord", 2, GlProgram.mTextureCoord);
        // 输入顶点纹理矩阵
        program.setUniformMatrix4fv("texMatrix", mStMatrix);

        // -------------------步骤二：指明输入输出
        // 指明输出buffer为cameraFrameBuffer。清空，同时cameraTexture变成（0，0，0，0）了
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClearColor(1F, 1F, 1F, 1F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 指明输入texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(texture.getTextureTarget(), texture.getID());
        GlUtil.checkGlError("glBindTexture");
        program.setSampler2D("inputTexture", 0);

        // --------------------步骤三：执行
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFlush();
    }

    @Override
    public void release() {

    }
}
