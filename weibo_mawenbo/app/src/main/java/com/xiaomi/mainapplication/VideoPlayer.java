package com.xiaomi.mainapplication;

import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.VideoView;

import java.io.IOException;

public class VideoPlayer {
    private static final String TAG = "VideoPlayer";
    private VideoView videoView;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    public VideoPlayer(VideoView videoView) {
        this.videoView = videoView;
        this.mediaPlayer = new MediaPlayer();
    }

    public void playVideo(String videoPath) {
        try {
            mediaPlayer.setDataSource(videoPath);
            Log.d(TAG, "setplayVideo: " + videoPath);
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "MediaPlayer error: " + what + ", extra: " + extra);
                    return false;
                }

            });
            // 准备
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "onPrepared");
                    // 在这里开始播放视频
                    SurfaceHolder holder = videoView.getHolder();
                    Log.d(TAG, "onPrepared: holder");
                    mp.setDisplay(holder);
                    Log.d(TAG, "setDisplay");
                    mp.start();
                    Log.d(TAG, "start");
                    // 打印视频相关信息
                    Log.d(TAG, "Video duration: " + mp.getDuration());
                    Log.d(TAG, "Video width: " + mp.getVideoWidth());
                    Log.d(TAG, "Video height: " + mp.getVideoHeight());
                    isPlaying = true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void pauseVideo() {
        if (isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void resumeVideo() {
        if (!isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public void stopVideo() {
        if (isPlaying) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    public void setLooping(boolean looping) {
        mediaPlayer.setLooping(looping);
    }

    public int getVideoWidth() {
        return mediaPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return mediaPlayer.getVideoHeight();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        mediaPlayer.setOnPreparedListener(listener);
    }
}
