package com.candy1126xx.superrecorder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class MediaCodecRecorder {

    private MediaFormat colorFormat;

    private MediaCodec encoder;
    private Surface surface;
    private MediaCodec.BufferInfo encoderBufferInfo = new MediaCodec.BufferInfo();
    private ByteBuffer[] encoderOutputBuffers;

    private AVMuxer muxer;

    private MediaCodecRecorderCallback callback;

    public void init(AVMuxer muxer, int exceptWidth, int exceptHeight) {
        this.muxer = muxer;

        colorFormat = MediaFormat.createVideoFormat("video/avc", exceptWidth, exceptHeight);
        colorFormat.setInteger(MediaFormat.KEY_WIDTH, exceptWidth);
        colorFormat.setInteger(MediaFormat.KEY_HEIGHT, exceptHeight);
        colorFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        colorFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1500000);
        colorFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        colorFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        try {
            encoder = MediaCodec.createEncoderByType("video/avc");
            encoder.configure(colorFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            surface = encoder.createInputSurface();
            encoder.start();
            encoderOutputBuffers = encoder.getOutputBuffers();
            if (callback != null) callback.onCreateEncoderSuccess();
        } catch (IOException e) {
            surface = null;
            if (callback != null) callback.onCreateEncoderFail();
        }
    }

    public Surface getInputSurface() {
        return surface;
    }

    public void setMediaCodecRecorderCallback(MediaCodecRecorderCallback callback) {
        this.callback = callback;
    }

    public interface MediaCodecRecorderCallback {
        void onCreateEncoderSuccess();

        void onCreateEncoderFail();
    }

    //------------------------------------以上代码在主线程
    //------------------------------------以下代码在Camera线程

    public void onSurfaceRender() {
        synchronized (this) {
            writeToFile();
        }
    }

    private void writeToFile() {
        while (encoder != null) {
            int encoderStatus = encoder.dequeueOutputBuffer(encoderBufferInfo, 0L);

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                muxer.addTrack(encoder.getOutputFormat(), 1);
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = encoder.getOutputBuffers();
            } else if (encoderStatus >= 0) {
                if ((encoderBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    encoder.stop();
                    encoder.release();
                    encoder = null;
                } else {
                    muxer.writeSample(encoderOutputBuffers[encoderStatus], encoderBufferInfo, 1);
                    encoder.releaseOutputBuffer(encoderStatus, false);
                }
            }
        }
    }

    public void close() {
        if (encoder != null) encoder.signalEndOfInputStream();
    }
}
