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

    private int exceptWidth;
    private int exceptHeight;

    private Rect scaleRect;

    private int _InputTextureTarget;
    private int _InputTextureID;

    private RenderOutput _RenderOutput;

    public BeautyRender() {
    }

    public void realize() {
        cameraProgram = new GlProgram(RuntimeFilter.getStandardVertShader(), RuntimeFilter.getStandardFragShader());

        gaussProgram = new GlProgram(RuntimeFilter.getBlurVertexShader(), RuntimeFilter.getBlurFragShader());

        mixProgram = new GlProgram(RuntimeFilter.getMixVertexShader(), RuntimeFilter.getMixFragShader());

        cameraTexture = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());

        gaussTexture = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        gaussFrameBuffer = new GlFrameBuffer(gaussTexture.getID());
    }

    public void unrealize() {
        cameraProgram = null;

        gaussProgram = null;

        mixProgram = null;

        cameraTexture = null;
    }

    public void setInputTexture(int textureTarget, int textureID) {
        this._InputTextureTarget = textureTarget;
        this._InputTextureID = textureID;
    }

    public void setInputTransform(float[] mat4) {
        this._TextureTransform = mat4;
    }

    public void setInputSize(int cameraWidth, int cameraHeight, int exceptWidth, int exceptHeight) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.exceptWidth = exceptWidth;
        this.exceptHeight = exceptHeight;

        scaleRect = getScaleRect();

        if (cameraTexture != null) cameraTexture.release();
        cameraTexture = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        if (cameraFrameBuffer != null) cameraFrameBuffer.release();
        cameraFrameBuffer = new GlFrameBuffer(cameraTexture.getID());

        if (gaussTexture != null) gaussTexture.release();
        gaussTexture = new GlTexture(GLES20.GL_TEXTURE_2D, exceptWidth, exceptHeight);
        if (gaussFrameBuffer != null) gaussFrameBuffer.release();
        gaussFrameBuffer = new GlFrameBuffer(gaussTexture.getID());
    }

    public void setRenderOutput(RenderOutput output) {
        this._RenderOutput = output;
    }

    public void draw() {
        /*
           cameraProgram的作用是使用摄像头图像所形成的纹理，填充cameraFrameBuffer，
           即：使cameraTexture = 摄像头图像所形成的纹理。

           Vertex Shader：
           四个输入变量：顶点在模型坐标系中的坐标、顶点从模型坐标系到世界坐标系的矩阵、
                       顶点纹理在模型坐标系中的坐标、顶点纹理矩阵
           两个输出变量：顶点在世界坐标系中的坐标，以及所对应的纹理坐标

           attribute vec2 position;       顶点在模型坐标系中的坐标
           attribute vec2 textureCoord;   顶点纹理在模型坐标系中的坐标
           uniform mat4 verMatrix;        顶点从模型坐标系到世界坐标系的矩阵
           uniform mat4 texMatrix;        顶点纹理从模型坐标系到世界坐标系的矩阵
           varying vec2 textureCoordinate;所对应的纹理坐标

           void main() {
               gl_Position =  verMatrix * vec4(position,0,1); 顶点在世界坐标系中的坐标
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
        // 输入顶点在模型坐标系中的坐标
        int id1 = GLES20.glGetAttribLocation(cameraProgram.getID(), "position");
        GlUtil.checkGlError("glGetAttribLocation");
        GlUtil.checkLocation(id1, "position");
        GLES20.glEnableVertexAttribArray(id1);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glVertexAttribPointer(
                id1, // 变量的id
                2, // 每个变量所包含的元素的个数，比如一个点有两个元素x和y，那么就是2
                GLES20.GL_FLOAT, // 元素的类型
                false, // 是否归一化
                0, // 变量之间的间隔
                GlUtil.createFloatBuffer(new float[]{ // 装载变量数据的Buffer
                        0.0F, (float) exceptHeight,
                        (float) exceptWidth, (float) exceptHeight,
                        0.0F, 0.0F,
                        (float) exceptWidth, 0.0F
                }));
        // 输入顶点从模型坐标系到世界坐标系的矩阵
        int id2 = GLES20.glGetUniformLocation(cameraProgram.getID(), "verMatrix");
        GlUtil.checkLocation(id2, "verMatrix");
        GLES20.glUniformMatrix4fv(
                id2, // 变量的id
                1, // 矩阵个数
                false, // 是否要转置矩阵
                GlProgram.getScaleTranslation(scaleRect), // 矩阵数据
                0 // 偏移
        );
        GlUtil.checkGlError("glUniformMatrix4fv");
        // 输入顶点纹理在模型坐标系中的坐标
        cameraProgram.setVertexAttriArray("textureCoord", 2,
                new float[]{
                        0.0F, 0.0F,
                        1.0F, 0.0F,
                        0.0F, 1.0F,
                        1.0F, 1.0F
                });
        // 输入顶点纹理矩阵
        int id4 = GLES20.glGetUniformLocation(cameraProgram.getID(), "texMatrix");
        GlUtil.checkLocation(id4, "texMatrix");
        GLES20.glUniformMatrix4fv(
                id4, // 变量的id
                1, // 矩阵个数
                false, // 是否要转置矩阵
                _TextureTransform, // 矩阵数据
                0 // 偏移
        );
        GlUtil.checkGlError("glUniformMatrix4fv");

        // -------------------步骤二：指明输入输出
        // 指明输出buffer为cameraFrameBuffer。清空，同时cameraTexture变成（0，0，0，0）了
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, cameraFrameBuffer.getID());
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, exceptWidth, exceptHeight);
        // 把摄像头图像形成的纹理存放在GL_TEXTURE0/GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(_InputTextureTarget, _InputTextureID);
        // 指明输入texture为摄像头图像形成的纹理。0与GL_TEXTURE0对应
        int id3 = GLES20.glGetUniformLocation(cameraProgram.getID(), "camerTexture");
        GlUtil.checkLocation(id3, "camerTexture");
        GLES20.glUniform1i(id3, 0);

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

           #extension GL_OES_EGL_image_external : require
           precision mediump float;
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
        gaussProgram.setFloat("texelWidthOffset", (float) Math.sqrt(2.0D) / (float) exceptWidth);
        // 输入滤波器采样纵向偏移
        gaussProgram.setFloat("texelHeightOffset", (float) Math.sqrt(2.0D) / (float) exceptWidth);

        // -------------------步骤二：指明输入输出
        // 指明输出buffer为gaussFrameBuffer1。清空，同时gaussTexture变成（0，0，0，0）了
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, gaussFrameBuffer.getID());
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, exceptWidth, exceptHeight);
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
        _RenderOutput.beginFrame();
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
        _RenderOutput.endFrame();
    }

    // 计算cameraSize放大后的Rect
    private Rect getScaleRect(){
        Rect rect = new Rect();
        float ratio = (float) cameraHeight / (float) cameraWidth;
        rect.left = 0;
        rect.top = 0;
        rect.right = exceptWidth;
        rect.bottom = (int) (exceptWidth * ratio);
        return rect;
    }
}
