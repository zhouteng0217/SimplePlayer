package com.zt.exoplayer;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.zt.core.base.BasePlayer;

import java.util.Map;

public class GoogleExoPlayer extends BasePlayer {

    private SimpleExoPlayer simpleExoPlayer;
    private DataSource.Factory dataSourceFactory;

    private Surface surface;

    private int videoWidth;
    private int videoHeight;

    public GoogleExoPlayer(Context context) {
        super(context);
        dataSourceFactory = getDataSource();
    }

    @Override
    protected void initPlayerImpl() {
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context);
        simpleExoPlayer.addListener(playerEventListener);
        simpleExoPlayer.addVideoListener(videoListener);
        simpleExoPlayer.setRepeatMode(isLooping() ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
        if (surface != null) {
            simpleExoPlayer.setVideoSurface(surface);
        }
        simpleExoPlayer.prepare(buildMediaSource(uri, null));
    }

    @Override
    protected boolean isPlayingImpl() {
        if (simpleExoPlayer == null) {
            return false;
        }
        int playbackState = simpleExoPlayer.getPlaybackState();
        return playbackState != Player.STATE_IDLE
                && playbackState != Player.STATE_ENDED
                && simpleExoPlayer.getPlayWhenReady();
    }

    @Override
    protected void playImpl() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    protected void pauseImpl() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void releaseImpl() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.release();
            simpleExoPlayer.removeListener(playerEventListener);
            simpleExoPlayer.removeVideoListener(videoListener);
        }
        surface = null;
    }

    @Override
    protected void destroyImpl() {
        releaseImpl();
        simpleExoPlayer = null;
    }

    @Override
    public float getAspectRation() {
        return simpleExoPlayer == null || videoHeight == 0 ? 1.0f : (float) videoWidth / videoHeight;
    }

    @Override
    public int getVideoWidth() {
        return videoWidth;
    }

    @Override
    public int getVideoHeight() {
        return videoHeight;
    }

    @Override
    public int getCurrentPosition() {
        return simpleExoPlayer == null ? 0 : (int) simpleExoPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return simpleExoPlayer == null ? 0 : (int) simpleExoPlayer.getDuration();
    }

    @Override
    protected void seekToImpl(int position) {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.seekTo(position);
        }
    }

    @Override
    public void setSurface(Surface surface) {
        this.surface = surface;
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setVideoSurface(surface);
        }
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        setSurface(holder == null ? null : holder.getSurface());
    }

    @Override
    public void setOptions() {

    }

    @Override
    protected void setEnableMediaCodec(boolean isEnable) {

    }

    @Override
    protected void setEnableOpenSLES(boolean isEnable) {

    }

    @Override
    public long getTcpSpeed() {
        return 0;
    }

    private Player.EventListener playerEventListener = new Player.EventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_READY:
                    onBufferingEnd();
                    if (!isPrepared) {
                        onPreparedImpl();
                    }
                    break;
                case Player.STATE_ENDED:
                    onBufferingEnd();
                    onCompletionImpl();
                    break;
                case Player.STATE_IDLE:
                    break;
                case Player.STATE_BUFFERING:
                    if (isPrepared) {
                        onBufferingStart();
                        onBufferingUpdateImpl(simpleExoPlayer.getBufferedPercentage());
                    }
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            onErrorImpl();
        }
    };

    private VideoListener videoListener = new VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            videoWidth = width;
            videoHeight = height;
            onVideoSizeChangedImpl(width, height);
        }
    };

    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
        if (uri.getScheme().equals("rtmp")) {
            RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory(null);
            return new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                    .createMediaSource(uri);
        }
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        DefaultDataSourceFactory defaultDataSourceFactory = new DefaultDataSourceFactory(context, null, dataSourceFactory);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), defaultDataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(dataSourceFactory), defaultDataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(new DefaultHlsDataSourceFactory(dataSourceFactory))
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    protected DataSource.Factory getDataSource() {
        String userAgent = Util.getUserAgent(context, context.getPackageName());
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();

        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(userAgent, defaultBandwidthMeter);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                dataSourceFactory.getDefaultRequestProperties().set(header.getKey(), header.getValue());
            }
        }

        return new DefaultDataSourceFactory(context, new DefaultBandwidthMeter(), dataSourceFactory);
    }

}