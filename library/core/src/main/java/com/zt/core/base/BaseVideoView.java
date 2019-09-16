package com.zt.core.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;

import com.zt.core.R;
import com.zt.core.listener.OnFullscreenChangedListener;
import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.OnVideoSizeChangedListener;
import com.zt.core.player.AndroidPlayer;
import com.zt.core.render.SurfaceRenderView;
import com.zt.core.render.TextureRenderView;
import com.zt.core.util.VideoUtils;

import java.util.Map;

/**
 * 构建一个基本的播放器视图View, 实现IVideoView接口，通过BaseVideoController, 实现播放器核心与UI的交互
 */
public abstract class BaseVideoView extends FrameLayout implements IVideoView, OnVideoSizeChangedListener,OnStateChangedListener {

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

    protected ViewGroup surfaceContainer;

    private boolean isShowMobileDataDialog = false;

    //是否支持重力感应自动横竖屏，默认支持
    private boolean supportSensorRotate = true;

    //是否跟随系统的方向锁定，默认跟随
    private boolean rotateWithSystem = true;

    protected OrientationHelper orientationHelper;

    private boolean isFullScreen = false;

    private int mSystemUiVisibility;

    //全屏之前，正常状态下控件的宽高
    private int originWidth;
    private int originHeight;

    //父视图
    private ViewParent viewParent;
    //当前view在父视图中的位置
    private int positionInParent;

    //actionbar可见状态记录
    private boolean actionBarVisible;

    protected OnVideoSizeChangedListener onVideoSizeChangedListener;
    protected OnFullscreenChangedListener onFullScreenChangeListener;
    protected OnStateChangedListener onStateChangedListener;

    public BaseVideoView(@NonNull Context context) {
        this(context, null);
    }

