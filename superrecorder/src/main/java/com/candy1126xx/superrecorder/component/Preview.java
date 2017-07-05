package com.candy1126xx.superrecorder.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class Preview extends SurfaceView {

    public Preview(Context context) {
        super(context);
    }

    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Preview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
}
