package com.candy1126xx.superrecorder;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017/6/30 0030.
 */

public class AudioMission {

    private AudioManager audioManager;

    private AudioCodecRecorder audioCodec;

    private AVMuxer muxer;

    private ByteBuffer buffer;

    private int read;

    private boolean isRunning;

    public AudioMission(AudioManager audioManager, AudioCodecRecorder audioCodec, AVMuxer muxer) {
        this.audioManager = audioManager;
        this.audioCodec = audioCodec;
        this.muxer = muxer;
        this.buffer = ByteBuffer.allocateDirect(this.audioManager.getBufferSizeInBytes());

        this.audioManager.getAudioRecord().startRecording();
        this.isRunning = true;

        while (isRunning) {
            this.buffer.clear();
            this.read = this.audioManager.getAudioRecord().read(buffer, buffer.capacity());

            if (this.read > 0) {
                this.buffer.position(this.read);
                this.buffer.flip();

//                this.audioCodec.encode(this.buffer);
            }
        }
    }

    public void finish() {
        isRunning = false;
    }

}