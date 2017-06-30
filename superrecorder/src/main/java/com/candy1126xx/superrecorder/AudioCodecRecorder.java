package com.candy1126xx.superrecorder;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/6/30 0030.
 */

public class AudioCodecRecorder {

    private AVMuxer muxer;

    private MediaFormat audioFormat;

    private MediaCodec encoder;

    private ByteBuffer[] encoderInputBuffers;
    private ByteBuffer[] encoderOutputBuffers;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    private long startTime; // 这个时间不重要，只要不违背时间序列就可以，因为在muxer中会重新计算

    private AudioCodecRecorderCallback callback;

    public void init(AVMuxer muxer) {
        this.muxer = muxer;

        audioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 44100, 1);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 100 * 1024);

        try {
            encoder = MediaCodec.createEncoderByType("audio/mp4a-latm");
            encoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();
            encoderInputBuffers = encoder.getInputBuffers();
            encoderOutputBuffers = encoder.getOutputBuffers();
            startTime = System.nanoTime() / 1000L;
            if (callback != null) callback.onCreateEncoderSuccess();
        } catch (IOException e) {
            if (callback != null) callback.onCreateEncoderFail();
        }
    }

    public void encode(ByteBuffer buffer) {
        int inputBufferIndex = encoder.dequeueInputBuffer(0);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = encoderInputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buffer);
            encoder.queueInputBuffer(inputBufferIndex, 0, inputBuffer.limit(), System.nanoTime() / 1000L - startTime, 0);

            while (encoder != null) {
                int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, 0);

                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break;
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    muxer.addTrack(encoder.getOutputFormat(), 2);
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    encoderOutputBuffers = encoder.getOutputBuffers();
                } else if (encoderStatus >= 0) {
                    if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                        encoder.stop();
                        encoder.release();
                        encoder = null;
                    } else {
//                        muxer.writeSample(encoderOutputBuffers[encoderStatus], bufferInfo, 2);
                        encoder.releaseOutputBuffer(encoderStatus, false);
                    }
                }
            }
        }
    }

    public void close() {
        if (encoder != null) encoder.signalEndOfInputStream();
    }

    public interface AudioCodecRecorderCallback {
        void onCreateEncoderSuccess();

        void onCreateEncoderFail();
    }

}
