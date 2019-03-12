package com.zt.core.base;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.PlayerListener;
import com.zt.core.util.VideoUtils;

import java.util.Map;

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

    protected Uri uri;

    protected OnStateChangedListener onStateChangeListener;
    protected PlayerListener playerListener;

    protected AudioManager audioManager;
    protected Context context;

    protected boolean isPrepared = false; //播放器是否已经prepared了

    protected PlayerConfig playerConfig;

    protected Map<String, String> headers;

    protected int bufferedPercentage;

    public BasePlayer(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {
        this.playerConfig = playerConfig;
    }

    public void setVideoPath(String url) {
        setVideoPath(url, null);
    }

    protected void setVideoPath(String url, Map<String, String> headers) {
        if (!TextUtils.isEmpty(url)) {
            uri = Uri.parse(url);
        }
        this.headers = headers;
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

    protected void onStateChange(int state) {
        currentState = state;
        if (onStateChangeListener != null) {
            onStateChangeListener.onStateChange(state);
        }
    }

    public void play() {
        requestAudioFocus();
        VideoUtils.keepScreenOn(context);
        onStateChange(STATE_PLAYING);
        playImpl();
    }

    public void pause() {
        if (!isPlaying()) {
            return;
        }
        VideoUtils.removeScreenOn(context);
        onStateChange(STATE_PAUSED);
        pauseImpl();
    }

    public void initPlayer() {
        isPrepared = false;
        if (uri == null) {
            return;
        }
        initPlayerImpl();
        onStateChange(STATE_PREPARING);
    }

    public boolean isPlaying() {
        return isPrepared && isPlayingImpl();
    }

    public void release() {
        abandonAudioFocus();
        VideoUtils.removeScreenOn(context);
        isPrepared = false;
        onStateChange(STATE_IDLE);
        releaseImpl();
    }

    public void destroy() {
        abandonAudioFocus();
        VideoUtils.removeScreenOn(context);
        isPrepared = false;
        onStateChange(STATE_IDLE);
        destroyImpl();
    }

    public void seekTo(int position) {
        onStateChange(STATE_SEEK_START);
        seekToImpl(position);
    }

    //prepare成功后的具体实现
    protected void onPreparedImpl() {
        isPrepared = true;
        onStateChange(STATE_PREPARED);
        play();
    }

    //onCompletion的具体实现
    protected void onCompletionImpl() {
        abandonAudioFocus();
        VideoUtils.removeScreenOn(context);
        onStateChange(STATE_COMPLETED);
    }

    //onBufferingUpdate具体实现
    protected void onBufferingUpdateImpl(int percent) {
        bufferedPercentage = percent;
        onStateChange(STATE_BUFFERING);
    }

    protected void onSeekCompleteImpl() {
        onStateChange(STATE_SEEK_END);
    }

    protected boolean onErrorImpl() {
        onStateChange(STATE_ERROR);
        return true;
    }

    protected void onVideoSizeChangedImpl(int width,int height) {
        if (playerListener != null) {
            playerListener.onVideoSizeChanged(width, height);
        }
    }

    //初始化播放器
    protected abstract void initPlayerImpl();

    //是否正在播放
    protected abstract boolean isPlayingImpl();

    //播放
    protected abstract void playImpl();

    //暂停
    protected abstract void pauseImpl();

    //释放播放器
    protected abstract void releaseImpl();

    //销毁播放器
    protected abstract void destroyImpl();

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
    protected abstract void seekToImpl(int position);

    //设置TextureView渲染界面
    public abstract void setSurface(Surface surface);

    //设置SurfaceView渲染界面
    public abstract void setDisplay(SurfaceHolder holder);

    //针对某些播放器内核，比如IjkPlayer，进行的一些额外设置
    public abstract void setOptions();

    //是否支持硬解码
    protected abstract void setEnableMediaCodec(boolean isEnable);

    //是否启用OpenSL ES
    protected abstract void setEnableOpenSLES(boolean isEnable);

    //获取缓冲网速
    public abstract long getTcpSpeed();

}
