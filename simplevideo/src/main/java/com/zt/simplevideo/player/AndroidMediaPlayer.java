package com.zt.simplevideo.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;

import com.zt.simplevideo.base.BasePlayer;
import com.zt.simplevideo.util.VideoUtils;

/**
 * 原生mediaplayer实现的封装的播放器
 */

public class AndroidMediaPlayer extends BasePlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener, TextureView.SurfaceTextureListener {

    private static final int MSG_RELEASE = 101;
    private static final int MSG_DESTORY = 102;

    private MediaPlayer mediaPlayer;
    private SurfaceTexture savedSurfaceTexture;

    protected int bufferedPercentage;

    private MediaPlayerHandler mediaPlayerHandler; //用于处理mediaplayer的release等耗时操作

    private boolean isPrepared = false; //播放器是否已经prepared了

    private class MediaPlayerHandler extends Handler {
        private MediaPlayerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mediaPlayer == null) {
                return;
            }
            switch (msg.what) {
                case MSG_RELEASE:
                    mediaPlayer.release();
                    break;
                case MSG_DESTORY:
                    mediaPlayer.release();
                    resetSurface();
                    mediaPlayer = null;
                    break;
            }
        }
    }

    public AndroidMediaPlayer(Context context) {
        super(context);
        HandlerThread handlerThread = new HandlerThread(this.getClass().getName());
        handlerThread.start();
        mediaPlayerHandler = new MediaPlayerHandler(handlerThread.getLooper());
    }

    private void prepare() {
        try {

            isPrepared = false;

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

    @Override
    public void destroy() {
        abandonAudioFocus();
        VideoUtils.removeScreenOn(context);
        isPrepared = false;
        onStateChange(STATE_IDLE);
        Message message = Message.obtain();
        message.what = MSG_DESTORY;
        mediaPlayerHandler.sendMessage(message);
    }

    @Override
    public void release() {
        abandonAudioFocus();
        VideoUtils.removeScreenOn(context);
        isPrepared = false;
        onStateChange(STATE_IDLE);
        Message message = Message.obtain();
        message.what = MSG_RELEASE;
        mediaPlayerHandler.sendMessage(message);
    }

    @Override
    public void resetSurface() {
        savedSurfaceTexture = null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        onStateChange(STATE_PREPARED);
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        abandonAudioFocus();
        VideoUtils.removeScreenOn(context);
        onStateChange(STATE_COMPLETED);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferedPercentage = percent;
        onStateChange(STATE_BUFFERING);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        onStateChange(STATE_SEEK_END);
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
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(width, height);
        }
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

    @Override
    public void play() {
        requestAudioFocus();
        VideoUtils.keepScreenOn(context);
        mediaPlayer.start();
        onStateChange(STATE_PLAYING);
    }

    @Override
    public void pause() {
        if (!isPlaying()) {
            return;
        }
        VideoUtils.removeScreenOn(context);
        mediaPlayer.pause();
        onStateChange(STATE_PAUSED);
    }

    private void onStateChange(int state) {
        currentState = state;
        if (onStateChangeListener != null) {
            onStateChangeListener.onStateChange(state);
        }
    }

    public void seekTo(int msec) {
        onStateChange(STATE_SEEK_START);
        mediaPlayer.seekTo(msec);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer != null && isPrepared && mediaPlayer.isPlaying();
    }

    public int getBufferedPercentage() {
        return bufferedPercentage;
    }

    @Override
    public int getDuration() {
        int duration = -1;
        try {
            duration = mediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }

    @Override
    public int getCurrentPosition() {
        int position = 0;
        try {
            position = mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return position;
    }

    @Override
    public float getAspectRation() {
        return mediaPlayer == null || mediaPlayer.getVideoHeight() == 0 ? 1.0f : (float) mediaPlayer.getVideoWidth() / mediaPlayer.getVideoHeight();
    }

    @Override
    public int getVideoWidth() {
        return mediaPlayer == null || !isPrepared ? 0 : mediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mediaPlayer == null || !isPrepared ? 0 : mediaPlayer.getVideoHeight();
    }
}
