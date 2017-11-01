package com.candy1126xx.superrecorder.record;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class RuntimeFilter {

    public static String getStandardVertShader() {
        return "uniform mat4 texMatrix;\n" +
                "uniform mat4 verMatrix;\n" +
                "attribute vec2 position;\n" +
                "attribute vec2 textureCoord;\n" +
                "varying vec2 textureCoordinate;\n" +
                "\n" +
                "void main() {\n" +
                "    gl_Position = verMatrix * vec4(position,0,1);\n" +
                "    textureCoordinate = (texMatrix * vec4(textureCoord,0,1)).xy;\n" +
                "}";
    }

    public static String getStandardFragShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying mediump vec2 textureCoordinate;\n" +
                "uniform samplerExternalOES camerTexture;\n" +
                "\n" +
                "void main(void) {\n" +
                "    gl_FragColor =texture2D(camerTexture, textureCoordinate);\n" +
                "}";
    }

    public static String getBlurVertexShader() {
        return "attribute vec2 position;\n" +
                "attribute vec2 inputTextureCoordinate;\n" +
                "uniform float texelWidthOffset;\n" +
                "uniform float texelHeightOffset;\n" +
                "varying vec2 blurCoordinates[3];\n" +
                "\n" +
                "void main() {\n" +
                "    gl_Position = vec4(position.xy,0,1);\n" +
                "    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
                "    blurCoordinates[0] = inputTextureCoordinate.xy;\n" +
                "    blurCoordinates[1] = inputTextureCoordinate.xy + singleStepOffset;\n" +
                "    blurCoordinates[2] = inputTextureCoordinate.xy - singleStepOffset;\n" +
                "}";
    }

    public static String getBlurFragShader() {
        float[] weights = new float[2];
        float sumOfWeights = 1.0F;

        int i;
        for (i = 0; i < 2; i++) {
            weights[i] = gaussian(2.0D, 2.0D);
            sumOfWeights += weights[i];
        }

        for (i = 0; i < 2; i++) {
            weights[i] /= sumOfWeights;
        }

        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform sampler2D inputImageTexture;\n" +
                "varying vec2 blurCoordinates[3];\n" +
                "\n" +
                "void main() {\n" +
                "    vec4 sum = vec4(0.0);\n" +
                "    sum += texture2D(inputImageTexture, blurCoordinates[0]);\n" +
                "    sum += texture2D(inputImageTexture, blurCoordinates[1]) * "+ weights[0] +";\n" +
                "    sum += texture2D(inputImageTexture, blurCoordinates[2]) * "+ weights[1] +";\n" +
                "    gl_FragColor = sum;\n" +
                "}";
    }

    public static String getMixVertexShader() {
        return "attribute vec2 position;\n" +
                "attribute vec2 inputTextureCoordinate;\n" +
                "varying vec2 textureCoordinate;\n" +
                "\n" +
                "void main() {\n" +
                "    gl_Position =  vec4(position.xy,0,1);\n" +
                "    textureCoordinate = inputTextureCoordinate;\n" +
                "}";
    }

    public static String getMixFragShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying mediump vec2 textureCoordinate;\n" +
                "uniform sampler2D inputImageTexture;\n" +
                "uniform sampler2D inputImageTexture2;\n" +
                "\n" +
                "void main(void) {\n" +
                "    gl_FragColor = mix(texture2D(inputImageTexture, textureCoordinate.xy),\n" +
                "                       texture2D(inputImageTexture2, textureCoordinate.xy), \n" +
                "                       0.3);\n" +
                "}";
    }

    // 高斯函数，sigma方差，x距离
    private static float gaussian(double sigma, double x) {
        return (float) (1.0D / (sigma * Math.sqrt(6.283185307179586D)) * Math.exp(-(x * x) / (2.0D * sigma * sigma)));
    }
}
