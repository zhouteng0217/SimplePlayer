package com.zt.core.base;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.onVideoSizeChangedListener;
import com.zt.core.player.AndroidPlayer;
import com.zt.core.render.SurfaceRenderView;
import com.zt.core.render.TextureRenderView;
import com.zt.core.util.VideoUtils;

import java.util.Map;

//定义播放器UI层操作逻辑接口
public abstract class AbstractVideoController implements onVideoSizeChangedListener, OnStateChangedListener {

    protected String url;

    protected @RawRes
    int rawId;

    protected String assetFileName;

    protected Map<String, String> headers;

    private BasePlayer player;
    private PlayerConfig playerConfig;

    //播放器渲染画面视图
    private BaseRenderView renderView;

    //承载播放器渲染画面的容器视图
    protected ViewGroup surfaceContainer;

    public AbstractVideoController(ViewGroup surfaceContainer) {
        playerConfig = new PlayerConfig.Builder().build();
        this.surfaceContainer = surfaceContainer;
        if (surfaceContainer == null) {
            throw new IllegalArgumentException("must have a container to load the render view");
        }
    }

    //region DataSource

    public void setVideoPath(String url) {
        setVideoPath(url, null);
    }

    public void setVideoPath(String url, Map<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    //设置raw下视频的路径
    public void setVideoRawPath(@RawRes int rawId) {
        this.rawId = rawId;
    }

    //设置assets下视频的路径
    public void setVideoAssetPath(String assetFileName) {
        this.assetFileName = assetFileName;
    }

    private void setDataSource() {
        if (assetFileName != null) {
            player.setVideoAssetPath(assetFileName);
        } else if (rawId != 0) {
            player.setVideoRawPath(rawId);
        } else {
            player.setVideoPath(url, headers);
        }
    }

    //endregion

    private void initPlayer(Context context) {
        player = newPlayerInstance(context);
        player.setOnVideoSizeChangedListener(this);
        player.setOnStateChangeListener(this);
        player.setPlayerConfig(playerConfig);
        setDataSource();
        player.initPlayer();
    }

    protected void startVideo() {
        int currentState = player == null ? BasePlayer.STATE_IDLE : player.getCurrentState();
        if (currentState == BasePlayer.STATE_IDLE || currentState == BasePlayer.STATE_ERROR) {
            prepareToPlay();
        } else if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    protected void prepareToPlay() {

        initPlayer(surfaceContainer.getContext());

        surfaceContainer.removeAllViews();

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);

        renderView = newRenderViewInstance(surfaceContainer.getContext());
        if (renderView != null) {
            renderView.setPlayer(player);
            surfaceContainer.addView(renderView.getRenderView(), layoutParams);
        }
    }

    protected BaseRenderView newRenderViewInstance(Context context) {
        switch (playerConfig.renderType) {
            case PlayerConfig.RENDER_TEXTURE_VIEW:
                return new TextureRenderView(context);
            case PlayerConfig.RENDER_SURFACE_VIEW:
                return new SurfaceRenderView(context);
        }
        return null;
    }

    protected BasePlayer newPlayerInstance(Context context) {
        if (playerConfig != null && playerConfig.player != null) {
            return playerConfig.player;
        }
        return new AndroidPlayer(context);
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {
        this.playerConfig = playerConfig;
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        resizeTextureView(width, height);
    }

    //根据视频内容重新调整视频渲染区域大小
    public void resizeTextureView(int width, int height) {
        if (width == 0 || height == 0 || renderView == null || renderView.getRenderView() == null) {
            return;
        }
        float aspectRation = (float) width / height;

        int parentWidth = surfaceContainer.getWidth();
        int parentHeight = surfaceContainer.getHeight();

        int w, h;

        if (aspectRation >= 1) {
            w = parentWidth;
            h = (int) (w / aspectRation);
        } else {
            h = parentHeight;
            w = (int) (h * aspectRation);
        }

        ViewGroup.LayoutParams layoutParams = renderView.getRenderView().getLayoutParams();
        layoutParams.width = w;
        layoutParams.height = h;
        renderView.getRenderView().setLayoutParams(layoutParams);
    }

    //视频全屏策略，竖向全屏，横向全屏，还是根据宽高比来选择
    public int getFullScreenOrientation() {
        if (playerConfig.screenMode == PlayerConfig.PORTRAIT_FULLSCREEN_MODE) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        if (playerConfig.screenMode == PlayerConfig.AUTO_FULLSCREEN_MODE) {
            return player.getAspectRation() >= 1 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void release() {
        if (player != null) {
            player.release();
        }
    }

    public void replay() {
        if (player != null) {
            player.seekTo(0);
            start();
        }
    }

    public void destroy() {
        if (player != null) {
            player.destroy();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    public void seekTo(long position) {
        if (player != null) {
            player.seekTo(position);
        }
    }

    public long getCurrentPosition() {
        return player == null ? 0 : player.getCurrentPosition();
    }

    public long getDuration() {
        return player == null ? 0 : player.getDuration();
    }

    public int getStreamMaxVolume() {
        return player == null ? 0 : player.getStreamMaxVolume();
    }

    public void setStreamVolume(int value) {
        if (player != null) {
            player.setStreamVolume(value);
        }
    }

    public int getStreamVolume() {
        return player != null ? player.getStreamVolume() : 0;
    }

    public boolean isInPlaybackState() {
        return player != null && player.isInPlaybackState();
    }

    protected boolean isLocalVideo() {
        return !TextUtils.isEmpty(assetFileName) || rawId != 0 || (!TextUtils.isEmpty(url) && url.startsWith("file"));
    }

    public void start() {
        if (isLocalVideo() || VideoUtils.isWifiConnected(surfaceContainer.getContext())) {
            startVideo();
        } else {
            showMobileDataDialog();
        }
    }

    public int getVideoWidth() {
        return player != null ? player.getVideoWidth() : 0;
    }

    public int getVideoHeight() {
        return player != null ? player.getVideoHeight() : 0;
    }

    //数据连接情况下，应该展示的逻辑
    protected abstract void showMobileDataDialog();

    @Override
    public abstract void onStateChange(int state);

    public BasePlayer getPlayer() {
        return player;
    }
}
