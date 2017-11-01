package com.candy1126xx.superrecorder.filter;

import android.graphics.Rect;

import com.candy1126xx.superrecorder.openglwrapper.GlTexture;

/**
 * Created by Administrator on 2017/10/27 0027.
 */

public interface RenderImpl {

    void init();

    void drawTexture(GlTexture texture, float[] surfaceCoord, Rect scaleRect, float[] mStMatrix);

    void release();
}
