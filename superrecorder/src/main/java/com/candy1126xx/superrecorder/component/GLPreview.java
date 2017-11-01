package com.candy1126xx.superrecorder.component;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.candy1126xx.superrecorder.filter.RenderImpl;
import com.candy1126xx.superrecorder.filter.RenderWrapper;
import com.candy1126xx.superrecorder.openglwrapper.GlTexture;

/**
 * Created by Administrator on 2017/10/26 0026.
 */

public class GLPreview extends GLSurfaceView {

    private RenderWrapper wrapper;

    public GLPreview(Context context) {
        super(context);
    }

    public GLPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);
        wrapper = new RenderWrapper(this);
        setRenderer(wrapper);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private int width, height;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (width == 0) width = getWidth();
        if (height == 0) height = getHeight();
        if (width != height) {
            height = width;
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.width = width;
            lp.height = height;
            setLayoutParams(lp);
            getHolder().setFixedSize(width, height);
        }
    }

    public RenderWrapper getWrapper() {
        return wrapper;
    }
}
