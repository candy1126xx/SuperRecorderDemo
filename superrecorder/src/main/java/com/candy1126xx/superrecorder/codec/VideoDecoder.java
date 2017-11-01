package com.candy1126xx.superrecorder.codec;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.Surface;

import com.candy1126xx.superrecorder.model.Size;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/10/26 0026.
 */

public class VideoDecoder {

    private String videoPath;

    private Surface surface;

    private MediaExtractor extractor;

    private MediaFormat format;

    private MediaCodec codec;

    private Size resolution;

    private MediaCodec.BufferInfo decoderBufferInfo = new MediaCodec.BufferInfo(); // Decoder解码后Buffer的信息

    public VideoDecoder(String videoPath, Surface surface) {
        this.videoPath = videoPath;
        this.surface = surface;
    }

    public void analysis() {
        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(videoPath);
            int numTracks = extractor.getTrackCount();
            for (int i = 0; i < numTracks; i++) {
                MediaFormat f = extractor.getTrackFormat(i);
                String mime = f.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video")) {
                    extractor.selectTrack(i);
                    format = f;
                    codec = MediaCodec.createDecoderByType(mime);
                    codec.configure(format, surface, null, 0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decode() {
        if (codec == null) return;
        codec.start();
        boolean outputDone = false; // 当输出END_FLAG时置为true
        boolean inputDone = false; // 当readSampleData返回-1时置为true
        while (!outputDone) {
            if (!inputDone) {
                int inputIndex = codec.dequeueInputBuffer(0);
                if (inputIndex >= 0) {
                    ByteBuffer inputBuffer = codec.getInputBuffers()[inputIndex];
                    inputBuffer.clear();

                    int sampleSize = extractor.readSampleData(inputBuffer, 0);
                    if (sampleSize >= 0) {
                        codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.getSampleTime(), extractor.getSampleFlags());
                        extractor.advance();
                    } else {
                        codec.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                    }
                }
            }

            if (!outputDone) {
                int outputIndex = codec.dequeueOutputBuffer(decoderBufferInfo, 0);
                if (outputIndex >= 0) {
                    if ((decoderBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }
                    codec.releaseOutputBuffer(outputIndex, true);
                }
            }
        }
    }

    public Size getResolution() {
        if (resolution == null) {
            int width = format.getInteger(MediaFormat.KEY_WIDTH);
            if (format.containsKey("crop-left") && format.containsKey("crop-right")) {
                width = format.getInteger("crop-right") + 1 - format.getInteger("crop-left");
            }
            int height = format.getInteger(MediaFormat.KEY_HEIGHT);
            if (format.containsKey("crop-top") && format.containsKey("crop-bottom")) {
                height = format.getInteger("crop-bottom") + 1 - format.getInteger("crop-top");
            }
            resolution = new Size(width, height);
        }
        return resolution;
    }

    public MediaCodec getCodec() {
        return codec;
    }

}
