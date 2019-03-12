package com.zt.core.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.zt.core.base.BasePlayer;
import com.zt.core.util.VideoUtils;

/**
 * 原生mediaplayer实现的封装的播放器
 */

public class AndroidPlayer extends BasePlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {

    protected static final int MSG_RELEASE = 101;
    protected static final int MSG_DESTORY = 102;

    protected MediaPlayer mediaPlayer;

    protected MediaPlayerHandler mediaPlayerHandler; //用于处理mediaplayer的release等耗时操作


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
                    mediaPlayer = null;
                    break;
            }
        }
    }

    public AndroidPlayer(Context context) {
        super(context);
        HandlerThread handlerThread = new HandlerThread(this.getClass().getName());
        handlerThread.start();
        mediaPlayerHandler = new MediaPlayerHandler(handlerThread.getLooper());
    }

    @Override
    public void initPlayerImpl() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(isLooping());
            mediaPlayer.setDataSource(context, uri, headers);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {

        }
    }

    @Override
    protected void destroyImpl() {
        Message message = Message.obtain();
        message.what = MSG_DESTORY;
        mediaPlayerHandler.sendMessage(message);
    }

    @Override
    protected void releaseImpl() {
        Message message = Message.obtain();
        message.what = MSG_RELEASE;
        mediaPlayerHandler.sendMessage(message);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        onPreparedImpl();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onCompletionImpl();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        onBufferingUpdateImpl(percent);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        onSeekCompleteImpl();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return onErrorImpl();
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
        onVideoSizeChangedImpl(width, height);
    }

    @Override
    public void setSurface(Surface surface) {
        if (mediaPlayer != null) {
            mediaPlayer.setSurface(surface);
        }
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(holder);
        }
    }

    @Override
    protected void playImpl() {
        mediaPlayer.start();
    }

    @Override
    protected void pauseImpl() {
        mediaPlayer.pause();
    }

    @Override
    protected void seekToImpl(int msec) {
        mediaPlayer.seekTo(msec);
    }

    @Override
    protected boolean isPlayingImpl() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
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
        return mediaPlayer == null ? 0 : mediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mediaPlayer == null ? 0 : mediaPlayer.getVideoHeight();
    }

    @Override
    public void setOptions() {

    }

    @Override
    public void setEnableMediaCodec(boolean isEnable) {

    }

    @Override
    protected void setEnableOpenSLES(boolean isEnable) {

    }

    @Override
    public long getTcpSpeed() {
        return 0;
    }
}
