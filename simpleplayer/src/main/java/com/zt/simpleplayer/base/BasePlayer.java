package com.zt.simpleplayer.base;

import android.content.Context;
import android.media.AudioManager;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.zt.simpleplayer.listener.OnStateChangedListener;
import com.zt.simpleplayer.listener.PlayerListener;

public abstract class BasePlayer {

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

    protected int currentState = STATE_IDLE;

    protected String url;

    protected OnStateChangedListener onStateChangeListener;
    protected PlayerListener playerListener;

    protected AudioManager audioManager;
    protected Context context;

    public BasePlayer(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setVideoPath(String url) {
        this.url = url;
    }

    public void setOnStateChangeListener(OnStateChangedListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    public void setPlayerListener(PlayerListener playerListener) {
        this.playerListener = playerListener;
    }

    public int getCurrentState() {
        return currentState;
    }

    public String getUrl() {
        return url;
    }

    //region audiomanager

    //获取音频焦点
    protected void requestAudioFocus() {
        audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    //丢弃音频焦点
    protected void abandonAudioFocus() {
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    protected AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
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

    //获取最大音量
    public int getStreamMaxVolume() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    //获取当前音量
    public int getStreamVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    //设置音量
    public void setStreamVolume(int value) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
    }

    //endregion

    public boolean isInPlaybackState() {
        return currentState != STATE_ERROR
                && currentState != STATE_IDLE
                && currentState != STATE_PREPARING
                && currentState != STATE_COMPLETED;
    }

    //初始化播放器
    public abstract void initPlayer();

    //是否正在播放
    public abstract boolean isPlaying();

    //播放
    public abstract void play();

    //暂停
    public abstract void pause();

    //释放播放器
    public abstract void release();

    //销毁播放器
    public abstract void destroy();

    //获取视频内容宽高比
    public abstract float getAspectRation();

    //获取视频内容宽度
    public abstract int getVideoWidth();

    //获取视频内容高度
    public abstract int getVideoHeight();

    //获取当前播放进度
    public abstract int getCurrentPosition();

    //获取播放总进度
    public abstract int getDuration();

    //跳转到指定播放位置
    public abstract void seekTo(int position);

    //设置TextureView渲染界面
    public abstract void setSurface(Surface surface);

    //设置SurfaceView渲染界面
    public abstract void setDisplay(SurfaceHolder holder);

}
