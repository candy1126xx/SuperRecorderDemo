package com.candy1126xx.superrecorder.record;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import static android.media.AudioFormat.CHANNEL_OUT_FRONT_CENTER;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.AudioRecord.STATE_INITIALIZED;
import static android.media.MediaRecorder.AudioSource.MIC;

/**
 * Created by Administrator on 2017/6/30 0030.
 */

public class AudioManager {

    private AudioRecord audioRecord;

    private int bufferSizeInBytes;

    private AudioManagerCallback callback;

    public void openMic() {
        bufferSizeInBytes = AudioRecord.getMinBufferSize(44100, CHANNEL_OUT_FRONT_CENTER, ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MIC, 44100, CHANNEL_OUT_FRONT_CENTER, ENCODING_PCM_16BIT, bufferSizeInBytes);
        int state = audioRecord.getState();
        if (state == STATE_INITIALIZED) {
            if (callback != null) callback.onOpenMicSuccess();
        } else {
            if (callback != null) callback.onOpenMicFail();
        }
    }

    public void closeMic() {
        audioRecord.release();
        audioRecord = null;
    }

    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public int getBufferSizeInBytes() {
        return bufferSizeInBytes;
    }

    public void setCallback(AudioManagerCallback callback) {
        this.callback = callback;
    }

    public interface AudioManagerCallback {
        void onOpenMicSuccess();

        void onOpenMicFail();
    }

}
