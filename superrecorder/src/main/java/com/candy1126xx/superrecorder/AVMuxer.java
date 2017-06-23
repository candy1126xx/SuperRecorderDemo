package com.candy1126xx.superrecorder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/6/23 0023.
 */

public class AVMuxer {

    private MediaMuxer muxer;
    private File outputFile;
    private int videoTrackerIndex;

    private boolean writeToFile;

    private long startTime;
    private long recordTime;

    private int frameCount;

    public void init() {
        try {
            outputFile = new File(Environment.getExternalStorageDirectory() + File.separator + "demo.mp4");
            if (outputFile.exists()) outputFile.delete();
            outputFile.createNewFile();
            muxer = new MediaMuxer(outputFile.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------以上代码在主线程
    //------------------------------------以下代码在Camera线程

    public void start(long startTime) {
        this.startTime = startTime;
        this.writeToFile = true;
    }

    public void stop() {
        this.recordTime += System.nanoTime() / 1000L - startTime;
        this.writeToFile = false;
    }

    public void close() {
        muxer.stop();
        muxer.release();
    }

    public void addVideoTrack(MediaFormat format) {
        videoTrackerIndex = muxer.addTrack(format);
        muxer.start();
        start(System.nanoTime() / 1000L);
    }

    public void writeVideoSample(ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        if (!writeToFile) return;
        bufferInfo.presentationTimeUs = System.nanoTime() / 1000L - startTime + recordTime;
        muxer.writeSampleData(videoTrackerIndex, byteBuf, bufferInfo);
        frameCount++;
    }

}
