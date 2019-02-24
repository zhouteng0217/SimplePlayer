package com.zt.listvideo;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;

/**
 * 原生mediaplayer实现的封装的播放器
 */

public class BasePlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener, TextureView.SurfaceTextureListener {

    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_BUFFERING = 3;  //缓冲中
    public static final int STATE_BUFFERING_START = 4; //暂停播放开始缓冲更多数据
    public static final int STATE_BUFFERING_END = 5; //缓冲了足够的数据重新开始播放
    public static final int STATE_PLAYING = 6;
    public static final int STATE_PAUSED = 7;
    public static final int STATE_COMPLETED = 8;

    private int currentState = BasePlayer.STATE_IDLE;

    private StateCallback stateCallback;
    private MediaPlayer.OnBufferingUpdateListener bufferingUpdateListener;
    private MediaPlayer.OnSeekCompleteListener seekCompleteListener;

    private MediaPlayer mediaPlayer;
    private String url;
    private SurfaceTexture savedSurfaceTexture;
    private TextureView textureView;

    protected int bufferedPercentage;

    public void setVideoPath(String url) {
        this.url = url;
    }

    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
        textureView.setSurfaceTextureListener(this);
    }

    private void prepare() {
        try {
            onStateChange(STATE_PREPARING);

            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.prepareAsync();
            mediaPlayer.setSurface(new Surface(savedSurfaceTexture));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        release();
        mediaPlayer = null;
        resetSurface();
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            onStateChange(STATE_IDLE);
        }
    }

    public void resetSurface() {
        savedSurfaceTexture = null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        onStateChange(STATE_PREPARED);
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onStateChange(STATE_COMPLETED);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferedPercentage = percent;
        onStateChange(STATE_BUFFERING);
        if (bufferingUpdateListener != null) {
            bufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (seekCompleteListener != null) {
            seekCompleteListener.onSeekComplete(mp);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        onStateChange(STATE_ERROR);
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                onStateChange(STATE_BUFFERING_START);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                onStateChange(STATE_BUFFERING_END);
                break;
        }
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            prepare();
        } else {
            textureView.setSurfaceTexture(savedSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return savedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void play() {
        mediaPlayer.start();
        onStateChange(STATE_PLAYING);
    }

    public void pause() {
        mediaPlayer.pause();
        onStateChange(STATE_PAUSED);
    }

    private void onStateChange(int state) {
        currentState = state;
        if (stateCallback != null) {
            stateCallback.onStateChange(state);
        }
    }

    public void seekTo(int msec) {
        mediaPlayer.seekTo(msec);
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getBufferedPercentage() {
        return bufferedPercentage;
    }

    public int getDuration() {
        int duration = -1;
        try {
            duration = mediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }

    public int getCurrentPosition() {
        int position = 0;
        try {
            position = mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return position;
    }

    public boolean isInPlaybackState() {
        return (mediaPlayer != null &&
                currentState != STATE_ERROR &&
                currentState != STATE_IDLE &&
                currentState != STATE_PREPARING);
    }


    public void setStateCallback(StateCallback stateCallback) {
        this.stateCallback = stateCallback;
    }

    public void setBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener bufferingUpdateListener) {
        this.bufferingUpdateListener = bufferingUpdateListener;
    }

    public void setSeekCompleteListener(MediaPlayer.OnSeekCompleteListener seekCompleteListener) {
        this.seekCompleteListener = seekCompleteListener;
    }

    public int getCurrentState() {
        return currentState;
    }

    public String getUrl() {
        return url;
    }
}
