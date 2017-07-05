package com.candy1126xx.superrecorder.record;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/6/23 0023.
 */

public class ClipMuxer {

    private volatile MediaMuxer muxer;
    private volatile int videoTrackerIndex = -1;
    private volatile int audioTrackerIndex = -1;

    private volatile boolean writeToFile;

    private volatile long startTime;
    private volatile long duration;

    private volatile int videoFrameCount;

    private ByteBuffer videoConfigBuffer;
    private MediaCodec.BufferInfo videoBufferInfo;
    private ByteBuffer audioConfigBuffer;
    private MediaCodec.BufferInfo audioBufferInfo;

    private final static Object lock = new Object();

    private ClipMuxerCallback callback;

    //------------------------------------Project线程
    public ClipMuxer(File outputFile) {
        synchronized (lock) {
            try {
                if (outputFile.exists()) outputFile.delete();
                outputFile.createNewFile();
                muxer = new MediaMuxer(outputFile.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public long getDuration() {
        return duration;
    }

    public void stop() {
        synchronized (lock) {
            writeToFile = false;
            muxer.release();
        }
    }

    public void configVideo(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        synchronized (lock) {
            videoConfigBuffer = byteBuffer;
            videoBufferInfo = bufferInfo;
        }
    }

    public void configAudio(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        synchronized (lock) {
            audioConfigBuffer = byteBuffer;
            audioBufferInfo = bufferInfo;
        }
    }

    public void addTrack(MediaFormat format, int type) {
        synchronized (lock) {
            switch (type) {
                case 1:
                    videoTrackerIndex = muxer.addTrack(format);
                    break;
                case 2:
                    audioTrackerIndex = muxer.addTrack(format);
                    break;
            }
            if (videoTrackerIndex != -1 && audioTrackerIndex != -1) {
                muxer.start();
                muxer.writeSampleData(videoTrackerIndex, videoConfigBuffer, videoBufferInfo);
                muxer.writeSampleData(audioTrackerIndex, audioConfigBuffer, audioBufferInfo);
                writeToFile = true;
            }
        }
    }
    //------------------------------------Project线程

    //------------------------------------Camera/Mic线程
    public void writeSample(ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo, int type) {
        synchronized (lock) {
            if (!writeToFile) return;
            switch (type) {
                case 1:
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 && videoFrameCount == 0) {
                        // 第一个关键帧
                        startTime = System.nanoTime() / 1000L;
                        bufferInfo.presentationTimeUs = 0L;
                        muxer.writeSampleData(videoTrackerIndex, byteBuf, bufferInfo);
                        videoFrameCount = 1;
                        callback.onProgress(0);
                    } else if (videoFrameCount > 0) {
                        // 第一个关键帧之后的帧
                        duration = System.nanoTime() / 1000L - startTime;
                        bufferInfo.presentationTimeUs = duration;
                        muxer.writeSampleData(videoTrackerIndex, byteBuf, bufferInfo);
                        videoFrameCount++;
                        callback.onProgress(duration);
                    } else {
                        // 第一个关键帧之前的帧
                    }
                    break;
                case 2:
                    if (videoFrameCount > 0) {
                        bufferInfo.presentationTimeUs = System.nanoTime() / 1000L - startTime;
                        muxer.writeSampleData(audioTrackerIndex, byteBuf, bufferInfo);
                    }
                    break;
            }
        }
    }
    //------------------------------------Camera/Mic线程

    public void setClipMuxerCallback(ClipMuxerCallback callback) {
        this.callback = callback;
    }

    public interface ClipMuxerCallback{
        void onProgress(long duration);
    }

}
