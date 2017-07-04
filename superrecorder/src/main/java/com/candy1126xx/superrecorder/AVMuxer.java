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

    //------------------------------------Project线程
    public void stop() {
        writeToFile = false;
        muxer.release();
    }
    //------------------------------------Project线程

    //------------------------------------Camera/Mic线程
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
            writeToFile = true;
        }
    }

    public synchronized void writeSample(ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo, int type) {
        if (!writeToFile) return;
        switch (type) {
            case 1:
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 && videoFrameCount == 0) {
                    // 这是接收到的第一个关键帧
                    startTime = System.nanoTime() / 1000L;
                    bufferInfo.presentationTimeUs = 0L;
                    muxer.writeSampleData(videoTrackerIndex, byteBuf, bufferInfo);
                    videoFrameCount = 1;
                } else if (videoFrameCount > 0){
                    // 第一个关键帧之后的帧
                    bufferInfo.presentationTimeUs = System.nanoTime() / 1000L - startTime;
                    muxer.writeSampleData(videoTrackerIndex, byteBuf, bufferInfo);
                    videoFrameCount++;
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
    //------------------------------------Camera/Mic线程

}
