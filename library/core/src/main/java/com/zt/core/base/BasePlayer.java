package com.zt.core.base;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.RawRes;
import android.text.TextUtils;

import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.OnVideoSizeChangedListener;

import java.io.IOException;
import java.util.Map;

public abstract class BasePlayer implements IMediaPlayer {

    protected static final int MSG_RELEASE = 101;
    protected static final int MSG_DESTORY = 102;

    protected int currentState = STATE_IDLE;

    protected Uri uri;
    protected Map<String, String> headers;
    protected @RawRes int rawId;
    protected String assetFileName;

    protected OnStateChangedListener onStateChangeListener;
    protected OnVideoSizeChangedListener onVideoSizeChangedListener;

    protected Context context;

    protected boolean isPrepared = false; //播放器是否已经prepared了

    protected PlayerConfig playerConfig;

    protected int bufferedPercentage;

    protected MediaPlayerHandler mediaPlayerHandler; //用于处理release等耗时操作

    protected PlayerAudioManager playerAudioManager;

    protected class MediaPlayerHandler extends Handler {

        private MediaPlayerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RELEASE:
                    releaseImpl();
                    break;
                case MSG_DESTORY:
                    destroyImpl();
                    break;
            }
        }
    }

    public BasePlayer(Context context) {

        //使用application的context避免内存泄露
        this.context = context.getApplicationContext();

        playerAudioManager = new PlayerAudioManager(this.context, this);

        HandlerThread handlerThread = new HandlerThread(this.getClass().getName());
        handlerThread.start();
        mediaPlayerHandler = new MediaPlayerHandler(handlerThread.getLooper());
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {
        this.playerConfig = playerConfig;
    }

    //region DataSource

    //设置视频播放路径 (网络路径和本地文件路径)
    protected void setVideoPath(String url, Map<String, String> headers) {
        if (!TextUtils.isEmpty(url)) {
            uri = Uri.parse(url);
        }
        this.headers = headers;
    }

    //设置raw下视频的路径
    protected void setVideoRawPath(@RawRes int rawId) {
        this.rawId = rawId;
    }

    //设置assets下视频的路径
    protected void setVideoAssetPath(String assetFileName) {
        this.assetFileName = assetFileName;
    }

    //endregion

    public void setOnStateChangeListener(OnStateChangedListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    public int getCurrentState() {
        return currentState;
    }

    //获取最大音量
    public int getStreamMaxVolume() {
        return playerAudioManager.getStreamMaxVolume();
    }

    //获取当前音量
    public int getStreamVolume() {
        return playerAudioManager.getStreamVolume();
    }

    //设置音量
    public void setStreamVolume(int value) {
        playerAudioManager.setStreamVolume(value);
    }

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

    @Override
    public void play() {
        playerAudioManager.requestAudioFocus();
        playImpl();
        onStateChange(STATE_PLAYING);
    }

    @Override
    public void pause() {
        if (!isPlaying()) {
            return;
        }
        pauseImpl();
        onStateChange(STATE_PAUSED);
    }

    public void initPlayer() {
        isPrepared = false;
        if (noDataSource()) {
            return;
        }
        initPlayerImpl();
        onStateChange(STATE_PREPARING);
    }

    protected boolean noDataSource() {
        return uri == null && rawId == 0 && TextUtils.isEmpty(assetFileName);
    }

    @Override
    public boolean isPlaying() {
        return isPrepared && isPlayingImpl();
    }

    @Override
    public void release() {
        playerAudioManager.abandonAudioFocus();
        isPrepared = false;
        onStateChange(STATE_IDLE);

        Message message = Message.obtain();
        message.what = MSG_RELEASE;
        mediaPlayerHandler.sendMessage(message);
    }

    @Override
    public void destroy() {
        playerAudioManager.destroy();
        isPrepared = false;
        onStateChange(STATE_IDLE);

        Message message = Message.obtain();
        message.what = MSG_DESTORY;
        mediaPlayerHandler.sendMessage(message);
    }

    @Override
    public void seekTo(long position) {
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
        playerAudioManager.abandonAudioFocus();
        onStateChange(STATE_COMPLETED);
    }

    //onBufferingUpdate具体实现
    protected void onBufferingUpdateImpl(int percent) {
        bufferedPercentage = percent;
    }

    protected void onSeekCompleteImpl() {

    }

    protected boolean onErrorImpl() {
        onStateChange(STATE_ERROR);
        return true;
    }

    protected void onBufferingStart() {
        onStateChange(STATE_BUFFERING_START);
    }

    protected void onBufferingEnd() {
        onStateChange(STATE_BUFFERING_END);
    }

    protected void onVideoSizeChangedImpl(int width, int height) {
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(width, height);
        }
    }

    protected boolean isLooping() {
        return playerConfig != null && playerConfig.looping;
    }

    //设置播放数据源
    protected abstract void setDataSource() throws IOException;

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

    //获取视频内容高宽比
    public abstract float getAspectRation();

    //获取视频内容宽度
    public abstract int getVideoWidth();

    //获取视频内容高度
    public abstract int getVideoHeight();

    //获取当前播放进度
    public abstract long getCurrentPosition();

    //获取播放总进度
    public abstract long getDuration();

    //跳转到指定播放位置
    protected abstract void seekToImpl(long position);

    //针对某些播放器内核，比如IjkPlayer，进行的一些额外设置
    public abstract void setOptions();

    //是否支持硬解码
    protected abstract void setEnableMediaCodec(boolean isEnable);

    //是否启用OpenSL ES
    protected abstract void setEnableOpenSLES(boolean isEnable);

    //获取缓冲网速
    public abstract long getTcpSpeed();

}
