package com.zt.core.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.AttributeSet;
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
import com.zt.core.util.VideoUtils;

import java.util.Map;

/**
 * Created by zhouteng on 2019-09-18
 * <p>
 * 播放器布局基类，用于添加播放器画面视图
 */
public abstract class BaseVideoView extends FrameLayout implements IVideoView {

    protected ViewGroup surfaceContainer;
    protected OrientationHelper orientationHelper;

    //是否支持重力感应自动横竖屏，默认支持
    private boolean supportSensorRotate = true;

    //是否跟随系统的方向锁定，默认跟随
    private boolean rotateWithSystem = true;

    //播放器配置
    private PlayerConfig playerConfig;

    //播放器播放画面视图
    private RenderContainerView renderContainerView;

    protected OnVideoSizeChangedListener onVideoSizeChangedListener;
    protected OnFullscreenChangedListener onFullScreenChangeListener;
    protected OnStateChangedListener onStateChangedListener;

    private boolean isShowMobileDataDialog = false;

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

    public BaseVideoView(@NonNull Context context) {
        this(context, null);
    }

    public BaseVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void setVideoUrlPath(String url) {
        renderContainerView.setVideoUrlPath(url);
    }

    public void setVideoRawPath(@RawRes int rawId) {
        renderContainerView.setVideoRawPath(rawId);
    }

    public void setVideoAssetPath(String assetFileName) {
        renderContainerView.setVideoAssetPath(assetFileName);
    }

    public void setVideoHeaders(Map<String, String> headers) {
        renderContainerView.setVideoHeaders(headers);
    }

    @Override
    public RenderContainerView getRenderContainerView() {
        return renderContainerView;
    }

    protected void init(Context context) {
        LayoutInflater.from(context).inflate(getLayoutId(), this);
        surfaceContainer = findViewById(getSurfaceContainerId());
        playerConfig = new PlayerConfig.Builder().build();
        orientationHelper = new OrientationHelper(this);

        renderContainerView = new RenderContainerView(context);
        addRenderContainer(renderContainerView);

        renderContainerView.setVideoView(this);
    }

    //添加播放器画面视图，到播放器界面上
    @Override
    public void addRenderContainer(RenderContainerView renderContainerView) {
        surfaceContainer.removeAllViews();
        surfaceContainer.addView(renderContainerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public BaseVideoView getPlayView() {
        return this;
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(width, height);
        }
        resizeTextureView(width, height);
    }

    @Override
    public void onStateChange(int state) {
        if (onStateChangedListener != null) {
            onStateChangedListener.onStateChange(state);
        }
        updatePlayIcon(state);
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

    public void setPlayerConfig(PlayerConfig playerConfig) {
        if (playerConfig != null) {
            this.playerConfig = playerConfig;
            renderContainerView.setPlayerConfig(playerConfig);
        }
    }

    protected abstract @IdRes
    int getSurfaceContainerId();

    protected abstract @LayoutRes
    int getLayoutId();

    public abstract boolean onBackKeyPressed();

    /**
     * @return 控制是否支持重力感旋转屏幕来全屏等操作，竖向全屏模式和智能全屏模式下不开启重力感应旋转屏幕，避免造成奇怪的交互。
     */
    @Override
    public boolean supportSensorRotate() {
        return supportSensorRotate && playerConfig.screenMode == PlayerConfig.LANDSCAPE_FULLSCREEN_MODE;
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

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        this.onStateChangedListener = onStateChangedListener;
    }

    public void setOnFullscreenChangeListener(OnFullscreenChangedListener onFullScreenChangeListener) {
        this.onFullScreenChangeListener = onFullScreenChangeListener;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    //region 播放器相关

    public void replay() {
        BasePlayer player = renderContainerView.getPlayer();
        if (player != null) {
            player.seekTo(0);
            start();
        }
    }

    public void start() {
        orientationHelper.start();
        renderContainerView.start();
    }

    public void pause() {
        BasePlayer player = renderContainerView.getPlayer();
        if (player != null) {
            player.pause();
        }
    }

    public boolean isPlaying() {
        BasePlayer player = renderContainerView.getPlayer();
        return player != null && player.isPlaying();
    }

    public void release() {
        BasePlayer player = renderContainerView.getPlayer();
        if (player != null) {
            player.release();
        }
    }

    public void destroy() {
        BasePlayer player = renderContainerView.getPlayer();
        if (player != null) {
            player.destroy();
        }
    }

    public long getDuration() {
        BasePlayer player = renderContainerView.getPlayer();
        return player == null ? 0 : player.getDuration();
    }

    public void seekTo(long position) {
        BasePlayer player = renderContainerView.getPlayer();
        if (player != null) {
            player.seekTo(position);
        }
    }

    public long getCurrentPosition() {
        BasePlayer player = renderContainerView.getPlayer();
        return player == null ? 0 : player.getCurrentPosition();
    }

    public int getStreamMaxVolume() {
        BasePlayer player = renderContainerView.getPlayer();
        return player == null ? 0 : player.getStreamMaxVolume();
    }

    public boolean isInPlaybackState() {
        BasePlayer player = renderContainerView.getPlayer();
        return player != null && player.isInPlaybackState();
    }

    public int getStreamVolume() {
        BasePlayer player = renderContainerView.getPlayer();
        return player != null ? player.getStreamVolume() : 0;
    }

    public void setStreamVolume(int value) {
        BasePlayer player = renderContainerView.getPlayer();
        if (player != null) {
            player.setStreamVolume(value);
        }
    }
    //endregion

    /**
     * 数据网络下，默认提示框
     */
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
                renderContainerView.startVideo();
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
        BasePlayer player = renderContainerView.getPlayer();
        return player == null ? 0 : player.getVideoWidth();
    }

    private int getVideoHeight() {
        BasePlayer player = renderContainerView.getPlayer();
        return player == null ? 0 : player.getVideoHeight();
    }

    //根据视频内容重新调整视频渲染区域大小
    private void resizeTextureView(int width, int height) {
        IRenderView renderView = renderContainerView.getRenderView();
        if (renderView == null || renderView.getRenderView() == null || height == 0 || width == 0) {
            return;
        }

        float aspectRation = playerConfig.aspectRatio == 0 ? (float) height / width : playerConfig.aspectRatio;

        int parentWidth = getWidth();
        int parentHeight = getHeight();

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
        BasePlayer player = renderContainerView.getPlayer();
        if (playerConfig.screenMode == PlayerConfig.AUTO_FULLSCREEN_MODE && player != null) {
            return player.getAspectRation() < 1 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }


    //endregion
}