    public BaseVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        LayoutInflater.from(context).inflate(getLayoutId(), this);
        surfaceContainer = findViewById(getSurfaceContainerId());
        playerConfig = new PlayerConfig.Builder().build();
        orientationHelper = new OrientationHelper(this);
    }

    @Override
    public void setOnFullscreenChangeListener(OnFullscreenChangedListener onFullscreenChangeListener) {
        this.onFullScreenChangeListener = onFullscreenChangeListener;
    }

    @Override
    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        this.onStateChangedListener = onStateChangedListener;
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    //region 播放器行为

    @Override
    public void setVideoUrlPath(String url) {
        this.url = url;
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

    @Override
    public void setVideoHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    @Override
    public void start() {
        orientationHelper.start();
        if (isLocalVideo() || VideoUtils.isWifiConnected(getContext())) {
            startVideo();
        } else {
            showMobileDataDialog();
        }
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

        getSurfaceContainer().removeAllViews();

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);

        renderView = newRenderViewInstance(context);
        if (renderView != null) {
            renderView.setPlayer(player);
            getSurfaceContainer().addView(renderView.getRenderView(), layoutParams);
        }
    }

    protected IRenderView newRenderViewInstance(Context context) {
        if (renderView != null) {
            return renderView;
        }
        switch (playerConfig.renderType) {
            case PlayerConfig.RENDER_TEXTURE_VIEW:
                return new TextureRenderView(context);
            case PlayerConfig.RENDER_SURFACE_VIEW:
                return new SurfaceRenderView(context);
        }
        return null;
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
        if (player != null) {
            return player;
        }
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
        clearRenderView();
        if (player != null) {
            player.destroy();
            player = null;
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

    public boolean isInPlaybackState() {
        return player != null && player.isInPlaybackState();
    }

    public int getStreamVolume() {
        return player != null ? player.getStreamVolume() : 0;
    }

    public void setStreamVolume(int value) {
        if (player != null) {
            player.setStreamVolume(value);
        }
    }

    //endregion

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(width, height);
        }
        resizeTextureView(width, height);
    }

    protected abstract @IdRes
    int getSurfaceContainerId();

    protected abstract @LayoutRes
    int getLayoutId();

    public abstract boolean onBackKeyPressed();

    @Override
    public void onStateChange(int state) {
        if (onStateChangedListener != null) {
            onStateChangedListener.onStateChange(state);
        }
        updatePlayIcon(state);
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {
        if (playerConfig != null) {
            this.playerConfig = playerConfig;
        }
    }

    protected void updatePlayIcon(int state) {
        if (state == BasePlayer.STATE_PLAYING) {
            setPlayingIcon();
        } else if (state != BasePlayer.STATE_BUFFERING_START && state != BasePlayer.STATE_BUFFERING_END) {
            setPausedIcon();
        }
    }

    //设置播放时，播放按钮图标
    protected abstract void setPlayingIcon();

    //设置暂停时，播放按钮图标
    protected abstract void setPausedIcon();

    public IRenderView getRenderView() {
        return renderView;
    }

    @Override
    public void setRenderView(IRenderView renderView) {
        this.renderView = renderView;
    }

    public void clearRenderView() {
        renderView = null;
    }

    @Override
    public ViewGroup getSurfaceContainer() {
        return surfaceContainer;
    }

    @Override
    public BaseVideoView getPlayView() {
        return this;
    }

    @Override
    public BasePlayer getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(BasePlayer player) {
        this.player = player;
    }

    @Override
    public PlayerConfig getPlayConfig() {
        return playerConfig;
    }

    @Override
    public void showMobileDataDialog() {
        if (isShowMobileDataDialog) {
            return;
        }
        isShowMobileDataDialog = true;
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(context.getString(R.string.mobile_data_tips));
        builder.setPositiveButton(context.getString(R.string.continue_playing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startVideo();
            }
        });
        builder.setNegativeButton(context.getString(R.string.stop_play), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //region 全屏处理

    /**
     * 正常情况下，通过点击全屏按钮来全屏
     */
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

        Activity activity = VideoUtils.getActivity(getContext());

        actionBarVisible = VideoUtils.isActionBarVisible(activity);

        mSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();

        activity.setRequestedOrientation(orientation);

        VideoUtils.hideSupportActionBar(activity, true);
        VideoUtils.addFullScreenFlag(activity);
        VideoUtils.hideNavKey(activity);

        changeToFullScreen();

        postRunnableToResizeTexture();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullscreenChange(true);
        }
    }

    private void postRunnableToResizeTexture() {
        post(new Runnable() {
            @Override
            public void run() {
                resizeTextureView(getVideoWidth(), getVideoHeight());
            }
        });
    }

    private int getVideoWidth() {
        return player == null ? 0 : player.getVideoWidth();
    }

    private int getVideoHeight() {
        return player == null ? 0 : player.getVideoHeight();
    }

    //根据视频内容重新调整视频渲染区域大小
    private void resizeTextureView(int width, int height) {
        IRenderView renderView = getRenderView();

        if (renderView == null || renderView.getRenderView() == null || height == 0 || width == 0) {
            return;
        }

        float aspectRation = playerConfig.aspectRatio == 0 ? (float) height / width : playerConfig.aspectRatio;

        int parentWidth = getSurfaceContainer().getWidth();
        int parentHeight = getSurfaceContainer().getHeight();

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

    /**
     * 通过获取到Activity的ID_ANDROID_CONTENT根布局，来添加视频控件，并全屏
     */
    protected void changeToFullScreen() {

        originWidth = getWidth();
        originHeight = getHeight();

        viewParent = getParent();
        positionInParent = ((ViewGroup) viewParent).indexOfChild(this);

        ViewGroup vp = getRootViewGroup();

        removePlayerFromParent();

        FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setBackgroundColor(Color.BLACK);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.addView(this, lp);
        vp.addView(frameLayout, lpParent);
    }

    /**
     * 获取到Activity的ID_ANDROID_CONTENT根布局
     *
     * @return
     */
    private ViewGroup getRootViewGroup() {
        Activity activity = (Activity) getContext();
        if (activity != null) {
            return (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        }
        return null;
    }

    private void removePlayerFromParent() {
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }
    }

    @Override
    public void exitFullscreen() {
        exitFullscreenWithOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void exitFullscreenWithOrientation(int orientation) {

        isFullScreen = false;

        Activity activity = VideoUtils.getActivity(getContext());

        activity.setRequestedOrientation(orientation);

        VideoUtils.showSupportActionBar(activity, actionBarVisible);
        VideoUtils.clearFullScreenFlag(activity);

        activity.getWindow().getDecorView().setSystemUiVisibility(mSystemUiVisibility);

        changeToNormalScreen();

        postRunnableToResizeTexture();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullscreenChange(false);
        }
    }

    /**
     * 对应上面的全屏模式，来恢复到全屏之前的样式
     */
    protected void changeToNormalScreen() {
        ViewGroup vp = getRootViewGroup();
        vp.removeView((View) getParent());
        removePlayerFromParent();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(originWidth, originHeight);
        setLayoutParams(layoutParams);

        if (viewParent != null) {
            ((ViewGroup) viewParent).addView(this, positionInParent);
        }
    }

    @Override
    public boolean isFullScreen() {
        return isFullScreen;
    }

    /**
     * 视频全屏策略，竖向全屏，横向全屏，还是根据宽高比来智能选择
     */
    protected int getFullScreenOrientation() {
        if (playerConfig.screenMode == PlayerConfig.PORTRAIT_FULLSCREEN_MODE) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        if (playerConfig.screenMode == PlayerConfig.AUTO_FULLSCREEN_MODE) {
            return player.getAspectRation() < 1 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    /**
     * @return 控制是否支持重力感旋转屏幕来全屏等操作，竖向全屏模式和智能全屏模式下不开启重力感应旋转屏幕，避免造成奇怪的交互。
     */
    @Override
    public boolean supportSensorRotate() {
        return supportSensorRotate && getPlayConfig().screenMode == PlayerConfig.LANDSCAPE_FULLSCREEN_MODE;
    }

    public void setSupportSensorRotate(boolean supportSensorRotate) {
        this.supportSensorRotate = supportSensorRotate;
    }

    @Override
    public boolean rotateWithSystem() {
        return rotateWithSystem;
    }

    public void setRotateWithSystem(boolean rotateWithSystem) {
        this.rotateWithSystem = rotateWithSystem;
    }

    //endregion
}
