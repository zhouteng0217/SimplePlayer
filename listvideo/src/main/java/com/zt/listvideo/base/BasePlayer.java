package com.zt.listvideo.base;

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

import com.zt.listvideo.listener.StateCallback;
import com.zt.listvideo.util.VideoUtils;

/**
 * 原生mediaplayer实现的封装的播放器
 */

public class BasePlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener, TextureView.SurfaceTextureListener {

    private static final int MSG_RELEASE = 101;
    private static final int MSG_DESTORY = 102;

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
    public static final int STATE_SEEK_START = 9;  //开始seek
    public static final int STATE_SEEK_END = 10;   //seek结束

    private int currentState = BasePlayer.STATE_IDLE;

    private StateCallback stateCallback;
    private MediaPlayer mediaPlayer;
    private String url;
    private SurfaceTexture savedSurfaceTexture;
    private TextureView textureView;

    protected int bufferedPercentage;

    private MediaPlayerHandler mediaPlayerHandler; //用于处理mediaplayer的release等耗时操作

    protected AudioManager audioManager;

    protected Context context;

    private boolean isPrepared = false; //播放器是否已经prepared了

    private MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener;

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

    public BasePlayer(Context context) {
        this.context = context;
        HandlerThread handlerThread = new HandlerThread(this.getClass().getName());
        handlerThread.start();
        mediaPlayerHandler = new MediaPlayerHandler(handlerThread.getLooper());
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setVideoPath(String url) {
        this.url = url;
    }

    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
        textureView.setSurfaceTextureListener(this);
    }

    //region audiomanager
    private void requestAudioFocus() {
        audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    private void abandonAudioFocus() {
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    public int getStreamMaxVolume() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public int getStreamVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void setStreamVolume(int value) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
    }

    //endregion

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

    public void destroy() {
        abandonAudioFocus();
        VideoUtils.removeScreenOn(context);
        isPrepared = false;
        onStateChange(STATE_IDLE);
        Message message = Message.obtain();
        message.what = MSG_DESTORY;
        mediaPlayerHandler.sendMessage(message);
    }

    public void release() {
        abandonAudioFocus();
        VideoUtils.removeScreenOn(context);
        isPrepared = false;
        onStateChange(STATE_IDLE);
        Message message = Message.obtain();
        message.what = MSG_RELEASE;
        mediaPlayerHandler.sendMessage(message);
    }

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
            onVideoSizeChangedListener.onVideoSizeChanged(mp, width, height);
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

    public void play() {
        requestAudioFocus();
        VideoUtils.keepScreenOn(context);
        mediaPlayer.start();
        onStateChange(STATE_PLAYING);
    }

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
        if (stateCallback != null) {
            stateCallback.onStateChange(state);
        }
    }

    public void seekTo(int msec) {
        onStateChange(STATE_SEEK_START);
        mediaPlayer.seekTo(msec);
    }

    public boolean isPlaying() {
        return mediaPlayer != null && isPrepared && mediaPlayer.isPlaying();
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

    public int getCurrentState() {
        return currentState;
    }

    public String getUrl() {
        return url;
    }

    //视频内容宽高比
    public float getAspectRation() {
        return mediaPlayer == null || mediaPlayer.getVideoHeight() == 0 ? 1.0f : (float) mediaPlayer.getVideoWidth() / mediaPlayer.getVideoHeight();
    }

    public int getVideoWidth() {
        return mediaPlayer == null || !isPrepared ? 0 : mediaPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return mediaPlayer == null || !isPrepared ? 0 : mediaPlayer.getVideoHeight();
    }

    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }
}
