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
import com.zt.core.listener.OnFullScreenChangedListener;

import java.util.Map;

/**
 *  构建一个基本的播放器视图View, 实现IVideoView接口，通过BaseVideoController, 实现播放器核心与UI的交互
 */
public abstract class BaseVideoView extends FrameLayout implements IVideoView {

    protected OnFullScreenChangedListener onFullScreenChangeListener;

    protected BaseVideoController videoController;

    protected ViewGroup surfaceContainer;

    private boolean isShowMobileDataDialog = false;

    private boolean supportSensorRotate = true;

    private boolean rotateWithSystem = false;

    private OrientationHelper orientationHelper;

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

    public void setOnFullScreenChangeListener(OnFullScreenChangedListener onFullScreenChangeListener) {
        this.onFullScreenChangeListener = onFullScreenChangeListener;
    }

    @Override
    public void startFullScreen() {
        videoController.startFullScreen();
        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullScreenChange(true);
        }
    }

    @Override
    public void exitFullscreen() {
        videoController.exitFullscreen();
        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullScreenChange(false);
        }
    }

    @Override
    public boolean isPlaying() {
        return videoController.isPlaying();
    }

    @Override
    public void start() {
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

    public abstract void setTitle(String titleText);

    public abstract void onStateChange(int state);

    public void setPlayerConfig(PlayerConfig playerConfig) {
        videoController.setPlayerConfig(playerConfig);
    }

    @Override
    public ViewGroup getSurfaceContainer() {
        return surfaceContainer;
    }

    @Override
    public ViewGroup getPlayView() {
        return this;
    }

    @Override
    public BasePlayer getPlayer() {
        return videoController.getPlayer();
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

    @Override
    public boolean supportSensorRotate() {
        return supportSensorRotate;
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
