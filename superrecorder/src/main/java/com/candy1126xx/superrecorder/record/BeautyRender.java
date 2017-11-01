package com.candy1126xx.superrecorder.record;

import android.graphics.Rect;
import android.opengl.GLES20;

import com.candy1126xx.superrecorder.openglwrapper.GlFrameBuffer;
import com.candy1126xx.superrecorder.openglwrapper.GlProgram;
import com.candy1126xx.superrecorder.openglwrapper.GlTexture;
import com.candy1126xx.superrecorder.openglwrapper.GlUtil;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class BeautyRender {

    // Camera Shader Program
    private GlProgram cameraProgram;

    private GlProgram gaussProgram;

    private GlProgram mixProgram;

    private GlTexture cameraTexture;
    private GlFrameBuffer cameraFrameBuffer;

    private GlTexture gaussTexture;
    private GlFrameBuffer gaussFrameBuffer;

    private float[] _TextureTransform;

    private int cameraWidth;
    private int cameraHeight;

    private int surfaceWidth;
    private int surfaceHeight;

    private Rect scaleRect;

    private int _InputTextureTarget;
    private int _InputTextureID;

    public BeautyRender() {
    }

    public void realize() {
        cameraProgram = new GlProgram(RuntimeFilter.getStandardVertShader(), RuntimeFilter.getStandardFragShader());

        gaussProgram = new GlProgram(RuntimeFilter.getBlurVertexShader(), RuntimeFilter.getBlurFragShader());

        mixProgram = new GlProgram(RuntimeFilter.getMixVertexShader(), RuntimeFilter.getMixFragShader());

        cameraTexture = new GlTexture(surfaceWidth, surfaceHeight);
        cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());

        gaussTexture = new GlTexture(surfaceWidth, surfaceHeight);
        gaussFrameBuffer = new GlFrameBuffer(gaussTexture.getID());
    }

    public void unrealize() {
        cameraProgram = null;

        gaussProgram = null;

        mixProgram = null;

        cameraTexture = null;
    }

    public void setInputTexture(GlTexture texture) {
        this._InputTextureTarget = texture.getTextureTarget();
        this._InputTextureID = texture.getID();
    }

    public void setInputTransform(float[] mat4) {
        this._TextureTransform = mat4;
    }

    public void setInputSize(int cameraWidth, int cameraHeight, int surfaceWidth, int surfaceHeight) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.surfaceWidth = surfaceWidth;
        this.surfaceHeight = surfaceHeight;

        scaleRect = getScaleRect();

        if (cameraTexture != null) cameraTexture.release();
        cameraTexture = new GlTexture(surfaceWidth, surfaceHeight);
        if (cameraFrameBuffer != null) cameraFrameBuffer.release();
        cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());

        if (gaussTexture != null) gaussTexture.release();
        gaussTexture = new GlTexture(surfaceWidth, surfaceHeight);
        if (gaussFrameBuffer != null) gaussFrameBuffer.release();
        gaussFrameBuffer = new GlFrameBuffer(gaussTexture.getID());
    }

    public void draw() {
        /*
           cameraProgram的作用是使用摄像头图像所形成的纹理，填充cameraFrameBuffer，
           即：使cameraTexture = 摄像头图像所形成的纹理。

           Vertex Shader：
           四个输入变量：顶点在世界坐标系中的坐标、正交投影矩阵、
                       顶点纹理标准坐标、顶点纹理矩阵
           两个输出变量：顶点在投影坐标系中的坐标，以及所对应的纹理坐标

           attribute vec2 position;       顶点在世界坐标系中的坐标
           attribute vec2 textureCoord;   顶点纹理标准坐标
           uniform mat4 verMatrix;        正交投影矩阵
           uniform mat4 texMatrix;        顶点纹理矩阵
           varying vec2 textureCoordinate;所对应的纹理坐标

           void main() {
               gl_Position =  verMatrix * vec4(position,0,1);
               textureCoordinate = (texMatrix * vec4(textureCoord,0,1)).xy;
           }

           textureCoordinate从Vertex Shader输出后，经过图形管线，在光栅化这一步会进行插值运算，
           即输入Fragment Shader时，textureCoordinate不再是顶点纹理坐标，而是插值得出的片元纹理坐标

           Fragment Shader:
           两个输入变量：片元纹理坐标、纹理单元的索引
           一个输出变量：片元的色彩值

           precision mediump float;
           varying mediump vec2 textureCoordinate;
           uniform sampler2D camerTexture;

           void main(void) {
               gl_FragColor =texture2D(camerTexture, textureCoordinate);
           }

         */
        // --------------------步骤一：指明shader程序及变量
        // 使用cameraProgram
        GLES20.glUseProgram(cameraProgram.getID());
        GlUtil.checkGlError("glUseProgram");
        // 输入顶点在世界坐标系中的坐标
        cameraProgram.setVertexAttriArray("position", 2,
                new float[]{ // 装载变量数据的Buffer
                        0.0F, (float) surfaceHeight,
                        (float) surfaceWidth, (float) surfaceHeight,
                        0.0F, 0.0F,
                        (float) surfaceWidth, 0.0F
                });
        // 输入顶点从世界坐标系到视锥坐标系的矩阵
        cameraProgram.setUniformMatrix4fv("verMatrix", GlProgram.getScaleTranslation(scaleRect));
        // 输入顶点纹理标准坐标
        cameraProgram.setVertexAttriArray("textureCoord", 2, GlProgram.mTextureCoord);
        // 输入顶点纹理矩阵
        cameraProgram.setUniformMatrix4fv("texMatrix", _TextureTransform);

        // -------------------步骤二：指明输入输出
        // 指明输出buffer为cameraFrameBuffer。清空，同时cameraTexture变成（0，0，0，0）了
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, cameraFrameBuffer.getID());
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        // 把摄像头图像形成的纹理存放在GL_TEXTURE0/GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(_InputTextureTarget, _InputTextureID);
        GlUtil.checkGlError("glBindTexture");
        // 指明输入texture为摄像头图像形成的纹理。0与GL_TEXTURE0对应
        cameraProgram.setSampler2D("camerTexture", 0);

        // --------------------步骤三：执行
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        /*
           gaussProgram的作用是使用cameraTexture，经过高斯模糊后，填充gaussFrameBuffer，
           即：使gaussTexture = 高斯模糊后的cameraTexture。

           Vertex Shader：
           四个输入变量：顶点在模型坐标系中的坐标、 顶点纹理在模型坐标系中的坐标、
                       滤波器采样横向偏移、滤波器采样纵向偏移
           两个输出变量：顶点在世界坐标系中的坐标、滤波器采样坐标（顶点）

           attribute vec2 position;                  顶点在模型坐标系中的坐标
           attribute vec2 inputTextureCoordinate;    顶点纹理在模型坐标系中的坐标
           uniform float texelWidthOffset;           滤波器采样横向偏移
           uniform float texelHeightOffset;          滤波器采样纵向偏移
           varying vec2 blurCoordinates[2];          滤波器采样坐标（顶点）

           void main() {
               gl_Position = vec4(position.xy,0,1);
               vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
               blurCoordinates[0] = inputTextureCoordinate.xy;
               blurCoordinates[1] = inputTextureCoordinate.xy + singleStepOffset;
               blurCoordinates[2] = inputTextureCoordinate.xy - singleStepOffset;
           }

           同样地，blurCoordinates在输入Fragment Shader前，也被插值为片元的采样坐标

           Fragment Shader:
           两个输入变量：滤波器采样坐标（片元）、纹理单元的索引
           一个输出变量：片元的色彩值

           #extension GL_OES_EGL_image_external : require precision mediump float;
           uniform sampler2D inputImageTexture;      纹理单元的索引
           varying vec2 blurCoordinates[2];          滤波器采样坐标（片元）

           void main() {
               vec4 sum = vec4(0.0);
               sum += texture2D(inputImageTexture, blurCoordinates[0]) * 高斯权重;
               sum += texture2D(inputImageTexture, blurCoordinates[1]) * 高斯权重;
               sum += texture2D(inputImageTexture, blurCoordinates[2]) * 高斯权重;
               gl_FragColor = sum;
           }
         */

        // --------------------步骤一：指明shader程序及变量
        // 使用gaussProgram
        GLES20.glUseProgram(gaussProgram.getID());
        GlUtil.checkGlError("glUseProgram");
        // 输入顶点在模型坐标系中的坐标
        gaussProgram.setVertexAttriArray("position", 2, new float[]{
                -1.0F, -1.0F,
                1.0F, -1.0F,
                -1.0F, 1.0F,
                1.0F, 1.0F
        });
        // 输入顶点纹理在模型坐标系中的坐标
        gaussProgram.setVertexAttriArray("inputTextureCoordinate", 2, new float[]{
                0.0F, 0.0F,
                1.0F, 0.0F,
                0.0F, 1.0F,
                1.0F, 1.0F
        });
        // 输入滤波器采样横向偏移，2.0D与高斯计算中的距离相对应
        gaussProgram.setFloat("texelWidthOffset", (float) Math.sqrt(2.0D) / (float) surfaceWidth);
        // 输入滤波器采样纵向偏移
        gaussProgram.setFloat("texelHeightOffset", (float) Math.sqrt(2.0D) / (float) surfaceWidth);

        // -------------------步骤二：指明输入输出
        // 指明输出buffer为gaussFrameBuffer1。清空，同时gaussTexture变成（0，0，0，0）了
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, gaussFrameBuffer.getID());
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        // 把cameraTexture存放在GL_TEXTURE1/GL_TEXTURE_2D
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTexture.getID());
        // 指明输入texture为cameraTexture。1与GL_TEXTURE1对应
        gaussProgram.setSampler2D("inputImageTexture", 1);

        // --------------------步骤三：执行
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        /*
           mixProgram的作用是混合原图和模糊图，输出到帧缓冲区
         */
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        GLES20.glUseProgram(mixProgram.getID());
        mixProgram.setVertexAttriArray("position", 2, GlProgram.mVertexLocation);
        mixProgram.setVertexAttriArray("inputTextureCoordinate", 2, GlProgram.mTextureCoord);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTexture.getID());
        GlUtil.checkGlError("glBindTexture");
        mixProgram.setSampler2D("inputImageTexture", 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gaussTexture.getID());
        GlUtil.checkGlError("glBindTexture");
        mixProgram.setSampler2D("inputImageTexture2", 2);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    // 计算cameraSize到surfaceSize的缩放
    private Rect getScaleRect() {
        Rect rect = new Rect();
        float ratio = (float) cameraHeight / (float) cameraWidth;
        rect.left = 0;
        rect.top = 0;
        rect.right = surfaceWidth;
        rect.bottom = (int) (surfaceWidth * ratio);
        return rect;
    }
}
