package com.candy1126xx.superrecorder.filter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.view.Surface;

import com.candy1126xx.superrecorder.openglwrapper.GlTexture;
import com.candy1126xx.superrecorder.player.DemoPlayer;
import com.candy1126xx.superrecorder.player.ExtractorRendererBuilder;

/**
 * 纹理ID 连接 GL的texture 和 Android的SurfaceTexture
 * Surface 连接 SurfaceTexture 和 MediaCodec/Camera
 */

public class FilterPreviewMission {

    private Context context;

    private String originVideoPath; // 原始视频地址

    private DemoPlayer mediaPlayer;

    private Surface originSurface; // 接收解码数据的surface

    private SurfaceTexture originSurfaceTexture; // 接收解码数据的surfaceTexture

    private GlTexture originTexture; // 接收解码数据的texture

    private RenderWrapper wrapper;

    public FilterPreviewMission(Context context, String originVideoPath, RenderWrapper wrapper) {
        this.context = context;
        this.originVideoPath = originVideoPath;
        this.wrapper = wrapper;

        releaseSurface();
        initSurface();
        releasePlayer();
        initPlayer();
    }

    private void releaseSurface() {
        if (originSurface != null) {
            originSurface.release();
            originSurface = null;
        }
        if (originSurfaceTexture != null) {
            originSurfaceTexture.release();
            originSurfaceTexture = null;
        }
        if (originTexture != null) {
            originTexture.release();
            originTexture = null;
        }
    }

    private void initSurface() {
        originTexture = new GlTexture();
        originSurfaceTexture = new SurfaceTexture(originTexture.getID());
        originSurface = new Surface(originSurfaceTexture);
        wrapper.init(originSurfaceTexture, originTexture);
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void initPlayer() {
        mediaPlayer = new DemoPlayer(new ExtractorRendererBuilder(context, "SuperRecorder", Uri.parse(originVideoPath)));
        mediaPlayer.setSurface(originSurface);
        mediaPlayer.setPlayWhenReady(false);
        mediaPlayer.prepare();
    }

    public void start(int index) {
        if (!wrapper.isGLContextCreated()) return;
        switch (index) {
            case 0:
                wrapper.setRender(new SimpleRender());
                break;
            case 1:
                wrapper.setRender(new GrayRender());
                break;
        }
        mediaPlayer.seekTo(0L);
        mediaPlayer.start();
    }
}
