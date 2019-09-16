package com.zt.core.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zt.core.R;
import com.zt.core.listener.OnFullscreenChangedListener;
import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.OnVideoSizeChangedListener;
import com.zt.core.player.AndroidPlayer;

import java.util.Map;

/**
 * 构建一个基本的播放器视图View, 实现IVideoView接口，通过BaseVideoController, 实现播放器核心与UI的交互
 */
public abstract class BaseVideoView extends FrameLayout implements IVideoView {

    protected OnFullscreenChangedListener onFullScreenChangeListener;
    protected OnStateChangedListener onStateChangedListener;

    protected BaseVideoController videoController;

    protected ViewGroup surfaceContainer;

    private boolean isShowMobileDataDialog = false;

    //是否支持重力感应自动横竖屏，默认支持
    private boolean supportSensorRotate = true;

    //是否跟随系统的方向锁定，默认跟随
    private boolean rotateWithSystem = true;

    protected OrientationHelper orientationHelper;

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
        videoController = new BaseVideoController(this);
        orientationHelper = new OrientationHelper(this);
    }

    @Override
    public void setVideoUrlPath(String url) {
        videoController.setVideoUrlPath(url);
    }

    //设置raw下视频的路径
    @Override
    public void setVideoRawPath(@RawRes int rawId) {
        videoController.setVideoRawPath(rawId);
    }

    //设置assets下视频的路径
    @Override
    public void setVideoAssetPath(String assetFileName) {
        videoController.setVideoAssetPath(assetFileName);
    }

    @Override
    public void setVideoHeaders(Map<String, String> headers) {
        videoController.setVideoHeaders(headers);
    }

    @Override
    public boolean isFullScreen() {
        return videoController.isFullScreen();
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
        videoController.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
    }

    @Override
    public void startFullscreen() {
        videoController.startFullscreen();
        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullscreenChange(true);
        }
    }

    @Override
    public void startFullscreenWithOrientation(int orientation) {
        videoController.startFullscreenWithOrientation(orientation);
        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullscreenChange(true);
        }
    }

    @Override
    public void exitFullscreen() {
        videoController.exitFullscreen();
        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullscreenChange(false);
        }
    }

    @Override
    public void exitFullscreenWithOrientation(int orientation) {
        videoController.exitFullscreenWithOrientation(orientation);
        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullscreenChange(false);
        }
    }

    @Override
    public boolean isPlaying() {
        return videoController.isPlaying();
    }

    @Override
    public void start() {
        orientationHelper.start();
        videoController.start();
    }

    @Override
    public void release() {
        videoController.release();
    }

    @Override
    public void replay() {
        videoController.replay();
    }

    @Override
    public void destroy() {
        videoController.destroy();
    }

    @Override
    public void pause() {
        videoController.pause();
    }

    protected abstract @IdRes
    int getSurfaceContainerId();

    protected abstract @LayoutRes
    int getLayoutId();

    public abstract boolean onBackKeyPressed();

    public void onStateChange(int state) {
        if (onStateChangedListener != null) {
            onStateChangedListener.onStateChange(state);
        }
        updatePlayIcon(state);
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {
        videoController.setPlayerConfig(playerConfig);
    }

    protected void updatePlayIcon(int state) {
        if (state == AndroidPlayer.STATE_PLAYING) {
            setPlayingIcon();
        } else if (state == AndroidPlayer.STATE_ERROR) {
            setPausedIcon();
        } else {
            setPausedIcon();
        }
    }

    //设置播放时，播放按钮图标
    protected abstract void setPlayingIcon();

    //设置暂停时，播放按钮图标
    protected abstract void setPausedIcon();

    public IRenderView getRenderView() {
        return videoController.getRenderView();
    }

    @Override
    public void setRenderView(IRenderView renderView) {
        videoController.setRenderView(renderView);
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
        return videoController.getPlayer();
    }

    @Override
    public PlayerConfig getPlayConfig() {
        return videoController.getPlayConfig();
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
                videoController.startVideo();
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
}
