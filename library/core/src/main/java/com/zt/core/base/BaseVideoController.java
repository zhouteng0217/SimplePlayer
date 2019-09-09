package com.zt.core.base;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;

import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.OnVideoSizeChangedListener;
import com.zt.core.player.AndroidPlayer;
import com.zt.core.render.SurfaceRenderView;
import com.zt.core.render.TextureRenderView;
import com.zt.core.util.VideoUtils;

import java.util.Map;

/**
 * 播放器UI层和播放层桥梁，连接播放器UI视图和播放器播放逻辑
 */
public class BaseVideoController implements IVideoController, OnVideoSizeChangedListener, OnStateChangedListener {

    protected String url;

    protected @RawRes
    int rawId;

    protected String assetFileName;

    protected Map<String, String> headers;

    private BasePlayer player;
    private PlayerConfig playerConfig;

    //播放器渲染画面视图
    private IRenderView renderView;
    protected IVideoView videoView;

    protected boolean isFullScreen = false;

    protected int mSystemUiVisibility;

    //正常状态下控件的宽高
    protected int originWidth;
    protected int originHeight;

    //父视图
    protected ViewParent viewParent;
    //当前view在父视图中的位置
    protected int positionInParent;

    //actionbar可见状态记录
    private boolean actionBarVisible;

    protected Context context;
    private ViewGroup playerView;

    protected OnVideoSizeChangedListener onVideoSizeChangedListener;

    public BaseVideoController(IVideoView videoView) {
        playerConfig = new PlayerConfig.Builder().build();
        this.videoView = videoView;
        if (videoView == null || videoView.getPlayView() == null || videoView.getSurfaceContainer() == null) {
            throw new IllegalArgumentException("the play view or the surface container should not be null");
        }
        playerView = videoView.getPlayView();
        context = videoView.getPlayView().getContext();
    }

    //region DataSource

    @Override
    public void setVideoUrlPath(String url) {
        this.url = url;
    }

    @Override
    public void setVideoHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    //设置raw下视频的路径
    @Override
    public void setVideoRawPath(@RawRes int rawId) {
        this.rawId = rawId;
    }

    //设置assets下视频的路径
    @Override
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

    //region init player

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

        initPlayer(context);

