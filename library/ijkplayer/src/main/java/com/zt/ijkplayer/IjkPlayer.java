package com.zt.ijkplayer;

import android.content.Context;
import android.media.AudioManager;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.zt.core.base.BasePlayer;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkPlayer extends BasePlayer implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener {

    private IjkMediaPlayer mediaPlayer;

    public IjkPlayer(Context context) {
        super(context);
    }

    @Override
    protected void initPlayerImpl() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new IjkMediaPlayer();
            setOptions();
            mediaPlayer.setLooping(isLooping());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);
            setDataSource();
            mediaPlayer.prepareAsync();
        } catch (Exception e) {

        }
    }

    @Override
    protected void setDataSource() throws IOException {
        if (assetFileDescriptor != null) {
            mediaPlayer.setDataSource(new RawDataSourceProvider(assetFileDescriptor));
        } else {
            mediaPlayer.setDataSource(context, uri, headers);
        }
    }

    @Override
    public boolean isPlayingImpl() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
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
    protected void destroyImpl() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void releaseImpl() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
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

    @Override
    public long getCurrentPosition() {
        long position = 0;
        try {
            position = mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return position;
    }

    @Override
    public long getDuration() {
        long duration = -1;
        try {
            duration = mediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }

    @Override
    protected void seekToImpl(long position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
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

    /**
     * 重写该方法来实现设置ijkplayer的各项设置，设置项参考 https://github.com/Bilibili/ijkplayer/blob/master/ijkmedia/ijkplayer/ff_ffplay_options.h
     */
    @Override
    public void setOptions() {
        if (playerConfig != null) {
            setEnableMediaCodec(playerConfig.enableMediaCodec);
            setEnableOpenSLES(playerConfig.enableOpenSLES);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        }
    }

    protected void setEnableMediaCodec(boolean isEnable) {
        if (mediaPlayer != null) {
            int value = isEnable ? 1 : 0;
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", value);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", value);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", value);
        }
    }

    @Override
    protected void setEnableOpenSLES(boolean isEnable) {
        if (mediaPlayer != null) {
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", isEnable ? 1 : 0);
        }
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        onPreparedImpl();
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        onVideoSizeChangedImpl(width, height);
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        onCompletionImpl();
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return onErrorImpl();
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                onBufferingStart();
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                onBufferingEnd();
                break;
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
        onBufferingUpdateImpl(percent);
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        onSeekCompleteImpl();
    }

    @Override
    public long getTcpSpeed() {
        return mediaPlayer.getTcpSpeed();
    }
}
