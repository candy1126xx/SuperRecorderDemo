package com.candy1126xx.superrecorder.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.candy1126xx.superrecorder.model.Clip;

import java.util.LinkedList;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class RecordProgress extends View {

    public RecordProgress(Context context) {
        super(context);
    }

    public RecordProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private long duration; // 总时长

    private LinkedList<Clip> clips = new LinkedList<>();

    private Paint paint;

    private RectF rectF;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            rectF = new RectF(0, 0, 0, 20);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < clips.size(); i++) {
            drawClip(i == clips.size() - 1, clips.get(i), canvas);
        }
    }

    private void drawClip(boolean current, Clip clip, Canvas canvas) {
        paint.setColor(current ? Color.BLUE : Color.GREEN);
        rectF.left = calPosition(clip.startTime);
        rectF.right = calPosition(clip.endTime) - 5;
        canvas.drawRect(rectF, paint);
        paint.setColor(Color.WHITE);
        rectF.left = calPosition(clip.endTime) - 5;
        rectF.right = calPosition(clip.endTime);
        canvas.drawRect(rectF, paint);
    }

    private float calPosition(long timePoint) {
        return getWidth() * (float) timePoint / (float) duration;
    }

    public void init(long duration) {
        this.duration = duration;
    }

    public void setProgress(LinkedList<Clip> clips) {
        this.clips = clips;
        postInvalidate();
    }
}
