package com.zt.core.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.OnVideoSizeChangedListener;
import com.zt.core.player.AndroidPlayer;
import com.zt.core.render.SurfaceRenderView;
import com.zt.core.render.TextureRenderView;
import com.zt.core.util.VideoUtils;

import java.util.Map;

/**
 * 承载播放画面的视图
 */
public class RenderContainerView extends FrameLayout implements OnVideoSizeChangedListener, OnStateChangedListener {

    protected String url;

    protected @RawRes
    int rawId;

    protected String assetFileName;

    protected Map<String, String> headers;

    //播放器配置
    private PlayerConfig playerConfig;

    //播放器核心
    private BasePlayer player;

    //播放器渲染画面视图
    private IRenderView renderView;

    //播放器控制层界面
    private IVideoView videoView;

    public RenderContainerView(@NonNull Context context) {
        this(context, null);
    }

    public RenderContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RenderContainerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RenderContainerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void setVideoView(IVideoView videoView) {
        this.videoView = videoView;
    }

    //region 播放器行为

    public void start() {
        //本地视频，wifi连接下直接播放
        if (isLocalVideo() || VideoUtils.isWifiConnected(getContext())) {
            startVideo();
        } else {
            videoView.handleMobileData();
        }
    }

    public void setVideoUrlPath(String url) {
        this.url = url;
    }

    //设置raw下视频的路径
    public void setVideoRawPath(@RawRes int rawId) {
        this.rawId = rawId;
    }

    //设置assets下视频的路径
    public void setVideoAssetPath(String assetFileName) {
        this.assetFileName = assetFileName;
    }

    public void setVideoHeaders(Map<String, String> headers) {
        this.headers = headers;
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

        Context context = getContext();

        initPlayer(context);

        removeAllViews();

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);

        renderView = newRenderViewInstance(context);
        if (renderView != null) {
            renderView.setPlayer(player);
            addView(renderView.getRenderView(), layoutParams);
        }
    }

    protected IRenderView newRenderViewInstance(Context context) {
        switch (playerConfig.renderType) {
            case PlayerConfig.RENDER_TEXTURE_VIEW:
                return new TextureRenderView(context);
            case PlayerConfig.RENDER_SURFACE_VIEW:
                return new SurfaceRenderView(context);
        }
        return null;
    }

    public IRenderView getRenderView() {
        return renderView;
    }

    protected void initPlayer(Context context) {
        player = newPlayerInstance(context);
        player.setOnVideoSizeChangedListener(this);
        player.setOnStateChangeListener(this);
        player.setPlayerConfig(playerConfig);
        setDataSource();
        player.initPlayer();
    }

    protected BasePlayer newPlayerInstance(Context context) {
        if (playerConfig != null && playerConfig.player != null) {
            return playerConfig.player;
        }
        return new AndroidPlayer(context);
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

    protected boolean isLocalVideo() {
        return !TextUtils.isEmpty(assetFileName) || rawId != 0 || (!TextUtils.isEmpty(url) && url.startsWith("file"));
    }

    public void release() {
        if (player != null) {
            player.release();
        }
    }

    public void destroy() {
        if (player != null) {
            player.destroy();
            player = null;
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    //endregion

    @Override
    public void onVideoSizeChanged(int width, int height) {
        videoView.onVideoSizeChanged(width, height);
    }

    @Override
    public void onStateChange(int state) {
        videoView.onStateChange(state);
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {
        if (playerConfig != null) {
            this.playerConfig = playerConfig;
        }
    }

    public BasePlayer getPlayer() {
        return player;
    }


}
