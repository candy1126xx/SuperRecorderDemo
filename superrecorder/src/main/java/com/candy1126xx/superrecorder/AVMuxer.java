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
    private int videoTrackerIndex = -1;
    private int audioTrackerIndex = -1;

    private boolean writeToFile;

    private long startTime;

    private int videoFrameCount;
    private int audioFrameCount;

    public AVMuxer(File outputFile) {
        try {
            if (outputFile.exists()) outputFile.delete();
            outputFile.createNewFile();
            muxer = new MediaMuxer(outputFile.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------以上代码在主线程
    //------------------------------------以下代码在Camera线程

    private void start() {
        this.startTime = System.nanoTime() / 1000L;
        this.writeToFile = true;
    }

    public void stop() {
        writeToFile = false;
        muxer.release();
    }

    public synchronized void addTrack(MediaFormat format, int type) {
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
            start();
        }
    }

    public synchronized void writeSample(ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo, int type) {
        if (!writeToFile) return;
        switch (type) {
            case 1:
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 || videoFrameCount > 0) {
                    bufferInfo.presentationTimeUs = System.nanoTime() / 1000L - startTime;
                    muxer.writeSampleData(videoTrackerIndex, byteBuf, bufferInfo);
                    videoFrameCount++;
                }
                break;
            case 2:
                bufferInfo.presentationTimeUs = System.nanoTime() / 1000L - startTime;
                muxer.writeSampleData(audioTrackerIndex, byteBuf, bufferInfo);
                audioFrameCount++;
                break;
        }
    }

}
