package com.candy1126xx.superrecorder;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class GlFilter {

    public static final float[] IDENTITY_MATRIX = new float[16];

    public GlFilter() {
    }

    public static String getStandardVertShader() {
        String vertShader = "uniform mat4 texMatrix;\nuniform mat4 verMatrix;\nattribute vec2 position;\nattribute vec2 textureCoord;\nvarying vec2 textureCoordinate;\nvoid main()\n{\n    gl_Position =  verMatrix * vec4(position,0,1);\n    textureCoordinate = (texMatrix * vec4(textureCoord,0,1) ).xy;\n}";
        return vertShader;
    }

    public static String getStandardFragShader(int target) {
        String extension;
        String sampler;
        switch(target) {
            case GLES20.GL_TEXTURE_2D:
                extension = "";
                sampler = "uniform sampler2D camerTexture;\n";
                break;
            case 36197:
                extension = "#extension GL_OES_EGL_image_external : require\n";
                sampler = "uniform samplerExternalOES camerTexture;\n";
                break;
            default:
                throw new IllegalArgumentException("invalid target: " + target);
        }

        String fragShader = extension + "precision mediump float;\n" + "varying mediump vec2 textureCoordinate;\n" + sampler + "\n" + "void main(void)\n" + "{\n" + "  gl_FragColor =texture2D(camerTexture, textureCoordinate );\n" + "}\n";
        return fragShader;
    }

    public static String getVertexShaderForOptimizedBlurOfRadius(int blurRadius, float sigma) {
        if(blurRadius == 0) {
            Log.e("GlFilter", "blur radius is 0");
            return "";
        } else {
            float[] standardGaussianWeights = new float[blurRadius + 1];
            float sumOfWeights = 0.0F;

            int numberOfOptimizedOffsets;
            for(numberOfOptimizedOffsets = 0; numberOfOptimizedOffsets < blurRadius + 1; ++numberOfOptimizedOffsets) {
                standardGaussianWeights[numberOfOptimizedOffsets] = (float)gaussian((double)sigma, (double)numberOfOptimizedOffsets);
                if(numberOfOptimizedOffsets == 0) {
                    sumOfWeights += standardGaussianWeights[numberOfOptimizedOffsets];
                } else {
                    sumOfWeights = (float)((double)sumOfWeights + 2.0D * (double)standardGaussianWeights[numberOfOptimizedOffsets]);
                }
            }

            for(numberOfOptimizedOffsets = 0; numberOfOptimizedOffsets < blurRadius + 1; ++numberOfOptimizedOffsets) {
                standardGaussianWeights[numberOfOptimizedOffsets] /= sumOfWeights;
            }

            Log.w("GlFilter", standardGaussianWeights.toString());
            numberOfOptimizedOffsets = Math.min(blurRadius / 2 + blurRadius % 2, 7);
            float[] optimizedGaussianOffsets = new float[numberOfOptimizedOffsets];
            String mVertShader = "";
            int currentOptimizedOffset;
            float firstWeight;
            float secondWeight;
            float optimizedWeight;
            if(blurRadius % 2 == 0) {
                for(currentOptimizedOffset = 0; currentOptimizedOffset < numberOfOptimizedOffsets; ++currentOptimizedOffset) {
                    firstWeight = standardGaussianWeights[currentOptimizedOffset * 2 + 1];
                    secondWeight = standardGaussianWeights[currentOptimizedOffset * 2 + 2];
                    optimizedWeight = firstWeight + secondWeight;
                    optimizedGaussianOffsets[currentOptimizedOffset] = (firstWeight * (float)(currentOptimizedOffset * 2 + 1) + secondWeight * (float)(currentOptimizedOffset * 2 + 2)) / optimizedWeight;
                }

                mVertShader = String.format("attribute vec2 position;\nattribute vec2 inputTextureCoordinate;\n\nuniform float texelWidthOffset;\nuniform float texelHeightOffset;\n\nvarying vec2 blurCoordinates[%1$d];\n\nvoid main()\n{\n   gl_Position = vec4(position.xy,0,1);\n\n   vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n", new Object[]{Long.valueOf((long)(1 + numberOfOptimizedOffsets * 2))});
                mVertShader = mVertShader + "blurCoordinates[0] = inputTextureCoordinate.xy;\n";

                for(currentOptimizedOffset = 0; currentOptimizedOffset < numberOfOptimizedOffsets; ++currentOptimizedOffset) {
                    mVertShader = mVertShader + String.format("blurCoordinates[%1$d] = inputTextureCoordinate.xy + singleStepOffset * %2$f;\nblurCoordinates[%3$d] = inputTextureCoordinate.xy - singleStepOffset * %4$f;\n", new Object[]{Long.valueOf((long)(currentOptimizedOffset * 2 + 1)), Float.valueOf(optimizedGaussianOffsets[currentOptimizedOffset]), Long.valueOf((long)(currentOptimizedOffset * 2 + 2)), Float.valueOf(optimizedGaussianOffsets[currentOptimizedOffset])});
                }
            } else {
                for(currentOptimizedOffset = 0; currentOptimizedOffset < numberOfOptimizedOffsets; ++currentOptimizedOffset) {
                    if(currentOptimizedOffset == 0) {
                        firstWeight = standardGaussianWeights[currentOptimizedOffset] / 2.0F;
                    } else {
                        firstWeight = standardGaussianWeights[currentOptimizedOffset * 2];
                    }

                    secondWeight = standardGaussianWeights[currentOptimizedOffset * 2 + 1];
                    optimizedWeight = firstWeight + secondWeight;
                    optimizedGaussianOffsets[currentOptimizedOffset] = (firstWeight * (float)(currentOptimizedOffset * 2) + secondWeight * (float)(currentOptimizedOffset * 2 + 1)) / optimizedWeight;
                }

                mVertShader = String.format("attribute vec2 position;\nattribute vec2 inputTextureCoordinate;\n\nuniform float texelWidthOffset;\nuniform float texelHeightOffset;\n\nvarying vec2 blurCoordinates[%1$d];\n\nvoid main()\n{\n   gl_Position = vec4(position.xy,0,1);\n\n   vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n", new Object[]{Integer.valueOf(numberOfOptimizedOffsets * 2)});

                for(currentOptimizedOffset = 0; currentOptimizedOffset < numberOfOptimizedOffsets; ++currentOptimizedOffset) {
                    mVertShader = mVertShader + String.format("blurCoordinates[%1$d] = inputTextureCoordinate.xy + singleStepOffset * %2$f;\nblurCoordinates[%3$d] = inputTextureCoordinate.xy - singleStepOffset * %4$f;\n", new Object[]{Long.valueOf((long)(currentOptimizedOffset * 2)), Float.valueOf(optimizedGaussianOffsets[currentOptimizedOffset]), Long.valueOf((long)(currentOptimizedOffset * 2 + 1)), Float.valueOf(optimizedGaussianOffsets[currentOptimizedOffset])});
                }
            }

            mVertShader = mVertShader + "}\n";
            Log.w("GaussVertShader", mVertShader);
            return mVertShader;
        }
    }

    public static String getFragmentShaderForOptimizedBlurOfRadius(int blurRadius, float sigma) {
        if(blurRadius == 0) {
            Log.e("GlFilter", "blurRadius is 0 failed");
            return "";
        } else {
            float[] standardGaussianWeights = new float[blurRadius + 1];
            float sumOfWeights = 0.0F;

            int numberOfOptimizedOffsets;
            for(numberOfOptimizedOffsets = 0; numberOfOptimizedOffsets < blurRadius + 1; ++numberOfOptimizedOffsets) {
                standardGaussianWeights[numberOfOptimizedOffsets] = (float)(1.0D / Math.sqrt(6.283185307179586D * Math.pow((double)sigma, 2.0D)) * Math.exp(-Math.pow((double)numberOfOptimizedOffsets, 2.0D) / (2.0D * Math.pow((double)sigma, 2.0D))));
                if(numberOfOptimizedOffsets == 0) {
                    sumOfWeights += standardGaussianWeights[numberOfOptimizedOffsets];
                } else {
                    sumOfWeights = (float)((double)sumOfWeights + 2.0D * (double)standardGaussianWeights[numberOfOptimizedOffsets]);
                }
            }

            for(numberOfOptimizedOffsets = 0; numberOfOptimizedOffsets < blurRadius + 1; ++numberOfOptimizedOffsets) {
                standardGaussianWeights[numberOfOptimizedOffsets] /= sumOfWeights;
            }

            numberOfOptimizedOffsets = Math.min(blurRadius / 2 + blurRadius % 2, 7);
            int trueNumberOfOptimizedOffsets = blurRadius / 2 + blurRadius % 2;
            String mFragShader = "";
            float secondWeight;
            float optimizedWeight;
            float optimizedOffset;
            if(blurRadius % 2 == 0) {
                mFragShader = String.format("#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform sampler2D inputImageTexture;\nuniform float texelWidthOffset;\nuniform float texelHeightOffset;\n\nvarying vec2 blurCoordinates[%d];\n\nvoid main()\n{\n   vec4 sum = vec4(0.0);\n", new Object[]{Integer.valueOf(1 + numberOfOptimizedOffsets * 2)});
                String currentOverlowTextureRead = Float.toString(standardGaussianWeights[0]);
                mFragShader = mFragShader + "sum += texture2D(inputImageTexture, blurCoordinates[0]) * " + currentOverlowTextureRead + ";\n";

                int firstWeight;
                for(firstWeight = 0; firstWeight < numberOfOptimizedOffsets; ++firstWeight) {
                    secondWeight = standardGaussianWeights[firstWeight * 2 + 1];
                    optimizedWeight = standardGaussianWeights[firstWeight * 2 + 2];
                    optimizedOffset = secondWeight + optimizedWeight;
                    mFragShader = mFragShader + String.format("sum += texture2D(inputImageTexture, blurCoordinates[%1$d]) * %2$f;\nsum += texture2D(inputImageTexture, blurCoordinates[%3$d]) * %4$f;\n", new Object[]{Long.valueOf((long)(firstWeight * 2 + 1)), Float.valueOf(optimizedOffset), Long.valueOf((long)(firstWeight * 2 + 2)), Float.valueOf(optimizedOffset)});
                }

                if(trueNumberOfOptimizedOffsets > numberOfOptimizedOffsets) {
                    mFragShader = mFragShader + "vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n";

                    for(firstWeight = numberOfOptimizedOffsets; firstWeight < trueNumberOfOptimizedOffsets; ++firstWeight) {
                        secondWeight = standardGaussianWeights[firstWeight * 2 + 1];
                        optimizedWeight = standardGaussianWeights[firstWeight * 2 + 2];
                        optimizedOffset = secondWeight + optimizedWeight;
                        float optimizedOffset1 = (secondWeight * (float)(firstWeight * 2 + 1) + optimizedWeight * (float)(firstWeight * 2 + 2)) / optimizedOffset;
                        mFragShader = mFragShader + String.format("sum += texture2D(inputImageTexture, blurCoordinates[0] + singleStepOffset * %1$f) * %2$f;\nsum += texture2D(inputImageTexture, blurCoordinates[0] - singleStepOffset * %3$f) * %4$f;\n", new Object[]{Float.valueOf(optimizedOffset1), Float.valueOf(optimizedOffset), Float.valueOf(optimizedOffset1), Float.valueOf(optimizedOffset)});
                    }
                }
            } else {
                mFragShader = String.format("#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform sampler2D inputImageTexture;\nuniform float texelWidthOffset;\nuniform float texelHeightOffset;\n\nvarying vec2 blurCoordinates[%d];\n\nvoid main()\n{\n   vec4 sum = vec4(0.0);\n", new Object[]{Integer.valueOf(numberOfOptimizedOffsets * 2)});

                int var13;
                float var14;
                for(var13 = 0; var13 < numberOfOptimizedOffsets; ++var13) {
                    if(var13 == 0) {
                        var14 = standardGaussianWeights[var13] / 2.0F;
                    } else {
                        var14 = standardGaussianWeights[var13 * 2];
                    }

                    secondWeight = standardGaussianWeights[var13 * 2 + 1];
                    optimizedWeight = var14 + secondWeight;
                    mFragShader = mFragShader + String.format("sum += texture2D(inputImageTexture, blurCoordinates[%1$d]) * %2$f;\nsum += texture2D(inputImageTexture, blurCoordinates[%3$d]) * %4$f;\n", new Object[]{Long.valueOf((long)(var13 * 2)), Float.valueOf(optimizedWeight), Long.valueOf((long)(var13 * 2 + 1)), Float.valueOf(optimizedWeight)});
                }

                if(trueNumberOfOptimizedOffsets > numberOfOptimizedOffsets) {
                    mFragShader = mFragShader + "vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n";

                    for(var13 = numberOfOptimizedOffsets; var13 < trueNumberOfOptimizedOffsets; ++var13) {
                        var14 = standardGaussianWeights[var13 * 2 + 1];
                        secondWeight = standardGaussianWeights[var13 * 2 + 2];
                        optimizedWeight = var14 + secondWeight;
                        optimizedOffset = (var14 * (float)(var13 * 2 + 1) + secondWeight * (float)(var13 * 2 + 2)) / optimizedWeight;
                        mFragShader = mFragShader + String.format("sum += texture2D(inputImageTexture, blurCoordinates[0] + singleStepOffset * %1$f) * %2$f;\nsum += texture2D(inputImageTexture, blurCoordinates[0] - singleStepOffset * %3$f) * %4$f;\n", new Object[]{Float.valueOf(optimizedOffset), Float.valueOf(optimizedWeight), Float.valueOf(optimizedOffset), Float.valueOf(optimizedWeight)});
                    }
                }
            }

            mFragShader = mFragShader + "gl_FragColor = sum;\n}\n";
            Log.w("GaussFragShader", mFragShader);
            return mFragShader;
        }
    }

    public static String getBeautySkinningVertShader() {
        String BeautySkinningVertShader = "attribute vec2 position;\nattribute vec2 inputTextureCoordinate;\nvarying vec2 textureCoordinate;\nvoid main()\n{\n    gl_Position =  vec4(position.xy,0,1);\n    textureCoordinate = inputTextureCoordinate;\n}";
        return BeautySkinningVertShader;
    }

    public static String getBeautySkinningFragShader() {
        String BeautyVideoFragmentShaderString = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying mediump vec2 textureCoordinate;\n//varying mediump vec2 textureCoordinate2;\nuniform sampler2D inputImageTexture;\nuniform sampler2D inputImageTexture2;\nuniform sampler2D inputImageTexture3;\nuniform float skinRed;\nuniform float skinBlue;\n\n#define Blend_pLitf(base,blend)   (min(1.0,max(0.0,((base)+2.0*(blend)-1.0))))\n#define Blend_hLitf(base,blend) ((blend)<=0.5?(blend)*(base)/0.5:1.0-(1.0-(blend))*(1.0-(base))/0.5)\n\nvoid main(void)\n{\n  float x;float y;\n  x = textureCoordinate.x;\n  y = textureCoordinate.y;\n  vec4 oral =texture2D(inputImageTexture, vec2(x,y));\n  vec4 gauss =texture2D(inputImageTexture2, vec2(x,y));\n  \n  vec4 curve = oral;\n  curve.r = texture2D(inputImageTexture3,vec2(curve.r,0.5)).b;\n  curve.g = texture2D(inputImageTexture3,vec2(curve.g,0.5)).b;\n  curve.b = texture2D(inputImageTexture3,vec2(curve.b,0.5)).b;\n  \n  float G = oral.g;\n  float G1 = 1.0 - gauss.g;\n  G1 = Blend_pLitf(G, G1);\n  float G2=mix(G,G1,0.5);\n  \n  G2=Blend_hLitf(G2, G2);\n  G2=Blend_hLitf(G2, G2);\n  G2=Blend_hLitf(G2, G2);\n  vec4 temp=mix(curve,oral,G2);\n  float Offset = max(0.0,min(1.0,(oral.r - (skinRed - 0.5))));\n  float alpha = 0.0;\n  if(Offset < 0.5)\n  {\n     alpha = Offset*2.0;//254 - (127 - Offset) * 2;\n  }\n  else\n  {\n      alpha = 1.0;\n  }\n  \n  //float b = step(0.5,Offset);\n  //alpha = (1 - b) * Offset * 2.0 + b;\n\n  float OffsetJ = max(0.0,min(1.0,(oral.b - skinBlue)));\n  alpha = max(alpha - OffsetJ / 2.0,0.0);\n  \n  oral = mix(oral,temp,alpha);\n  \n  gl_FragColor = oral;\n}\n";
        return BeautyVideoFragmentShaderString;
    }

    private static double gaussian(double sigma, double x) {
        return 1.0D / (sigma * Math.sqrt(6.283185307179586D)) * Math.exp(-(x * x) / (2.0D * sigma * sigma));
    }

    static {
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }

}