        videoView.getSurfaceContainer().removeAllViews();

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);

        renderView = newRenderViewInstance(context);
        if (renderView != null) {
            renderView.setPlayer(player);
            videoView.getSurfaceContainer().addView(renderView.getRenderView(), layoutParams);
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

    protected BasePlayer newPlayerInstance(Context context) {
        if (playerConfig != null && playerConfig.player != null) {
            return playerConfig.player;
        }
        return new AndroidPlayer(context);
    }

    //endregion

    public void setPlayerConfig(PlayerConfig playerConfig) {
        if (playerConfig != null) {
            this.playerConfig = playerConfig;
        }
    }

    @Override
    public PlayerConfig getPlayConfig() {
        return playerConfig;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(width, height);
        }
        resizeTextureView(width, height);
    }

    //根据视频内容重新调整视频渲染区域大小
    protected void resizeTextureView(int width, int height) {
        if (width == 0 || height == 0 || renderView == null || renderView.getRenderView() == null) {
            return;
        }

        float aspectRation = playerConfig.aspectRatio == 0 ? (float) height / width : playerConfig.aspectRatio;

        int parentWidth = videoView.getSurfaceContainer().getWidth();
        int parentHeight = videoView.getSurfaceContainer().getHeight();

        int w, h;

        if (aspectRation < 1) {
            w = parentWidth;
            h = (int) (w * aspectRation);
        } else {
            h = parentHeight;
            w = (int) (h / aspectRation);
        }
        renderView.setVideoSize(w, h);
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    @Override
    public void release() {
        if (player != null) {
            player.release();
        }
    }

    @Override
    public void replay() {
        if (player != null) {
            player.seekTo(0);
            start();
        }
    }

    @Override
    public void destroy() {
        if (player != null) {
            player.destroy();
        }
    }

    @Override
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
        if (isLocalVideo() || VideoUtils.isWifiConnected(context)) {
            startVideo();
        } else {
            videoView.showMobileDataDialog();
        }
    }

    public int getVideoWidth() {
        return player != null ? player.getVideoWidth() : 0;
    }

    public int getVideoHeight() {
        return player != null ? player.getVideoHeight() : 0;
    }

    @Override
    public void onStateChange(int state) {
        videoView.onStateChange(state);
    }

    @Override
    public BasePlayer getPlayer() {
        return player;
    }

    //region FullScreen

    /**
     * 视频全屏策略，竖向全屏，横向全屏，还是根据宽高比来智能选择
     */
    public int getFullScreenOrientation() {
        if (playerConfig.screenMode == PlayerConfig.PORTRAIT_FULLSCREEN_MODE) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        if (playerConfig.screenMode == PlayerConfig.AUTO_FULLSCREEN_MODE) {
            return player.getAspectRation() < 1 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    @Override
    public boolean isFullScreen() {
        return isFullScreen;
    }

    /**
     * 正常情况下，通过点击全屏按钮来全屏
     */
    @Override
    public void startFullscreen() {
        startFullscreenWithOrientation(getFullScreenOrientation());
    }

    /**
     * 通过重力感应，旋转屏幕来全屏
     *
     * @param orientation
     */
    @Override
    public void startFullscreenWithOrientation(int orientation) {

        isFullScreen = true;

        Activity activity = VideoUtils.getActivity(context);

        actionBarVisible = VideoUtils.isActionBarVisible(activity);

        mSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();

        activity.setRequestedOrientation(orientation);

        VideoUtils.hideSupportActionBar(activity, true);
        VideoUtils.addFullScreenFlag(activity);
        VideoUtils.hideNavKey(activity);

        changeToFullScreen();

        postRunnableToResizeTexture();
    }

    @Override
    public void exitFullscreen() {
        exitFullscreenWithOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void exitFullscreenWithOrientation(int orientation) {

        isFullScreen = false;

        Activity activity = VideoUtils.getActivity(context);

        activity.setRequestedOrientation(orientation);

        VideoUtils.showSupportActionBar(activity, actionBarVisible);
        VideoUtils.clearFullScreenFlag(activity);

        activity.getWindow().getDecorView().setSystemUiVisibility(mSystemUiVisibility);

        changeToNormalScreen();

        postRunnableToResizeTexture();
    }

    /**
     * 通过获取到Activity的ID_ANDROID_CONTENT根布局，来添加视频控件，并全屏
     */
    protected void changeToFullScreen() {

        originWidth = playerView.getWidth();
        originHeight = playerView.getHeight();

        viewParent = playerView.getParent();
        positionInParent = ((ViewGroup) viewParent).indexOfChild(playerView);

        ViewGroup vp = getRootViewGroup();

        removePlayerFromParent();

        FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.BLACK);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.addView(playerView, lp);
        vp.addView(frameLayout, lpParent);
    }


    /**
     * 对应上面的全屏模式，来恢复到全屏之前的样式
     */
    protected void changeToNormalScreen() {
        ViewGroup vp = getRootViewGroup();
        vp.removeView((View) playerView.getParent());
        removePlayerFromParent();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(originWidth, originHeight);
        playerView.setLayoutParams(layoutParams);

        if (viewParent != null) {
            ((ViewGroup) viewParent).addView(playerView, positionInParent);
        }
    }

    /**
     * 获取到Activity的ID_ANDROID_CONTENT根布局
     *
     * @return
     */
    protected ViewGroup getRootViewGroup() {
        Activity activity = (Activity) context;
        if (activity != null) {
            return (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        }
        return null;
    }

    protected void removePlayerFromParent() {
        ViewParent parent = playerView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(playerView);
        }
    }

    protected void postRunnableToResizeTexture() {
        playerView.post(new Runnable() {
            @Override
            public void run() {
                resizeTextureView(getVideoWidth(), getVideoHeight());
            }
        });
    }

    //endregion
}
