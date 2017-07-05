package com.candy1126xx.superrecorder.record;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;

import com.candy1126xx.superrecorder.model.Clip;
import com.candy1126xx.superrecorder.model.ProjectParameter;
import com.candy1126xx.superrecorder.model.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import static android.media.MediaMetadataRetriever.METADATA_KEY_DURATION;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH;
import static android.media.MediaMetadataRetriever.OPTION_NEXT_SYNC;

/**
 * Created by Administrator on 2017/6/30 0030.
 */

public class ProjectManager implements ClipMuxer.ClipMuxerCallback {

    //---------------------------------生成Clip
    private ClipMuxer currentMuxer;
    //---------------------------------生成Clip

    //---------------------------------合成
    private MergeMuxer mergeMuxer;
    //---------------------------------合成

    //---------------------------------合成复用
    private long videoDuration;
    private long audioDuration;

    private ByteBuffer inputBuffer;

    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    private int sampleSize;

    private MediaExtractor currentExtractor;
    //---------------------------------合成复用

    //---------------------------------Project管理
    private LinkedList<Clip> clips = new LinkedList<>();

    private String rootPath; // Project的根目录

    private String tempPath; // 存放Clips的文件夹

    private String resultPath; // 输出文件的路径

    private String avatarPath; // 输出封面的路径
    //---------------------------------Project管理

    private ProjectManagerCallback callback;

    public ProjectManager(ProjectParameter parameter) {
        this.rootPath = parameter.getOutputPath();
        this.tempPath = rootPath + File.separator + parameter.getTitle();
        this.resultPath = tempPath + ".mp4";
        this.avatarPath = tempPath + ".png";

        File rootFile = new File(rootPath);
        if (!rootFile.exists()) rootFile.mkdirs();
        File tempFile = new File(tempPath);
        if (tempFile.exists()) tempFile.delete();
        tempFile.mkdirs();
    }

    public void createNewClip() {
        Clip clip = new Clip();
        clip.path = tempPath + File.separator + clips.size() + ".mp4";
        clip.duration = 0L;
        clip.startTime = calTotalDuration();
        clip.endTime = clip.startTime;
        currentMuxer = new ClipMuxer(new File(clip.path));
        currentMuxer.setClipMuxerCallback(this);
        clips.addLast(clip);
        if (callback != null) callback.onNewClipCreated(clip);
    }

    private long calTotalDuration() {
        long d = 0;
        for (Clip clip : clips) {
            d += clip.duration;
        }
        return d;
    }

    public void stopCurrentClip() {
        if (currentMuxer != null) {
            currentMuxer.stop();
            currentMuxer = null;
            if (callback != null) callback.onCurrentClipStop();
            if (clips.getLast().duration < 1000) deleteCurrentClip(); // 如果Clip太短，无法保证写入了有效数据
        }
    }

    public void deleteCurrentClip() {
        if (clips.size() <= 0) return;
        File currentClip = new File(clips.getLast().path);
        if (currentClip.exists() && currentClip.delete()) {
            clips.removeLast();
            if (callback != null) callback.onCurrentClipDelete();
        }
    }

    public void mergeAllClips() {
        if (callback != null) callback.onStartMerge();

        if (currentMuxer != null) stopCurrentClip();
        mergeMuxer = new MergeMuxer(new File(resultPath));
        for (Clip clip : clips) {
            mergeClip(clip.path);
        }
        mergeMuxer.stop();
        mergeMuxer = null;
        inputBuffer = null;
        bufferInfo = null;
        clips = null;

        if (callback != null) callback.onAllClipsMerged(produceVideo());
    }

    private Video produceVideo() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(resultPath);
        Bitmap avatarBitmap = retriever.getFrameAtTime(0, OPTION_NEXT_SYNC);
        String duration = retriever.extractMetadata(METADATA_KEY_DURATION);
        String width = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT);
        retriever.release();

        if (avatarBitmap == null) {
            return null;
        } else {
            File file = new File(avatarPath);
            OutputStream stream = null;
            try {
                Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
                int quality = 100;
                if (file.exists() && !file.delete()) return null;
                if (!file.createNewFile()) return null;
                stream = new FileOutputStream(file);
                avatarBitmap.compress(format, quality, stream);
                avatarBitmap.recycle();
                avatarBitmap = null;
            } catch (IOException e) {
                return null;
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                        stream = null;
                    }
                } catch (IOException e) {

                }
            }
        }

        Video video = new Video();
        video.coverPath = avatarPath;
        video.width = width;
        video.height = height;
        video.outputPath = resultPath;
        video.duration = duration;

        return video;
    }

    private void mergeClip(String clipPath) {
        try {
            currentExtractor = new MediaExtractor();
            currentExtractor.setDataSource(clipPath);
            int numTracks = currentExtractor.getTrackCount();

            // 先加入轨道
            for (int i = 0; i < numTracks; i++) {
                currentExtractor.selectTrack(i);
                MediaFormat format = currentExtractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("video")) {
                    mergeMuxer.addTrack(format, 1);
                } else if (format.getString(MediaFormat.KEY_MIME).startsWith("audio")) {
                    mergeMuxer.addTrack(format, 2);
                }
                currentExtractor.unselectTrack(i);
            }

            for (int i = 0; i < numTracks; i++) {
                currentExtractor.selectTrack(i);
                currentExtractor.seekTo(0, MediaExtractor.SEEK_TO_NEXT_SYNC);
                MediaFormat format = currentExtractor.getTrackFormat(i);
                int maxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                inputBuffer = ByteBuffer.allocate(maxInputSize);

                if (format.getString(MediaFormat.KEY_MIME).startsWith("video")) {
                    while ((sampleSize = currentExtractor.readSampleData(inputBuffer, 0)) >= 0) {
                        bufferInfo.offset = 0;
                        bufferInfo.size = sampleSize;
                        bufferInfo.flags = currentExtractor.getSampleFlags();
                        bufferInfo.presentationTimeUs = videoDuration + currentExtractor.getSampleTime();
                        mergeMuxer.writeSample(inputBuffer, bufferInfo, 1);
                        currentExtractor.advance();
                    }
                    videoDuration += format.getLong(MediaFormat.KEY_DURATION);
                    currentExtractor.unselectTrack(i);
                } else if (format.getString(MediaFormat.KEY_MIME).startsWith("audio")) {
                    while ((sampleSize = currentExtractor.readSampleData(inputBuffer, 0)) >= 0) {
                        bufferInfo.offset = 0;
                        bufferInfo.size = sampleSize;
                        bufferInfo.flags = currentExtractor.getSampleFlags();
                        bufferInfo.presentationTimeUs = audioDuration + currentExtractor.getSampleTime();
                        mergeMuxer.writeSample(inputBuffer, bufferInfo, 2);
                        currentExtractor.advance();
                    }
                    audioDuration += format.getLong(MediaFormat.KEY_DURATION);
                    currentExtractor.unselectTrack(i);
                }
            }

            currentExtractor.release();
            currentExtractor = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClipMuxer getCurrentMuxer() {
        return currentMuxer;
    }

    public void setCallback(ProjectManagerCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onProgress(long duration) {
        Clip last = clips.getLast();
        last.duration = duration;
        last.endTime = last.startTime + last.duration;
        callback.onCurrentClipProgress(duration, calTotalDuration());
    }

    public interface ProjectManagerCallback {
        void onNewClipCreated(Clip newClip);

        void onCurrentClipProgress(long currentClipDuration, long totalDuration);

        void onCurrentClipStop();

        void onCurrentClipDelete();

        void onStartMerge();

        void onAllClipsMerged(Video video);
    }
}
