package com.zt.core.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.zt.core.base.BasePlayer;
import com.zt.core.base.IFloatView;
import com.zt.core.base.RenderContainerView;
import com.zt.core.util.VideoUtils;

/**
 * Created by zhouteng on 2019-9-10
 * <p>
 * 悬浮视频管理
 */
public class FloatVideoManager implements IFloatView.FloatViewListener {

    private IFloatView videoView;

    private WindowManager windowManager;

    private IFloatView.LayoutParams layoutParams;

    private static FloatVideoManager instance;
    private WindowManager.LayoutParams wmParams;

    //用于从悬浮小窗口模式，跳回正常的Activity界面的Intent
    private Intent intent;

    public static FloatVideoManager getInstance() {
        if (instance == null) {
            instance = new FloatVideoManager();
        }
        return instance;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public void startFloatVideo(IFloatView videoView, Intent intent) {
        this.videoView = videoView;
        this.intent = intent;
        layoutParams = videoView.getFloatVideoLayoutParams();
        createFloatVideo();
    }

    private void createFloatVideo() {
        if (videoView == null) {
            return;
        }
        videoView.setFloatVideoViewListener(this);

        Context context = videoView.getPlayView().getContext();

        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();
        wmParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.TOP | Gravity.LEFT | Gravity.START;
        wmParams.width = layoutParams.width;
        wmParams.height = layoutParams.height;
        wmParams.x = layoutParams.x;
        wmParams.y = layoutParams.y;

        windowManager.addView(videoView.getPlayView(), wmParams);
        windowManager.updateViewLayout(videoView.getPlayView(), wmParams);
    }

    @Override
    public void closeVideoView() {
        if (videoView != null) {
            windowManager.removeViewImmediate(videoView.getPlayView());
            videoView.getPlayView().destroy();
            videoView = null;
            windowManager = null;
        }
    }

    @Override
    public void backToNormalView() {
        if (videoView != null && intent != null) {
            videoView.getPlayView().getContext().startActivity(intent);
        }
    }

    public int getCurrentPlayState() {
        return videoView != null ? videoView.getPlayView().getCurrentState() : BasePlayer.STATE_IDLE;
    }

    //销毁当前小窗口的控制层,并从窗体移除掉
    public void destroyVideoView() {
        if (videoView != null) {
            videoView.destroyPlayerController();
            windowManager.removeViewImmediate(videoView.getPlayView());
            videoView = null;
            windowManager = null;
            intent = null;
        }
    }

    //将当前小窗口播放器的画面层剥离出来
    public RenderContainerView getRenderContainerViewOffParent() {
        if (videoView == null) {
            return null;
        }
        return videoView.getRenderContainerViewOffParent();
    }

    //region  移动小窗体

    private float xInScreen;
    private float yInScreen;

    private float xInView;
    private float yInView;

    @Override
    public boolean onTouch(MotionEvent event) {

        if (videoView == null || videoView.getPlayView() == null) {
            return false;
        }

        xInScreen = event.getRawX();
        yInScreen = event.getRawY() - VideoUtils.getStatusBarHeight(videoView.getPlayView().getContext());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return false;
    }

    private void updateViewPosition() {

        wmParams.y = (int) (yInScreen - yInView);
        wmParams.x = (int) (xInScreen - xInView);
        windowManager.updateViewLayout(videoView.getPlayView(), wmParams);
    }

    //endregion
}
